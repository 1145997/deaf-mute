package dev.forint.deafmute.modules.gestureflow.dto;

import lombok.Data;

@Data
public class GestureFlowOutputDTO {

    private Long id;

    private Long endNodeId;

    private String endNodeCode;

    private String outputType;

    private String outputText;

    private Long phraseTemplateId;

    private String controlAction;

    private String ttsText;

    private String displayText;
}
