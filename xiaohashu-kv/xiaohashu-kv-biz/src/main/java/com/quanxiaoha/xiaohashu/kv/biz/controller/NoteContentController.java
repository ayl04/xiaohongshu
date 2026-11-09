package com.quanxiaoha.xiaohashu.kv.biz.controller;

import com.quanxiaoha.framework.common.response.Response;

import com.quanxiaoha.xiaohashu.kv.biz.service.NoteContentService;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.AddNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.FindNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.vo.NoteContentVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/kv")
public class NoteContentController {

    private final NoteContentService noteContentService;

    @PostMapping(value = "/note/content/add")
    public Response<?> addNoteContent(@Validated @RequestBody AddNoteContentDTO noteContentDTO) {
        return noteContentService.addNoteContent(noteContentDTO);
    }

    @PostMapping(value = "/note/content/find")
    public Response<NoteContentVo> findNoteContent(@Validated @RequestBody FindNoteContentDTO noteContentDTO) {
        return noteContentService.findNoteContent(noteContentDTO);
    }

    @PostMapping(value = "/note/content/delete")
    public Response<?> deleteNoteContent(@Validated @RequestBody FindNoteContentDTO noteContentDTO) {
        return noteContentService.deleteNoteContent(noteContentDTO);
    }
}
