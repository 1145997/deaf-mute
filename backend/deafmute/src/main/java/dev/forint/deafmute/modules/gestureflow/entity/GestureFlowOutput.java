package dev.forint.deafmute.modules.gestureflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gesture_flow_output")
public class GestureFlowOutput {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flowId;

    private Long endNodeId;

    private String outputType;

    private String outputText;

    private Long phraseTemplateId;

    private String controlAction;

    private String ttsText;

    private String displayText;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
