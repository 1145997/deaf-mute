package dev.forint.deafmute.modules.comment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.comment.dto.AdminCommentQueryDTO;
import dev.forint.deafmute.modules.comment.dto.CommentAddDTO;
import dev.forint.deafmute.modules.comment.entity.Comment;
import dev.forint.deafmute.modules.comment.vo.CommentAdminVO;
import dev.forint.deafmute.modules.comment.vo.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {

    void add(CommentAddDTO dto);

    List<CommentVO> getListByPostId(Long postId);

    Page<CommentAdminVO> getAdminPage(AdminCommentQueryDTO dto);

    void deleteByAdmin(Long id);

    void updateStatusByAdmin(Long id, Integer status);
}
