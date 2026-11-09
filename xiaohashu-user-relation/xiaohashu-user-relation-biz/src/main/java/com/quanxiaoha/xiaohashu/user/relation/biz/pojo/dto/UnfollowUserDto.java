package com.quanxiaoha.xiaohashu.user.relation.biz.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取关用户请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnfollowUserDto {

    @NotNull(message = "被取关用户 ID 不能为空")
    private Long followUserId;
}