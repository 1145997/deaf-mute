package dev.forint.deafmute.modules.comment.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;

    private Long postId;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private Long parentId;

    private String content;

    private Integer status;

    private LocalDateTime createTime;

    private List<CommentVO> children;
}
