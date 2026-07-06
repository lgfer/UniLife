# UniLife · 校园生活服务平台

基于 SpringBoot + Redis + RocketMQ 的校园生活服务后端，围绕「高并发优惠券秒杀」核心场景，
覆盖校园周边商家信息查询、优惠券秒杀、订单支付与超时关单、种草社区、关注 Feed 流、附近商家、签到统计等模块。

技术栈：SpringBoot 2.3 · MySQL 8 · Redis · Lua · MyBatis-Plus · RocketMQ 4.9

## 核心技术点

| #   | 技术点                                    | 代码位置                                                                |
| --- | -------------------------------------- | ------------------------------------------------------------------- |
| 1   | Redis + Lua 原子完成库存校验与一人一单判定，防超卖        | `resources/seckill.lua`、`CouponOrderServiceImpl#seckillCoupon`      |
| 2   | RocketMQ 异步下单，削峰解耦，数据库压力平滑消化           | `mq/OrderCreateConsumer`                                            |
| 3   | 逻辑过期防热点 Key 击穿 + 空值缓存防穿透               | `utils/CacheClient`                                                 |
| 4   | 先更新库再删缓存，删除失败投 MQ 补偿重试 + TTL 兜底        | `MerchantServiceImpl#update`、`mq/CacheRetryConsumer`                |
| 5   | Redis + AOP + 注解式滑动窗口限流，全局/IP/用户三维度可叠加 | `ratelimit` 包、`resources/ratelimit.lua`，用法见 `CouponOrderController` |
| 6   | RocketMQ 延迟消息分等级逼近，定时关闭超时未支付订单并回补库存    | `mq/MqSender#sendOrderCloseDelay`、`mq/OrderCloseConsumer`           |
| 7   | 乐观锁（状态即版本号）解决支付回调与超时关单的并发冲突            | `CouponOrderServiceImpl#handlePayCallback` / `#checkAndCloseOrder`  |

## 本地启动

环境：JDK 8、Maven 3.6+、MySQL 8、Redis 6+、RocketMQ 4.9.x

1. 建库导数据：`mysql -uroot -p < src/main/resources/db/unilife.sql`

2. 启动 Redis（默认 `127.0.0.1:6379`）

3. 启动 RocketMQ：
   
   ```bash
   nohup sh bin/mqnamesrv &
   nohup sh bin/mqbroker -n localhost:9876 &
   ```

4. 预热秒杀库存（对应 SQL 种子里的两张秒杀券）：
   
   ```bash
   redis-cli set seckill:coupon:stock:3 100
   redis-cli set seckill:coupon:stock:4 200
   ```
   
   > 通过 `POST /coupon/seckill` 新建的秒杀券会在创建时自动写入 Redis 库存，无需手动预热。

5. 按需修改 `application.yaml` 的数据库账号密码，然后 `mvn spring-boot:run`

6. 登录拿 token：
   
   - `POST /user/code?phone=13800001001`（验证码打印在控制台日志）
   - `POST /user/login`，body：`{"phone":"13800001001","code":"控制台里的验证码"}`
   - 之后的请求带上 header `authorization: {token}`

## 秒杀链路演示

1. 抢券：`POST /coupon-order/seckill/3` → 秒回订单号（订单由 MQ 异步落库，status=1 未支付）
2. 查单：`GET /coupon-order/{orderId}`
3. 模拟支付回调：`POST /pay/callback/{orderId}` → status=2 已支付
4. 或者不支付，等 15 分钟：延迟消息分等级到期后自动关单 → status=4 已取消，数据库与 Redis 库存回补、一人一单资格释放
5. 关单之后再打一次支付回调 → 返回「订单已超时关闭，支付金额将原路退回」——两边都以 `where status=未支付` 更新，只有一方能赢

## 压测说明

- 用 JMeter 压秒杀接口时，给每个线程设置随机 `X-Forwarded-For` 头来模拟多 IP，否则会被单 IP 限流（20 次/秒）正确拦截
- 单用户 1 次/秒、全局 2000 次/秒的阈值都在 `CouponOrderController` 的注解上，可按压测目标调整
- `CouponOrderControllerTest#login` 可批量登录种子用户，把 token 写入 `src/main/resources/tokens.txt` 供 JMeter CSV 引用

## 目录速览

```
com.unilife
├── controller      REST 接口（秒杀入口带三级限流注解）
├── service         业务层（订单 / 商家 / 帖子 / 关注 / 用户）
├── mq              RocketMQ 常量、发送器与三个消费者
├── ratelimit       注解式滑动窗口限流（注解 + 切面 + Lua）
├── utils           缓存工具 / 全局 ID / 分布式锁 / 常量
└── config          MVC / Redisson / MyBatis-Plus / 全局异常
```
