package dev.forint.deafmute.modules.phrasetemplate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhraseTemplateStatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
