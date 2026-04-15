package dev.forint.deafmute.modules.gestureflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GestureFlowAddDTO {

    @NotBlank(message = "动作流编码不能为空")
    private String flowCode;

    @NotBlank(message = "动作流名称不能为空")
    private String flowName;

    @NotBlank(message = "动作流类型不能为空")
    private String flowType;

    @NotBlank(message = "触发模式不能为空")
    private String triggerMode;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @NotNull(message = "是否内置不能为空")
    private Integer isBuiltin;

    private String description;

    @Valid
    private List<GestureFlowNodeDTO> nodeList;

    @Valid
    private List<GestureFlowOutputDTO> outputList;
}
