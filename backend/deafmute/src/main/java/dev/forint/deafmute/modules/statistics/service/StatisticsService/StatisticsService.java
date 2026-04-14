package dev.forint.deafmute.modules.statistics.service;

import dev.forint.deafmute.modules.statistics.vo.DashboardOverviewVO;
import dev.forint.deafmute.modules.statistics.vo.TrendVO;

import java.util.List;

public interface StatisticsService {

    DashboardOverviewVO getOverview();

    List<TrendVO> getRecent7DaysTrend();
}