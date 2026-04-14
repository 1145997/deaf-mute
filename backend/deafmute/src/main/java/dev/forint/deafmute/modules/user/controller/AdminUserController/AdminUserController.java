package dev.forint.deafmute.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.modules.user.dto.AdminUserQueryDTO;
import dev.forint.deafmute.modules.user.dto.UserStatusDTO;
import dev.forint.deafmute.modules.user.entity.User;
import dev.forint.deafmute.modules.user.service.UserService;
import dev.forint.deafmute.modules.user.vo.UserListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminUserQueryDTO dto) {
        Page<UserListVO> page = userService.getAdminPage(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());

        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userService.getDetailById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UserStatusDTO dto) {
        userService.updateStatus(id, dto.getStatus());
        return Result.success("用户状态更新成功", null);
    }
}