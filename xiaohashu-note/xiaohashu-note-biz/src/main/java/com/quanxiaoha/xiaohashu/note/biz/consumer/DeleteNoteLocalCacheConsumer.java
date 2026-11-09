package com.quanxiaoha.xiaohashu.note.biz.consumer;

import com.quanxiaoha.xiaohashu.note.biz.constant.MQConstants;
import com.quanxiaoha.xiaohashu.note.biz.service.INoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohashu_group",
        topic = MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE,// 消费的主题 Topic
        messageModel = MessageModel.BROADCASTING // 广播模式
)
@RequiredArgsConstructor
public class DeleteNoteLocalCacheConsumer implements RocketMQListener<String> {

    private final INoteService noteService;
    @Override
    public void onMessage(String message) {
        Long noteId = Long.valueOf(message);
        log.info("## 消费者消费成功, noteId: {}", noteId);
        noteService.deleteNoteLocalCache(noteId);
    }
}
