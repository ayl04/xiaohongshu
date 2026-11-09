package com.quanxiaoha.xiaohashu.gateway.filter;

import com.quanxiaoha.xiaohashu.gateway.auth.SaTokenConfigure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Order(-90)
public class AddUserId2HeaderFilter implements GlobalFilter {

    /**
     * 请求头中，用户 ID 的键
     */
    private static final String HEADER_USER_ID = "userId";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("==================> TokenConvertFilter");

        // 从 SaReactorFilter 存入的 attributes 中读取用户 ID
        Long userId = exchange.getAttribute(SaTokenConfigure.USER_ID_ATTRIBUTE);

        // 未登录则直接放行（不注入 userId 请求头）
        if (userId == null) {
            return chain.filter(exchange);
        }

        log.info("## 当前登录的用户 ID: {}", userId);
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(HEADER_USER_ID, String.valueOf(userId)))// 将用户 ID 设置到请求头中
                .build();

        return chain.filter(newExchange);
    }
}
