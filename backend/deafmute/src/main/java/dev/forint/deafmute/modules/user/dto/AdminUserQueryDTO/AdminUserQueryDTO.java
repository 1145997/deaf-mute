package dev.forint.deafmute.modules.user.dto;

import lombok.Data;

@Data
public class AdminUserQueryDTO {

    private String keyword;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}