package dev.forint.deafmute.modules.recognitionconfig.dto;

import lombok.Data;

@Data
public class AdminRecognitionConfigQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;

    private Integer activeFlag;
}
