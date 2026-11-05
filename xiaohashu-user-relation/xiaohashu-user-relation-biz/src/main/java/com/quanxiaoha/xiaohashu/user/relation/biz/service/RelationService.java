package com.quanxiaoha.xiaohashu.user.relation.biz.service;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.relation.biz.pojo.dto.FollowUserDto;

public interface RelationService {
    Response<?> follow(FollowUserDto userDto);
}
