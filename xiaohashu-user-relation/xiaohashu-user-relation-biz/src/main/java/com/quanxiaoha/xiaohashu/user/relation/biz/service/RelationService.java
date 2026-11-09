package com.quanxiaoha.xiaohashu.user.relation.biz.service;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.user.relation.biz.pojo.dto.FollowUserDto;
import com.quanxiaoha.xiaohashu.user.relation.biz.pojo.dto.UnfollowUserDto;

public interface RelationService {
    Response<?> follow(FollowUserDto userDto);

    Response<?> unfollow(UnfollowUserDto unfollowUser);
}
