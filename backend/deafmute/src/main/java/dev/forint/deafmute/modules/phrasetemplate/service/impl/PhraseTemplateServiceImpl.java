package dev.forint.deafmute.modules.phrasetemplate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.phrasetemplate.dto.AdminPhraseTemplateQueryDTO;
import dev.forint.deafmute.modules.phrasetemplate.dto.PhraseTemplateAddDTO;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;
import dev.forint.deafmute.modules.phrasetemplate.mapper.PhraseTemplateMapper;
import dev.forint.deafmute.modules.phrasetemplate.service.PhraseTemplateService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PhraseTemplateServiceImpl extends ServiceImpl<PhraseTemplateMapper, PhraseTemplate> implements PhraseTemplateService {

    @Override
    public List<PhraseTemplate> getEnabledList(String sceneType) {
        return this.list(new LambdaQueryWrapper<PhraseTemplate>()
                .eq(PhraseTemplate::getStatus, 1)
                .eq(StringUtils.hasText(sceneType), PhraseTemplate::getSceneType, sceneType)
                .orderByAsc(PhraseTemplate::getSort)
                .orderByAsc(PhraseTemplate::getId));
    }

    @Override
    public Page<PhraseTemplate> getAdminPage(AdminPhraseTemplateQueryDTO dto) {
        Page<PhraseTemplate> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<PhraseTemplate> wrapper = new LambdaQueryWrapper<PhraseTemplate>()
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(PhraseTemplate::getPhraseText, dto.getKeyword())
                        .or()
                        .like(PhraseTemplate::getPhraseCode, dto.getKeyword()))
                .eq(StringUtils.hasText(dto.getSceneType()), PhraseTemplate::getSceneType, dto.getSceneType())
                .eq(dto.getStatus() != null, PhraseTemplate::getStatus, dto.getStatus())
                .orderByAsc(PhraseTemplate::getSort)
                .orderByAsc(PhraseTemplate::getId);
        return this.page(page, wrapper);
    }

    @Override
    public PhraseTemplate getDetail(Long id) {
        PhraseTemplate phraseTemplate = this.getById(id);
        if (phraseTemplate == null) {
            throw new RuntimeException("短语模板不存在");
        }
        return phraseTemplate;
    }

    @Override
    public void add(PhraseTemplateAddDTO dto) {
        validateUnique(null, dto.getPhraseCode());
        PhraseTemplate phraseTemplate = new PhraseTemplate();
        BeanUtils.copyProperties(dto, phraseTemplate);
        if (!StringUtils.hasText(phraseTemplate.getTtsText())) {
            phraseTemplate.setTtsText(phraseTemplate.getPhraseText());
        }
        this.save(phraseTemplate);
    }

    @Override
    public void updatePhrase(Long id, PhraseTemplateAddDTO dto) {
        PhraseTemplate phraseTemplate = getDetail(id);
        validateUnique(id, dto.getPhraseCode());
        BeanUtils.copyProperties(dto, phraseTemplate);
        if (!StringUtils.hasText(phraseTemplate.getTtsText())) {
            phraseTemplate.setTtsText(phraseTemplate.getPhraseText());
        }
        this.updateById(phraseTemplate);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        PhraseTemplate phraseTemplate = getDetail(id);
        phraseTemplate.setStatus(status);
        this.updateById(phraseTemplate);
    }

    private void validateUnique(Long id, String phraseCode) {
        long codeCount = this.count(new LambdaQueryWrapper<PhraseTemplate>()
                .eq(PhraseTemplate::getPhraseCode, phraseCode)
                .ne(id != null, PhraseTemplate::getId, id));
        if (codeCount > 0) {
            throw new RuntimeException("短语编码已存在");
        }
    }
}
