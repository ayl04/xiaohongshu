package com.quanxiaoha.xiaohashu.user.relation.biz.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户粉丝表
 * </p>
 *
 * @author ayl
 * @since 2026-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_fans")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Fans implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 粉丝的用户ID
     */
    private Long fansUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
