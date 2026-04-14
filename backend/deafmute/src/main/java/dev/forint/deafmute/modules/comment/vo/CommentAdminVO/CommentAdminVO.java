package dev.forint.deafmute.modules.comment.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentAdminVO {

    private Long id;

    private Long infoId;

    private Long userId;

    private Long parentId;

    private String content;

    private Integer status;

    private LocalDateTime createTime;
}