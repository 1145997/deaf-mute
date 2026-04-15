package dev.forint.deafmute.modules.gestureflow.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GestureFlowNodeDTO {

    private Long id;

    private String nodeCode;

    private String nodeName;

    private Long parentNodeId;

    private String parentNodeCode;

    private Long gestureLibraryId;

    private Integer isStart;

    private Integer isEnd;

    private Integer nodeOrder;

    private BigDecimal confidenceMin;

    private Integer holdMs;

    private Integer debounceMs;

    private Integer cooldownMs;

    private Integer requiredHits;

    private Integer maxIntervalMs;

    private Integer resetOnFail;

    private Integer allowRepeat;

    private String successNextStrategy;

    private String failStrategy;

    private String remark;
}
