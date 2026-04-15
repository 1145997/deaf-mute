package dev.forint.deafmute.modules.gesturelibrary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.gesturelibrary.dto.AdminGestureLibraryQueryDTO;
import dev.forint.deafmute.modules.gesturelibrary.dto.GestureLibraryAddDTO;
import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;
import dev.forint.deafmute.modules.gesturelibrary.mapper.GestureLibraryMapper;
import dev.forint.deafmute.modules.gesturelibrary.service.GestureLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GestureLibraryServiceImpl extends ServiceImpl<GestureLibraryMapper, GestureLibrary> implements GestureLibraryService {

    @Override
    public List<GestureLibrary> getEnabledList() {
        return this.list(new LambdaQueryWrapper<GestureLibrary>()
                .eq(GestureLibrary::getStatus, 1)
                .orderByAsc(GestureLibrary::getSort)
                .orderByAsc(GestureLibrary::getId));
    }

    @Override
    public Page<GestureLibrary> getAdminPage(AdminGestureLibraryQueryDTO dto) {
        Page<GestureLibrary> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<GestureLibrary> wrapper = new LambdaQueryWrapper<GestureLibrary>()
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(GestureLibrary::getGestureName, dto.getKeyword())
                        .or()
                        .like(GestureLibrary::getGestureCode, dto.getKeyword()))
                .eq(dto.getStatus() != null, GestureLibrary::getStatus, dto.getStatus())
                .eq(dto.getIsBuiltin() != null, GestureLibrary::getIsBuiltin, dto.getIsBuiltin())
                .orderByAsc(GestureLibrary::getSort)
                .orderByAsc(GestureLibrary::getId);
        return this.page(page, wrapper);
    }

    @Override
    public GestureLibrary getDetail(Long id) {
        GestureLibrary gestureLibrary = this.getById(id);
        if (gestureLibrary == null) {
            throw new RuntimeException("基础手势不存在");
        }
        return gestureLibrary;
    }

    @Override
    public void add(GestureLibraryAddDTO dto) {
        validateUnique(null, dto.getGestureCode(), dto.getDetectionKey());
        GestureLibrary gestureLibrary = new GestureLibrary();
        BeanUtils.copyProperties(dto, gestureLibrary);
        this.save(gestureLibrary);
    }

    @Override
    public void updateGesture(Long id, GestureLibraryAddDTO dto) {
        GestureLibrary gestureLibrary = getDetail(id);
        validateUnique(id, dto.getGestureCode(), dto.getDetectionKey());
        BeanUtils.copyProperties(dto, gestureLibrary);
        this.updateById(gestureLibrary);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        GestureLibrary gestureLibrary = getDetail(id);
        gestureLibrary.setStatus(status);
        this.updateById(gestureLibrary);
    }

    private void validateUnique(Long id, String gestureCode, String detectionKey) {
        long codeCount = this.count(new LambdaQueryWrapper<GestureLibrary>()
                .eq(GestureLibrary::getGestureCode, gestureCode)
                .ne(id != null, GestureLibrary::getId, id));
        if (codeCount > 0) {
            throw new RuntimeException("手势编码已存在");
        }

        long detectionKeyCount = this.count(new LambdaQueryWrapper<GestureLibrary>()
                .eq(GestureLibrary::getDetectionKey, detectionKey)
                .ne(id != null, GestureLibrary::getId, id));
        if (detectionKeyCount > 0) {
            throw new RuntimeException("识别器key已存在");
        }
    }
}
