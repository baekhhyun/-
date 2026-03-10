
package com.ruoyi.vaccine.service.impl;

import com.ruoyi.vaccine.mapper.DashboardMapper;
import com.ruoyi.vaccine.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页统计Service实现类
 */
@Service
public class DashboardServiceImpl implements IDashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public int getTotalAppointments() {
        return dashboardMapper.selectTotalAppointments();
    }

    @Override
    public int getTodayAppointments() {
        return dashboardMapper.selectTodayAppointments();
    }

    @Override
    public int getPendingCount() {
        return dashboardMapper.selectCountByStatus("0");
    }

    @Override
    public int getCompletedCount() {
        return dashboardMapper.selectCountByStatus("2");
    }

    @Override
    public int getVaccineCount() {
        return dashboardMapper.selectVaccineCount();
    }

    @Override
    public List<Map<String, Object>> getStatusDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询各状态数量
        List<Map<String, Object>> dbData = dashboardMapper.selectStatusDistribution();

        // 状态名称映射
        Map<String, String> statusNameMap = new HashMap<>();
        statusNameMap.put("0", "待确认");
        statusNameMap.put("1", "已确认");
        statusNameMap.put("2", "已完成");
        statusNameMap.put("3", "已取消");

        // 转换为前端需要的格式
        for (Map<String, Object> item : dbData) {
            String status = (String) item.get("status");
            Long count = (Long) item.get("count");

            Map<String, Object> data = new HashMap<>();
            data.put("name", statusNameMap.getOrDefault(status, "未知"));
            data.put("value", count);

            result.add(data);
        }

        return result;
    }

    @Override
    public List<String> get14DaysRange() {
        List<String> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar cal = Calendar.getInstance();

        // 前7天
        for (int i = 7; i >= 1; i--) {
            cal.add(Calendar.DAY_OF_YEAR, -i);
            result.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, i);
        }

        // 今天
        result.add(sdf.format(cal.getTime()));

        // 后7天
        for (int i = 1; i <= 7; i++) {
            cal.add(Calendar.DAY_OF_YEAR, i);
            result.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, -i);
        }

        return result;
    }

    @Override
    public List<Integer> get14DaysCount() {
        List<Integer> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        // 前7天
        for (int i = 7; i >= 1; i--) {
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateStr = sdf.format(cal.getTime());
            result.add(dashboardMapper.selectSuccessCountByDate(dateStr));
            cal.add(Calendar.DAY_OF_YEAR, i);
        }

        // 今天
        String todayStr = sdf.format(cal.getTime());
        result.add(dashboardMapper.selectSuccessCountByDate(todayStr));

        // 后7天
        for (int i = 1; i <= 7; i++) {
            cal.add(Calendar.DAY_OF_YEAR, i);
            String dateStr = sdf.format(cal.getTime());
            result.add(dashboardMapper.selectSuccessCountByDate(dateStr));
            cal.add(Calendar.DAY_OF_YEAR, -i);
        }

        // 调试日志
        System.out.println("14天日期: " + result);

        return result;
    }

    @Override
    public List<String> getLast30Days() {
        List<String> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar cal = Calendar.getInstance();

        for (int i = 29; i >= 0; i--) {
            cal.add(Calendar.DAY_OF_YEAR, -i);
            result.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, i);
        }

        return result;
    }

    @Override
    public List<Integer> getLast30DaysCount() {
        List<Integer> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();

        for (int i = 29; i >= 0; i--) {
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateStr = sdf.format(cal.getTime());

            // 只统计成功预约的（已确认+已完成）
            int count = dashboardMapper.selectSuccessCountByDate(dateStr);

            result.add(count);
            cal.add(Calendar.DAY_OF_YEAR, i);
        }

        return result;
    }
    @Override
    public List<Map<String, Object>> getVaccineRanking() {
        return dashboardMapper.selectVaccineRanking();
    }

    @Override
    public List<Map<String, Object>> getTimeSlotDistribution() {
        List<Map<String, Object>> dbData = dashboardMapper.selectTimeSlotDistribution();

        // 时间段名称映射
        Map<String, String> slotNameMap = new HashMap<>();
        slotNameMap.put("1", "上午");
        slotNameMap.put("2", "下午");
        slotNameMap.put("3", "晚上");

        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : dbData) {
            String slot = (String) item.get("time_slot");
            Long count = (Long) item.get("count");

            Map<String, Object> data = new HashMap<>();
            data.put("name", slotNameMap.getOrDefault(slot, slot));
            data.put("value", count);

            result.add(data);
        }

        return result;
    }
}
