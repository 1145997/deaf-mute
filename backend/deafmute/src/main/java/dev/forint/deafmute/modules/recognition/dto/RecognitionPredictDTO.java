package dev.forint.deafmute.modules.recognition.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RecognitionPredictDTO {

    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    private Integer frameNo;

    private Long capturedAt;

    private String source;

    private String handedness;

    private Boolean mirrored;

    private String cameraFacing;

    private String gestureCode;

    @Valid
    private List<RecognitionLandmarkDTO> landmarks;
}
