package dev.forint.deafmute.modules.recognition.vo;

import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;
import dev.forint.deafmute.modules.recognitionconfig.entity.RecognitionConfig;
import lombok.Data;

import java.util.List;

@Data
public class RecognitionBootstrapVO {

    private RecognitionConfig activeConfig;

    private List<GestureLibrary> gestureList;

    private List<PhraseTemplate> phraseList;

    private List<RecognitionFlowSimpleVO> flowList;

    private List<String> supportedLandmarkGestureCodes;

    private Integer landmarkPointCount;

    private Boolean supportsHandedness;

    private Boolean supportsMirrored;

    private String landmarkCoordinateMode;

    private String landmarkOrigin;

    private String landmarkOrder;

    private String landmarkHandMode;

    private String landmarkAssumptions;
}
