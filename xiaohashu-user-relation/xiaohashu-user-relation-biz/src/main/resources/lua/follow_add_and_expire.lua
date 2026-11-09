--[[
  脚本作用：关注列表 ZSet 不存在、且 DB 中没有历史关注记录时，添加首个关注项并设置过期时间。
  入参：
    KEYS[1] : 「我的关注」列表 Redis Key（following:{userId}）
    ARGV[1] : 被关注用户 ID（followUserId）
    ARGV[2] : 关注时间戳（作为 ZSet 的 score）
    ARGV[3] : 过期时间（秒，1~2 天随机值，防缓存雪崩）
  返回值：
    0 : 成功
  调用方：RelationServiceImpl#follow() 中 ZSet 不存在且 DB 空时的分支
]]
local key = KEYS[1] -- 操作的 Redis Key
local followUserId = ARGV[1] -- 关注的用户ID
local timestamp = ARGV[2] -- 时间戳
local expireSeconds = ARGV[3] -- 过期时间（秒）

-- ZADD 添加关注关系
redis.call('ZADD', key, timestamp, followUserId)
-- 设置过期时间
redis.call('EXPIRE', key, expireSeconds)
return 0