package dev.forint.deafmute.modules.phrasetemplate.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;
import dev.forint.deafmute.modules.phrasetemplate.service.PhraseTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/phrase-template")
@RequiredArgsConstructor
public class PhraseTemplateController {

    private final PhraseTemplateService phraseTemplateService;

    @GetMapping("/list")
    public Result<List<PhraseTemplate>> list(@RequestParam(required = false) String sceneType) {
        return Result.success(phraseTemplateService.getEnabledList(sceneType));
    }
}
