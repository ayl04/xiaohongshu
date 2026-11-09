--[[
  脚本作用：关注列表 ZSet 不存在、但 DB 中存在历史关注记录时，把 DB 数据批量回灌到 ZSet
           并设置过期时间（懒加载初始化，一次性恢复整份关注列表）。
  入参：
    KEYS[1]      : 「我的关注」列表 Redis Key（following:{userId}）
    ARGV[奇数位] : 关注时间戳（作为 ZSet 的 score）
    ARGV[偶数位] : 被关注用户 ID（作为 ZSet 的 member）
    ARGV[末位]   : 过期时间（秒）
  返回值：
    0 : 成功
  调用方：RelationServiceImpl#follow() 中 ZSet 不存在但 DB 有数据时的分支
]]
-- 操作的 Key
local key = KEYS[1]

-- 准备批量添加数据的参数
local zaddArgs = {}

-- 遍历 ARGV 参数，将分数和值按顺序插入到 zaddArgs 变量中
for i = 1, #ARGV - 1, 2 do
    table.insert(zaddArgs, ARGV[i])      -- 分数（关注时间）
    table.insert(zaddArgs, ARGV[i+1])    -- 值（关注的用户ID）
end

-- 调用 ZADD 批量插入数据
redis.call('ZADD', key, unpack(zaddArgs))

-- 设置 ZSet 的过期时间
local expireTime = ARGV[#ARGV] -- 最后一个参数为过期时间
redis.call('EXPIRE', key, expireTime)

return 0
