package dev.forint.deafmute.modules.recognition.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.forint.deafmute.modules.recognition.dto.RecognitionPredictDTO;
import dev.forint.deafmute.modules.recognition.dto.RecognitionSessionActionDTO;
import dev.forint.deafmute.modules.recognition.dto.RecognitionSessionStartDTO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionBootstrapVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionRecordVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionResultVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionSessionActionVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionSessionVO;

public interface RecognitionService {

    RecognitionBootstrapVO getBootstrap();

    RecognitionSessionVO startSession(RecognitionSessionStartDTO dto, Long userId);

    RecognitionResultVO predict(RecognitionPredictDTO dto);

    RecognitionSessionActionVO resetSession(RecognitionSessionActionDTO dto);

    RecognitionSessionActionVO closeSession(RecognitionSessionActionDTO dto);

    Page<RecognitionRecordVO> getMyRecordPage(Long userId, Integer pageNum, Integer pageSize);
}
