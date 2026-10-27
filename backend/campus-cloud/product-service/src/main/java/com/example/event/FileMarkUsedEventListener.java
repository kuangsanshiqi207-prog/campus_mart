package com.example.event;

import com.example.api.file.FileFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 事务提交后异步标记文件已使用（远程副作用，失败仅告警，不影响主流程）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileMarkUsedEventListener {

    private final FileFeignClient fileFeignClient;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileMarkUsed(FileMarkUsedEvent event) {
        for (Long fileId : event.getFileIds()) {
            try {
                fileFeignClient.markUsed(fileId);
            } catch (Exception e) {
                log.warn("标记文件已使用失败 fileId={}：{}", fileId, e.getMessage());
            }
        }
    }
}
