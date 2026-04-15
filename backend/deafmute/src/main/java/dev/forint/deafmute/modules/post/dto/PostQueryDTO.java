package dev.forint.deafmute.modules.post.dto;

import lombok.Data;

@Data
public class PostQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long categoryId;

    private String keyword;

    private Integer status;

    private String sortBy;
}
