package com.example.service.announcementManage;

import com.example.dto.announcementManage.AdminAnnouncementQueryDTO;
import com.example.dto.announcementManage.AnnouncementDTO;
import com.example.result.PageResult;

public interface AdminAnnouncementService {

    PageResult listAnnouncements(AdminAnnouncementQueryDTO query);

    Long createAnnouncement(AnnouncementDTO dto);

    void updateAnnouncement(Long id, AnnouncementDTO dto);

    void deleteAnnouncement(Long id);
}