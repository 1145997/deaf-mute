package dev.forint.deafmute.modules.post.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostListVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String contentPreview;

    private String coverImage;

    private Integer status;

    private Integer viewCount;

    private Integer commentCount;

    private Integer likeCount;

    private String sourceType;

    private LocalDateTime createTime;
}
