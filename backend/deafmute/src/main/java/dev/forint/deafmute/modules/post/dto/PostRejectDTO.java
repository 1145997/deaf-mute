package dev.forint.deafmute.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostRejectDTO {

    @NotBlank(message = "驳回原因不能为空")
    private String auditReason;
}
