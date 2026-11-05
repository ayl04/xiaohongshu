package com.quanxiaoha.xiaohashu.ky.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteContentDTO {

    @NotBlank(message = "笔记 ID 不能为空")
    private String uuid;
}
