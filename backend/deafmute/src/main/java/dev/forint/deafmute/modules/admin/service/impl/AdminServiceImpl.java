package dev.forint.deafmute.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.common.utils.JwtUtils;
import dev.forint.deafmute.modules.admin.entity.Admin;
import dev.forint.deafmute.modules.admin.mapper.AdminMapper;
import dev.forint.deafmute.modules.admin.service.AdminService;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.admin.vo.AdminInfoVO;
import org.springframework.beans.BeanUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    private final JwtUtils jwtUtils;
    private final AdminTokenUtils adminTokenUtils;

    @Override
    public Map<String, Object> login(String username, String password) {
        Admin admin = this.getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username));

        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!password.equals(admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new RuntimeException("该管理员账号已被禁用");
        }

        String token = jwtUtils.createToken(admin.getId(), admin.getUsername(), admin.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", admin.getUsername());
        data.put("nickname", admin.getNickname());
        data.put("role", admin.getRole());

        return data;
    }

    @Override
    public AdminInfoVO getCurrentAdminInfo() {
        Long adminId = adminTokenUtils.getCurrentAdminId();

        Admin admin = this.getById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }

        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new RuntimeException("管理员账号已被禁用");
        }

        AdminInfoVO vo = new AdminInfoVO();
        BeanUtils.copyProperties(admin, vo);
        return vo;
    }
}