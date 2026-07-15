package com.medireserve.common.constant;

/**
 * 缓存Key常量
 * 统一管理所有缓存Key的前缀和格式，便于维护和避免冲突
 */
public class CacheKeyConstants {

    // ==================== 缓存前缀 ====================

    /**
     * 科室列表缓存前缀
     * 值类型：List<DepartmentVO>
     */
    public static final String DEPARTMENTS = "cache:departments";

    /**
     * 职称列表缓存前缀
     * 值类型：List<Title>
     */
    public static final String TITLES = "cache:titles";

    /**
     * 医生列表（分页）缓存前缀
     * 格式：cache:doctors:{department}:{keyword}:{page}:{size}
     * 值类型：PageInfo<DoctorListVO>（序列化为JSON）
     */
    public static final String DOCTORS = "cache:doctors";

    /**
     * 排班日历缓存前缀
     * 格式：cache:schedules:{doctorId}:{date}
     * 值类型：List<ScheduleCalendarVO>
     */
    public static final String SCHEDULES = "cache:schedules";

    /**
     * 排班详情缓存前缀
     * 格式：cache:schedule:{scheduleId}
     * 值类型：Schedule
     */
    public static final String SCHEDULE_DETAIL = "cache:schedule";

    /**
     * 医生信息缓存前缀
     * 格式：cache:doctor:{doctorId}
     * 值类型：Doctor
     */
    public static final String DOCTOR_INFO = "cache:doctor";

    // ==================== 缓存Key构建方法 ====================

    /**
     * 构建医生列表缓存Key
     */
    public static String buildDoctorsKey(String department, String keyword, int page, int size) {
        String dept = department == null ? "all" : department;
        String kw = keyword == null || keyword.isEmpty() ? "none" : keyword;
        return DOCTORS + ":" + dept + ":" + kw + ":" + page + ":" + size;
    }

    /**
     * 构建排班日历缓存Key
     */
    public static String buildSchedulesKey(Long doctorId, String date) {
        return SCHEDULES + ":" + doctorId + ":" + date;
    }

    /**
     * 构建排班详情缓存Key
     */
    public static String buildScheduleKey(Long scheduleId) {
        return SCHEDULE_DETAIL + ":" + scheduleId;
    }

    /**
     * 构建医生信息缓存Key
     */
    public static String buildDoctorKey(Long doctorId) {
        return DOCTOR_INFO + ":" + doctorId;
    }

    /**
     * 获取所有医生列表缓存Key的匹配模式（用于批量清除）
     */
    public static String getDoctorsPattern() {
        return DOCTORS + ":*";
    }

    /**
     * 获取某个医生所有排班缓存Key的匹配模式
     */
    public static String getSchedulesPattern(Long doctorId) {
        return SCHEDULES + ":" + doctorId + ":*";
    }
}