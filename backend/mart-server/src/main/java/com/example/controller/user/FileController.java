package com.example.controller.user;

import com.example.result.Result;
import com.example.service.common.FileService;
import com.example.vo.common.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户端-文件", description = "文件上传")
@RestController
@RequestMapping("/user/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件", description = "支持 jpg/jpeg/png/gif/webp，最大 5MB")
    @PostMapping("/upload")
    public Result<FileVO> upload(
            @Parameter(description = "文件")
            @RequestParam("file") MultipartFile file) {
        return Result.success(fileService.upload(file));
    }
}