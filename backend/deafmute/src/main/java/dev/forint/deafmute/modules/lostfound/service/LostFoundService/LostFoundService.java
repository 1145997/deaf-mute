package dev.forint.deafmute.modules.lostfound.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.lostfound.dto.AdminLostFoundQueryDTO;
import dev.forint.deafmute.modules.lostfound.dto.LostFoundAddDTO;
import dev.forint.deafmute.modules.lostfound.dto.LostFoundQueryDTO;
import dev.forint.deafmute.modules.lostfound.entity.LostFound;
import dev.forint.deafmute.modules.lostfound.vo.LostFoundDetailVO;
import dev.forint.deafmute.modules.lostfound.vo.LostFoundListVO;

public interface LostFoundService extends IService<LostFound> {

    void add(LostFoundAddDTO dto);

    Page<LostFoundListVO> getPage(LostFoundQueryDTO dto);

    LostFoundDetailVO getDetail(Long id);

    void approve(Long id);

    void reject(Long id, String auditReason);

    Page<LostFoundListVO> getMyPage(Integer pageNum, Integer pageSize);

    void deleteMyById(Long id);

    void updateMyById(Long id, LostFoundAddDTO dto);

    Page<LostFoundListVO> getAdminPage(AdminLostFoundQueryDTO dto);

    Page<LostFoundListVO> getPendingPage(Integer pageNum, Integer pageSize);

    void finishMyById(Long id);

    void reopenMyById(Long id);
}