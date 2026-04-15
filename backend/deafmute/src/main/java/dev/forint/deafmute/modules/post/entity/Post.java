package dev.forint.deafmute.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String content;

    private String coverImage;

    private Integer status;

    private String auditReason;

    private Long auditAdminId;

    private LocalDateTime auditTime;

    private Integer viewCount;

    private Integer commentCount;

    private Integer likeCount;

    private String sourceType;

    private Long sourceRecordId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
