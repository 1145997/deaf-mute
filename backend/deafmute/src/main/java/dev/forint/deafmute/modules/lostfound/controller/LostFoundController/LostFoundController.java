package dev.forint.deafmute.modules.lostfound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import dev.forint.deafmute.modules.lostfound.dto.LostFoundAddDTO;
import dev.forint.deafmute.modules.lostfound.dto.LostFoundQueryDTO;
import dev.forint.deafmute.modules.lostfound.service.LostFoundService;
import dev.forint.deafmute.modules.lostfound.vo.LostFoundListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import dev.forint.deafmute.modules.lostfound.vo.LostFoundDetailVO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/lostfound")
@RequiredArgsConstructor
@Profile("legacy-lostfound")
public class LostFoundController {

    private final UserTokenUtils userTokenUtils;
    private final LostFoundService lostFoundService;

    @PostMapping
    public Result<Void> add(@RequestBody @Valid LostFoundAddDTO dto) {
        userTokenUtils.checkUserLogin();

        lostFoundService.add(dto);
        return Result.success("发布成功", null);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(LostFoundQueryDTO dto) {
        Page<LostFoundListVO> page = lostFoundService.getPage(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());

        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<LostFoundDetailVO> detail(@PathVariable Long id) {
        return Result.success(lostFoundService.getDetail(id));
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> myList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        userTokenUtils.checkUserLogin();

        Page<LostFoundListVO> page = lostFoundService.getMyPage(pageNum, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());

        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userTokenUtils.checkUserLogin();

        lostFoundService.deleteMyById(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid LostFoundAddDTO dto) {
        userTokenUtils.checkUserLogin();

        lostFoundService.updateMyById(id, dto);
        return Result.success("修改成功，已重新进入审核", null);
    }

    @PutMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        userTokenUtils.checkUserLogin();
        lostFoundService.finishMyById(id);
        return Result.success("已标记为完成", null);
    }

    @PutMapping("/{id}/reopen")
    public Result<Void> reopen(@PathVariable Long id) {
        userTokenUtils.checkUserLogin();
        lostFoundService.reopenMyById(id);
        return Result.success("已恢复为进行中", null);
    }
}
