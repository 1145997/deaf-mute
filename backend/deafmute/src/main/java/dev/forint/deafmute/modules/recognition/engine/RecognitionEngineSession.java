package dev.forint.deafmute.modules.recognition.engine;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecognitionEngineSession {

    private String sessionId;

    private Long userId;

    private String clientType;

    private String sceneCode;

    private String engineType;

    private String appVersion;

    private LocalDateTime startedAt;

    private LocalDateTime lastActiveAt;

    private Long activeConfigId;

    private Long currentFlowId;

    private Long currentNodeId;

    private String currentNodeCode;

    private String state = "idle";

    private String stableGesture;

    private int stableCount;

    private long lastTriggerAt;

    private long lastNodeMatchedAt;
}
