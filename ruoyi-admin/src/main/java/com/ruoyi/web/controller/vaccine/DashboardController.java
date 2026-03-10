package com.ruoyi.web.controller.vaccine;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.vaccine.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页统计Controller
 * 提供管理员首页的所有统计数据接口
 */
@RestController
@RequestMapping("/vaccine/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private IDashboardService dashboardService;

    /**
     * 获取首页统计数据
     * @return {
     *   totalAppointments: 总预约数,
     *   todayAppointments: 今日预约数,
     *   pendingCount: 待处理预约数,
     *   completedCount: 已完成预约数,
     *   vaccineCount: 疫苗种类数,
     *   statusData: [{name: '待确认', value: 5}, ...]
     * }
     */
    @GetMapping("/stats")
    public AjaxResult getStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 总预约数
        result.put("totalAppointments", dashboardService.getTotalAppointments());

        // 2. 今日预约数
        result.put("todayAppointments", dashboardService.getTodayAppointments());

        // 3. 待处理预约数 (状态为0)
        result.put("pendingCount", dashboardService.getPendingCount());

        // 4. 已完成预约数 (状态为2)
        result.put("completedCount", dashboardService.getCompletedCount());

        // 5. 疫苗种类数
        result.put("vaccineCount", dashboardService.getVaccineCount());

        // 6. 预约状态分布数据（用于饼图）
        result.put("statusData", dashboardService.getStatusDistribution());

        return success(result);
    }

    /**
     * 获取预约趋势数据
     * @param type week-本周 month-本月
     * @return {
     *   dates: ['01-20', '01-21', ...],
     *   counts: [5, 8, 12, ...]
     * }
     */
    @GetMapping("/trend")
    public AjaxResult getTrendData(@RequestParam(defaultValue = "week") String type) {
        Map<String, Object> result = new HashMap<>();

        if ("week".equals(type)) {
            // 近7天趋势
            result.put("dates", dashboardService.get14DaysRange());
            result.put("counts", dashboardService.get14DaysCount());
        } else {
            // 近30天趋势
            result.put("dates", dashboardService.getLast30Days());
            result.put("counts", dashboardService.getLast30DaysCount());
        }

        return success(result);
    }

    /**
     * 获取疫苗预约排行榜
     * @return {
     *   vaccineRank: [{name: '乙肝疫苗', count: 25}, ...],
     *   timeSlotData: [{name: '上午', value: 45}, {name: '下午', value: 35}, {name: '晚上', value: 20}]
     * }
     */
    @GetMapping("/ranking")
    public AjaxResult getRanking() {
        Map<String, Object> result = new HashMap<>();

        // 1. 疫苗预约排行榜
        result.put("vaccineRank", dashboardService.getVaccineRanking());

        // 2. 时间段分布
        result.put("timeSlotData", dashboardService.getTimeSlotDistribution());

        return success(result);
    }
}