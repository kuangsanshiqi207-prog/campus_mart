package com.example.service.announcementManage.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.MessageConstant;
import com.example.context.BaseContext;
import com.example.dto.announcementManage.AdminAnnouncementQueryDTO;
import com.example.dto.announcementManage.AnnouncementDTO;
import com.example.entity.Announcement;
import com.example.exception.BaseException;
import com.example.mapper.message.AnnouncementMapper;
import com.example.result.PageResult;
import com.example.service.announcementManage.AdminAnnouncementService;
import com.example.vo.message.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private final AnnouncementMapper announcementMapper;

    // ==================== 公告列表 ====================

    @Override
    public PageResult listAnnouncements(AdminAnnouncementQueryDTO query) {
        query.setPageNum(normalizePageNum(query.getPageNum()));
        query.setPageSize(normalizePageSize(query.getPageSize()));

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        Long total = announcementMapper.countAdmin(query);
        List<Announcement> list = announcementMapper.listAdmin(query, offset, query.getPageSize());

        List<AnnouncementVO> voList = list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    // ==================== 发布公告 ====================

    @Override
    @Transactional
    public Long createAnnouncement(AnnouncementDTO dto) {
        validateStatus(dto.getStatus());

        Long adminId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();

        Announcement announcement = Announcement.builder()
                .id(IdUtil.getSnowflakeNextId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(dto.getStatus() != null ? dto.getStatus() : "published")
                .adminId(adminId)
                .createTime(now)
                .updateTime(now)
                .deleted(0)
                .build();

        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    // ==================== 修改公告 ====================

    @Override
    @Transactional
    public void updateAnnouncement(Long id, AnnouncementDTO dto) {
        Announcement exist = announcementMapper.getById(id);
        if (exist == null) {
            throw new BaseException(MessageConstant.ANNOUNCEMENT_NOT_FOUND);
        }

        if (dto.getStatus() != null) {
            validateStatus(dto.getStatus());
        }

        Announcement update = Announcement.builder()
                .id(id)
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(dto.getStatus())
                .build();

        announcementMapper.updateById(update);
    }


    // ==================== 删除公告 ====================

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement exist = announcementMapper.getById(id);
        if (exist == null) {
            throw new BaseException(MessageConstant.ANNOUNCEMENT_NOT_FOUND);
        }

        announcementMapper.deleteById(id);
    }

    // ==================== 私有方法 ====================

    private AnnouncementVO toVO(Announcement a) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(a, vo);
        return vo;
    }

    private void validateStatus(String status) {
        if (status != null
                && !"draft".equals(status)
                && !"published".equals(status)) {
            throw new BaseException(MessageConstant.ANNOUNCEMENT_STATUS_INVALID);
        }
    }

    private Integer normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) return 1;
        return pageNum;
    }

    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1 || pageSize > 100) return 10;
        return pageSize;
    }
}