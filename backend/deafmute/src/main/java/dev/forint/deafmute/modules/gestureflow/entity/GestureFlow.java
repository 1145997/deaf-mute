package dev.forint.deafmute.modules.gestureflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gesture_flow")
public class GestureFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String flowCode;

    private String flowName;

    private String flowType;

    private String triggerMode;

    private Integer status;

    private Integer priority;

    private Integer versionNo;

    private Long startNodeId;

    private String description;

    private Integer isBuiltin;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
