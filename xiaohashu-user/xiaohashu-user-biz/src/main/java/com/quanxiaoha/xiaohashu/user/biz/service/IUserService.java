package com.quanxiaoha.xiaohashu.user.biz.service;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByIdDTO;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByPhoneReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.req.RegisterUserReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.req.UpdateUserPasswordReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdVo;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByPhoneRspDTO;
import com.quanxiaoha.xiaohashu.user.biz.pojo.dto.UpdateUserInfoDto;
import com.quanxiaoha.xiaohashu.user.biz.pojo.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author ayl
 * @since 2026-10-28
 */
public interface IUserService extends IService<User> {

    Response<?> updateUserInfo(UpdateUserInfoDto userInfoDto);

    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);

    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    Response<FindUserByIdVo> findUserById(FindUserByIdDTO findUserByIdDTO);
}
