package dev.forint.deafmute.modules.gesturelibrary.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.gesturelibrary.dto.AdminGestureLibraryQueryDTO;
import dev.forint.deafmute.modules.gesturelibrary.dto.GestureLibraryAddDTO;
import dev.forint.deafmute.modules.gesturelibrary.dto.GestureLibraryStatusDTO;
import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;
import dev.forint.deafmute.modules.gesturelibrary.service.GestureLibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/gesture-library")
@RequiredArgsConstructor
public class AdminGestureLibraryController {

    private final AdminTokenUtils adminTokenUtils;
    private final GestureLibraryService gestureLibraryService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminGestureLibraryQueryDTO dto) {
        adminTokenUtils.checkAdminLogin();
        Page<GestureLibrary> page = gestureLibraryService.getAdminPage(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<GestureLibrary> detail(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        return Result.success(gestureLibraryService.getDetail(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid GestureLibraryAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        gestureLibraryService.add(dto);
        return Result.success("新增基础手势成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid GestureLibraryAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        gestureLibraryService.updateGesture(id, dto);
        return Result.success("修改基础手势成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid GestureLibraryStatusDTO dto) {
        adminTokenUtils.checkAdminLogin();
        gestureLibraryService.updateStatus(id, dto.getStatus());
        return Result.success("修改状态成功", null);
    }
}
