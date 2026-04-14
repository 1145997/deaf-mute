package dev.forint.deafmute.modules.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoticeAddDTO {

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    @NotNull(message = "是否置顶不能为空")
    private Integer isTop;

    @NotNull(message = "状态不能为空")
    private Integer status;
}