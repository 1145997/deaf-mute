package dev.forint.deafmute.modules.statistics.vo;

import lombok.Data;

@Data
public class DashboardOverviewVO {

    private Long totalUsers;

    private Long totalPosts;

    private Long pendingPosts;

    private Long totalComments;

    private Long totalCategories;

    private Long totalNotices;
}
