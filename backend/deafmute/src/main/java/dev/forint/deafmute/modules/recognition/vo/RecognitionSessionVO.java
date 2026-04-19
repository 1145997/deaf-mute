package dev.forint.deafmute.modules.recognition.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecognitionSessionVO {

    private String sessionId;

    private LocalDateTime startedAt;

    private Long activeConfigId;
}
