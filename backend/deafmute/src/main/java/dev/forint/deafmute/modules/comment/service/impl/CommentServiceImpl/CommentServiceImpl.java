package dev.forint.deafmute.modules.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.comment.dto.CommentAddDTO;
import dev.forint.deafmute.modules.comment.entity.Comment;
import dev.forint.deafmute.modules.comment.mapper.CommentMapper;
import dev.forint.deafmute.modules.comment.service.CommentService;
import dev.forint.deafmute.modules.comment.vo.CommentVO;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.modules.comment.dto.AdminCommentQueryDTO;
import dev.forint.deafmute.modules.comment.vo.CommentAdminVO;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    private final UserTokenUtils userTokenUtils;
    @Override
    public void add(CommentAddDTO dto) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);

        // 这里先写死用户ID，后面接小程序用户登录后再从 token 获取
        comment.setUserId(userTokenUtils.getCurrentUserId());

        // 默认正常显示
        comment.setStatus(1);

        this.save(comment);
    }

    @Override
    public List<CommentVO> getListByInfoId(Long infoId) {
        List<Comment> list = this.list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getInfoId, infoId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime)
                .orderByAsc(Comment::getId));

        return list.stream().map(item -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList();
    }

    @Override
    public Page<CommentAdminVO> getAdminPage(AdminCommentQueryDTO dto) {
        Page<Comment> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(dto.getInfoId() != null, Comment::getInfoId, dto.getInfoId())
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
    public void hideByAdmin(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        comment.setStatus(0);
        this.updateById(comment);
    }

    @Override
    public void showByAdmin(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        comment.setStatus(1);
        this.updateById(comment);
    }
}