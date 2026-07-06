-- 秒杀资格校验 + 库存预扣，整段脚本原子执行，从根上防超卖
-- ARGV[1] 优惠券id  ARGV[2] 用户id
-- 返回值：0 成功；1 库存不足（或未预热）；2 重复下单
local couponId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'seckill:coupon:stock:' .. couponId
local orderKey = 'seckill:coupon:order:' .. couponId

local stock = tonumber(redis.call('get', stockKey))
if stock == nil then
    return 1
end
if stock <= 0 then
    return 1
end
-- 一人一单：同一优惠券每个用户只能抢一次
if redis.call('sismember', orderKey, userId) == 1 then
    return 2
end
-- 校验与扣减同脚本执行，中间不会被其他命令插入
redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)
return 0
