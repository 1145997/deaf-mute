package dev.forint.deafmute.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {

    @NotBlank(message = "openid不能为空")
    private String openid;

    private String nickname;

    private String avatar;
}