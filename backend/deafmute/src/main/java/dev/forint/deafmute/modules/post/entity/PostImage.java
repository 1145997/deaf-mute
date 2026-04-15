package dev.forint.deafmute.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_image")
public class PostImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private String imageUrl;

    private Integer sort;

    private LocalDateTime createTime;
}
