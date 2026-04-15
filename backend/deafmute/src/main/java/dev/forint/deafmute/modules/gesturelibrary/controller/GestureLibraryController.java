package dev.forint.deafmute.modules.gesturelibrary.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;
import dev.forint.deafmute.modules.gesturelibrary.service.GestureLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/gesture-library")
@RequiredArgsConstructor
public class GestureLibraryController {

    private final GestureLibraryService gestureLibraryService;

    @GetMapping("/enabled")
    public Result<List<GestureLibrary>> enabledList() {
        return Result.success(gestureLibraryService.getEnabledList());
    }

    @GetMapping("/list")
    public Result<List<GestureLibrary>> list() {
        return Result.success(gestureLibraryService.getEnabledList());
    }
}
