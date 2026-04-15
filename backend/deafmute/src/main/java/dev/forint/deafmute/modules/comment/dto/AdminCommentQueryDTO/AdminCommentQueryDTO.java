package dev.forint.deafmute.modules.comment.dto;

import lombok.Data;

@Data
public class AdminCommentQueryDTO {

    private Long postId;

    private Long userId;

    private Integer status;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
