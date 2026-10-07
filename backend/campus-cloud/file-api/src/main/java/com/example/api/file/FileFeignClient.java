package com.example.api.file;

import com.example.result.Result;
import com.example.vo.common.FileVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "file-service", path = "/internal/file")
public interface FileFeignClient {

    /**
     * 校验文件归属并返回文件信息
     */
    @GetMapping("/getOwnedByUser")
    Result<FileVO> getFileOwnedByUser(@RequestParam("fileId") Long fileId,
                                      @RequestParam("userId") Long userId);

    /**
     * 标记文件已使用
     */
    @PutMapping("/markUsed")
    Result<Void> markUsed(@RequestParam("fileId") Long fileId);
}