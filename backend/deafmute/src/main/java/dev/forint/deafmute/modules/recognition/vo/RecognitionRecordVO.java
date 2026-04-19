package dev.forint.deafmute.modules.recognition.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecognitionRecordVO {

    private Long id;

    private String sessionId;

    private String matchedGestureCode;

    private String matchedFlowCode;

    private String matchedNodePath;

    private String outputType;

    private String outputText;

    private String controlAction;

    private String clientPlatform;

    private String inputMode;

    private String source;

    private LocalDateTime createTime;
}
