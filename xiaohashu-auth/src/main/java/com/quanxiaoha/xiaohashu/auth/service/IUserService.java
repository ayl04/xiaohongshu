package com.quanxiaoha.xiaohashu.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.pojo.dto.UpdatePasswordReqVO;
import com.quanxiaoha.xiaohashu.auth.pojo.dto.UserLoginReqVO;
import com.quanxiaoha.xiaohashu.auth.pojo.po.User;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author ayl
 * @since 2026-10-25
 */
@Service
public interface IUserService extends IService<User> {

    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);

    /**
     * 退出登录
     * @return
     */
    Response<?> logout();

    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);
}
