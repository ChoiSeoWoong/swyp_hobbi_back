package swyp.hobbi.swyphobbiback.common.exception;

import lombok.extern.slf4j.Slf4j;
import swyp.hobbi.swyphobbiback.common.error.ErrorCode;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 500;

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.warn("작업이 거부되었습니다. 재시도 시도 중...");
        log.warn("현재 작업 수 : {}, 큐의 크기 : {}", executor.getActiveCount(), executor.getQueue().size());

        boolean submitted = false;
        int attempt = 0;

        while(attempt < MAX_RETRY_COUNT && !executor.isShutdown()) {
            attempt++;

            try {
                Thread.sleep(RETRY_DELAY_MS);
                executor.execute(r);
                submitted = true;
                log.info("작업 재시도 성공! {}번째 시도", attempt);
                break;
            } catch (RejectedExecutionException e) {
                log.warn("작업 재시도 실패! {}번째 시도", attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if(!submitted) {
            log.error("최대 재시도 횟수({}) 초과, 작업을 수행하지 못했습니다.", attempt);
            throw new CustomException(ErrorCode.REJECTED_BY_THREAD_POOL);
        }
    }
}
