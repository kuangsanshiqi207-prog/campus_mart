package com.example.controller.admin;

import com.example.dto.announcementManage.AdminAnnouncementQueryDTO;
import com.example.dto.announcementManage.AnnouncementDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.announcementManage.AdminAnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-公告管理", description = "公告增删改查")
@RestController
@RequestMapping("/admin/announcements")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    @Operation(summary = "公告列表")
    @GetMapping
    public Result<PageResult> list(AdminAnnouncementQueryDTO query) {
        return Result.success(adminAnnouncementService.listAnnouncements(query));
    }

    @Operation(summary = "发布公告")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid AnnouncementDTO dto) {
        return Result.success(adminAnnouncementService.createAnnouncement(dto));
    }

    @Operation(summary = "修改公告")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @RequestBody @Valid AnnouncementDTO dto) {
        adminAnnouncementService.updateAnnouncement(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        adminAnnouncementService.deleteAnnouncement(id);
        return Result.success();
    }
}