package dev.forint.deafmute.modules.gesturelibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GestureLibraryAddDTO {

    @NotBlank(message = "手势编码不能为空")
    private String gestureCode;

    @NotBlank(message = "手势名称不能为空")
    private String gestureName;

    private String description;

    private String previewImage;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "是否内置不能为空")
    private Integer isBuiltin;

    @NotBlank(message = "识别器key不能为空")
    private String detectionKey;
}
