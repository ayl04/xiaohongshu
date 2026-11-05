package com.quanxiaoha.xiaohashu.ky.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddNoteContentDTO {

    @NotBlank(message = "笔记内容 UUID 不能为空")
    private String noteId;

    @NotBlank(message = "笔记内容不能为空")
    private String content;
}
