package com.quanxiaoha.xiaohashu.auth;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootTest
@Slf4j
public class ThreadPoolTaskExecutorTests {

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Test
    void testSubmit() {
        taskExecutor.submit(() -> log.info("异步线程中说: 犬小哈专栏"));
    }
}
