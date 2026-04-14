package dev.forint.deafmute.modules.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.forint.deafmute.modules.lostfound.dto.LostFoundAddDTO;
import dev.forint.deafmute.modules.lostfound.dto.LostFoundQueryDTO;
import dev.forint.deafmute.modules.lostfound.entity.LostFound;
import dev.forint.deafmute.modules.lostfound.mapper.LostFoundMapper;
import dev.forint.deafmute.modules.lostfound.service.LostFoundService;
import dev.forint.deafmute.modules.lostfound.vo.LostFoundListVO;
import dev.forint.deafmute.modules.lostfound.vo.LostFoundDetailVO;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import dev.forint.deafmute.modules.lostfound.dto.AdminLostFoundQueryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {
    private final UserTokenUtils userTokenUtils;
    @Override
    public void add(LostFoundAddDTO dto) {
        LostFound lostFound = new LostFound();
        BeanUtils.copyProperties(dto, lostFound);
        lostFound.setUserId(userTokenUtils.getCurrentUserId());
        lostFound.setStatus(0);
        lostFound.setViewCount(0);
        this.save(lostFound);
    }

    @Override
    public Page<LostFoundListVO> getPage(LostFoundQueryDTO dto) {
        Page<LostFound> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(dto.getType() != null, LostFound::getType, dto.getType())
                .eq(dto.getStatus() != null, LostFound::getStatus, dto.getStatus())
                .and(StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(LostFound::getTitle, dto.getKeyword())
                        .or()
                        .like(LostFound::getItemName, dto.getKeyword())
                        .or()
                        .like(LostFound::getDescription, dto.getKeyword())
                )
                .orderByDesc(LostFound::getCreateTime);

        Page<LostFound> entityPage = this.page(page, wrapper);

        Page<LostFoundListVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());

        voPage.setRecords(entityPage.getRecords().stream().map(item -> {
            LostFoundListVO vo = new LostFoundListVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    public LostFoundDetailVO getDetail(Long id) {
        LostFound lostFound = this.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("信息不存在");
        }

        lostFound.setViewCount((lostFound.getViewCount() == null ? 0 : lostFound.getViewCount()) + 1);
        this.updateById(lostFound);

        LostFoundDetailVO vo = new LostFoundDetailVO();
        BeanUtils.copyProperties(lostFound, vo);
        return vo;
    }

    @Override
    public void approve(Long id) {
        LostFound lostFound = this.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("信息不存在");
        }

        lostFound.setStatus(1);
        lostFound.setAuditReason(null);
        lostFound.setAuditAdminId(1L);
        lostFound.setAuditTime(java.time.LocalDateTime.now());

        this.updateById(lostFound);
    }

    @Override
    public void reject(Long id, String auditReason) {
        LostFound lostFound = this.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("信息不存在");
        }

        lostFound.setStatus(3);
        lostFound.setAuditReason(auditReason);
        lostFound.setAuditAdminId(1L);
        lostFound.setAuditTime(java.time.LocalDateTime.now());

        this.updateById(lostFound);
    }

    @Override
    public Page<LostFoundListVO> getMyPage(Integer pageNum, Integer pageSize) {
        Long currentUserId = userTokenUtils.getCurrentUserId();

        Page<LostFound> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<LostFound>()
                .eq(LostFound::getUserId, currentUserId)
                .orderByDesc(LostFound::getCreateTime);

        Page<LostFound> entityPage = this.page(page, wrapper);

        Page<LostFoundListVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());

        voPage.setRecords(entityPage.getRecords().stream().map(item -> {
            LostFoundListVO vo = new LostFoundListVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList());

        return voPage;
    }
    @Override
    public void deleteMyById(Long id) {
        Long currentUserId = userTokenUtils.getCurrentUserId();

        LostFound lostFound = this.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("信息不存在");
        }

        if (!currentUserId.equals(lostFound.getUserId())) {
            throw new RuntimeException("无权删除他人发布的信息");
        }

        this.removeById(id);
    }

    @Override
    public void updateMyById(Long id, LostFoundAddDTO dto) {
        Long currentUserId = userTokenUtils.getCurrentUserId();

        LostFound lostFound = this.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("信息不存在");
        }

        if (!currentUserId.equals(lostFound.getUserId())) {
            throw new RuntimeException("无权修改他人发布的信息");
        }

        BeanUtils.copyProperties(dto, lostFound);

        // 用户修改后重新进入待审核
        lostFound.setStatus(0);
        lostFound.setAuditReason(null);
        lostFound.setAuditAdminId(null);
        lostFound.setAuditTime(null);
        lostFound.setFinishTime(null);

        this.updateById(lostFound);
    }

    @Override
    public Page<LostFoundListVO> getAdminPage(AdminLostFoundQueryDTO dto) {
        Page<LostFound> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(dto.getType() != null, LostFound::getType, dto.getType())
                .eq(dto.getStatus() != null, LostFound::getStatus, dto.getStatus())
                .and(org.springframework.util.StringUtils.hasText(dto.getKeyword()), w -> w
                        .like(LostFound::getTitle, dto.getKeyword())
                        .or()
                        .like(LostFound::getItemName, dto.getKeyword())
                        .or()
                        .like(LostFound::getDescription, dto.getKeyword())
                        .or()
                        .like(LostFound::getContactName, dto.getKeyword())
                )
                .orderByDesc(LostFound::getCreateTime);

        Page<LostFound> entityPage = this.page(page, wrapper);

        Page<LostFoundListVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(item -> {
            LostFoundListVO vo = new LostFoundListVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    public Page<LostFoundListVO> getPendingPage(Integer pageNum, Integer pageSize) {
        Page<LostFound> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<LostFound>()
                .eq(LostFound::getStatus, 0)
                .orderByDesc(LostFound::getCreateTime);

        Page<LostFound> entityPage = this.page(page, wrapper);

        Page<LostFoundListVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(item -> {
            LostFoundListVO vo = new LostFoundListVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    public void finishMyById(Long id) {
        Long currentUserId = userTokenUtils.getCurrentUserId();

        LostFound lostFound = this.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("信息不存在");
        }

        if (!currentUserId.equals(lostFound.getUserId())) {
            throw new RuntimeException("无权操作他人发布的信息");
        }

        if (lostFound.getStatus() == null || lostFound.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许完结，只有已发布的信息才能标记为已完成");
        }

        lostFound.setStatus(2);
        lostFound.setFinishTime(java.time.LocalDateTime.now());

        this.updateById(lostFound);
    }

    @Override
    public void reopenMyById(Long id) {
        Long currentUserId = userTokenUtils.getCurrentUserId();

        LostFound lostFound = this.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("信息不存在");
        }

        if (!currentUserId.equals(lostFound.getUserId())) {
            throw new RuntimeException("无权操作他人发布的信息");
        }

        if (lostFound.getStatus() == null || lostFound.getStatus() != 2) {
            throw new RuntimeException("当前状态不允许恢复，只有已完成的信息才能恢复");
        }

        lostFound.setStatus(1);
        lostFound.setFinishTime(null);

        this.updateById(lostFound);
    }
}