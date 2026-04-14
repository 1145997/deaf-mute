package dev.forint.deafmute.modules.lostfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lost_found")
public class LostFound {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private String title;

    private String itemName;

    private Long categoryId;

    private String brand;

    private String color;

    private String description;

    private String image;

    private LocalDateTime eventTime;

    private String eventPlace;

    private String contactName;

    private String contactPhone;

    private String contactWechat;

    private Integer status;

    private Integer viewCount;

    private String auditReason;

    private Long auditAdminId;

    private LocalDateTime auditTime;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}