package dev.forint.deafmute.modules.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.category.dto.AdminCategoryQueryDTO;
import dev.forint.deafmute.modules.category.dto.CategoryAddDTO;
import dev.forint.deafmute.modules.category.entity.Category;
import dev.forint.deafmute.modules.category.mapper.CategoryMapper;
import dev.forint.deafmute.modules.category.service.CategoryService;
import dev.forint.deafmute.modules.post.entity.Post;
import dev.forint.deafmute.modules.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final PostMapper postMapper;

    @Override
    public List<Category> getEnabledCategoryList(String type) {
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(StringUtils.hasText(type), Category::getType, type)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Override
    public Page<Category> getAdminPage(AdminCategoryQueryDTO dto) {
        Page<Category> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .like(StringUtils.hasText(dto.getKeyword()), Category::getName, dto.getKeyword())
                .eq(StringUtils.hasText(dto.getType()), Category::getType, dto.getType())
                .eq(dto.getStatus() != null, Category::getStatus, dto.getStatus())
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId);
        return this.page(page, wrapper);
    }

    @Override
    public void add(CategoryAddDTO dto) {
        long count = this.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, dto.getName())
                .eq(Category::getType, dto.getType()));
        if (count > 0) {
            throw new RuntimeException("分类名称已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        this.save(category);
    }

    @Override
    public void updateCategory(Long id, CategoryAddDTO dto) {
        Category category = this.getById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        long count = this.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, dto.getName())
                .eq(Category::getType, dto.getType())
                .ne(Category::getId, id));
        if (count > 0) {
            throw new RuntimeException("分类名称已存在");
        }

        BeanUtils.copyProperties(dto, category);
        this.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = this.getById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        Long usedCount = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getCategoryId, id));
        if (usedCount != null && usedCount > 0) {
            throw new RuntimeException("该分类已被帖子使用，无法删除");
        }

        this.removeById(id);
    }
}
