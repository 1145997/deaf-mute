package dev.forint.deafmute.modules.recognition.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecognitionSessionStartDTO {

    @NotBlank(message = "clientType 不能为空")
    private String clientType;

    @NotBlank(message = "sceneCode 不能为空")
    private String sceneCode;

    @NotBlank(message = "engineType 不能为空")
    private String engineType;

    private String appVersion;
}
