package dev.forint.deafmute.modules.recognitionconfig.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.modules.recognitionconfig.entity.RecognitionConfig;
import dev.forint.deafmute.modules.recognitionconfig.service.RecognitionConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recognition-config")
@RequiredArgsConstructor
public class RecognitionConfigController {

    private final RecognitionConfigService recognitionConfigService;

    @GetMapping("/active")
    public Result<RecognitionConfig> active() {
        return Result.success(recognitionConfigService.getActiveConfig());
    }
}
