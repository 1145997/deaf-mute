package dev.forint.deafmute.modules.recognitionconfig.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.recognitionconfig.dto.AdminRecognitionConfigQueryDTO;
import dev.forint.deafmute.modules.recognitionconfig.dto.RecognitionConfigAddDTO;
import dev.forint.deafmute.modules.recognitionconfig.entity.RecognitionConfig;
import dev.forint.deafmute.modules.recognitionconfig.mapper.RecognitionConfigMapper;
import dev.forint.deafmute.modules.recognitionconfig.service.RecognitionConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RecognitionConfigServiceImpl extends ServiceImpl<RecognitionConfigMapper, RecognitionConfig> implements RecognitionConfigService {

    @Override
    public RecognitionConfig getActiveConfig() {
        RecognitionConfig config = this.getOne(new LambdaQueryWrapper<RecognitionConfig>()
                .eq(RecognitionConfig::getActiveFlag, 1)
                .orderByDesc(RecognitionConfig::getId)
                .last("limit 1"));
        if (config == null) {
            throw new RuntimeException("当前暂无生效识别配置");
        }
        return config;
    }

    @Override
    public Page<RecognitionConfig> getAdminPage(AdminRecognitionConfigQueryDTO dto) {
        Page<RecognitionConfig> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<RecognitionConfig> wrapper = new LambdaQueryWrapper<RecognitionConfig>()
                .like(StringUtils.hasText(dto.getKeyword()), RecognitionConfig::getConfigName, dto.getKeyword())
                .eq(dto.getActiveFlag() != null, RecognitionConfig::getActiveFlag, dto.getActiveFlag())
                .orderByDesc(RecognitionConfig::getActiveFlag)
                .orderByDesc(RecognitionConfig::getId);
        return this.page(page, wrapper);
    }

    @Override
    public RecognitionConfig getDetail(Long id) {
        RecognitionConfig config = this.getById(id);
        if (config == null) {
            throw new RuntimeException("识别配置不存在");
        }
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RecognitionConfigAddDTO dto) {
        validateUnique(null, dto.getConfigName());
        if (dto.getActiveFlag() != null && dto.getActiveFlag() == 1) {
            clearActiveFlag();
        }

        RecognitionConfig config = new RecognitionConfig();
        BeanUtils.copyProperties(dto, config);
        this.save(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long id, RecognitionConfigAddDTO dto) {
        RecognitionConfig config = getDetail(id);
        validateUnique(id, dto.getConfigName());
        if (dto.getActiveFlag() != null && dto.getActiveFlag() == 1) {
            clearActiveFlag();
        }

        BeanUtils.copyProperties(dto, config);
        this.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activate(Long id) {
        RecognitionConfig config = getDetail(id);
        clearActiveFlag();
        config.setActiveFlag(1);
        this.updateById(config);
    }

    private void validateUnique(Long id, String configName) {
        long count = this.count(new LambdaQueryWrapper<RecognitionConfig>()
                .eq(RecognitionConfig::getConfigName, configName)
                .ne(id != null, RecognitionConfig::getId, id));
        if (count > 0) {
            throw new RuntimeException("配置名称已存在");
        }
    }

    private void clearActiveFlag() {
        this.update(new LambdaUpdateWrapper<RecognitionConfig>()
                .set(RecognitionConfig::getActiveFlag, 0)
                .eq(RecognitionConfig::getActiveFlag, 1));
    }
}
