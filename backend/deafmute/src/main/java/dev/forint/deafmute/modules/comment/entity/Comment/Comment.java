package dev.forint.deafmute.modules.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long infoId;

    private Long userId;

    private Long parentId;

    private String content;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}