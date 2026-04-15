package dev.forint.deafmute.modules.recognitionconfig.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecognitionConfigAddDTO {

    @NotBlank(message = "配置名称不能为空")
    private String configName;

    @NotNull(message = "最小置信度不能为空")
    @DecimalMin(value = "0.0000", message = "最小置信度不能小于0")
    @DecimalMax(value = "1.0000", message = "最小置信度不能大于1")
    private BigDecimal confidenceMin;

    @NotNull(message = "持续命中时长不能为空")
    private Integer holdMs;

    @NotNull(message = "防抖时间不能为空")
    private Integer debounceMs;

    @NotNull(message = "冷却时间不能为空")
    private Integer cooldownMs;

    @NotNull(message = "连续命中次数不能为空")
    private Integer requiredHits;

    @NotNull(message = "节点最大间隔不能为空")
    private Integer maxIntervalMs;

    @NotNull(message = "锁定超时不能为空")
    private Integer lockTimeoutMs;

    @NotNull(message = "失败后是否重置不能为空")
    private Integer resetOnFail;

    @NotNull(message = "是否允许重复触发不能为空")
    private Integer allowRepeat;

    private String gestureOrderJson;

    @NotNull(message = "是否生效不能为空")
    private Integer activeFlag;

    private String remark;
}
