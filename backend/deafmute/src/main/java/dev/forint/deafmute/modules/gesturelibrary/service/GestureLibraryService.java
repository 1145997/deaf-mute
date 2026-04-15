package dev.forint.deafmute.modules.gesturelibrary.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.gesturelibrary.dto.AdminGestureLibraryQueryDTO;
import dev.forint.deafmute.modules.gesturelibrary.dto.GestureLibraryAddDTO;
import dev.forint.deafmute.modules.gesturelibrary.entity.GestureLibrary;

import java.util.List;

public interface GestureLibraryService extends IService<GestureLibrary> {

    List<GestureLibrary> getEnabledList();

    Page<GestureLibrary> getAdminPage(AdminGestureLibraryQueryDTO dto);

    GestureLibrary getDetail(Long id);

    void add(GestureLibraryAddDTO dto);

    void updateGesture(Long id, GestureLibraryAddDTO dto);

    void updateStatus(Long id, Integer status);
}
