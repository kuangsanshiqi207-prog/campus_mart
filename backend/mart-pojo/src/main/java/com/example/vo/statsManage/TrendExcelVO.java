package com.example.vo.statsManage;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendExcelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("项目")
    private String label;

    @ExcelProperty("数值")
    private Object value;
}