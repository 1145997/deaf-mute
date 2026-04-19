package dev.forint.deafmute.modules.recognition.engine;

import dev.forint.deafmute.modules.gestureflow.entity.GestureFlow;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlowNode;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlowOutput;
import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RecognitionEngineFlowSnapshot {

    private GestureFlow flow;

    private Map<Long, GestureFlowNode> nodeMap;

    private Map<Long, List<GestureFlowNode>> childrenMap;

    private Map<Long, GestureFlowOutput> outputMap;

    private Map<Long, GestureLibrary> gestureLibraryMap;

    private Map<Long, PhraseTemplate> phraseTemplateMap;

    private List<GestureFlowNode> startNodes;
}
