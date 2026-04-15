package dev.forint.deafmute.modules.gestureflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.gestureflow.dto.AdminGestureFlowQueryDTO;
import dev.forint.deafmute.modules.gestureflow.dto.GestureFlowAddDTO;
import dev.forint.deafmute.modules.gestureflow.entity.GestureFlow;
import dev.forint.deafmute.modules.gestureflow.vo.GestureFlowDetailVO;

public interface GestureFlowService extends IService<GestureFlow> {

    Page<GestureFlow> getAdminPage(AdminGestureFlowQueryDTO dto);

    GestureFlowDetailVO getDetail(Long id);

    void add(GestureFlowAddDTO dto);

    void updateFlow(Long id, GestureFlowAddDTO dto);

    void deleteFlow(Long id);
}
