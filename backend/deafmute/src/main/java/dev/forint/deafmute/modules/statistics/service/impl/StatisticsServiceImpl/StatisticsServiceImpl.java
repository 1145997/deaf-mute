package dev.forint.deafmute.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.forint.deafmute.modules.category.entity.Category;
import dev.forint.deafmute.modules.category.mapper.CategoryMapper;
import dev.forint.deafmute.modules.comment.entity.Comment;
import dev.forint.deafmute.modules.comment.mapper.CommentMapper;
import dev.forint.deafmute.modules.notice.entity.Notice;
import dev.forint.deafmute.modules.notice.mapper.NoticeMapper;
import dev.forint.deafmute.modules.post.entity.Post;
import dev.forint.deafmute.modules.post.mapper.PostMapper;
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
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final NoticeMapper noticeMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();
        vo.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
        vo.setTotalPosts(postMapper.selectCount(new LambdaQueryWrapper<>()));
        vo.setPendingPosts(postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 0)));
        vo.setTotalComments(commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1)));
        vo.setTotalCategories(categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1)));
        vo.setTotalNotices(noticeMapper.selectCount(new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, 1)));
        return vo;
    }

    @Override
    public List<TrendVO> getRecent7DaysContentTrend() {
        List<TrendVO> list = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();

            Long count = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                    .ge(Post::getCreateTime, start)
                    .lt(Post::getCreateTime, end));

            TrendVO vo = new TrendVO();
            vo.setDate(day.toString());
            vo.setCount(count);
            list.add(vo);
        }

        return list;
    }
}
