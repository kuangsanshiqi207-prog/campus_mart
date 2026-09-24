package com.example.vo.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势数据")
public class TrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "日期列表")
    private List<LocalDate> dates;

    @Schema(description = "数值列表")
    private List<Long> values;
}