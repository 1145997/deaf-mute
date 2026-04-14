package dev.forint.deafmute.modules.comment.dto;

import lombok.Data;

@Data
public class AdminCommentQueryDTO {

    private Long infoId;

    private Integer status;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}