package dev.forint.deafmute.modules.gestureflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.gestureflow.dto.AdminGestureFlowQueryDTO;
import dev.forint.deafmute.modules.gestureflow.dto.GestureFlowAddDTO;
import dev.forint.deafmute.modules.gestureflow.dto.GestureFlowNodeDTO;
import dev.forint.deafmute.modules.gestureflow.dto.GestureFlowOutputDTO;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlow;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlowNode;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlowOutput;
import dev.forint.deafmute.modules.gestureflow.mapper.GestureFlowMapper;
import dev.forint.deafmute.modules.gestureflow.mapper.GestureFlowNodeMapper;
import dev.forint.deafmute.modules.gestureflow.mapper.GestureFlowOutputMapper;
import dev.forint.deafmute.modules.gestureflow.service.GestureFlowService;
import dev.forint.deafmute.modules.gestureflow.vo.GestureFlowDetailVO;
import dev.forint.deafmute.modules.gestureflow.vo.GestureFlowNodeVO;
import dev.forint.deafmute.modules.gestureflow.vo.GestureFlowOutputVO;
import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;
import dev.forint.deafmute.modules.gesturelibrary.mapper.GestureLibraryMapper;
import dev.forint.deafmute.modules.phrasetemplate.entity.PhraseTemplate;
import dev.forint.deafmute.modules.phrasetemplate.mapper.PhraseTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestureFlowServiceImpl extends ServiceImpl<GestureFlowMapper, GestureFlow> implements GestureFlowService {

    private final GestureFlowNodeMapper gestureFlowNodeMapper;
    private final GestureFlowOutputMapper gestureFlowOutputMapper;
    private final GestureLibraryMapper gestureLibraryMapper;
    private final PhraseTemplateMapper phraseTemplateMapper;

    @Override
    public Page<GestureFlow> getAdminPage(AdminGestureFlowQueryDTO dto) {
        Page<GestureFlow> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<GestureFlow> wrapper = new LambdaQueryWrapper<GestureFlow>()
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(GestureFlow::getFlowName, dto.getKeyword())
                        .or()
                        .like(GestureFlow::getFlowCode, dto.getKeyword()))
                .eq(StringUtils.hasText(dto.getFlowType()), GestureFlow::getFlowType, dto.getFlowType())
                .eq(dto.getStatus() != null, GestureFlow::getStatus, dto.getStatus())
                .eq(dto.getIsBuiltin() != null, GestureFlow::getIsBuiltin, dto.getIsBuiltin())
                .orderByDesc(GestureFlow::getPriority)
                .orderByAsc(GestureFlow::getId);
        return this.page(page, wrapper);
    }

    @Override
    public GestureFlowDetailVO getDetail(Long id) {
        GestureFlow flow = getRequiredFlow(id);
        GestureFlowDetailVO vo = new GestureFlowDetailVO();
        BeanUtils.copyProperties(flow, vo);

        List<GestureFlowNode> nodeList = gestureFlowNodeMapper.selectList(new LambdaQueryWrapper<GestureFlowNode>()
                .eq(GestureFlowNode::getFlowId, id)
                .orderByAsc(GestureFlowNode::getNodeOrder)
                .orderByAsc(GestureFlowNode::getId));
        List<GestureFlowOutput> outputList = gestureFlowOutputMapper.selectList(new LambdaQueryWrapper<GestureFlowOutput>()
                .eq(GestureFlowOutput::getFlowId, id)
                .orderByAsc(GestureFlowOutput::getId));

        Map<Long, GestureLibrary> gestureLibraryMap = loadGestureLibraryMap(nodeList);
        Map<Long, GestureFlowNode> nodeMap = nodeList.stream().collect(Collectors.toMap(GestureFlowNode::getId, item -> item));
        Map<Long, PhraseTemplate> phraseTemplateMap = loadPhraseTemplateMap(outputList);

        vo.setNodeList(nodeList.stream().map(node -> toNodeVO(node, gestureLibraryMap)).toList());
        vo.setOutputList(outputList.stream().map(output -> toOutputVO(output, nodeMap, phraseTemplateMap)).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(GestureFlowAddDTO dto) {
        validateFlowUnique(null, dto.getFlowCode());
        validateConfig(dto);

        GestureFlow flow = new GestureFlow();
        BeanUtils.copyProperties(dto, flow);
        flow.setVersionNo(1);
        flow.setStartNodeId(null);
        this.save(flow);

        Long startNodeId = replaceNodesAndOutputs(flow.getId(), dto.getNodeList(), dto.getOutputList());
        flow.setStartNodeId(startNodeId);
        this.updateById(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFlow(Long id, GestureFlowAddDTO dto) {
        GestureFlow flow = getRequiredFlow(id);
        validateFlowUnique(id, dto.getFlowCode());
        validateConfig(dto);

        BeanUtils.copyProperties(dto, flow);
        flow.setStartNodeId(null);
        flow.setVersionNo((flow.getVersionNo() == null ? 1 : flow.getVersionNo()) + 1);
        this.updateById(flow);

        Long startNodeId = replaceNodesAndOutputs(flow.getId(), dto.getNodeList(), dto.getOutputList());
        flow.setStartNodeId(startNodeId);
        this.updateById(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFlow(Long id) {
        GestureFlow flow = getRequiredFlow(id);
        this.removeById(flow.getId());
    }

    private GestureFlow getRequiredFlow(Long id) {
        GestureFlow flow = this.getById(id);
        if (flow == null) {
            throw new RuntimeException("动作流不存在");
        }
        return flow;
    }

    private void validateFlowUnique(Long id, String flowCode) {
        long count = this.count(new LambdaQueryWrapper<GestureFlow>()
                .eq(GestureFlow::getFlowCode, flowCode)
                .ne(id != null, GestureFlow::getId, id));
        if (count > 0) {
            throw new RuntimeException("动作流编码已存在");
        }
    }

    private void validateConfig(GestureFlowAddDTO dto) {
        if (dto.getNodeList() == null || dto.getNodeList().isEmpty()) {
            throw new RuntimeException("动作流至少需要一个节点");
        }

        Set<String> nodeCodeSet = new HashSet<>();
        boolean hasStartNode = false;
        Set<Long> gestureIds = new HashSet<>();
        for (GestureFlowNodeDTO nodeDTO : dto.getNodeList()) {
            if (!StringUtils.hasText(nodeDTO.getNodeCode())) {
                throw new RuntimeException("节点编码不能为空");
            }
            if (!StringUtils.hasText(nodeDTO.getNodeName())) {
                throw new RuntimeException("节点名称不能为空");
            }
            if (nodeDTO.getGestureLibraryId() == null) {
                throw new RuntimeException("节点必须选择基础手势");
            }
            if (!nodeCodeSet.add(nodeDTO.getNodeCode())) {
                throw new RuntimeException("节点编码不能重复");
            }
            if (nodeDTO.getIsStart() != null && nodeDTO.getIsStart() == 1) {
                hasStartNode = true;
            }
            gestureIds.add(nodeDTO.getGestureLibraryId());
        }

        if (!hasStartNode) {
            throw new RuntimeException("动作流至少需要一个起始节点");
        }

        long gestureCount = gestureLibraryMapper.selectBatchIds(gestureIds).size();
        if (gestureCount != gestureIds.size()) {
            throw new RuntimeException("节点中存在无效的基础手势");
        }

        Set<Long> phraseIds = new HashSet<>();
        if (dto.getOutputList() != null) {
            for (GestureFlowOutputDTO outputDTO : dto.getOutputList()) {
                if (!StringUtils.hasText(outputDTO.getOutputType())) {
                    throw new RuntimeException("输出类型不能为空");
                }
                if (outputDTO.getPhraseTemplateId() != null) {
                    phraseIds.add(outputDTO.getPhraseTemplateId());
                }
            }
        }
        if (!phraseIds.isEmpty()) {
            long phraseCount = phraseTemplateMapper.selectBatchIds(phraseIds).size();
            if (phraseCount != phraseIds.size()) {
                throw new RuntimeException("输出中存在无效的短语模板");
            }
        }
    }

    private Long replaceNodesAndOutputs(Long flowId, List<GestureFlowNodeDTO> nodeList, List<GestureFlowOutputDTO> outputList) {
        gestureFlowOutputMapper.delete(new LambdaQueryWrapper<GestureFlowOutput>().eq(GestureFlowOutput::getFlowId, flowId));
        gestureFlowNodeMapper.delete(new LambdaQueryWrapper<GestureFlowNode>().eq(GestureFlowNode::getFlowId, flowId));

        Map<String, GestureFlowNode> nodeCodeMap = new HashMap<>();
        Map<Long, Long> requestNodeIdMap = new HashMap<>();
        List<NodeLinkHolder> linkHolders = new ArrayList<>();

        for (GestureFlowNodeDTO nodeDTO : nodeList) {
            GestureFlowNode node = new GestureFlowNode();
            BeanUtils.copyProperties(nodeDTO, node);
            node.setFlowId(flowId);
            node.setParentNodeId(null);
            gestureFlowNodeMapper.insert(node);

            nodeCodeMap.put(node.getNodeCode(), node);
            if (nodeDTO.getId() != null) {
                requestNodeIdMap.put(nodeDTO.getId(), node.getId());
            }
            linkHolders.add(new NodeLinkHolder(node, nodeDTO.getParentNodeId(), nodeDTO.getParentNodeCode()));
        }

        for (NodeLinkHolder holder : linkHolders) {
            Long resolvedParentId = resolveNodeId(holder.parentNodeId(), holder.parentNodeCode(), nodeCodeMap, requestNodeIdMap);
            if (!Objects.equals(holder.node().getParentNodeId(), resolvedParentId)) {
                holder.node().setParentNodeId(resolvedParentId);
                gestureFlowNodeMapper.updateById(holder.node());
            }
        }

        Long startNodeId = nodeCodeMap.values().stream()
                .filter(node -> node.getIsStart() != null && node.getIsStart() == 1)
                .sorted((a, b) -> {
                    int compare = Integer.compare(a.getNodeOrder() == null ? 0 : a.getNodeOrder(), b.getNodeOrder() == null ? 0 : b.getNodeOrder());
                    return compare != 0 ? compare : Long.compare(a.getId(), b.getId());
                })
                .map(GestureFlowNode::getId)
                .findFirst()
                .orElse(null);

        if (outputList != null) {
            for (GestureFlowOutputDTO outputDTO : outputList) {
                GestureFlowOutput output = new GestureFlowOutput();
                BeanUtils.copyProperties(outputDTO, output);
                output.setFlowId(flowId);
                output.setEndNodeId(resolveNodeId(outputDTO.getEndNodeId(), outputDTO.getEndNodeCode(), nodeCodeMap, requestNodeIdMap));
                gestureFlowOutputMapper.insert(output);
            }
        }

        return startNodeId;
    }

    private Long resolveNodeId(Long rawNodeId, String nodeCode, Map<String, GestureFlowNode> nodeCodeMap, Map<Long, Long> requestNodeIdMap) {
        if (StringUtils.hasText(nodeCode)) {
            GestureFlowNode node = nodeCodeMap.get(nodeCode);
            if (node == null) {
                throw new RuntimeException("节点引用不存在: " + nodeCode);
            }
            return node.getId();
        }

        if (rawNodeId == null) {
            return null;
        }

        Long actualId = requestNodeIdMap.get(rawNodeId);
        if (actualId != null) {
            return actualId;
        }

        boolean exists = nodeCodeMap.values().stream().anyMatch(node -> Objects.equals(node.getId(), rawNodeId));
        if (exists) {
            return rawNodeId;
        }

        throw new RuntimeException("节点引用不存在: " + rawNodeId);
    }

    private Map<Long, GestureLibrary> loadGestureLibraryMap(List<GestureFlowNode> nodeList) {
        Set<Long> ids = nodeList.stream()
                .map(GestureFlowNode::getGestureLibraryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return gestureLibraryMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(GestureLibrary::getId, item -> item));
    }

    private Map<Long, PhraseTemplate> loadPhraseTemplateMap(List<GestureFlowOutput> outputList) {
        Set<Long> ids = outputList.stream()
                .map(GestureFlowOutput::getPhraseTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return phraseTemplateMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(PhraseTemplate::getId, item -> item));
    }

    private GestureFlowNodeVO toNodeVO(GestureFlowNode node, Map<Long, GestureLibrary> gestureLibraryMap) {
        GestureFlowNodeVO vo = new GestureFlowNodeVO();
        BeanUtils.copyProperties(node, vo);
        GestureLibrary gestureLibrary = gestureLibraryMap.get(node.getGestureLibraryId());
        if (gestureLibrary != null) {
            vo.setGestureCode(gestureLibrary.getGestureCode());
            vo.setGestureName(gestureLibrary.getGestureName());
        }
        return vo;
    }

    private GestureFlowOutputVO toOutputVO(
            GestureFlowOutput output,
            Map<Long, GestureFlowNode> nodeMap,
            Map<Long, PhraseTemplate> phraseTemplateMap
    ) {
        GestureFlowOutputVO vo = new GestureFlowOutputVO();
        BeanUtils.copyProperties(output, vo);

        GestureFlowNode endNode = nodeMap.get(output.getEndNodeId());
        if (endNode != null) {
            vo.setEndNodeCode(endNode.getNodeCode());
            vo.setEndNodeName(endNode.getNodeName());
        }

        PhraseTemplate phraseTemplate = phraseTemplateMap.get(output.getPhraseTemplateId());
        if (phraseTemplate != null) {
            vo.setPhraseText(phraseTemplate.getPhraseText());
        }
        return vo;
    }

    private record NodeLinkHolder(
            GestureFlowNode node,
            Long parentNodeId,
            String parentNodeCode
    ) {
    }
}
