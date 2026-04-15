package dev.forint.deafmute.modules.phrasetemplate.dto;

import lombok.Data;

@Data
public class AdminPhraseTemplateQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;

    private String sceneType;

    private Integer status;
}
