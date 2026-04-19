package dev.forint.deafmute.modules.recognition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recognition_record")
public class RecognitionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sessionId;

    private String matchedGestureCode;

    private Long matchedFlowId;

    private String matchedNodePath;

    private String outputType;

    private String outputText;

    private String controlAction;

    private BigDecimal confidenceScore;

    private String requestPayloadJson;

    private String resultPayloadJson;

    private String clientPlatform;

    private String inputMode;

    private String source;

    private LocalDateTime createTime;
}
