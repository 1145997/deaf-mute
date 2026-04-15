package dev.forint.deafmute.modules.comment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.comment.dto.AdminCommentQueryDTO;
import dev.forint.deafmute.modules.comment.dto.CommentStatusDTO;
import dev.forint.deafmute.modules.comment.service.CommentService;
import dev.forint.deafmute.modules.comment.vo.CommentAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminTokenUtils adminTokenUtils;
    private final CommentService commentService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminCommentQueryDTO dto) {
        adminTokenUtils.checkAdminLogin();

        Page<CommentAdminVO> page = commentService.getAdminPage(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        commentService.deleteByAdmin(id);
        return Result.success("删除评论成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid CommentStatusDTO dto) {
        adminTokenUtils.checkAdminLogin();
        commentService.updateStatusByAdmin(id, dto.getStatus());
        return Result.success("评论状态更新成功", null);
    }

    @PutMapping("/{id}/hide")
    public Result<Void> hide(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        commentService.updateStatusByAdmin(id, 0);
        return Result.success("屏蔽评论成功", null);
    }

    @PutMapping("/{id}/show")
    public Result<Void> show(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        commentService.updateStatusByAdmin(id, 1);
        return Result.success("取消屏蔽成功", null);
    }
}
