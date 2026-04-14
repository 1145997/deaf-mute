package dev.forint.deafmute.modules.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusDTO {

    @NotNull(message = "用户状态不能为空")
    private Integer status;
}