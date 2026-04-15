package dev.forint.deafmute.modules.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import dev.forint.deafmute.modules.comment.dto.AdminCommentQueryDTO;
import dev.forint.deafmute.modules.comment.dto.CommentAddDTO;
import dev.forint.deafmute.modules.comment.entity.Comment;
import dev.forint.deafmute.modules.comment.mapper.CommentMapper;
import dev.forint.deafmute.modules.comment.service.CommentService;
import dev.forint.deafmute.modules.comment.vo.CommentAdminVO;
import dev.forint.deafmute.modules.comment.vo.CommentVO;
import dev.forint.deafmute.modules.user.entity.User;
import dev.forint.deafmute.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserTokenUtils userTokenUtils;
    private final UserMapper userMapper;

    @Override
    public void add(CommentAddDTO dto) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(userTokenUtils.getCurrentUserId());
        comment.setStatus(1);
        this.save(comment);
    }

    @Override
    public List<CommentVO> getListByPostId(Long postId) {
        List<Comment> list = this.list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime)
                .orderByAsc(Comment::getId));

        return list.stream().map(item -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(item, vo);
            vo.setChildren(Collections.emptyList());
            User user = userMapper.selectById(item.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            }
            return vo;
        }).toList();
    }

    @Override
    public Page<CommentAdminVO> getAdminPage(AdminCommentQueryDTO dto) {
        Page<Comment> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(dto.getPostId() != null, Comment::getPostId, dto.getPostId())
                .eq(dto.getUserId() != null, Comment::getUserId, dto.getUserId())
                .eq(dto.getStatus() != null, Comment::getStatus, dto.getStatus())
                .like(StringUtils.hasText(dto.getKeyword()), Comment::getContent, dto.getKeyword())
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> entityPage = this.page(page, wrapper);
        Page<CommentAdminVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(item -> {
            CommentAdminVO vo = new CommentAdminVO();
            BeanUtils.copyProperties(item, vo);
            User user = userMapper.selectById(item.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public void deleteByAdmin(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        this.removeById(id);
    }

    @Override
    public void updateStatusByAdmin(Long id, Integer status) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        comment.setStatus(status);
        this.updateById(comment);
    }
}
