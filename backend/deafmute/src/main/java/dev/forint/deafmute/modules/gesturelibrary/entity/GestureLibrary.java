package dev.forint.deafmute.modules.gesturelibrary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gesture_library")
public class GestureLibrary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String gestureCode;

    private String gestureName;

    private String description;

    private String previewImage;

    private Integer status;

    private Integer sort;

    private Integer isBuiltin;

    private String detectionKey;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
