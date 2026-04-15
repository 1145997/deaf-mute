package dev.forint.deafmute.modules.gestureflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.gestureflow.dto.AdminGestureFlowQueryDTO;
import dev.forint.deafmute.modules.gestureflow.dto.GestureFlowAddDTO;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlow;
import dev.forint.deafmute.modules.gestureflow.service.GestureFlowService;
import dev.forint.deafmute.modules.gestureflow.vo.GestureFlowDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/gesture-flow")
@RequiredArgsConstructor
public class AdminGestureFlowController {

    private final AdminTokenUtils adminTokenUtils;
    private final GestureFlowService gestureFlowService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminGestureFlowQueryDTO dto) {
        adminTokenUtils.checkAdminLogin();
        Page<GestureFlow> page = gestureFlowService.getAdminPage(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<GestureFlowDetailVO> detail(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        return Result.success(gestureFlowService.getDetail(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid GestureFlowAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        gestureFlowService.add(dto);
        return Result.success("新增动作流成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid GestureFlowAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        gestureFlowService.updateFlow(id, dto);
        return Result.success("修改动作流成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        gestureFlowService.deleteFlow(id);
        return Result.success("删除动作流成功", null);
    }
}
