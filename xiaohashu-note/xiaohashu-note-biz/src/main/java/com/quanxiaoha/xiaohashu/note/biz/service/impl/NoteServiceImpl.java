package com.quanxiaoha.xiaohashu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.quanxiaoha.framework.biz.context.holder.LoginUserContextHolder;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.note.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.note.biz.enums.NoteStatusEnum;
import com.quanxiaoha.xiaohashu.note.biz.enums.NoteTypeEnum;
import com.quanxiaoha.xiaohashu.note.biz.enums.NoteVisibleEnum;
import com.quanxiaoha.xiaohashu.note.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.note.biz.mapper.NoteMapper;
import com.quanxiaoha.xiaohashu.note.biz.pojo.FindNoteDetailVO;
import com.quanxiaoha.xiaohashu.note.biz.pojo.dto.*;
import com.quanxiaoha.xiaohashu.note.biz.pojo.po.Note;
import com.quanxiaoha.xiaohashu.note.biz.pojo.po.Topic;
import com.quanxiaoha.xiaohashu.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.quanxiaoha.xiaohashu.note.biz.rpc.KeyValueRpcService;
import com.quanxiaoha.xiaohashu.note.biz.rpc.UserRpcService;
import com.quanxiaoha.xiaohashu.note.biz.service.INoteService;
import com.quanxiaoha.xiaohashu.note.biz.service.ITopicService;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdVo;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 笔记表 服务实现类
 * </p>
 *
 * @author ayl
 * @since 2026-10-30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements INoteService {

    private final KeyValueRpcService keyValueRpcService;
    private final DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    private final ITopicService topicService;
    private final UserRpcService userRpcService;
    private final StringRedisTemplate redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 笔记详情本地缓存
     */
    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
            .build();

    /**
     * 笔记发布
     *
     * @param publishNoteDto
     * @return
     */
    @Override
    public Response<?> publishNote(PublishNoteDto publishNoteDto) {

        Integer type = publishNoteDto.getType();
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUrlStr = null;
        String videoUri = null;
        switch (noteTypeEnum){
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUris = publishNoteDto.getImgUris();

                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUris), "笔记图片不能为空");

                // 校验图片数量
                Preconditions.checkArgument(imgUris.size() <= 8, "笔记图片不能多于 8 张");

                imgUrlStr = StringUtils.join(imgUris, ",");
                break;
            case VIDEO: // 视频笔记
                videoUri = publishNoteDto.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // RPC: 调用分布式 ID 生成服务，生成笔记 ID
        String snowflakeId = distributedIdGeneratorRpcService.getSnowflakeId();

        // 笔记内容 UUID
        UUID uuid = null;
        Boolean isContentEmpty = true;
        if (StringUtils.isNotBlank(publishNoteDto.getContent())){
            isContentEmpty = false;
            uuid = UUID.randomUUID();
            boolean isSavedSuccess = keyValueRpcService.saveNoteContent(String.valueOf(uuid), publishNoteDto.getContent());

            // 若存储失败，抛出业务异常，提示用户发布笔记失败
            if (!isSavedSuccess) {
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }
        }

        Long topicId = publishNoteDto.getTopicId();
        Topic topic = null;
        if (Objects.nonNull(topicId)) {
            // 获取话题名称
            topic = topicService.getById(topicId);
        }

        Note note = new Note();
        note.setId(Long.valueOf(snowflakeId));
        note.setTitle(publishNoteDto.getTitle());
        note.setIsContentEmpty(isContentEmpty);
        note.setCreatorId(LoginUserContextHolder.getUserId());
        note.setTopicId(topicId);
        note.setTopicName(topic.getName());
        note.setType(publishNoteDto.getType());
        note.setImgUris(imgUrlStr);
        note.setVideoUri(videoUri);
        note.setVisible(NoteStatusEnum.NORMAL.getCode());
        LocalDateTime now = LocalDateTime.now();
        note.setCreateTime(now);
        note.setUpdateTime(now);
        note.setIsTop(Boolean.FALSE);
        note.setStatus(NoteStatusEnum.NORMAL.getCode());
        note.setContentUuid(String.valueOf(uuid));

        try {
            save(note);
        } catch (Exception e) {
            log.error("==> 笔记存储失败", e);

            if (StringUtils.isNotBlank(String.valueOf(uuid))) {
                keyValueRpcService.deleteNoteContent(String.valueOf(uuid));
            }
        }


        return Response.success();
    }

    @Override
    @SneakyThrows
    public Response<FindNoteDetailVO> findNoteDetail(FindNoteDetailDto noteDetailDto) {
        Long noteId = noteDetailDto.getId();


        Long userId = LoginUserContextHolder.getUserId();

        // 先从本地缓存中查询
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailRspVOStrLocalCache)) {
            FindNoteDetailVO findNoteDetailRspVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailVO.class);
            log.info("==> 命中了本地缓存；{}", findNoteDetailRspVOStrLocalCache);
            // 可见性校验
            checkNoteVisibleFromVO(userId, findNoteDetailRspVO);
            return Response.success(findNoteDetailRspVO);
        }

        String noteDetailKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String noteDetailStr = redisTemplate.opsForValue().get(noteDetailKey);

        // 若缓存中有该笔记的数据，则直接返回
        if (StringUtils.isNotBlank(noteDetailStr)) {
            if ("null".equals(noteDetailStr)){
                throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
            }
            FindNoteDetailVO noteDetailVO = JsonUtils.parseObject(noteDetailStr, FindNoteDetailVO.class);
            // 将用户信息存入本地缓存
            // 写入本地缓存
            LOCAL_CACHE.put(noteId,
                        Objects.isNull(noteDetailVO) ? "null" : JsonUtils.toJsonString(noteDetailVO));
            // 可见性校验
            checkNoteVisibleFromVO(userId, noteDetailVO);

            return Response.success(noteDetailVO);
        }


        Note note = lambdaQuery()
                .eq(Note::getId, noteId)
                .eq(Note::getStatus, NoteStatusEnum.NORMAL.getCode())
                .one();


        // 若该笔记不存在，则抛出业务异常
        if (Objects.isNull(note)) {
            long expireSeconds = 60 + RandomUtil.randomInt(60);
            redisTemplate.opsForValue().set(noteDetailKey, "null", expireSeconds, TimeUnit.SECONDS);
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 可见性校验
        Integer visible = note.getVisible();
        checkNoteVisible(visible, userId, note.getCreatorId());


        // 并发查询优化
        // RPC: 调用用户服务
        Long creatorId = note.getCreatorId();
        CompletableFuture<FindUserByIdVo> userResultFuture = CompletableFuture
                .supplyAsync(() -> userRpcService.findById(creatorId), threadPoolTaskExecutor);

        // RPC: 调用 K-V 存储服务获取内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(note.getIsContentEmpty(), Boolean.FALSE)) {
            contentResultFuture  = CompletableFuture
                    .supplyAsync(() -> keyValueRpcService.findNoteContent(note.getContentUuid()), threadPoolTaskExecutor);
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<FindNoteDetailVO> resultFuture  = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                    // 获取 Future 返回的结果
                    FindUserByIdVo userInfo = userResultFuture.join();
                    String content = finalContentResultFuture.join();
                    // 笔记类型
                    Integer noteType = note.getType();
                    // 图文笔记图片链接(字符串)
                    String imgUrisStr = note.getImgUris();
                    // 图文笔记图片链接(集合)
                    List<String> imgUris = null;
                    // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
                    if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode())
                            && StringUtils.isNotBlank(imgUrisStr)) {
                        imgUris = List.of(imgUrisStr.split(","));
                    }


                    // 构建返参 VO 实体类
                    return FindNoteDetailVO.builder()
                            .id(note.getId())
                            .type(note.getType())
                            .title(note.getTitle())
                            .content(content)
                            .imgUris(imgUris)
                            .topicId(note.getTopicId())
                            .topicName(note.getTopicName())
                            .creatorId(note.getCreatorId())
                            .creatorName(userInfo.getNickName())
                            .avatar(userInfo.getAvatar())
                            .videoUri(note.getVideoUri())
                            .updateTime(note.getUpdateTime())
                            .visible(note.getVisible())
                            .build();
                });

        FindNoteDetailVO noteDetailVO = resultFuture.get();

        LOCAL_CACHE.put(noteId, JsonUtils.toJsonString(noteDetailVO));
        // 异步线程中将笔记详情存入 Redis
        threadPoolTaskExecutor.submit(() -> {
            try {
                String noteDetailJson = JsonUtils.toJsonString(noteDetailVO);
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                redisTemplate.opsForValue().set(noteDetailKey, noteDetailJson, expireSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                // 兜底异常，防止异步报错无感知
                log.error("==> 异步写入笔记详情 Redis 缓存失败, noteId: {}", noteId, e);
            }
        });


        return Response.success(noteDetailVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> updateNote(UpdateNoteDto updateNoteDto) {
        Integer type = updateNoteDto.getType();
        Long noteId = updateNoteDto.getId();
        Long userId = LoginUserContextHolder.getUserId();

        // 1. 查原笔记并校验权限
        Note oldNote = lambdaQuery()
                .eq(Note::getId, noteId)
                .eq(Note::getStatus, NoteStatusEnum.NORMAL.getCode())
                .eq(Note::getCreatorId, userId)
                .one();

        if (Objects.isNull(oldNote)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        // 2. 处理图文/视频参数
        String imgUris = null;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT:
                List<String> imgUriList = updateNoteDto.getImgUris();
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");
                imgUris = StringUtils.join(imgUriList, ",");
                break;
            case VIDEO:
                videoUri = updateNoteDto.getVideoUri();
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // 3. 话题处理（防 NPE）
        Long topicId = updateNoteDto.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            Topic topic = topicService.lambdaQuery()
                    .eq(Topic::getId, topicId)
                    .eq(Topic::getIsDeleted, 0)
                    .one();
            if (Objects.isNull(topic)) {
                throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
            }
            topicName = topic.getName();
        }

        // 4. 更新数据库（先把 DB 的操作全部做完）
        String content = updateNoteDto.getContent();
        String oldContentUuid = oldNote.getContentUuid();
        String currentContentUuid = oldContentUuid;
        boolean needUpdateUuid = false;

        if (StringUtils.isNotBlank(content) && StringUtils.isBlank(oldContentUuid)) {
            currentContentUuid = UUID.randomUUID().toString();
            needUpdateUuid = true;
        }

        Note note = Note.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUris(imgUris)
                .title(updateNoteDto.getTitle())
                .topicId(topicId)
                .topicName(topicName)
                .type(type)
                .updateTime(LocalDateTime.now())
                .videoUri(videoUri)
                .contentUuid(currentContentUuid)
                .build();

        lambdaUpdate()
                .eq(Note::getId, noteId)
                .update(note);

        // 延迟双删的缓存 key，事务提交后在 afterCommit 里使用
        String key = RedisKeyConstants.buildNoteDetailKey(noteId);

        // 5. 处理 KV 存储（此时如果 RPC 失败，抛出异常会让 MySQL 回滚）
        boolean isUpdateContentSuccess;
        if (StringUtils.isBlank(content)) {
            if (StringUtils.isNotBlank(oldContentUuid)) {
                isUpdateContentSuccess = keyValueRpcService.deleteNoteContent(oldContentUuid);
            } else {
                isUpdateContentSuccess = true;
            }
        } else {
            isUpdateContentSuccess = keyValueRpcService.saveNoteContent(currentContentUuid, content);
        }

        if (!isUpdateContentSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }

        // 6. 使用事务同步器，在事务提交成功后再删缓存
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {

                // 第一次删：删除 Redis 缓存
                redisTemplate.delete(key);

                // 第二次删（延迟双删）：发送延时消息，N 秒后再次删除 Redis，兜底并发窗口期写回的脏数据
                Message<String> message = MessageBuilder.withPayload(String.valueOf(noteId))
                        .build();
                rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, message,
                        new SendCallback() {
                            @Override
                            public void onSuccess(SendResult sendResult) {
                                log.info("## 延时删除 Redis 笔记缓存消息发送成功...");
                            }

                            @Override
                            public void onException(Throwable e) {
                                log.error("## 延时删除 Redis 笔记缓存消息发送失败...", e);
                            }
                        },
                        3000, // 超时时间(毫秒)
                        1 // 延迟级别，1 表示延时 1s
                );

                // 广播删除各实例本地缓存
                rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
                log.info("====> MQ：删除笔记本地缓存发送成功...");
                log.info("==> 事务提交成功，已删除笔记详情缓存，noteId: {}", noteId);
            }
        });

        return Response.success();
    }

    @Override
    public void deleteNoteLocalCache(Long noteId) {
        LOCAL_CACHE.invalidate(noteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> deleteNote(DeleteNoteDto deleteNoteDto) {

        Long noteId = deleteNoteDto.getId();

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();

        // 逻辑删除
        boolean isSuccess = lambdaUpdate()
                .eq(Note::getId, noteId)
                .eq(Note::getCreatorId, currUserId)
                .eq(Note::getStatus, NoteStatusEnum.NORMAL.getCode())
                .set(Note::getStatus, NoteStatusEnum.DELETED.getCode())
                .set(Note::getUpdateTime, LocalDateTime.now())
                .update();

        if (!isSuccess){
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        String key = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(key);
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");
        return Response.success();
    }

    @Override
    public Response<?> visibleOnlyMe(NoteDto noteDto) {
        // 笔记 ID
        Long noteId = noteDto.getId();

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();

        // 执行更新 SQL
        boolean isSuccess = lambdaUpdate()
                .eq(Note::getId, noteId)
                .eq(Note::getCreatorId, currUserId)
                .eq(Note::getStatus, NoteStatusEnum.NORMAL.getCode())
                .set(Note::getUpdateTime, LocalDateTime.now())
                .set(Note::getVisible, NoteVisibleEnum.PRIVATE.getCode())
                .update();

        // 若影响的行数为 0，则表示该笔记无法修改为仅自己可见
        if (!isSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_VISIBLE_ONLY_ME);
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return Response.success();
    }

    @Override
    public Response<?> topNote(TopNoteDto topNoteDto) {
        // 笔记 ID
        Long noteId = topNoteDto.getId();
        // 是否置顶
        Boolean isTop = topNoteDto.getIsTop();

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();


        // 执行更新 SQL
        boolean isSuccess = lambdaUpdate()
                .eq(Note::getId, noteId)
                .eq(Note::getCreatorId, currUserId)
                .eq(Note::getStatus, NoteStatusEnum.NORMAL.getCode())
                .set(Note::getUpdateTime, LocalDateTime.now())
                .set(Note::getIsTop, isTop)
                .update();

        if (!isSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return Response.success();
    }


    /**
     * 校验笔记的可见性
     * @param visible 是否可见
     * @param currUserId 当前用户 ID
     * @param creatorId 笔记创建者
     */
    private void checkNoteVisible(Integer visible, Long currUserId, Long creatorId) {
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(currUserId, creatorId)) { // 仅自己可见, 并且访问用户为笔记创建者才能访问，非本人则抛出异常
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }

    /**
     * 校验笔记的可见性（针对 VO 实体类）
     * @param userId
     * @param findNoteDetailRspVO
     */
    private void checkNoteVisibleFromVO(Long userId, FindNoteDetailVO findNoteDetailRspVO) {
        if (Objects.nonNull(findNoteDetailRspVO)) {
            Integer visible = findNoteDetailRspVO.getVisible();
            checkNoteVisible(visible, userId, findNoteDetailRspVO.getCreatorId());
        }
    }
}
