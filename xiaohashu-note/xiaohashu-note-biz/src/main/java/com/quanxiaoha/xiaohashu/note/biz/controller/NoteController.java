package com.quanxiaoha.xiaohashu.note.biz.controller;


import com.quanxiaoha.framework.biz.operationlog.aspect.ApiOperationLog;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.note.biz.pojo.FindNoteDetailVO;
import com.quanxiaoha.xiaohashu.note.biz.pojo.dto.*;
import com.quanxiaoha.xiaohashu.note.biz.service.INoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 笔记表 前端控制器
 * </p>
 *
 * @author ayl
 * @since 2026-10-30
 */
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {

    private final INoteService noteService;

    @PostMapping(value = "/publish")
    @ApiOperationLog(description = "笔记发布")
    public Response<?> publishNote(@Validated @RequestBody PublishNoteDto publishNoteDto) {
        return noteService.publishNote(publishNoteDto);
    }

    @PostMapping(value = "/detail")
    @ApiOperationLog(description = "笔记详情")
    public Response<FindNoteDetailVO> findNoteDetail(@Validated @RequestBody FindNoteDetailDto noteDetailDto) {
        return noteService.findNoteDetail(noteDetailDto);
    }

    @PostMapping(value = "/update")
    @ApiOperationLog(description = "笔记修改")
    public Response<?> updateNote(@Validated @RequestBody UpdateNoteDto updateNoteDto) {
        return noteService.updateNote(updateNoteDto);
    }

    @PostMapping(value = "/delete")
    @ApiOperationLog(description = "删除笔记")
    public Response<?> deleteNote(@Validated @RequestBody DeleteNoteDto deleteNoteDto) {
        return noteService.deleteNote(deleteNoteDto);
    }

    @PostMapping(value = "/visible/onlyme")
    @ApiOperationLog(description = "笔记仅对自己可见")
    public Response<?> visibleOnlyMe(@Validated @RequestBody NoteDto noteDto) {
        return noteService.visibleOnlyMe(noteDto);
    }

    @PostMapping(value = "/top")
    @ApiOperationLog(description = "置顶/取消置顶笔记")
    public Response<?> topNote(@Validated @RequestBody TopNoteDto topNoteDto) {
        return noteService.topNote(topNoteDto);
    }

}
