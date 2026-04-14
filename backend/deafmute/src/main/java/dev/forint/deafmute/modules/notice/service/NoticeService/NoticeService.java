package dev.forint.deafmute.modules.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.notice.dto.NoticeAddDTO;
import dev.forint.deafmute.modules.notice.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {

    void add(NoticeAddDTO dto);

    void updateNotice(Long id, NoticeAddDTO dto);

    void deleteNotice(Long id);

    List<Notice> getPublicList();

    Notice getPublicDetail(Long id);

    Page<Notice> getAdminPage(Integer pageNum, Integer pageSize);
}