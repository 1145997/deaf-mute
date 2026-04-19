package dev.forint.deafmute.modules.recognition.vo;

import lombok.Data;

@Data
public class RecognitionFlowSimpleVO {

    private Long id;

    private String flowCode;

    private String flowName;

    private String flowType;

    private Integer priority;
}
