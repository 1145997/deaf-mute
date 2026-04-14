package dev.forint.deafmute.modules.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LostFoundRejectDTO {

    @NotBlank(message = "驳回原因不能为空")
    private String auditReason;
}