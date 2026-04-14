package dev.forint.deafmute.modules.user.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.modules.user.dto.UserLoginDTO;
import dev.forint.deafmute.modules.user.dto.WxUserLoginDTO;
import dev.forint.deafmute.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/wx-login")
    public Result<Map<String, Object>> wxLogin(@RequestBody @Valid WxUserLoginDTO dto) {
        return Result.success(userService.wxLogin(dto));
    }
}