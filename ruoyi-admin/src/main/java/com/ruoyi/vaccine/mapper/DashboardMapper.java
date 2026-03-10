
package com.ruoyi.vaccine.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 首页统计Mapper接口
 */
public interface DashboardMapper {

    /**
     * 查询总预约数
     */
    int selectTotalAppointments();

    /**
     * 查询今日预约数
     */
    int selectTodayAppointments();

    /**
     * 根据状态查询数量
     */
    int selectCountByStatus(@Param("status") String status);

    /**
     * 查询疫苗种类数
     */
    int selectVaccineCount();

    /**
     * 查询状态分布
     */
    List<Map<String, Object>> selectStatusDistribution();

    /**
     * 根据日期查询预约数量
     */
    int selectCountByDate(@Param("dateStr") String dateStr);
    int selectSuccessCountByDate(@Param("dateStr") String dateStr);
    /**
     * 查询疫苗预约排行榜
     */
    List<Map<String, Object>> selectVaccineRanking();

    /**
     * 查询时间段分布
     */
    List<Map<String, Object>> selectTimeSlotDistribution();
}