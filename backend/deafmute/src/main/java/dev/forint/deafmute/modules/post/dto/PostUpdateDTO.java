package dev.forint.deafmute.modules.post.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostUpdateDTO {

    private Long categoryId;

    private String title;

    private String content;

    private String coverImage;

    private List<String> imageList;
}
