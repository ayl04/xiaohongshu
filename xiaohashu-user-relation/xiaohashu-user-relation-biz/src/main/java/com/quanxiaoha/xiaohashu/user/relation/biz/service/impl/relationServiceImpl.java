package com.quanxiaoha.xiaohashu.user.relation.biz.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.quanxiaoha.framework.biz.context.holder.LoginUserContextHolder;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.util.DateUtils;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.user.api.UserFeignApi;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByIdDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdVo;
import com.quanxiaoha.xiaohashu.user.relation.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.user.relation.biz.enums.LuaResultEnum;
import com.quanxiaoha.xiaohashu.user.relation.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.user.relation.biz.pojo.dto.FollowUserDto;
import com.quanxiaoha.xiaohashu.user.relation.biz.pojo.dto.FollowUserMqDTO;
import com.quanxiaoha.xiaohashu.user.relation.biz.pojo.po.Following;
import com.quanxiaoha.xiaohashu.user.relation.biz.service.IFollowingService;
import com.quanxiaoha.xiaohashu.user.relation.biz.service.RelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class relationServiceImpl implements RelationService {

    private final StringRedisTemplate redisTemplate;
    private final UserFeignApi userFeignApi;
    private final IFollowingService followingService;
    private final RocketMQTemplate rocketMQTemplate;


    @Override
    public Response<?> follow(FollowUserDto userDto) {

        Long followUserId = userDto.getFollowUserId();
        Long userId = LoginUserContextHolder.getUserId();

        if (Objects.equals(userId, followUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }

        FindUserByIdDTO findUserByIdDTO = new FindUserByIdDTO();
        findUserByIdDTO.setId(followUserId);
        Response<FindUserByIdVo> response = userFeignApi.findById(findUserByIdDTO);
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }

        String followingRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        script.setResultType(Long.class);

        LocalDateTime now = LocalDateTime.now();
        long timestamp = DateUtils.localDateTime2Timestamp(now);

        // ✅ 修复：timestamp 必须转 String
        Long result = redisTemplate.execute(script,
                Collections.singletonList(followingRedisKey),
                String.valueOf(followUserId),
                String.valueOf(timestamp));

        checkLuaScriptResult(result);

        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) {
            List<Following> followingList = followingService.lambdaQuery()
                    .eq(Following::getUserId, userId)
                    .list();

            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            if (CollUtil.isEmpty(followingList)) {
                DefaultRedisScript<Long> script1 = new DefaultRedisScript<>();
                script1.setScriptSource(
                        new ResourceScriptSource(
                                new ClassPathResource("/lua/follow_add_and_expire.lua")));
                script1.setResultType(Long.class);

                // ✅ 修复：timestamp、expireSeconds 都必须转 String
                redisTemplate.execute(script1,
                        Collections.singletonList(followingRedisKey),
                        String.valueOf(followUserId),
                        String.valueOf(timestamp),
                        String.valueOf(expireSeconds));
            } else {
                Object[] luaArgs = buildLuaArgs(followingList, expireSeconds);

                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                // ✅ 修复：再次执行 script 时 timestamp 也要转 String
                result = redisTemplate.execute(script,
                        Collections.singletonList(followingRedisKey),
                        String.valueOf(followUserId),
                        String.valueOf(timestamp));
                checkLuaScriptResult(result);
            }
        }

        FollowUserMqDTO followUserMqDTO = FollowUserMqDTO.builder()
                .userId(userId)
                .followUserId(followUserId)
                .createTime(now)
                .build();

        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMqDTO)).build();
        String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_FOLLOW;

        log.info("==> 开始发送关注操作 MQ, 消息体: {}", followUserMqDTO);

        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }

    private static void checkLuaScriptResult(Long result) {
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf(result);
        if (Objects.isNull(luaResultEnum)) throw new RuntimeException("Lua 返回结果错误");
        switch (luaResultEnum) {
            case FOLLOW_LIMIT -> throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            case ALREADY_FOLLOWED -> throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWED);
        }
    }

    /**
     * ✅ 修复：所有元素统一转为 String
     */
    private static Object[] buildLuaArgs(List<Following> followingList, long expireSeconds) {
        int argsLength = followingList.size() * 2 + 1;
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (Following following : followingList) {
            // ✅ score 转 String
            luaArgs[i] = String.valueOf(DateUtils.localDateTime2Timestamp(following.getCreateTime()));
            // ✅ value 转 String
            luaArgs[i + 1] = String.valueOf(following.getFollowingUserId());
            i += 2;
        }

        // ✅ 过期时间转 String
        luaArgs[argsLength - 1] = String.valueOf(expireSeconds);
        return luaArgs;
    }



}