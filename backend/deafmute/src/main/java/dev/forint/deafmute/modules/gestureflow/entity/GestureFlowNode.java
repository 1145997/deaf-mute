package dev.forint.deafmute.modules.gestureflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("gesture_flow_node")
public class GestureFlowNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flowId;

    private String nodeCode;

    private String nodeName;

    private Long parentNodeId;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
