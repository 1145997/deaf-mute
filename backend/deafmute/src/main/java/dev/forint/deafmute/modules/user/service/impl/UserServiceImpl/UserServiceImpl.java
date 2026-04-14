package dev.forint.deafmute.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.common.utils.JwtUtils;
import dev.forint.deafmute.modules.user.dto.UserLoginDTO;
import dev.forint.deafmute.modules.user.entity.User;
import dev.forint.deafmute.modules.user.mapper.UserMapper;
import dev.forint.deafmute.modules.user.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.modules.user.dto.AdminUserQueryDTO;
import dev.forint.deafmute.modules.user.vo.UserListVO;
import dev.forint.deafmute.modules.user.dto.WxUserLoginDTO;
import dev.forint.deafmute.modules.user.dto.WechatCode2SessionResponse;
import dev.forint.deafmute.modules.user.service.WechatAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtils jwtUtils;
    private final WechatAuthService wechatAuthService;
    @Override
    public Map<String, Object> login(UserLoginDTO dto) {
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, dto.getOpenid()));

        if (user != null && user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("该账号已被禁用");
        }

        if (user == null) {
            user = new User();
            user.setOpenid(dto.getOpenid());
            user.setNickname(dto.getNickname());
            user.setAvatar(dto.getAvatar());
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            this.save(user);
        } else {
            user.setLastLoginTime(LocalDateTime.now());

            if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
                user.setNickname(dto.getNickname());
            }
            if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
                user.setAvatar(dto.getAvatar());
            }

            this.updateById(user);
        }

        String token = jwtUtils.createToken(user.getId(), user.getOpenid(), "user");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("openid", user.getOpenid());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());

        return data;
    }

    @Override
    public Page<UserListVO> getAdminPage(AdminUserQueryDTO dto) {
        Page<User> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(User::getNickname, dto.getKeyword())
                        .or()
                        .like(User::getRealName, dto.getKeyword())
                        .or()
                        .like(User::getStudentNo, dto.getKeyword())
                        .or()
                        .like(User::getPhone, dto.getKeyword())
                        .or()
                        .like(User::getEmail, dto.getKeyword())
                )
                .orderByDesc(User::getCreateTime);

        Page<User> entityPage = this.page(page, wrapper);

        Page<UserListVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(item -> {
            UserListVO vo = new UserListVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    public User getDetailById(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("状态值不合法");
        }

        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    public Map<String, Object> wxLogin(WxUserLoginDTO dto) {
        WechatCode2SessionResponse wxResp = wechatAuthService.code2Session(dto.getCode());
        String openid = wxResp.getOpenid();

        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid));

        if (user != null && user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("该账号已被禁用");
        }

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(dto.getNickname());
            user.setAvatar(dto.getAvatar());
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            this.save(user);
        } else {
            user.setLastLoginTime(LocalDateTime.now());

            if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
                user.setNickname(dto.getNickname());
            }
            if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
                user.setAvatar(dto.getAvatar());
            }

            this.updateById(user);
        }

        String token = jwtUtils.createToken(user.getId(), user.getOpenid(), "user");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("openid", user.getOpenid());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());

        return data;
    }
}