package com.ruoyi.vaccine.service;

import java.util.List;
import java.util.Map;

/**
 * 首页统计Service接口
 */
public interface IDashboardService {

    /**
     * 获取总预约数
     */
    int getTotalAppointments();

    /**
     * 获取今日预约数
     */
    int getTodayAppointments();

    /**
     * 获取待处理预约数 (状态0)
     */
    int getPendingCount();

    /**
     * 获取已完成预约数 (状态2)
     */
    int getCompletedCount();

    /**
     * 获取疫苗种类数
     */
    int getVaccineCount();

    /**
     * 获取预约状态分布
     * @return [{name: '待确认', value: 5}, {name: '已确认', value: 10}, ...]
     */
    List<Map<String, Object>> getStatusDistribution();

    /**
     * 获取近7天日期列表
     */
    List<String> get14DaysRange();

    /**
     * 获取14天预约数量（前7天+后7天）
     */
    List<Integer> get14DaysCount();

    /**
     * 获取近30天日期列表
     */
    List<String> getLast30Days();

    /**
     * 获取近30天预约数量
     */
    List<Integer> getLast30DaysCount();

    /**
     * 获取疫苗预约排行榜
     * @return [{name: '乙肝疫苗', count: 25}, {name: '流感疫苗', count: 20}, ...]
     */
    List<Map<String, Object>> getVaccineRanking();

    /**
     * 获取时间段分布
     * @return [{name: '上午', value: 45}, {name: '下午', value: 35}, {name: '晚上', value: 20}]
     */
    List<Map<String, Object>> getTimeSlotDistribution();
}