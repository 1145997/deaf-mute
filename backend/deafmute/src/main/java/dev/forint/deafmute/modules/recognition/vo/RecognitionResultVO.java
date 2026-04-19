package dev.forint.deafmute.modules.recognition.vo;

import lombok.Data;

import java.util.List;

@Data
public class RecognitionResultVO {

    private String gesture;

    private String code;

    private String label;

    private Boolean locked;

    private String state;

    private String outputType;

    private String displayText;

    private String ttsText;

    private String controlAction;

    private String matchedFlowCode;

    private String matchedNodeCode;

    private String traceId;

    private String inputType;

    private Integer stableCount;

    private Integer requiredHits;

    private Boolean debounced;

    private List<String> detectedGestureCandidates;
}
