package dev.forint.deafmute.modules.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryAddDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "排序号不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}