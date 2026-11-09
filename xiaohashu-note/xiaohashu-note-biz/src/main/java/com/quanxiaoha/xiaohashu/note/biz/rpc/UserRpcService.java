package com.quanxiaoha.xiaohashu.note.biz.rpc;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.api.UserFeignApi;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByIdDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserRpcService {

    private final UserFeignApi userFeignApi;

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    public FindUserByIdVo findById(Long userId) {

        FindUserByIdDTO findUserByIdDTO = new FindUserByIdDTO();
        findUserByIdDTO.setId(userId);
        Response<FindUserByIdVo> response = userFeignApi.findById(findUserByIdDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }
}
