package dev.forint.deafmute.modules.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.notice.dto.NoticeAddDTO;
import dev.forint.deafmute.modules.notice.entity.Notice;
import dev.forint.deafmute.modules.notice.mapper.NoticeMapper;
import dev.forint.deafmute.modules.notice.service.NoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public void add(NoticeAddDTO dto) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        notice.setPublishAdminId(1L);
        this.save(notice);
    }

    @Override
    public void updateNotice(Long id, NoticeAddDTO dto) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        BeanUtils.copyProperties(dto, notice);
        this.updateById(notice);
    }

    @Override
    public void deleteNotice(Long id) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new RuntimeException("公告不存在");
        }
        this.removeById(id);
    }

    @Override
    public List<Notice> getPublicList() {
        return this.list(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getCreateTime));
    }

    @Override
    public Notice getPublicDetail(Long id) {
        Notice notice = this.getById(id);
        if (notice == null || notice.getStatus() == null || notice.getStatus() != 1) {
            throw new RuntimeException("公告不存在");
        }
        return notice;
    }

    @Override
    public Page<Notice> getAdminPage(Integer pageNum, Integer pageSize) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        return this.page(page, new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getCreateTime));
    }
}