package dev.forint.deafmute.modules.gestureflow.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GestureFlowDetailVO {

    private Long id;

    private String flowCode;

    private String flowName;

    private String flowType;

    private String triggerMode;

    private Integer status;

    private Integer priority;

    private Integer versionNo;

    private Long startNodeId;

    private Integer isBuiltin;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<GestureFlowNodeVO> nodeList;

    private List<GestureFlowOutputVO> outputList;
}
