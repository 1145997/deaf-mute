package dev.forint.deafmute.modules.post.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import dev.forint.deafmute.modules.post.dto.PostAddDTO;
import dev.forint.deafmute.modules.post.dto.PostQueryDTO;
import dev.forint.deafmute.modules.post.dto.PostUpdateDTO;
import dev.forint.deafmute.modules.post.service.PostService;
import dev.forint.deafmute.modules.post.vo.PostDetailVO;
import dev.forint.deafmute.modules.post.vo.PostListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final UserTokenUtils userTokenUtils;
    private final PostService postService;

    @PostMapping
    public Result<Void> add(@RequestBody @Valid PostAddDTO dto) {
        userTokenUtils.checkUserLogin();
        postService.add(dto);
        return Result.success("发布成功", null);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(PostQueryDTO dto) {
        if (dto.getStatus() == null) {
            dto.setStatus(1);
        }

        Page<PostListVO> page = postService.getPage(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<PostDetailVO> detail(@PathVariable Long id) {
        return Result.success(postService.getDetail(id));
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> myList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        userTokenUtils.checkUserLogin();

        Page<PostListVO> page = postService.getMyPage(pageNum, pageSize);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid PostUpdateDTO dto) {
        userTokenUtils.checkUserLogin();
        postService.updateMyById(id, dto);
        return Result.success("修改成功，已重新进入审核", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userTokenUtils.checkUserLogin();
        postService.deleteMyById(id);
        return Result.success("删除成功", null);
    }
}
