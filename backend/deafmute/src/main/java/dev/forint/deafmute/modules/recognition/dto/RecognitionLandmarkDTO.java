package dev.forint.deafmute.modules.recognition.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecognitionLandmarkDTO {

    @NotNull(message = "x 不能为空")
    private Double x;

    @NotNull(message = "y 不能为空")
    private Double y;

    private Double z = 0.0;
}
