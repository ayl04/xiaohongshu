package com.quanxiaoha.xiaohashu.auth.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author ayl
 * @since 2026-10-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 小哈书号(唯一凭证)
     */
    private String xiaohashuId;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 背景图
     */
    private String backgroundImg;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别(0：女 1：男)
     */
    private Integer sex;

    /**
     * 状态(0：启用 1：禁用)
     */
    private Integer status;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除(0：未删除 1：已删除)
     */
    private Boolean isDeleted;


}
