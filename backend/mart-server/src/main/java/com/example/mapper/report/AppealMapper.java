package com.example.mapper.report;

import com.example.entity.Appeal;
import com.example.vo.report.AppealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppealMapper {

    Appeal getById(Long id);

    Appeal getByReportId(Long reportId);

    void insert(Appeal appeal);

    AppealVO getAppealVOById(Long id);

    List<AppealVO> listByReportId(Long reportId);
}