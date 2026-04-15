package dev.forint.deafmute.modules.recognitionconfig.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.recognitionconfig.dto.AdminRecognitionConfigQueryDTO;
import dev.forint.deafmute.modules.recognitionconfig.dto.RecognitionConfigAddDTO;
import dev.forint.deafmute.modules.recognitionconfig.entity.RecognitionConfig;

public interface RecognitionConfigService extends IService<RecognitionConfig> {

    RecognitionConfig getActiveConfig();

    Page<RecognitionConfig> getAdminPage(AdminRecognitionConfigQueryDTO dto);

    RecognitionConfig getDetail(Long id);

    void add(RecognitionConfigAddDTO dto);

    void updateConfig(Long id, RecognitionConfigAddDTO dto);

    void activate(Long id);
}
