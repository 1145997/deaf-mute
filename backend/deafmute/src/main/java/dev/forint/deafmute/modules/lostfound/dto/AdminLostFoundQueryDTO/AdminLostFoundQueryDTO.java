package dev.forint.deafmute.modules.lostfound.dto;

import lombok.Data;

@Data
public class AdminLostFoundQueryDTO {

    private Integer type;

    private Integer status;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}