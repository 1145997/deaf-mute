package dev.forint.deafmute.modules.gestureflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GestureFlowOutputVO {

    private Long id;

    private Long endNodeId;

    private String endNodeCode;

    private String endNodeName;

    private String outputType;

    private String outputText;

    private Long phraseTemplateId;

    private String phraseText;

    private String controlAction;

    private String ttsText;

    private String displayText;

    private LocalDateTime createTime;
}
