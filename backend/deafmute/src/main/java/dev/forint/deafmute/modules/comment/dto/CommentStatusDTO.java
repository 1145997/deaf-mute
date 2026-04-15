package dev.forint.deafmute.modules.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
