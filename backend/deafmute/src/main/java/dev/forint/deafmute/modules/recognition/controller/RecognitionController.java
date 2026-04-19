package dev.forint.deafmute.modules.recognition.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.JwtUtils;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import dev.forint.deafmute.modules.recognition.dto.RecognitionPredictDTO;
import dev.forint.deafmute.modules.recognition.dto.RecognitionSessionActionDTO;
import dev.forint.deafmute.modules.recognition.dto.RecognitionSessionStartDTO;
import dev.forint.deafmute.modules.recognition.service.RecognitionService;
import dev.forint.deafmute.modules.recognition.vo.RecognitionBootstrapVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionRecordVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionResultVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionSessionActionVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionSessionVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecognitionController {

    private final RecognitionService recognitionService;
    private final UserTokenUtils userTokenUtils;
    private final JwtUtils jwtUtils;
    private final HttpServletRequest request;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @GetMapping("/recognition/bootstrap")
    public Result<RecognitionBootstrapVO> bootstrap() {
        return Result.success(recognitionService.getBootstrap());
    }

    @PostMapping("/recognition/session/start")
    public Result<RecognitionSessionVO> startSession(@RequestBody @Valid RecognitionSessionStartDTO dto) {
        return Result.success(recognitionService.startSession(dto, getOptionalCurrentUserId()));
    }

    @PostMapping("/recognition/predict")
    public Result<RecognitionResultVO> predict(@RequestBody @Valid RecognitionPredictDTO dto) {
        return Result.success(recognitionService.predict(dto));
    }

    @PostMapping("/recognition/session/reset")
    public Result<RecognitionSessionActionVO> resetSession(@RequestBody @Valid RecognitionSessionActionDTO dto) {
        return Result.success(recognitionService.resetSession(dto));
    }

    @PostMapping("/recognition/session/close")
    public Result<RecognitionSessionActionVO> closeSession(@RequestBody @Valid RecognitionSessionActionDTO dto) {
        return Result.success(recognitionService.closeSession(dto));
    }

    @GetMapping("/recognition-record/my")
    public Result<Map<String, Object>> myRecordPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        userTokenUtils.checkUserLogin();
        Page<RecognitionRecordVO> page = recognitionService.getMyRecordPage(userTokenUtils.getCurrentUserId(), pageNum, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    private Long getOptionalCurrentUserId() {
        try {
            String tokenHeader = request.getHeader(header);
            if (tokenHeader == null || tokenHeader.isBlank()) {
                return null;
            }
            String token = tokenHeader.startsWith(tokenPrefix)
                    ? tokenHeader.substring(tokenPrefix.length()).trim()
                    : tokenHeader.trim();
            Claims claims = jwtUtils.parseToken(token);
            String role = claims.get("role", String.class);
            if (!"user".equals(role)) {
                return null;
            }
            return Long.valueOf(claims.getSubject());
        } catch (Exception ex) {
            return null;
        }
    }
}
