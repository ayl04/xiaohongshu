package com.quanxiaoha.xiaohashu.ky.api;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.ky.constant.ApiConstants;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.AddNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.dto.FindNoteContentDTO;
import com.quanxiaoha.xiaohashu.ky.pojo.vo.NoteContentVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentDTO noteContentDTO);

    @PostMapping(value = PREFIX + "/note/content/find")
    Response<NoteContentVo> findNoteContent(@RequestBody FindNoteContentDTO noteContentDTO);

    @PostMapping(value = PREFIX + "/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody FindNoteContentDTO noteContentDTO);
}
