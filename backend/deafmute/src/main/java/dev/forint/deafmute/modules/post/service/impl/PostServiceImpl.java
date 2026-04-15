package dev.forint.deafmute.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import dev.forint.deafmute.modules.category.entity.Category;
import dev.forint.deafmute.modules.category.mapper.CategoryMapper;
import dev.forint.deafmute.modules.post.dto.AdminPostQueryDTO;
import dev.forint.deafmute.modules.post.dto.PostAddDTO;
import dev.forint.deafmute.modules.post.dto.PostQueryDTO;
import dev.forint.deafmute.modules.post.dto.PostUpdateDTO;
import dev.forint.deafmute.modules.post.entity.Post;
import dev.forint.deafmute.modules.post.entity.PostImage;
import dev.forint.deafmute.modules.post.mapper.PostImageMapper;
import dev.forint.deafmute.modules.post.mapper.PostMapper;
import dev.forint.deafmute.modules.post.service.PostService;
import dev.forint.deafmute.modules.post.vo.PostDetailVO;
import dev.forint.deafmute.modules.post.vo.PostListVO;
import dev.forint.deafmute.modules.user.entity.User;
import dev.forint.deafmute.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserTokenUtils userTokenUtils;
    private final AdminTokenUtils adminTokenUtils;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final PostImageMapper postImageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(PostAddDTO dto) {
        Post post = new Post();
        BeanUtils.copyProperties(dto, post);
        post.setUserId(userTokenUtils.getCurrentUserId());
        post.setStatus(0);
        post.setViewCount(0);
        post.setCommentCount(0);
        post.setLikeCount(0);
        if (!StringUtils.hasText(post.getSourceType())) {
            post.setSourceType("MANUAL");
        }
        if (!StringUtils.hasText(post.getCoverImage()) && dto.getImageList() != null && !dto.getImageList().isEmpty()) {
            post.setCoverImage(dto.getImageList().get(0));
        }
        this.save(post);
        replacePostImages(post.getId(), dto.getImageList());
    }

    @Override
    public Page<PostListVO> getPage(PostQueryDTO dto) {
        Page<Post> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(dto.getCategoryId() != null, Post::getCategoryId, dto.getCategoryId())
                .eq(dto.getStatus() != null, Post::getStatus, dto.getStatus())
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(Post::getTitle, dto.getKeyword())
                        .or()
                        .like(Post::getContent, dto.getKeyword()))
                .orderByDesc(Post::getCreateTime);

        if ("hot".equalsIgnoreCase(dto.getSortBy())) {
            wrapper.orderByDesc(Post::getViewCount).orderByDesc(Post::getCommentCount);
        }

        return convertPage(this.page(page, wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO getDetail(Long id) {
        Post post = getRequiredPost(id);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        this.updateById(post);
        return toDetailVO(post);
    }

    @Override
    public Page<PostListVO> getMyPage(Integer pageNum, Integer pageSize) {
        Long currentUserId = userTokenUtils.getCurrentUserId();
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, currentUserId)
                .orderByDesc(Post::getCreateTime);
        return convertPage(this.page(page, wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyById(Long id, PostUpdateDTO dto) {
        Long currentUserId = userTokenUtils.getCurrentUserId();
        Post post = getRequiredPost(id);
        if (!currentUserId.equals(post.getUserId())) {
            throw new RuntimeException("无权修改他人发布的帖子");
        }

        if (dto.getCategoryId() != null) {
            post.setCategoryId(dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            post.setTitle(dto.getTitle());
        }
        if (StringUtils.hasText(dto.getContent())) {
            post.setContent(dto.getContent());
        }
        if (dto.getCoverImage() != null) {
            post.setCoverImage(dto.getCoverImage());
        }
        if (dto.getImageList() != null) {
            replacePostImages(post.getId(), dto.getImageList());
            if (!StringUtils.hasText(post.getCoverImage()) && !dto.getImageList().isEmpty()) {
                post.setCoverImage(dto.getImageList().get(0));
            }
        }

        post.setStatus(0);
        post.setAuditReason(null);
        post.setAuditAdminId(null);
        post.setAuditTime(null);
        this.updateById(post);
    }

    @Override
    public void deleteMyById(Long id) {
        Long currentUserId = userTokenUtils.getCurrentUserId();
        Post post = getRequiredPost(id);
        if (!currentUserId.equals(post.getUserId())) {
            throw new RuntimeException("无权删除他人发布的帖子");
        }
        this.removeById(id);
    }

    @Override
    public Page<PostListVO> getAdminPage(AdminPostQueryDTO dto) {
        Page<Post> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(dto.getCategoryId() != null, Post::getCategoryId, dto.getCategoryId())
                .eq(dto.getStatus() != null, Post::getStatus, dto.getStatus())
                .eq(dto.getUserId() != null, Post::getUserId, dto.getUserId())
                .eq(StringUtils.hasText(dto.getSourceType()), Post::getSourceType, dto.getSourceType())
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(Post::getTitle, dto.getKeyword())
                        .or()
                        .like(Post::getContent, dto.getKeyword()))
                .orderByDesc(Post::getCreateTime);

        return convertPage(this.page(page, wrapper));
    }

    @Override
    public Page<PostListVO> getPendingPage(Integer pageNum, Integer pageSize, String keyword, Long categoryId) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 0)
                .eq(categoryId != null, Post::getCategoryId, categoryId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Post::getTitle, keyword)
                        .or()
                        .like(Post::getContent, keyword))
                .orderByDesc(Post::getCreateTime);
        return convertPage(this.page(page, wrapper));
    }

    @Override
    public void approve(Long id) {
        Post post = getRequiredPost(id);
        post.setStatus(1);
        post.setAuditReason(null);
        post.setAuditAdminId(adminTokenUtils.getCurrentAdminId());
        post.setAuditTime(LocalDateTime.now());
        this.updateById(post);
    }

    @Override
    public void reject(Long id, String auditReason) {
        Post post = getRequiredPost(id);
        post.setStatus(2);
        post.setAuditReason(auditReason);
        post.setAuditAdminId(adminTokenUtils.getCurrentAdminId());
        post.setAuditTime(LocalDateTime.now());
        this.updateById(post);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Post post = getRequiredPost(id);
        post.setStatus(status);
        this.updateById(post);
    }

    private Post getRequiredPost(Long id) {
        Post post = this.getById(id);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        return post;
    }

    private Page<PostListVO> convertPage(Page<Post> entityPage) {
        Page<PostListVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toListVO).toList());
        return voPage;
    }

    private PostListVO toListVO(Post post) {
        PostListVO vo = new PostListVO();
        BeanUtils.copyProperties(post, vo);
        vo.setContentPreview(buildPreview(post.getContent()));
        if (!StringUtils.hasText(vo.getCoverImage())) {
            vo.setCoverImage(getFirstImage(post.getId()));
        }

        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        return vo;
    }

    private PostDetailVO toDetailVO(Post post) {
        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);
        vo.setImageList(getImageList(post.getId()));
        if (!StringUtils.hasText(vo.getCoverImage()) && !vo.getImageList().isEmpty()) {
            vo.setCoverImage(vo.getImageList().get(0));
        }

        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        return vo;
    }

    private String buildPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return content.length() > 80 ? content.substring(0, 80) : content;
    }

    private void replacePostImages(Long postId, List<String> imageList) {
        postImageMapper.delete(new LambdaQueryWrapper<PostImage>().eq(PostImage::getPostId, postId));
        if (imageList == null || imageList.isEmpty()) {
            return;
        }

        for (int i = 0; i < imageList.size(); i++) {
            String imageUrl = imageList.get(i);
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }

            PostImage postImage = new PostImage();
            postImage.setPostId(postId);
            postImage.setImageUrl(imageUrl);
            postImage.setSort(i + 1);
            postImageMapper.insert(postImage);
        }
    }

    private List<String> getImageList(Long postId) {
        return postImageMapper.selectList(new LambdaQueryWrapper<PostImage>()
                        .eq(PostImage::getPostId, postId)
                        .orderByAsc(PostImage::getSort)
                        .orderByAsc(PostImage::getId))
                .stream()
                .map(PostImage::getImageUrl)
                .toList();
    }

    private String getFirstImage(Long postId) {
        List<String> imageList = getImageList(postId);
        return imageList.isEmpty() ? null : imageList.get(0);
    }
}
