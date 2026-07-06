package com.unilife.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unilife.dto.Result;
import com.unilife.entity.CouponOrder;
import com.unilife.mapper.CouponOrderMapper;
import com.unilife.mq.MqConstants;
import com.unilife.mq.MqSender;
import com.unilife.service.ICouponOrderService;
import com.unilife.service.ISeckillCouponService;
import com.unilife.utils.OrderStatus;
import com.unilife.utils.RedisIdWorker;
import com.unilife.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

import static com.unilife.utils.RedisConstants.SECKILL_ORDER_KEY;
import static com.unilife.utils.RedisConstants.SECKILL_STOCK_KEY;

@Slf4j
@Service
public class CouponOrderServiceImpl extends ServiceImpl<CouponOrderMapper, CouponOrder> implements ICouponOrderService {

    @Resource
    private ISeckillCouponService seckillCouponService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MqSender mqSender;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 秒杀下单：请求线程只在 Redis 里完成"库存 + 一人一单"的校验与预扣，
     * 真正的建单落库交给 MQ 异步处理。核心接口只剩一次 Lua 调用，
     * 峰值流量被挡在 Redis 层，数据库压力由消费者按自身速率平滑消化。
     */
    @Override
    public Result seckillCoupon(Long couponId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("coupon_order");

        // 1. Lua 原子校验 + 预扣库存（脚本内先判后扣，防超卖）
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                couponId.toString(), userId.toString()
        );
        int r = result == null ? 1 : result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }

        // 2. 组装订单消息，投递 MQ 异步落库
        CouponOrder order = new CouponOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setCouponId(couponId);
        try {
            mqSender.sendOrderCreate(JSONUtil.toJsonStr(order));
        } catch (Exception e) {
            // 消息没发出去，订单不会创建：回滚 Redis 预扣，避免库存被白白占用
            log.error("下单消息发送失败，回滚 Redis 预扣，orderId={}", orderId, e);
            stringRedisTemplate.opsForValue().increment(SECKILL_STOCK_KEY + couponId);
            stringRedisTemplate.opsForSet().remove(SECKILL_ORDER_KEY + couponId, userId.toString());
            return Result.fail("下单失败，请稍后重试");
        }

        // 3. 立即返回订单号，真正的订单稍后由消费者写入数据库
        return Result.ok(orderId);
    }

    /**
     * MQ 消费侧建单：幂等（消息可能重复投递）→ 一人一单兜底 → 条件扣库存 → 落库。
     * 前置校验已经在 Lua 里做过一遍，这里的校验是数据库层面的最后一道防线。
     */
    @Override
    @Transactional
    public boolean createCouponOrder(CouponOrder couponOrder) {
        // 幂等校验：RocketMQ 保证至少投递一次，重复消息直接跳过
        if (getById(couponOrder.getId()) != null) {
            log.warn("重复消息，订单已存在：{}", couponOrder.getId());
            return false;
        }
        Long userId = couponOrder.getUserId();
        // 一人一单兜底
        int count = query().eq("user_id", userId)
                .eq("coupon_id", couponOrder.getCouponId()).count();
        if (count > 0) {
            log.warn("用户已购买过该券，userId={}", userId);
            return false;
        }
        // 带条件扣库存：stock > 0 是防超卖的最后一道防线
        boolean success = seckillCouponService.update()
                .setSql("stock = stock - 1")
                .eq("coupon_id", couponOrder.getCouponId())
                .gt("stock", 0)
                .update();
        if (!success) {
            log.error("数据库库存不足，couponId={}", couponOrder.getCouponId());
            return false;
        }
        couponOrder.setStatus(OrderStatus.UNPAID);
        couponOrder.setCreateTime(LocalDateTime.now());
        save(couponOrder);
        return true;
    }

    /**
     * 模拟支付回调。
     * 支付回调与超时关单可能并发到达，两边都以 where status = 未支付 作为更新条件，
     * 用状态字段充当版本号（乐观锁），保证只有一方能推进状态，订单流转不会错乱。
     */
    @Override
    public Result handlePayCallback(Long orderId) {
        CouponOrder order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (OrderStatus.PAID.equals(order.getStatus())) {
            // 支付平台的回调可能重试，幂等返回成功
            return Result.ok();
        }
        boolean success = update()
                .set("status", OrderStatus.PAID)
                .set("pay_time", LocalDateTime.now())
                .eq("id", orderId)
                .eq("status", OrderStatus.UNPAID)
                .update();
        if (!success) {
            // 乐观锁更新失败：说明超时关单先一步成功，真实场景在这里触发原路退款
            log.warn("支付回调晚于关单到达，orderId={}", orderId);
            return Result.fail("订单已超时关闭，支付金额将原路退回");
        }
        return Result.ok();
    }

    /**
     * 分等级超时关单：
     * 未到超时点 → 按剩余时间续投更小等级的延迟消息，逐级逼近精确超时时刻；
     * 已到超时点且仍未支付 → 乐观锁关单，回补数据库与 Redis 库存并释放一人一单资格。
     */
    @Override
    @Transactional
    public void checkAndCloseOrder(Long orderId) {
        CouponOrder order = getById(orderId);
        if (order == null) {
            log.warn("关单检查：订单不存在（可能消费早于落库），orderId={}", orderId);
            return;
        }
        if (!OrderStatus.UNPAID.equals(order.getStatus())) {
            // 已支付 / 已关闭，本条延迟链路自然结束
            return;
        }
        long elapsed = Duration.between(order.getCreateTime(), LocalDateTime.now()).getSeconds();
        long remain = MqConstants.ORDER_TIMEOUT_SECONDS - elapsed;
        if (remain > 5) {
            // 未到超时点：续投下一等级的延迟消息
            mqSender.sendOrderCloseDelay(orderId, remain);
            return;
        }
        // 乐观锁关单：与支付回调竞争，只有一方能更新成功
        boolean closed = update()
                .set("status", OrderStatus.CLOSED)
                .set("update_time", LocalDateTime.now())
                .eq("id", orderId)
                .eq("status", OrderStatus.UNPAID)
                .update();
        if (!closed) {
            log.info("关单前订单已被支付，orderId={}", orderId);
            return;
        }
        // 回补数据库库存
        seckillCouponService.update()
                .setSql("stock = stock + 1")
                .eq("coupon_id", order.getCouponId())
                .update();
        // 回补 Redis 预扣库存，并释放该用户的一人一单资格（允许重新抢购）
        stringRedisTemplate.opsForValue().increment(SECKILL_STOCK_KEY + order.getCouponId());
        stringRedisTemplate.opsForSet().remove(SECKILL_ORDER_KEY + order.getCouponId(), order.getUserId().toString());
        log.info("订单超时未支付，已关闭并回补库存，orderId={}", orderId);
    }
}
