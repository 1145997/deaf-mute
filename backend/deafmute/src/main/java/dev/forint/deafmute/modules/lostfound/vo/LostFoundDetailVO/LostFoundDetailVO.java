package dev.forint.deafmute.modules.lostfound.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostFoundDetailVO {

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
}