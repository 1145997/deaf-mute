package dev.forint.deafmute.modules.phrasetemplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhraseTemplateAddDTO {

    @NotBlank(message = "短语编码不能为空")
    private String phraseCode;

    @NotBlank(message = "短语文本不能为空")
    private String phraseText;

    private String ttsText;

    private String sceneType;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "排序不能为空")
    private Integer sort;
}
