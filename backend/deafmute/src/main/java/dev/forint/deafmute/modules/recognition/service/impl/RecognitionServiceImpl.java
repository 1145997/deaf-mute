package dev.forint.deafmute.modules.recognition.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlow;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlowNode;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlowOutput;
import dev.forint.deafmute.modules.gestureflow.mapper.GestureFlowMapper;
import dev.forint.deafmute.modules.gestureflow.mapper.GestureFlowNodeMapper;
import dev.forint.deafmute.modules.gestureflow.mapper.GestureFlowOutputMapper;
import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;
import dev.forint.deafmute.modules.gesturelibrary.service.GestureLibraryService;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;
import dev.forint.deafmute.modules.phrasetemplate.service.PhraseTemplateService;
import dev.forint.deafmute.modules.recognition.dto.RecognitionLandmarkDTO;
import dev.forint.deafmute.modules.recognition.dto.RecognitionPredictDTO;
import dev.forint.deafmute.modules.recognition.dto.RecognitionSessionActionDTO;
import dev.forint.deafmute.modules.recognition.dto.RecognitionSessionStartDTO;
import dev.forint.deafmute.modules.recognition.engine.BasicGestureDetector;
import dev.forint.deafmute.modules.recognition.engine.RecognitionEngineFlowSnapshot;
import dev.forint.deafmute.modules.recognition.engine.RecognitionEngineLandmark;
import dev.forint.deafmute.modules.recognition.engine.RecognitionEngineSession;
import dev.forint.deafmute.modules.recognition.entity.RecognitionRecord;
import dev.forint.deafmute.modules.recognition.mapper.RecognitionRecordMapper;
import dev.forint.deafmute.modules.recognition.service.RecognitionService;
import dev.forint.deafmute.modules.recognition.vo.RecognitionBootstrapVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionFlowSimpleVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionRecordVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionResultVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionSessionActionVO;
import dev.forint.deafmute.modules.recognition.vo.RecognitionSessionVO;
import dev.forint.deafmute.modules.recognitionconfig.entity.RecognitionConfig;
import dev.forint.deafmute.modules.recognitionconfig.service.RecognitionConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecognitionServiceImpl implements RecognitionService {

    private static final ConcurrentHashMap<String, RecognitionEngineSession> SESSION_STORE = new ConcurrentHashMap<>();
    private static final String INPUT_TYPE_GESTURE_CODE = "gestureCode";
    private static final String INPUT_TYPE_LANDMARKS = "landmarks";
    private static final String LANDMARK_COORDINATE_MODE = "NORMALIZED_XYZ_0_TO_1";
    private static final String LANDMARK_ORIGIN = "TOP_LEFT";
    private static final String LANDMARK_ORDER = "MEDIAPIPE_HANDS_21";
    private static final String LANDMARK_HAND_MODE = "SINGLE_HAND_FIRST_ONLY";
    private static final String LANDMARK_ASSUMPTIONS = "当前按单手 21 点 landmarks 处理，多手场景请仅上传第一只手；可透传 handedness、mirrored、cameraFacing 作为联调信息，但当前基础检测器尚未按这些字段分支判定。";

    private final RecognitionConfigService recognitionConfigService;
    private final GestureLibraryService gestureLibraryService;
    private final PhraseTemplateService phraseTemplateService;
    private final GestureFlowMapper gestureFlowMapper;
    private final GestureFlowNodeMapper gestureFlowNodeMapper;
    private final GestureFlowOutputMapper gestureFlowOutputMapper;
    private final RecognitionRecordMapper recognitionRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public RecognitionBootstrapVO getBootstrap() {
        RecognitionConfig activeConfig = recognitionConfigService.getActiveConfig();
        List<GestureLibrary> enabledGestureList = gestureLibraryService.getEnabledList();

        RecognitionBootstrapVO vo = new RecognitionBootstrapVO();
        vo.setActiveConfig(activeConfig);
        vo.setGestureList(enabledGestureList);
        vo.setPhraseList(phraseTemplateService.getEnabledList(null));
        vo.setFlowList(gestureFlowMapper.selectList(new LambdaQueryWrapper<GestureFlow>()
                        .eq(GestureFlow::getStatus, 1)
                        .orderByDesc(GestureFlow::getPriority)
                        .orderByAsc(GestureFlow::getId))
                .stream()
                .map(this::toFlowSimpleVO)
                .toList());
        vo.setSupportedLandmarkGestureCodes(resolveSupportedLandmarkGestureCodes(activeConfig, enabledGestureList));
        vo.setLandmarkPointCount(BasicGestureDetector.LANDMARK_POINT_COUNT);
        vo.setSupportsHandedness(false);
        vo.setSupportsMirrored(false);
        vo.setLandmarkCoordinateMode(LANDMARK_COORDINATE_MODE);
        vo.setLandmarkOrigin(LANDMARK_ORIGIN);
        vo.setLandmarkOrder(LANDMARK_ORDER);
        vo.setLandmarkHandMode(LANDMARK_HAND_MODE);
        vo.setLandmarkAssumptions(LANDMARK_ASSUMPTIONS);
        return vo;
    }

    @Override
    public RecognitionSessionVO startSession(RecognitionSessionStartDTO dto, Long userId) {
        RecognitionConfig activeConfig = recognitionConfigService.getActiveConfig();
        RecognitionEngineSession session = new RecognitionEngineSession();
        session.setSessionId(buildSessionId());
        session.setUserId(userId);
        session.setClientType(dto.getClientType());
        session.setSceneCode(dto.getSceneCode());
        session.setEngineType(dto.getEngineType());
        session.setAppVersion(dto.getAppVersion());
        session.setStartedAt(LocalDateTime.now());
        session.setLastActiveAt(LocalDateTime.now());
        session.setActiveConfigId(activeConfig.getId());
        SESSION_STORE.put(session.getSessionId(), session);

        RecognitionSessionVO vo = new RecognitionSessionVO();
        vo.setSessionId(session.getSessionId());
        vo.setStartedAt(session.getStartedAt());
        vo.setActiveConfigId(session.getActiveConfigId());
        return vo;
    }

    @Override
    public RecognitionResultVO predict(RecognitionPredictDTO dto) {
        RecognitionEngineSession session = getRequiredSession(dto.getSessionId());
        session.setLastActiveAt(LocalDateTime.now());

        RecognitionConfig activeConfig = recognitionConfigService.getActiveConfig();
        int requiredHits = activeConfig.getRequiredHits() == null ? 1 : activeConfig.getRequiredHits();
        String inputType = resolveInputType(dto);
        List<String> gestureOrder = resolveGestureOrder(activeConfig);
        List<String> detectedGestureCandidates = resolveGestureCandidates(dto, gestureOrder);
        long now = System.currentTimeMillis();

        if (activeConfig.getDebounceMs() != null && now - session.getLastTriggerAt() < activeConfig.getDebounceMs()) {
            return emptyResult(session, inputType, session.getStableCount(), requiredHits, true, detectedGestureCandidates);
        }

        Map<Long, RecognitionEngineFlowSnapshot> snapshotMap = loadEnabledFlowSnapshotMap();

        if (session.getCurrentFlowId() != null && session.getCurrentNodeId() != null) {
            RecognitionEngineFlowSnapshot currentSnapshot = snapshotMap.get(session.getCurrentFlowId());
            if (currentSnapshot == null) {
                clearSessionFlowState(session);
            } else {
                GestureFlowNode currentNode = currentSnapshot.getNodeMap().get(session.getCurrentNodeId());
                Integer maxIntervalMs = currentNode != null && currentNode.getMaxIntervalMs() != null
                        ? currentNode.getMaxIntervalMs()
                        : activeConfig.getMaxIntervalMs();
                if (maxIntervalMs != null && now - session.getLastNodeMatchedAt() > maxIntervalMs) {
                    clearSessionFlowState(session);
                }
            }
        }

        String gesture = resolveGesture(dto, detectedGestureCandidates);
        if (!StringUtils.hasText(gesture)) {
            session.setStableGesture(null);
            session.setStableCount(0);
            return emptyResult(session, inputType, session.getStableCount(), requiredHits, false, detectedGestureCandidates);
        }

        if (!gesture.equals(session.getStableGesture())) {
            session.setStableGesture(gesture);
            session.setStableCount(1);
            return emptyResult(session, inputType, session.getStableCount(), requiredHits, false, detectedGestureCandidates);
        }

        session.setStableCount(session.getStableCount() + 1);
        if (session.getStableCount() < requiredHits) {
            return emptyResult(session, inputType, session.getStableCount(), requiredHits, false, detectedGestureCandidates);
        }
        int triggeredStableCount = session.getStableCount();
        session.setStableGesture(null);
        session.setStableCount(0);

        if (session.getCurrentFlowId() != null && session.getCurrentNodeId() != null) {
            RecognitionResultVO currentFlowResult = processInCurrentFlow(
                    session,
                    gesture,
                    snapshotMap,
                    activeConfig,
                    now,
                    dto,
                    inputType,
                    triggeredStableCount,
                    requiredHits,
                    detectedGestureCandidates
            );
            if (currentFlowResult != null) {
                return currentFlowResult;
            }
        }

        RecognitionResultVO startFlowResult = processFromStartNodes(
                session,
                gesture,
                snapshotMap,
                now,
                dto,
                inputType,
                triggeredStableCount,
                requiredHits,
                detectedGestureCandidates
        );
        if (startFlowResult != null) {
            return startFlowResult;
        }

        return unmatchedGestureResult(session, gesture, inputType, triggeredStableCount, requiredHits, detectedGestureCandidates);
    }

    @Override
    public RecognitionSessionActionVO resetSession(RecognitionSessionActionDTO dto) {
        RecognitionEngineSession session = getRequiredSession(dto.getSessionId());
        clearSessionFlowState(session);
        session.setStableGesture(null);
        session.setStableCount(0);
        session.setLastTriggerAt(0L);
        session.setLastNodeMatchedAt(0L);
        return actionResult("session reset");
    }

    @Override
    public RecognitionSessionActionVO closeSession(RecognitionSessionActionDTO dto) {
        getRequiredSession(dto.getSessionId());
        SESSION_STORE.remove(dto.getSessionId());
        return actionResult("session closed");
    }

    @Override
    public Page<RecognitionRecordVO> getMyRecordPage(Long userId, Integer pageNum, Integer pageSize) {
        Page<RecognitionRecord> page = new Page<>(pageNum, pageSize);
        Page<RecognitionRecord> entityPage = recognitionRecordMapper.selectPage(page, new LambdaQueryWrapper<RecognitionRecord>()
                .eq(RecognitionRecord::getUserId, userId)
                .orderByDesc(RecognitionRecord::getCreateTime)
                .orderByDesc(RecognitionRecord::getId));

        Set<Long> flowIds = entityPage.getRecords().stream()
                .map(RecognitionRecord::getMatchedFlowId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, GestureFlow> flowMap = flowIds.isEmpty()
                ? Collections.emptyMap()
                : gestureFlowMapper.selectBatchIds(flowIds).stream().collect(Collectors.toMap(GestureFlow::getId, item -> item));

        Page<RecognitionRecordVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(record -> {
            RecognitionRecordVO vo = new RecognitionRecordVO();
            BeanUtils.copyProperties(record, vo);
            GestureFlow flow = flowMap.get(record.getMatchedFlowId());
            if (flow != null) {
                vo.setMatchedFlowCode(flow.getFlowCode());
            }
            return vo;
        }).toList());
        return voPage;
    }

    private RecognitionResultVO processInCurrentFlow(
            RecognitionEngineSession session,
            String gesture,
            Map<Long, RecognitionEngineFlowSnapshot> snapshotMap,
            RecognitionConfig activeConfig,
            long now,
            RecognitionPredictDTO dto,
            String inputType,
            int stableCount,
            int requiredHits,
            List<String> detectedGestureCandidates
    ) {
        RecognitionEngineFlowSnapshot snapshot = snapshotMap.get(session.getCurrentFlowId());
        if (snapshot == null) {
            clearSessionFlowState(session);
            return null;
        }

        GestureFlowNode currentNode = snapshot.getNodeMap().get(session.getCurrentNodeId());
        if (currentNode == null) {
            clearSessionFlowState(session);
            return null;
        }

        List<GestureFlowNode> children = snapshot.getChildrenMap().getOrDefault(currentNode.getId(), List.of());
        GestureFlowNode matchedNode = children.stream()
                .sorted(Comparator.comparing(node -> node.getNodeOrder() == null ? 0 : node.getNodeOrder()))
                .filter(node -> gestureMatches(node, gesture, snapshot))
                .findFirst()
                .orElse(null);

        if (matchedNode == null) {
            Integer resetOnFail = currentNode.getResetOnFail() != null ? currentNode.getResetOnFail() : activeConfig.getResetOnFail();
            if (resetOnFail != null && resetOnFail == 1) {
                clearSessionFlowState(session);
            }
            return emptyResult(session, inputType, stableCount, requiredHits, false, detectedGestureCandidates);
        }

        return applyMatchedNode(session, snapshot, matchedNode, gesture, now, dto, inputType, stableCount, requiredHits, detectedGestureCandidates);
    }

    private RecognitionResultVO processFromStartNodes(
            RecognitionEngineSession session,
            String gesture,
            Map<Long, RecognitionEngineFlowSnapshot> snapshotMap,
            long now,
            RecognitionPredictDTO dto,
            String inputType,
            int stableCount,
            int requiredHits,
            List<String> detectedGestureCandidates
    ) {
        List<RecognitionEngineFlowSnapshot> snapshots = new ArrayList<>(snapshotMap.values());
        snapshots.sort((a, b) -> {
            int priorityA = a.getFlow().getPriority() == null ? 0 : a.getFlow().getPriority();
            int priorityB = b.getFlow().getPriority() == null ? 0 : b.getFlow().getPriority();
            return Integer.compare(priorityB, priorityA);
        });

        for (RecognitionEngineFlowSnapshot snapshot : snapshots) {
            for (GestureFlowNode startNode : snapshot.getStartNodes()) {
                if (gestureMatches(startNode, gesture, snapshot)) {
                    return applyMatchedNode(session, snapshot, startNode, gesture, now, dto, inputType, stableCount, requiredHits, detectedGestureCandidates);
                }
            }
        }
        return null;
    }

    private RecognitionResultVO applyMatchedNode(
            RecognitionEngineSession session,
            RecognitionEngineFlowSnapshot snapshot,
            GestureFlowNode node,
            String gesture,
            long now,
            RecognitionPredictDTO dto,
            String inputType,
            int stableCount,
            int requiredHits,
            List<String> detectedGestureCandidates
    ) {
        session.setLastTriggerAt(now);
        session.setLastNodeMatchedAt(now);

        RecognitionResultVO result = new RecognitionResultVO();
        result.setGesture(gesture);
        result.setMatchedFlowCode(snapshot.getFlow().getFlowCode());
        result.setMatchedNodeCode(node.getNodeCode());
        result.setTraceId("trace_" + UUID.randomUUID().toString().replace("-", ""));
        populateDebugFields(result, inputType, stableCount, requiredHits, false, detectedGestureCandidates);

        if (node.getIsEnd() != null && node.getIsEnd() == 1) {
            GestureFlowOutput output = snapshot.getOutputMap().get(node.getId());
            PhraseTemplate phraseTemplate = output == null ? null : snapshot.getPhraseTemplateMap().get(output.getPhraseTemplateId());

            String displayText = firstText(
                    output == null ? null : output.getDisplayText(),
                    output == null ? null : output.getOutputText(),
                    phraseTemplate == null ? null : phraseTemplate.getPhraseText(),
                    node.getNodeName()
            );
            String ttsText = firstText(
                    output == null ? null : output.getTtsText(),
                    phraseTemplate == null ? null : phraseTemplate.getTtsText(),
                    displayText
            );
            String outputType = output == null ? "NONE" : output.getOutputType();
            String code = phraseTemplate != null && StringUtils.hasText(phraseTemplate.getPhraseCode())
                    ? phraseTemplate.getPhraseCode()
                    : firstText(output == null ? null : output.getControlAction(), snapshot.getFlow().getFlowCode(), node.getNodeCode());

            result.setCode(code);
            result.setLabel(displayText);
            result.setLocked(false);
            result.setState("idle");
            result.setOutputType(outputType);
            result.setDisplayText(displayText);
            result.setTtsText(ttsText);
            result.setControlAction(output == null ? null : output.getControlAction());

            persistRecord(session, snapshot.getFlow(), node, result, dto, inputType);
            clearSessionFlowState(session);
            return result;
        }

        session.setCurrentFlowId(snapshot.getFlow().getId());
        session.setCurrentNodeId(node.getId());
        session.setCurrentNodeCode(node.getNodeCode());
        session.setState("locked");

        result.setCode(node.getNodeCode());
        result.setLabel(node.getNodeName());
        result.setLocked(true);
        result.setState("locked");
        result.setOutputType("NONE");
        result.setDisplayText(node.getNodeName());
        result.setTtsText(node.getNodeName());
        return result;
    }

    private boolean gestureMatches(GestureFlowNode node, String detectedGesture, RecognitionEngineFlowSnapshot snapshot) {
        GestureLibrary gestureLibrary = snapshot.getGestureLibraryMap().get(node.getGestureLibraryId());
        if (gestureLibrary == null) {
            return false;
        }
        return detectedGesture.equals(gestureLibrary.getGestureCode()) || detectedGesture.equals(gestureLibrary.getDetectionKey());
    }

    private String resolveGesture(RecognitionPredictDTO dto, List<String> detectedGestureCandidates) {
        if (StringUtils.hasText(dto.getGestureCode())) {
            return dto.getGestureCode().trim();
        }
        if (detectedGestureCandidates == null || detectedGestureCandidates.isEmpty()) {
            return null;
        }
        return detectedGestureCandidates.get(0);
    }

    private Map<Long, RecognitionEngineFlowSnapshot> loadEnabledFlowSnapshotMap() {
        List<GestureFlow> flows = gestureFlowMapper.selectList(new LambdaQueryWrapper<GestureFlow>()
                .eq(GestureFlow::getStatus, 1)
                .orderByDesc(GestureFlow::getPriority)
                .orderByAsc(GestureFlow::getId));
        if (flows.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> flowIds = flows.stream().map(GestureFlow::getId).collect(Collectors.toSet());
        List<GestureFlowNode> allNodes = gestureFlowNodeMapper.selectList(new LambdaQueryWrapper<GestureFlowNode>()
                .in(GestureFlowNode::getFlowId, flowIds)
                .orderByAsc(GestureFlowNode::getNodeOrder)
                .orderByAsc(GestureFlowNode::getId));
        List<GestureFlowOutput> allOutputs = gestureFlowOutputMapper.selectList(new LambdaQueryWrapper<GestureFlowOutput>()
                .in(GestureFlowOutput::getFlowId, flowIds)
                .orderByAsc(GestureFlowOutput::getId));

        Set<Long> gestureIds = allNodes.stream().map(GestureFlowNode::getGestureLibraryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> phraseIds = allOutputs.stream().map(GestureFlowOutput::getPhraseTemplateId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, GestureLibrary> gestureMap = gestureIds.isEmpty()
                ? Collections.emptyMap()
                : gestureLibraryService.listByIds(gestureIds).stream().collect(Collectors.toMap(GestureLibrary::getId, item -> item));
        Map<Long, PhraseTemplate> phraseMap = phraseIds.isEmpty()
                ? Collections.emptyMap()
                : phraseTemplateService.listByIds(phraseIds).stream().collect(Collectors.toMap(PhraseTemplate::getId, item -> item));

        Map<Long, List<GestureFlowNode>> nodeByFlow = allNodes.stream().collect(Collectors.groupingBy(GestureFlowNode::getFlowId));
        Map<Long, List<GestureFlowOutput>> outputByFlow = allOutputs.stream().collect(Collectors.groupingBy(GestureFlowOutput::getFlowId));

        Map<Long, RecognitionEngineFlowSnapshot> snapshotMap = new HashMap<>();
        for (GestureFlow flow : flows) {
            List<GestureFlowNode> flowNodes = nodeByFlow.getOrDefault(flow.getId(), List.of());
            List<GestureFlowOutput> flowOutputs = outputByFlow.getOrDefault(flow.getId(), List.of());

            RecognitionEngineFlowSnapshot snapshot = new RecognitionEngineFlowSnapshot();
            snapshot.setFlow(flow);
            snapshot.setNodeMap(flowNodes.stream().collect(Collectors.toMap(GestureFlowNode::getId, item -> item)));
            snapshot.setChildrenMap(flowNodes.stream()
                    .filter(node -> node.getParentNodeId() != null)
                    .collect(Collectors.groupingBy(GestureFlowNode::getParentNodeId)));
            snapshot.setOutputMap(flowOutputs.stream()
                    .filter(output -> output.getEndNodeId() != null)
                    .collect(Collectors.toMap(GestureFlowOutput::getEndNodeId, item -> item, (a, b) -> a)));
            snapshot.setGestureLibraryMap(flowNodes.stream()
                    .map(GestureFlowNode::getGestureLibraryId)
                    .filter(Objects::nonNull)
                    .filter(gestureMap::containsKey)
                    .collect(Collectors.toMap(id -> id, gestureMap::get, (a, b) -> a)));
            snapshot.setPhraseTemplateMap(flowOutputs.stream()
                    .map(GestureFlowOutput::getPhraseTemplateId)
                    .filter(Objects::nonNull)
                    .filter(phraseMap::containsKey)
                    .collect(Collectors.toMap(id -> id, phraseMap::get, (a, b) -> a)));
            snapshot.setStartNodes(flowNodes.stream()
                    .filter(node -> node.getIsStart() != null && node.getIsStart() == 1)
                    .sorted(Comparator.comparing(node -> node.getNodeOrder() == null ? 0 : node.getNodeOrder()))
                    .toList());
            snapshotMap.put(flow.getId(), snapshot);
        }
        return snapshotMap;
    }

    private List<String> parseGestureOrder(String gestureOrderJson) {
        if (!StringUtils.hasText(gestureOrderJson)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(gestureOrderJson, new TypeReference<List<String>>() {
            });
        } catch (Exception ignored) {
            String raw = gestureOrderJson.replace("[", "").replace("]", "").replace("\"", "");
            return List.of(raw.split("[,\\n\\r]+")).stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
        }
    }

    private RecognitionEngineLandmark toEngineLandmark(RecognitionLandmarkDTO dto) {
        return new RecognitionEngineLandmark(dto.getX(), dto.getY(), dto.getZ() == null ? 0.0D : dto.getZ());
    }

    private RecognitionFlowSimpleVO toFlowSimpleVO(GestureFlow flow) {
        RecognitionFlowSimpleVO vo = new RecognitionFlowSimpleVO();
        BeanUtils.copyProperties(flow, vo);
        return vo;
    }

    private RecognitionSessionActionVO actionResult(String message) {
        RecognitionSessionActionVO vo = new RecognitionSessionActionVO();
        vo.setOk(true);
        vo.setMessage(message);
        return vo;
    }

    private RecognitionResultVO emptyResult(
            RecognitionEngineSession session,
            String inputType,
            int stableCount,
            int requiredHits,
            boolean debounced,
            List<String> detectedGestureCandidates
    ) {
        RecognitionResultVO vo = new RecognitionResultVO();
        vo.setLocked("locked".equals(session.getState()));
        vo.setState(session.getState());
        vo.setOutputType("NONE");
        populateDebugFields(vo, inputType, stableCount, requiredHits, debounced, detectedGestureCandidates);
        return vo;
    }

    private RecognitionResultVO unmatchedGestureResult(
            RecognitionEngineSession session,
            String gesture,
            String inputType,
            int stableCount,
            int requiredHits,
            List<String> detectedGestureCandidates
    ) {
        RecognitionResultVO vo = emptyResult(session, inputType, stableCount, requiredHits, false, detectedGestureCandidates);
        vo.setGesture(gesture);
        vo.setCode(gesture);
        vo.setLabel(gesture);
        vo.setDisplayText(gesture);
        return vo;
    }

    private RecognitionEngineSession getRequiredSession(String sessionId) {
        RecognitionEngineSession session = SESSION_STORE.get(sessionId);
        if (session == null) {
            throw new RuntimeException("识别会话不存在");
        }
        return session;
    }

    private void clearSessionFlowState(RecognitionEngineSession session) {
        session.setCurrentFlowId(null);
        session.setCurrentNodeId(null);
        session.setCurrentNodeCode(null);
        session.setState("idle");
    }

    private String buildSessionId() {
        return "rec_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private void persistRecord(
            RecognitionEngineSession session,
            GestureFlow flow,
            GestureFlowNode node,
            RecognitionResultVO result,
            RecognitionPredictDTO dto,
            String inputType
    ) {
        try {
            RecognitionRecord record = new RecognitionRecord();
            record.setUserId(session.getUserId());
            record.setSessionId(session.getSessionId());
            record.setMatchedGestureCode(result.getGesture());
            record.setMatchedFlowId(flow.getId());
            record.setMatchedNodePath(buildNodePath(node, loadNodePathMap(flow.getId())));
            record.setOutputType(result.getOutputType());
            record.setOutputText(result.getDisplayText());
            record.setControlAction(result.getControlAction());
            record.setConfidenceScore(BigDecimal.ONE);
            record.setRequestPayloadJson(objectMapper.writeValueAsString(dto));
            record.setResultPayloadJson(objectMapper.writeValueAsString(result));
            record.setClientPlatform(session.getClientType());
            record.setInputMode(inputType);
            record.setSource(firstText(dto.getSource(), dto.getCameraFacing(), dto.getHandedness()));
            recognitionRecordMapper.insert(record);
        } catch (Exception ignored) {
            // keep predict path available even if record serialization fails
        }
    }

    private Map<Long, GestureFlowNode> loadNodePathMap(Long flowId) {
        return gestureFlowNodeMapper.selectList(new LambdaQueryWrapper<GestureFlowNode>()
                        .eq(GestureFlowNode::getFlowId, flowId))
                .stream()
                .collect(Collectors.toMap(GestureFlowNode::getId, item -> item));
    }

    private String buildNodePath(GestureFlowNode node, Map<Long, GestureFlowNode> nodeMap) {
        List<String> path = new ArrayList<>();
        GestureFlowNode current = node;
        int guard = 0;
        while (current != null && guard < 20) {
            path.add(current.getNodeCode());
            current = current.getParentNodeId() == null ? null : nodeMap.get(current.getParentNodeId());
            guard++;
        }
        Collections.reverse(path);
        return String.join("->", path);
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private List<String> resolveGestureOrder(RecognitionConfig activeConfig) {
        List<String> gestureOrder = parseGestureOrder(activeConfig.getGestureOrderJson());
        if (!gestureOrder.isEmpty()) {
            return gestureOrder;
        }
        return gestureLibraryService.getEnabledList().stream()
                .map(item -> StringUtils.hasText(item.getDetectionKey()) ? item.getDetectionKey() : item.getGestureCode())
                .toList();
    }

    private List<String> resolveGestureCandidates(RecognitionPredictDTO dto, List<String> gestureOrder) {
        if (StringUtils.hasText(dto.getGestureCode())) {
            return List.of(dto.getGestureCode().trim());
        }
        if (dto.getLandmarks() == null || dto.getLandmarks().isEmpty()) {
            return List.of();
        }
        return BasicGestureDetector.detectCandidates(dto.getLandmarks().stream()
                .map(this::toEngineLandmark)
                .toList(), gestureOrder);
    }

    private String resolveInputType(RecognitionPredictDTO dto) {
        if (StringUtils.hasText(dto.getGestureCode())) {
            return INPUT_TYPE_GESTURE_CODE;
        }
        if (dto.getLandmarks() != null && !dto.getLandmarks().isEmpty()) {
            return INPUT_TYPE_LANDMARKS;
        }
        return "unknown";
    }

    private void populateDebugFields(
            RecognitionResultVO result,
            String inputType,
            int stableCount,
            int requiredHits,
            boolean debounced,
            List<String> detectedGestureCandidates
    ) {
        result.setInputType(inputType);
        result.setStableCount(stableCount);
        result.setRequiredHits(requiredHits);
        result.setDebounced(debounced);
        result.setDetectedGestureCandidates(detectedGestureCandidates == null ? List.of() : detectedGestureCandidates);
    }

    private List<String> resolveSupportedLandmarkGestureCodes(RecognitionConfig activeConfig, List<GestureLibrary> enabledGestureList) {
        List<String> configuredOrder = resolveGestureOrder(activeConfig);
        List<String> supportedDetectorCodes = BasicGestureDetector.supportedGestureCodes();
        Map<String, GestureLibrary> byDetectionKey = enabledGestureList.stream()
                .filter(item -> StringUtils.hasText(item.getDetectionKey()))
                .collect(Collectors.toMap(GestureLibrary::getDetectionKey, item -> item, (a, b) -> a));
        Map<String, GestureLibrary> byGestureCode = enabledGestureList.stream()
                .collect(Collectors.toMap(GestureLibrary::getGestureCode, item -> item, (a, b) -> a));

        List<String> result = new ArrayList<>();
        for (String code : configuredOrder) {
            if (!supportedDetectorCodes.contains(code)) {
                continue;
            }
            GestureLibrary gestureLibrary = byDetectionKey.get(code);
            if (gestureLibrary == null) {
                gestureLibrary = byGestureCode.get(code);
            }
            String gestureCode = gestureLibrary == null ? code : gestureLibrary.getGestureCode();
            if (!result.contains(gestureCode)) {
                result.add(gestureCode);
            }
        }

        for (GestureLibrary gestureLibrary : enabledGestureList) {
            String detectionKey = StringUtils.hasText(gestureLibrary.getDetectionKey())
                    ? gestureLibrary.getDetectionKey()
                    : gestureLibrary.getGestureCode();
            if (supportedDetectorCodes.contains(detectionKey) && !result.contains(gestureLibrary.getGestureCode())) {
                result.add(gestureLibrary.getGestureCode());
            }
        }
        return result;
    }
}
