package dev.forint.deafmute.modules.category.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.modules.category.entity.Category;
import dev.forint.deafmute.modules.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam(required = false) String type) {
        return Result.success(categoryService.getEnabledCategoryList(type));
    }
}
