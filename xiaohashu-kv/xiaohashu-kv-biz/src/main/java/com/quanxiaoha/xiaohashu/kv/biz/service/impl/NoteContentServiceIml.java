package com.quanxiaoha.xiaohashu.kv.biz.service.impl;

import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;

import com.quanxiaoha.xiaohashu.kv.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.kv.biz.pojo.po.NoteContent;
import com.quanxiaoha.xiaohashu.kv.biz.pojo.repository.NoteContentRepository;
import com.quanxiaoha.xiaohashu.kv.biz.service.NoteContentService;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.AddNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.FindNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.vo.NoteContentVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class NoteContentServiceIml implements NoteContentService {

    private final NoteContentRepository noteContentRepository;

    @Override
    public Response<?> addNoteContent(AddNoteContentDTO noteContentDTO) {

        String noteId = noteContentDTO.getNoteId();
        NoteContent noteContent = NoteContent.builder()
                .id(UUID.fromString(noteId))
                .content(noteContentDTO.getContent())
                .build();


        noteContentRepository.save(noteContent);
        return Response.success();
    }

    @Override
    public Response<NoteContentVo> findNoteContent(FindNoteContentDTO noteContentDTO) {

        UUID uuid = UUID.fromString(noteContentDTO.getUuid());
        Optional<NoteContent> optional = noteContentRepository.findById(uuid);

        // 若笔记内容不存在
        if (!optional.isPresent()) {
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }

        NoteContent noteContent = optional.get();
        //构建返参 DTO
        NoteContentVo findNoteContentRspDTO = NoteContentVo.builder()
                .uuid(noteContent.getId())
                .content(noteContent.getContent())
                .build();

        return Response.success(findNoteContentRspDTO);
    }

    @Override
    public Response<?> deleteNoteContent(FindNoteContentDTO noteContentDTO) {

        noteContentRepository.deleteById(UUID.fromString(noteContentDTO.getUuid()));
        return Response.success();
    }
}
