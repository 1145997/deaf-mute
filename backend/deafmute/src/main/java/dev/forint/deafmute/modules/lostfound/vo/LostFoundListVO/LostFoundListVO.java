package dev.forint.deafmute.modules.lostfound.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostFoundListVO {

    private Long id;

    private Integer type;

    private String title;

    private String itemName;

    private Long categoryId;

    private String image;

    private String eventPlace;

    private LocalDateTime eventTime;

    private String contactName;

    private String contactPhone;

    private Integer status;

    private Integer viewCount;

    private LocalDateTime createTime;
}