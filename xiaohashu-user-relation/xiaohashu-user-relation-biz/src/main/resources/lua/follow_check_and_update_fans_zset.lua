--[[
  脚本作用：关注成功后，更新「被关注用户的粉丝列表」ZSet（消费 MQ 后执行）。
  入参：
    KEYS[1] : 被关注用户的「粉丝列表」Redis Key（fans:{followUserId}）
    ARGV[1] : 粉丝用户 ID（fansUserId）
    ARGV[2] : 关注时间戳（作为 ZSet 的 score）
  逻辑：
    1) ZSet 不存在则返回 -1；
    2) 粉丝数达到 5000 上限时，先 ZPOPMIN 淘汰最早关注的那个粉丝，再 ZADD 新粉丝。
  返回值：
    -1 : 粉丝 ZSet 不存在（注意：当前消费端未做 DB 懒加载初始化，会静默跳过）
     0 : 成功
  调用方：FollowUnfollowConsumer#handleFollowTagMessage() 中 DB 写库成功后执行
]]
local key = KEYS[1] -- 操作的 Redis Key
local fansUserId = ARGV[1] -- 粉丝ID
local timestamp = ARGV[2] -- 时间戳

-- 使用 EXISTS 命令检查 ZSET 粉丝列表是否存在
local exists = redis.call('EXISTS', key)
if exists == 0 then
    return -1
end

-- 获取粉丝列表大小
local size = redis.call('ZCARD', key)

-- 若超过 5000 个粉丝，则移除最早关注的粉丝
if size >= 5000 then
    redis.call('ZPOPMIN', key)
end

-- 添加新的粉丝关系
redis.call('ZADD', key, timestamp, fansUserId)
return 0
