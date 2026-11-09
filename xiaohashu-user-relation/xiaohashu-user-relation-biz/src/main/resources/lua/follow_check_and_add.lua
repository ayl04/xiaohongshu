--[[
  脚本作用：关注用户时，校验并添加关注关系到「我的关注」ZSet。
           （ZSet 已存在时的正常路径）
  入参：
    KEYS[1] : 「我的关注」列表 Redis Key（following:{userId}）
    ARGV[1] : 被关注用户 ID（followUserId）
    ARGV[2] : 关注时间戳（作为 ZSet 的 score）
  返回值：
    -1 : ZSet 不存在（需调用方从 DB 懒加载初始化）
    -2 : 关注数已达上限（1000）
    -3 : 已经关注过该用户
     0 : 添加成功
  调用方：RelationServiceImpl#follow() 中的 redisTemplate.execute(...)
]]
local key = KEYS[1]; -- 操作的 Redis Key
local followUserId = ARGV[1] -- 关注的用户ID
local timestamp = ARGV[2] -- 时间戳

-- 使用 EXISTS 命令检查 ZSET 是否存在
local exists = redis.call('EXISTS', key);
if exists == 0 then
    return -1
end
-- 校验关注人数是否上限（是否达到 1000）
local size = redis.call('ZCARD', key)
if size >= 1000 then
    return -2
end

-- 校验目标用户是否已经关注
if redis.call('ZSCORE', key, followUserId) then
    return -3
end

-- ZADD 添加关注关系
redis.call('ZADD', key, timestamp, followUserId)
return 0