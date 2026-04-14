package dev.forint.deafmute.modules.auth.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.admin.service.AdminService;
import dev.forint.deafmute.modules.admin.vo.AdminInfoVO;
import dev.forint.deafmute.modules.auth.dto.AdminLoginDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminTokenUtils adminTokenUtils;
    private final AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid AdminLoginDTO dto) {
        Map<String, Object> data = adminService.login(dto.getUsername(), dto.getPassword());
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result<AdminInfoVO> info() {
        adminTokenUtils.checkAdminLogin();
        return Result.success(adminService.getCurrentAdminInfo());
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        adminTokenUtils.checkAdminLogin();
        return Result.success("退出成功", null);
    }
}