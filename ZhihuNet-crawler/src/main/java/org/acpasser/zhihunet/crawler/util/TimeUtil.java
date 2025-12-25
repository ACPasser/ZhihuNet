package org.acpasser.zhihunet.crawler.util;

import org.acpasser.zhihunet.common.exception.BusinessException;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimeUtil {
    private static final long MAX_ALLOWED_SECONDS = TimeUnit.DAYS.toSeconds(365); // 最多1年
    private static final long MIN_ALLOWED_SECONDS = 60L; // 最少1分钟
    private static final String ERROR_TIME_TOO_LONG = "时间范围过长: %d秒，最大允许: %d秒";
    private static final String ERROR_TIME_TOO_SHORT = "时间范围过短: %d秒，最小允许: %d秒";

    /**
     * 判断「基准时间 + 偏移秒数」是否早于当前系统时间
     * @param baseTime 基准时间
     * @param seconds  偏移秒数
     * @return true=目标时间（baseTime+seconds）早于现在；false=目标时间晚于/等于现在
     */
    public static boolean isBeforeNow(Date baseTime, long seconds) {
        Instant baseInstant = baseTime.toInstant();
        Instant targetInstant = baseInstant.plusSeconds(seconds);
        return targetInstant.isBefore(Instant.now());
    }

    /**
     * 时间参数校验
     */
    public static void validateTimeParameters(long duration, TimeUnit unit) {
        // null检查
        Objects.requireNonNull(unit, "时间单位不能为null");

        // 正值检查
        if (duration <= 0) {
            throw new BusinessException(String.format("时间范围必须大于0: %d", duration));
        }

        // 单位特定校验
        switch (unit) {
            case NANOSECONDS:
            case MICROSECONDS:
            case MILLISECONDS:
                throw new BusinessException("不支持纳秒、微秒、毫秒级别的时间单位");
            case DAYS:
                if (duration > 365) {
                    throw new IllegalArgumentException("时间范围不能超过1年");
                }
                break;
        }

        // 转换为秒并检查范围
        long seconds;
        try {
            seconds = unit.toSeconds(duration);
        } catch (ArithmeticException e) {
            throw new BusinessException("时间间隔计算溢出: " + duration + " " + unit, e);
        }

        if (seconds > MAX_ALLOWED_SECONDS) {
            // Java 14+
            throw new BusinessException(ERROR_TIME_TOO_LONG.formatted(seconds,  MAX_ALLOWED_SECONDS));
        }

        if (seconds < MIN_ALLOWED_SECONDS) {
            throw new BusinessException(ERROR_TIME_TOO_SHORT.formatted(seconds, MIN_ALLOWED_SECONDS));
        }
    }
}
