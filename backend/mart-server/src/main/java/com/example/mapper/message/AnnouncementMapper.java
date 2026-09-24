package com.example.mapper.message;

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
}