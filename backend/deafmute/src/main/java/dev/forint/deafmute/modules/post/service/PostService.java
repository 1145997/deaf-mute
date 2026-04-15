package dev.forint.deafmute.modules.post.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.post.dto.AdminPostQueryDTO;
import dev.forint.deafmute.modules.post.dto.PostAddDTO;
import dev.forint.deafmute.modules.post.dto.PostQueryDTO;
import dev.forint.deafmute.modules.post.dto.PostUpdateDTO;
import dev.forint.deafmute.modules.post.entity.Post;
import dev.forint.deafmute.modules.post.vo.PostDetailVO;
import dev.forint.deafmute.modules.post.vo.PostListVO;

public interface PostService extends IService<Post> {

    void add(PostAddDTO dto);

    Page<PostListVO> getPage(PostQueryDTO dto);

    PostDetailVO getDetail(Long id);

    Page<PostListVO> getMyPage(Integer pageNum, Integer pageSize);

    void updateMyById(Long id, PostUpdateDTO dto);

    void deleteMyById(Long id);

    Page<PostListVO> getAdminPage(AdminPostQueryDTO dto);

    Page<PostListVO> getPendingPage(Integer pageNum, Integer pageSize, String keyword, Long categoryId);

    void approve(Long id);

    void reject(Long id, String auditReason);

    void updateStatus(Long id, Integer status);
}
