package dev.forint.deafmute.modules.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostFoundAddDTO {

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "物品名称不能为空")
    private String itemName;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String brand;

    private String color;

    @NotBlank(message = "描述不能为空")
    private String description;

    private String image;

    @NotNull(message = "时间不能为空")
    private LocalDateTime eventTime;

    @NotBlank(message = "地点不能为空")
    private String eventPlace;

    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String contactWechat;
}