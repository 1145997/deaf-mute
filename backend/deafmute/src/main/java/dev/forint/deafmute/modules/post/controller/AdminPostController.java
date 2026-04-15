package dev.forint.deafmute.modules.post.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.post.dto.AdminPostQueryDTO;
import dev.forint.deafmute.modules.post.dto.PostRejectDTO;
import dev.forint.deafmute.modules.post.dto.PostStatusDTO;
import dev.forint.deafmute.modules.post.service.PostService;
import dev.forint.deafmute.modules.post.vo.PostDetailVO;
import dev.forint.deafmute.modules.post.vo.PostListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/post")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminTokenUtils adminTokenUtils;
    private final PostService postService;

    @GetMapping("/pending")
    public Result<Map<String, Object>> pending(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        adminTokenUtils.checkAdminLogin();

        Page<PostListVO> page = postService.getPendingPage(pageNum, pageSize, keyword, categoryId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminPostQueryDTO dto) {
        adminTokenUtils.checkAdminLogin();

        Page<PostListVO> page = postService.getAdminPage(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<PostDetailVO> detail(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        return Result.success(postService.getDetail(id));
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        postService.approve(id);
        return Result.success("审核通过", null);
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody @Valid PostRejectDTO dto) {
        adminTokenUtils.checkAdminLogin();
        postService.reject(id, dto.getAuditReason());
        return Result.success("驳回成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid PostStatusDTO dto) {
        adminTokenUtils.checkAdminLogin();
        postService.updateStatus(id, dto.getStatus());
        return Result.success("状态更新成功", null);
    }
}
