package com.quanxiaoha.xiaohashu.user.biz.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.quanxiaoha.framework.biz.context.holder.LoginUserContextHolder;
import com.quanxiaoha.framework.common.enums.DeletedEnum;
import com.quanxiaoha.framework.common.enums.StatusEnum;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.framework.common.util.ParamUtils;
import com.quanxiaoha.xiaohashu.user.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.user.biz.constant.RoleConstants;
import com.quanxiaoha.xiaohashu.user.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.user.biz.enums.SexEnum;
import com.quanxiaoha.xiaohashu.user.biz.mapper.UserMapper;
import com.quanxiaoha.xiaohashu.user.biz.pojo.dto.UpdateUserInfoDto;
import com.quanxiaoha.xiaohashu.user.biz.pojo.po.Role;
import com.quanxiaoha.xiaohashu.user.biz.pojo.po.User;
import com.quanxiaoha.xiaohashu.user.biz.pojo.po.UserRoleRel;
import com.quanxiaoha.xiaohashu.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.quanxiaoha.xiaohashu.user.biz.rpc.OssRpcService;
import com.quanxiaoha.xiaohashu.user.biz.service.IRoleService;
import com.quanxiaoha.xiaohashu.user.biz.service.IUserRoleRelService;
import com.quanxiaoha.xiaohashu.user.biz.service.IUserService;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByIdDTO;
import com.quanxiaoha.xiaohashu.user.dto.req.FindUserByPhoneReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.req.RegisterUserReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.req.UpdateUserPasswordReqDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdVo;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByPhoneRspDTO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author ayl
 * @since 2026-10-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final OssRpcService ossRpcService;
    private final StringRedisTemplate redisTemplate;
    private final IUserRoleRelService userRoleRelService;
    private final IRoleService roleService;
    private final DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 用户信息本地缓存
     */
    private static final Cache<Long, FindUserByIdVo> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
            .build();


    @Override
    public Response<?> updateUserInfo(UpdateUserInfoDto userInfoDto) {
        User user = new User();
        // 设置当前需要更新的用户 ID
        user.setId(LoginUserContextHolder.getUserId());
        // 标识位：是否需要更新
        boolean needUpdate = false;

        // 头像
        MultipartFile avatarFile = userInfoDto.getAvatar();

        if (Objects.nonNull(avatarFile)) {
            String avatar = ossRpcService.uploadFile(avatarFile);
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatar);

            if (StringUtils.isBlank(avatar)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }

            user.setAvatar(avatar);
            needUpdate = true;
        }

        // 昵称
        String nickname = userInfoDto.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL);
            user.setNickname(nickname);
            needUpdate = true;
        }

        // 小哈书号
        String xiaohashuId = userInfoDto.getXiaohashuId();
        if (StringUtils.isNotBlank(xiaohashuId)) {
            Preconditions.checkArgument(ParamUtils.checkXiaohashuId(xiaohashuId), ResponseCodeEnum.XIAOHASHU_ID_VALID_FAIL);
            user.setXiaohashuId(xiaohashuId);
            needUpdate = true;
        }

        // 性别
        Integer sex = userInfoDto.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL);
            user.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = userInfoDto.getBirthday();
        if (Objects.nonNull(birthday)) {
            user.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = userInfoDto.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL);
            user.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = userInfoDto.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            String backgroundImg = ossRpcService.uploadFile(backgroundImgFile);
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundImg);

            if (StringUtils.isBlank(backgroundImg)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }

            user.setBackgroundImg(backgroundImg);
            needUpdate = true;
        }

        if (needUpdate) {
            // 更新用户信息
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO) {
        String phone = registerUserReqDTO.getPhone();

        // 先判断该手机号是否已被注册
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(user));

        // 若已注册，则直接返回用户 ID
        if (Objects.nonNull(user)) {
            return Response.success(user.getId());
        }

        // 否则注册新用户
        // 获取全局自增的小哈书 ID
        String xiaohashuId = distributedIdGeneratorRpcService.getXiaohashuId();
        String userIdStr = distributedIdGeneratorRpcService.getUserId();
        Long userId = Long.valueOf(userIdStr);

        User userRe = User.builder()
                .id(userId)
                .phone(phone)
                .xiaohashuId(xiaohashuId) // 自动生成小红书号 ID
                .nickname("小红薯" + xiaohashuId) // 自动生成昵称, 如：小红薯10000
                .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                .build();

        // 添加入库
        save(userRe);

        // 获取刚刚添加入库的用户 ID
        userId = userRe.getId();

        // 给该用户分配一个默认角色
        UserRoleRel userRoleDO = UserRoleRel.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userRoleRelService.save(userRoleDO);

        Role role = roleService.getById(RoleConstants.COMMON_USER_ROLE_ID);

        // 将该用户的角色 ID 存入 Redis 中
        List<String> roles = new ArrayList<>(1);
        roles.add(role.getRoleKey());

        String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        return Response.success(userId);
    }

    @Override
    public Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO) {
        String phone = findUserByPhoneReqDTO.getPhone();

        // 根据手机号查询用户信息
        User user = lambdaQuery().eq(User::getPhone, phone).one();

        // 判空
        if (Objects.isNull(user)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByPhoneRspDTO findUserByPhoneRspDTO = FindUserByPhoneRspDTO.builder()
                .id(user.getId())
                .password(user.getPassword())
                .build();

        return Response.success(findUserByPhoneRspDTO);
    }

    @Override
    public Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        // 获取当前请求对应的用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        User user = User.builder()
                .id(userId)
                .password(updateUserPasswordReqDTO.getEncodePassword()) // 加密后的密码
                .updateTime(LocalDateTime.now())
                .build();
        // 更新密码
        updateById(user);

        return Response.success();
    }

    @Override
    public Response<FindUserByIdVo> findUserById(FindUserByIdDTO findUserByIdDTO) {

        Long userId = findUserByIdDTO.getId();

        FindUserByIdVo findUserByIdRspDTOLocalCache = LOCAL_CACHE.getIfPresent(userId);
        if (Objects.nonNull(findUserByIdRspDTOLocalCache)){
            log.info("==> 命中了本地缓存；{}", findUserByIdRspDTOLocalCache);
            return Response.success(findUserByIdRspDTOLocalCache);
        }
        String key = RedisKeyConstants.buildUserInfoKey(userId);
        String userStr = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(userStr)){

            if ("null".equals(userStr)) {
                throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
            }

            FindUserByIdVo userVo = JsonUtils.parseObject(userStr, FindUserByIdVo.class);
            LOCAL_CACHE.put(userId, userVo);
            return Response.success(userVo);
        }
        User user = getById(userId);

        // 判空
        if (Objects.isNull(user)) {
            //缓存穿透：存空值 + 过期时间防大量空值查询内存爆增(同步写防瞬时流量)
            long expireSeconds = 60 + RandomUtil.randomInt(60);
            redisTemplate.opsForValue().set(key, "null", expireSeconds, TimeUnit.SECONDS);
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        FindUserByIdVo userVo = new FindUserByIdVo();
        userVo.setId(user.getId());
        userVo.setNickName(user.getNickname());
        userVo.setAvatar(user.getAvatar());


        LOCAL_CACHE.put(userId, userVo);
        // 异步将用户信息存入 Redis 缓存，提升响应速度
        threadPoolTaskExecutor.submit(() -> {
            try {
                // 缓存雪崩：过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
                long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
                redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(userVo), expireSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("==> 异步写入 Redis 用户缓存失败, userId: {}", userId, e);
            }
        });

        return Response.success(userVo);
    }
}
