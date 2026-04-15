package dev.forint.deafmute.modules.recognitionconfig.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recognition_config")
public class RecognitionConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configName;

    private BigDecimal confidenceMin;

    private Integer holdMs;

    private Integer debounceMs;

    private Integer cooldownMs;

    private Integer requiredHits;

    private Integer maxIntervalMs;

    private Integer lockTimeoutMs;

    private Integer resetOnFail;

    private Integer allowRepeat;

    private String gestureOrderJson;

    private Integer activeFlag;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
