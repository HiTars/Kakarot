package cn.org.tars.kakarot;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledUpdateResource
 *
 * @author zhumeng
 * @since 2016/12/18
 */

@Slf4j
public abstract class ScheduledUpdateResource<T> {

    protected T resource;
    private boolean firstFetch = false;

    /** 所有 ScheduledUpdateResource 共用的线程池 */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            5, (Runnable r) -> {
                Thread t = new Thread(r, "schedule-thread");
                t.setDaemon(true);
                return t;
            });


    public ScheduledUpdateResource(long interval, TimeUnit unit) {
        this(unit.toMillis(interval), true);
    }

    public ScheduledUpdateResource(long interval, TimeUnit unit, boolean isFirstRun) {
        this(unit.toMillis(interval), isFirstRun);
    }

    public ScheduledUpdateResource(long intervalInMillis, boolean isFirstRun) {
        /* first run */
        if(isFirstRun) {
            try {
                resource = fetch();
            } catch (Exception e) {
                log.warn("frist fetch failed", e);
            }
        }

        /* scheduled run */
        scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        resource = fetch();
                    } catch (Exception e) {
                        log.warn("scheduled fetch failed", e);
                    }
                }, intervalInMillis, intervalInMillis, TimeUnit.MILLISECONDS);
    }

    public T get() {
        if(resource == null && !firstFetch) {
            try {
                firstFetch = true;
                resource = fetch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resource;
    }

    public T refresh() throws Exception {
        resource = fetch();
        return resource;
    }

    protected abstract T fetch() throws Exception;
}
