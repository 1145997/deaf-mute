package dev.forint.deafmute.modules.post.dto;

import lombok.Data;

@Data
public class AdminPostQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;

    private Long categoryId;

    private Integer status;

    private Long userId;

    private String sourceType;
}
