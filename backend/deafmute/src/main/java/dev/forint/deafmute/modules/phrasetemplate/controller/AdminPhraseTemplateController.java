package dev.forint.deafmute.modules.phrasetemplate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.AdminTokenUtils;
import dev.forint.deafmute.modules.phrasetemplate.dto.AdminPhraseTemplateQueryDTO;
import dev.forint.deafmute.modules.phrasetemplate.dto.PhraseTemplateAddDTO;
import dev.forint.deafmute.modules.phrasetemplate.dto.PhraseTemplateStatusDTO;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;
import dev.forint.deafmute.modules.phrasetemplate.service.PhraseTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/phrase-template")
@RequiredArgsConstructor
public class AdminPhraseTemplateController {

    private final AdminTokenUtils adminTokenUtils;
    private final PhraseTemplateService phraseTemplateService;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(AdminPhraseTemplateQueryDTO dto) {
        adminTokenUtils.checkAdminLogin();
        Page<PhraseTemplate> page = phraseTemplateService.getAdminPage(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<PhraseTemplate> detail(@PathVariable Long id) {
        adminTokenUtils.checkAdminLogin();
        return Result.success(phraseTemplateService.getDetail(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid PhraseTemplateAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        phraseTemplateService.add(dto);
        return Result.success("新增短语模板成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid PhraseTemplateAddDTO dto) {
        adminTokenUtils.checkAdminLogin();
        phraseTemplateService.updatePhrase(id, dto);
        return Result.success("修改短语模板成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid PhraseTemplateStatusDTO dto) {
        adminTokenUtils.checkAdminLogin();
        phraseTemplateService.updateStatus(id, dto.getStatus());
        return Result.success("修改状态成功", null);
    }
}
