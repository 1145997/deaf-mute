package dev.forint.deafmute.modules.gestureflow.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GestureFlowNodeVO {

    private Long id;

    private String nodeCode;

    private String nodeName;

    private Long parentNodeId;

    private Long gestureLibraryId;

    private String gestureCode;

    private String gestureName;

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

    private LocalDateTime createTime;
}
