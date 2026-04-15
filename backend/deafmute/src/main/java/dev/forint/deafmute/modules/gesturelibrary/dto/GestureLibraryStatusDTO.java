package dev.forint.deafmute.modules.gesturelibrary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GestureLibraryStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
