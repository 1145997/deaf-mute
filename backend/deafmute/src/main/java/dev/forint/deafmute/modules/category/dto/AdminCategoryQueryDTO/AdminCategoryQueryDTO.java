package dev.forint.deafmute.modules.category.dto;

import lombok.Data;

@Data
public class AdminCategoryQueryDTO {

    private String keyword;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}