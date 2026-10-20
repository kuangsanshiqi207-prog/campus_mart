package com.example.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 商品发布/修改提交后，标记文件已使用的事件。
 */
@Data
@AllArgsConstructor
public class FileMarkUsedEvent {

    private List<Long> fileIds;
}
