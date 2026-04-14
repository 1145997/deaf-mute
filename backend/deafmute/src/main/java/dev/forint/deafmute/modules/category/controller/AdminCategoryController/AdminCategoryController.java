package dev.forint.deafmute.modules.category.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.category.dto.AdminCategoryQueryDTO;
import dev.forint.deafmute.modules.category.dto.CategoryAddDTO;
import dev.forint.deafmute.modules.category.entity.Category;
import dev.forint.deafmute.modules.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminTokenUtils adminTokenUtils;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminCategoryQueryDTO dto) {
        adminTokenUtils.checkAdminLogin();

        Page<Category> page = categoryService.getAdminPage(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());

        return Result.success(data);
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid CategoryAddDTO dto) {
        adminTokenUtils.checkAdminLogin();

        categoryService.add(dto);
        return Result.success("新增分类成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CategoryAddDTO dto) {
        adminTokenUtils.checkAdminLogin();

        categoryService.updateCategory(id, dto);
        return Result.success("修改分类成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();

        categoryService.deleteCategory(id);
        return Result.success("删除分类成功", null);
    }
}