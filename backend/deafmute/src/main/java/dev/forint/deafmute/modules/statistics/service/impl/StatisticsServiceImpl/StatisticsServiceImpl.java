package dev.forint.deafmute.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.forint.deafmute.modules.lostfound.entity.LostFound;
import dev.forint.deafmute.modules.lostfound.mapper.LostFoundMapper;
import dev.forint.deafmute.modules.notice.entity.Notice;
import dev.forint.deafmute.modules.notice.mapper.NoticeMapper;
import dev.forint.deafmute.modules.statistics.service.StatisticsService;
import dev.forint.deafmute.modules.statistics.vo.DashboardOverviewVO;
import dev.forint.deafmute.modules.statistics.vo.TrendVO;
import dev.forint.deafmute.modules.user.entity.User;
import dev.forint.deafmute.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserMapper userMapper;
    private final LostFoundMapper lostFoundMapper;
    private final NoticeMapper noticeMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        vo.setTotalUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)
        ));

        vo.setTotalInfos(lostFoundMapper.selectCount(new LambdaQueryWrapper<>()));

        vo.setPendingInfos(lostFoundMapper.selectCount(
                new LambdaQueryWrapper<LostFound>().eq(LostFound::getStatus, 0)
        ));

        vo.setPublishedInfos(lostFoundMapper.selectCount(
                new LambdaQueryWrapper<LostFound>().eq(LostFound::getStatus, 1)
        ));

        vo.setFinishedInfos(lostFoundMapper.selectCount(
                new LambdaQueryWrapper<LostFound>().eq(LostFound::getStatus, 2)
        ));

        vo.setRejectedInfos(lostFoundMapper.selectCount(
                new LambdaQueryWrapper<LostFound>().eq(LostFound::getStatus, 3)
        ));

        vo.setTotalNotices(noticeMapper.selectCount(
                new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, 1)
        ));

        return vo;
    }

    @Override
    public List<TrendVO> getRecent7DaysTrend() {
        List<TrendVO> list = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();

            Long count = lostFoundMapper.selectCount(
                    new LambdaQueryWrapper<LostFound>()
                            .ge(LostFound::getCreateTime, start)
                            .lt(LostFound::getCreateTime, end)
            );

            TrendVO vo = new TrendVO();
            vo.setDate(day.toString());
            vo.setCount(count);

            list.add(vo);
        }

        return list;
    }
}