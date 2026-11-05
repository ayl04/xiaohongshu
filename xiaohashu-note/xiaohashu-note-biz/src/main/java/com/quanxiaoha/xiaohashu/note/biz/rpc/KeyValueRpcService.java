package com.quanxiaoha.xiaohashu.note.biz.rpc;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.ky.api.KeyValueFeignApi;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.AddNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.FindNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.vo.NoteContentVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeyValueRpcService {

    private final KeyValueFeignApi keyValueFeignApi;

    /**
     * 保存笔记内容
     *
     * @param uuid
     * @param content
     * @return
     */
    public boolean saveNoteContent(String uuid, String content) {

        AddNoteContentDTO noteContentDTO = new AddNoteContentDTO();
        noteContentDTO.setNoteId(uuid);
        noteContentDTO.setContent(content);
        Response<?> response = keyValueFeignApi.addNoteContent(noteContentDTO);

        return Objects.nonNull(response) && response.isSuccess();
    }

    /**
     * 删除笔记内容
     *
     * @param uuid
     * @return
     */
    public boolean deleteNoteContent(String uuid) {

        FindNoteContentDTO findNoteContentDTO = new FindNoteContentDTO();
        findNoteContentDTO.setUuid(uuid);
        Response<?> response = keyValueFeignApi.deleteNoteContent(findNoteContentDTO);

        return Objects.nonNull(response) && response.isSuccess();
    }

    /**
     * 查询笔记内容
     *
     * @param uuid
     * @return
     */
    public String findNoteContent(String uuid) {

        FindNoteContentDTO noteContentDTO = new FindNoteContentDTO();
        noteContentDTO.setUuid(uuid);

        Response<NoteContentVo> response = keyValueFeignApi.findNoteContent(noteContentDTO);

        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }
        return response.getData().getContent();
    }
}
