package dev.forint.deafmute.modules.phrasetemplate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.phrasetemplate.dto.AdminPhraseTemplateQueryDTO;
import dev.forint.deafmute.modules.phrasetemplate.dto.PhraseTemplateAddDTO;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;

import java.util.List;

public interface PhraseTemplateService extends IService<PhraseTemplate> {

    List<PhraseTemplate> getEnabledList(String sceneType);

    Page<PhraseTemplate> getAdminPage(AdminPhraseTemplateQueryDTO dto);

    PhraseTemplate getDetail(Long id);

    void add(PhraseTemplateAddDTO dto);

    void updatePhrase(Long id, PhraseTemplateAddDTO dto);

    void updateStatus(Long id, Integer status);
}
