package com.quanxiaoha.xiaohashu.user.biz.runner;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.user.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.user.biz.pojo.po.Permission;
import com.quanxiaoha.xiaohashu.user.biz.pojo.po.Role;
import com.quanxiaoha.xiaohashu.user.biz.pojo.po.RolePermissionRel;
import com.quanxiaoha.xiaohashu.user.biz.service.IPermissionService;
import com.quanxiaoha.xiaohashu.user.biz.service.IRolePermissionRelService;
import com.quanxiaoha.xiaohashu.user.biz.service.IRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推送角色权限数据到 Redis 中
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PushRolePermissions2RedisRunner implements ApplicationRunner {

    private final IRoleService roleService;
    private final IRolePermissionRelService rolePermissionRelService;
    private final IPermissionService permissionService;
    private final StringRedisTemplate redisTemplate;

    // 权限同步标记 Key
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";


    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        try {
            // 是否能够同步数据: 原子操作，只有在键 PUSH_PERMISSION_FLAG 不存在时，才会设置该键的值为 "1"，并设置过期时间为 1 天
            boolean canPushed = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.DAYS));

            // 如果无法同步权限数据
            if (!canPushed) {
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }

            // 查询出所有角色
            List<Role> roleList = roleService.lambdaQuery()
                    .eq(Role::getStatus, 0)
                    .eq(Role::getIsDeleted, 0)
                    .list();

            if (CollUtil.isNotEmpty(roleList)) {
                // 拿到所有角色的 ID
                List<Long> roleIds = roleList.stream().map(Role::getId).toList();

                // 根据角色 ID, 批量查询出所有角色对应的权限
                List<RolePermissionRel> rolePermissionS = rolePermissionRelService.lambdaQuery()
                        .in(RolePermissionRel::getRoleId, roleIds)
                        .list();
                // 按角色 ID 分组, 每个角色 ID 对应多个权限 ID
                Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionS.stream().collect(
                        Collectors.groupingBy(RolePermissionRel::getRoleId,
                                Collectors.mapping(RolePermissionRel::getPermissionId, Collectors.toList()))
                );

                // 查询 APP 端所有被启用的权限
                List<Permission> permissionS = permissionService.lambdaQuery()
                        .eq(Permission::getStatus, 0)
                        .eq(Permission::getIsDeleted, 0)
                        .list();                // 权限 ID - 权限 DO
                Map<Long, Permission> permissionIdDOMap = permissionS.stream().collect(
                        Collectors.toMap(Permission::getId, permissionDO -> permissionDO)
                );

                // 组织 角色ID-权限 关系
                Map<String, List<String>> roleIdPermissionDOMap = Maps.newHashMap();

                // 循环所有角色
                roleList.forEach(roleDO -> {
                    // 当前角色 ID
                    Long roleId = roleDO.getId();
                    String roleKey = roleDO.getRoleKey();
                    // 当前角色 ID 对应的权限 ID 集合
                    List<Long> permissionIds = roleIdPermissionIdsMap.get(roleId);
                    if (CollUtil.isNotEmpty(permissionIds)) {
                        List<String> perDOS = Lists.newArrayList();
                        permissionIds.forEach(permissionId -> {
                            // 根据权限 ID 获取具体的权限 DO 对象
                            Permission permissionDO = permissionIdDOMap.get(permissionId);
                            if (Objects.nonNull(permissionDO)) {
                                perDOS.add(permissionDO.getPermissionKey());
                            }
                        });
                        roleIdPermissionDOMap.put(roleKey, perDOS);
                    }
                });

                // 同步至 Redis 中，方便后续网关查询鉴权使用
                roleIdPermissionDOMap.forEach((roleKey, permissions) -> {
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleKey);
                    redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(permissions));
                });
            }

            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        } catch (Exception e) {
            log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
        }

    }
}
