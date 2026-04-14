package dev.forint.deafmute.modules.statistics.vo;

import lombok.Data;

@Data
public class DashboardOverviewVO {

    private Long totalUsers;

    private Long totalInfos;

    private Long pendingInfos;

    private Long publishedInfos;

    private Long finishedInfos;

    private Long rejectedInfos;

    private Long totalNotices;
}