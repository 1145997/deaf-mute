package dev.forint.deafmute.modules.recognitionconfig.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.recognitionconfig.dto.AdminRecognitionConfigQueryDTO;
import dev.forint.deafmute.modules.recognitionconfig.dto.RecognitionConfigAddDTO;
import dev.forint.deafmute.modules.recognitionconfig.entity.RecognitionConfig;
import dev.forint.deafmute.modules.recognitionconfig.service.RecognitionConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/recognition-config")
@RequiredArgsConstructor
public class AdminRecognitionConfigController {

    private final AdminTokenUtils adminTokenUtils;
    private final RecognitionConfigService recognitionConfigService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminRecognitionConfigQueryDTO dto) {
        adminTokenUtils.checkAdminLogin();
        Page<RecognitionConfig> page = recognitionConfigService.getAdminPage(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/active")
    public Result<RecognitionConfig> active() {
        adminTokenUtils.checkAdminLogin();
        return Result.success(recognitionConfigService.getActiveConfig());
    }

    @GetMapping("/{id}")
    public Result<RecognitionConfig> detail(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        return Result.success(recognitionConfigService.getDetail(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid RecognitionConfigAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        recognitionConfigService.add(dto);
        return Result.success("新增识别配置成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid RecognitionConfigAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        recognitionConfigService.updateConfig(id, dto);
        return Result.success("修改识别配置成功", null);
    }

    @PutMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        recognitionConfigService.activate(id);
        return Result.success("启用识别配置成功", null);
    }
}
