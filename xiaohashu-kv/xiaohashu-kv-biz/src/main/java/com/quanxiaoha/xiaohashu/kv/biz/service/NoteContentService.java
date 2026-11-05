package com.quanxiaoha.xiaohashu.kv.biz.service;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.AddNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.FindNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.vo.NoteContentVo;


public interface NoteContentService {
    Response<?> addNoteContent(AddNoteContentDTO noteContentDTO);

    Response<NoteContentVo> findNoteContent(FindNoteContentDTO noteContentDTO);

    Response<?> deleteNoteContent(FindNoteContentDTO noteContentDTO);
}
