package dev.forint.deafmute.modules.recognition.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecognitionSessionActionDTO {

    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    private String reason;
}
