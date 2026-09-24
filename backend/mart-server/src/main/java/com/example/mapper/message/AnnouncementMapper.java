package com.example.mapper.message;

import com.example.dto.announcementManage.AdminAnnouncementQueryDTO;
import com.example.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    Announcement getById(Long id);

    List<Announcement> listPublished(@Param("offset") Integer offset,
                                     @Param("limit") Integer limit);

    Long countPublished();

    List<Announcement> listAdmin(@Param("query") AdminAnnouncementQueryDTO query,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    Long countAdmin(@Param("query") AdminAnnouncementQueryDTO query);

    void updateById(Announcement announcement);

    void deleteById(@Param("id") Long id);

    void insert(Announcement announcement);

}