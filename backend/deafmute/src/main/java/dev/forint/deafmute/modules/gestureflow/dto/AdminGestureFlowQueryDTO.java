package dev.forint.deafmute.modules.gestureflow.dto;

import lombok.Data;

@Data
public class AdminGestureFlowQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;

    private String flowType;

    private Integer status;

    private Integer isBuiltin;
}
