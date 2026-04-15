package dev.forint.deafmute.modules.post.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private Long categoryId;

    private String categoryName;

    private String title;

    private String content;

    private String coverImage;

    private List<String> imageList;

    private Integer status;

    private Integer viewCount;

    private Integer commentCount;

    private Integer likeCount;

    private String sourceType;

    private Long sourceRecordId;

    private String auditReason;

    private LocalDateTime createTime;
}
