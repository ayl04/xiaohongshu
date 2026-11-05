package com.quanxiaoha.xiaohashu.note.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.note.biz.pojo.FindNoteDetailVO;
import com.quanxiaoha.xiaohashu.note.biz.pojo.dto.*;
import com.quanxiaoha.xiaohashu.note.biz.pojo.po.Note;

/**
 * <p>
 * 笔记表 服务类
 * </p>
 *
 * @author ayl
 * @since 2026-10-30
 */
public interface INoteService extends IService<Note> {

    Response<?> publishNote(PublishNoteDto publishNoteDto);

    Response<FindNoteDetailVO> findNoteDetail(FindNoteDetailDto noteDetailDto);

    Response<?> updateNote(UpdateNoteDto updateNoteDto);

    /**
     * 删除本地笔记缓存
     * @param noteId
     */
    void deleteNoteLocalCache(Long noteId);

    Response<?> deleteNote(DeleteNoteDto deleteNoteDto);

    Response<?> visibleOnlyMe(NoteDto noteDto);

    Response<?> topNote(TopNoteDto topNoteDto);
}
