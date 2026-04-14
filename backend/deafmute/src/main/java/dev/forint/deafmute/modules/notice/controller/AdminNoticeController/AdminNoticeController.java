package dev.forint.deafmute.modules.notice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.notice.dto.NoticeAddDTO;
import dev.forint.deafmute.modules.notice.entity.Notice;
import dev.forint.deafmute.modules.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminTokenUtils adminTokenUtils;
    private final NoticeService noticeService;

    @PostMapping
    public Result<Void> add(@RequestBody @Valid NoticeAddDTO dto) {
        adminTokenUtils.checkAdminLogin();

        noticeService.add(dto);
        return Result.success("新增公告成功", null);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        adminTokenUtils.checkAdminLogin();

        Page<Notice> page = noticeService.getAdminPage(pageNum, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());

        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid NoticeAddDTO dto) {
        adminTokenUtils.checkAdminLogin();

        noticeService.updateNotice(id, dto);
        return Result.success("修改公告成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();

        noticeService.deleteNotice(id);
        return Result.success("删除公告成功", null);
    }
}