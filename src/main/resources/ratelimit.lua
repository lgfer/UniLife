-- 滑动窗口限流
-- KEYS[1] 窗口 ZSET
-- ARGV[1] 当前时间戳(ms)  ARGV[2] 窗口长度(ms)  ARGV[3] 阈值  ARGV[4] 本次请求唯一标识
-- 返回：1 放行；0 拒绝
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local max = tonumber(ARGV[3])

-- 先移除窗口之外的旧请求记录
redis.call('zremrangebyscore', KEYS[1], 0, now - window)
-- 统计窗口内的请求数
local current = redis.call('zcard', KEYS[1])
if current >= max then
    return 0
end
-- 放行：记录本次请求，并续一个窗口长度的过期时间兜底
redis.call('zadd', KEYS[1], now, ARGV[4])
redis.call('pexpire', KEYS[1], window)
return 1
