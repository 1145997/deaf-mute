package dev.forint.deafmute.modules.gesturelibrary.dto;

import lombok.Data;

@Data
public class AdminGestureLibraryQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;

    private Integer status;

    private Integer isBuiltin;
}
