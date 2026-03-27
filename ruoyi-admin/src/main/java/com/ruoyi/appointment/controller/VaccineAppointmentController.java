package com.ruoyi.appointment.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.vaccine.domain.VaccineInformation;
import com.ruoyi.vaccine.service.IVaccineInformationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.appointment.domain.VaccineAppointment;
import com.ruoyi.appointment.service.IVaccineAppointmentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import javax.validation.Valid;

/**
 * 疫苗预约Controller
 *
 * @author jzx
 * @date 2026-02-08
 */
@RestController
@RequestMapping("/vaccine/appointment")
public class VaccineAppointmentController extends BaseController
{
    @Autowired
    private IVaccineAppointmentService vaccineAppointmentService;

    @Autowired
    private IVaccineInformationService vaccineInformationService;

    /**
     * 查询疫苗预约列表（管理员用）- 使用关联查询
     */
    @GetMapping("/list")
    public TableDataInfo list(VaccineAppointment vaccineAppointment)
    {
        // 调试：查看接收到的参数
        System.out.println("========== 搜索参数 ==========");
        System.out.println("appointmentDate: " + vaccineAppointment.getAppointmentDate());
        System.out.println("status: " + vaccineAppointment.getStatus());
        System.out.println("vaccineId: " + vaccineAppointment.getVaccineId());
        System.out.println("userId: " + vaccineAppointment.getUserId());
        System.out.println("userName: " + vaccineAppointment.getUserName());
        System.out.println("vaccineName: " + vaccineAppointment.getVaccineName());
        System.out.println("manufacturer: " + vaccineAppointment.getManufacturer());

        startPage();
        List<VaccineAppointment> list = vaccineAppointmentService.selectVaccineAppointmentListWithInfo(vaccineAppointment);
        return getDataTable(list);
    }

    /**
     * 获取我的预约列表（用户端）
     */
    @GetMapping("/myList")
    public TableDataInfo myList(VaccineAppointment vaccineAppointment)
    {
        try {
            // 只查询当前用户的预约
            Long userId = SecurityUtils.getUserId();
            vaccineAppointment.setUserId(userId);

            startPage();
            List<VaccineAppointment> list = vaccineAppointmentService.selectMyAppointmentList(vaccineAppointment);

            // 为每个预约获取疫苗信息（如果需要补充信息）
            for (VaccineAppointment appointment : list) {
                if (appointment.getVaccineId() != null && appointment.getVaccineName() == null) {
                    try {
                        VaccineInformation vaccine = vaccineInformationService.selectVaccineInformationById(appointment.getVaccineId());
                        if (vaccine != null) {
                            appointment.setVaccineName(vaccine.getName());
                            appointment.setManufacturer(vaccine.getManufacturer());
                            appointment.setSuitableAge(vaccine.getSuitableAge());
                        }
                    } catch (Exception e) {
                        logger.warn("获取疫苗信息失败，疫苗ID: " + appointment.getVaccineId(), e);
                    }
                }
            }

            return getDataTable(list);
        } catch (Exception e) {
            logger.error("获取我的预约列表失败", e);
            return getDataTable(null);
        }
    }

    /**
     * 导出疫苗预约列表
     */
    @Log(title = "疫苗预约", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VaccineAppointment vaccineAppointment)
    {
        List<VaccineAppointment> list = vaccineAppointmentService.selectVaccineAppointmentListWithInfo(vaccineAppointment);
        ExcelUtil<VaccineAppointment> util = new ExcelUtil<VaccineAppointment>(VaccineAppointment.class);
        util.exportExcel(response, list, "疫苗预约数据");
    }

    /**
     * 获取疫苗预约详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        System.out.println("=== 查询预约详情，ID: " + id + " ===");

        VaccineAppointment appointment = vaccineAppointmentService.selectVaccineAppointmentById(id);

        // 转换时间段格式（如果需要）
        if (appointment != null && appointment.getTimeSlot() != null) {
            String timeSlot = appointment.getTimeSlot();
            if ("morning".equals(timeSlot)) {
                appointment.setTimeSlot("1");
            } else if ("afternoon".equals(timeSlot)) {
                appointment.setTimeSlot("2");
            } else if ("evening".equals(timeSlot)) {
                appointment.setTimeSlot("3");
            }
        }

        return success(appointment);
    }

    /**
     * 新增疫苗预约（用户提交预约）
     */
    @PostMapping
    public AjaxResult add(@RequestBody VaccineAppointment vaccineAppointment)
    {
        try {
            // 获取当前登录用户ID
            Long userId = SecurityUtils.getUserId();
            vaccineAppointment.setUserId(userId);

            // 设置默认状态为待确认
            vaccineAppointment.setStatus("0");

            // 设置创建时间
            vaccineAppointment.setCreateTime(new Date());
            vaccineAppointment.setCreateBy(SecurityUtils.getUsername());

            // 必填字段验证
            if (vaccineAppointment.getVaccineId() == null) {
                return AjaxResult.error("请选择疫苗");
            }
            if (vaccineAppointment.getAppointmentDate() == null) {
                return AjaxResult.error("请选择预约日期");
            }
            if (vaccineAppointment.getTimeSlot() == null) {
                return AjaxResult.error("请选择预约时段");
            }

            // 检查疫苗是否存在且可用
            VaccineInformation vaccine = vaccineInformationService.selectVaccineInformationById(vaccineAppointment.getVaccineId());
            if (vaccine == null) {
                return AjaxResult.error("疫苗不存在");
            }

            // 检查疫苗状态
            if (!"0".equals(vaccine.getStatus())) {
                return AjaxResult.error("该疫苗已停用");
            }

            // 检查库存
            if (vaccine.getStock() != null && vaccine.getStock() <= 0) {
                return AjaxResult.error("疫苗库存不足");
            }

            // 检查用户是否重复预约同一疫苗（未取消的）
            VaccineAppointment existCheck = new VaccineAppointment();
            existCheck.setUserId(userId);
            existCheck.setVaccineId(vaccineAppointment.getVaccineId());
            existCheck.setStatus("0"); // 待确认
            List<VaccineAppointment> existList = vaccineAppointmentService.selectVaccineAppointmentList(existCheck);

            if (existList.isEmpty()) {
                existCheck.setStatus("1"); // 已确认
                existList = vaccineAppointmentService.selectVaccineAppointmentList(existCheck);
            }

            if (!existList.isEmpty()) {
                return AjaxResult.error("您已预约过此疫苗，请勿重复预约");
            }

            // 检查用户当天是否有未取消的预约
            VaccineAppointment dayCheck = new VaccineAppointment();
            dayCheck.setUserId(userId);
            dayCheck.setAppointmentDate(vaccineAppointment.getAppointmentDate());
            List<VaccineAppointment> dayList = vaccineAppointmentService.selectVaccineAppointmentList(dayCheck);

            boolean hasActiveAppointment = dayList.stream()
                    .anyMatch(app -> !"3".equals(app.getStatus()));

            if (hasActiveAppointment) {
                return AjaxResult.error("您当天已有有效预约，不可重复预约");
            }

            // 检查所选时间段是否还有名额
            dayCheck.setUserId(null);
            dayCheck.setTimeSlot(vaccineAppointment.getTimeSlot());
            List<VaccineAppointment> slotList = vaccineAppointmentService.selectVaccineAppointmentList(dayCheck);
            long activeCount = slotList.stream()
                    .filter(app -> !"3".equals(app.getStatus()))
                    .count();

            int maxCapacity = 20; // 每个时间段最大容量
            if (activeCount >= maxCapacity) {
                return AjaxResult.error("该时间段已满，请选择其他时间段");
            }

            // 插入预约记录
            int result = vaccineAppointmentService.insertVaccineAppointment(vaccineAppointment);
            if (result > 0) {
                // 扣减库存
                if (vaccine.getStock() != null) {
                    vaccine.setStock(vaccine.getStock() - 1);
                    vaccineInformationService.updateVaccineInformation(vaccine);
                }
                return AjaxResult.success("预约成功，请等待管理员确认");
            } else {
                return AjaxResult.error("预约失败");
            }
        } catch (Exception e) {
            logger.error("预约失败", e);
            return AjaxResult.error("预约失败：" + e.getMessage());
        }
    }

    /**
     * 修改疫苗预约
     */
    @Log(title = "疫苗预约", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VaccineAppointment vaccineAppointment)
    {
        try {
            // 设置更新时间
            vaccineAppointment.setUpdateTime(new Date());
            vaccineAppointment.setUpdateBy(SecurityUtils.getUsername());

            int result = vaccineAppointmentService.updateVaccineAppointment(vaccineAppointment);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("更新预约失败", e);
            return AjaxResult.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 取消预约（用户端/管理员）
     */
    @PutMapping("/cancel/{appointmentId}")
    public AjaxResult cancel(@PathVariable Long appointmentId)
    {
        try {
            // 检查预约是否存在
            VaccineAppointment appointment = vaccineAppointmentService.selectVaccineAppointmentById(appointmentId);
            if (appointment == null) {
                return AjaxResult.error("预约不存在");
            }

            // 检查权限：如果是用户端，只能操作自己的预约
            Long userId = SecurityUtils.getUserId();
            if (!SecurityUtils.isAdmin(userId) && !appointment.getUserId().equals(userId)) {
                return AjaxResult.error("无权操作此预约");
            }

            // 只能取消待确认或已确认的预约
            if (!"0".equals(appointment.getStatus()) && !"1".equals(appointment.getStatus())) {
                return AjaxResult.error("当前状态不可取消");
            }

            // 更新状态为已取消
            appointment.setStatus("3");
            appointment.setUpdateTime(new Date());
            appointment.setUpdateBy(SecurityUtils.getUsername());

            int result = vaccineAppointmentService.updateVaccineAppointment(appointment);
            if (result > 0) {
                // 恢复疫苗库存
                try {
                    VaccineInformation vaccine = vaccineInformationService.selectVaccineInformationById(appointment.getVaccineId());
                    if (vaccine != null && vaccine.getStock() != null) {
                        vaccine.setStock(vaccine.getStock() + 1);
                        vaccineInformationService.updateVaccineInformation(vaccine);
                    }
                } catch (Exception e) {
                    logger.warn("恢复库存失败，预约ID: " + appointmentId, e);
                }
                return AjaxResult.success("取消成功");
            } else {
                return AjaxResult.error("取消失败");
            }
        } catch (Exception e) {
            logger.error("取消预约失败", e);
            return AjaxResult.error("取消失败：" + e.getMessage());
        }
    }

    /**
     * 删除疫苗预约（管理员用）
     */
    @Log(title = "疫苗预约", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        try {
            return toAjax(vaccineAppointmentService.deleteVaccineAppointmentByIds(ids));
        } catch (Exception e) {
            logger.error("删除预约失败", e);
            return AjaxResult.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/pendingCount")
    public AjaxResult getPendingCount() {
        // 记录日志
        logger.info("管理员查询待处理预约数量");
        try {
            // 调用Service层方法
            int count = vaccineAppointmentService.selectPendingCount();

            // 返回成功结果
            return success(count);
        } catch (Exception e) {
            // 如果出错，返回错误信息
            logger.error("查询待处理数量失败", e);
            return error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取指定日期的各时间段剩余名额
     */
    @GetMapping("/getTimeSlotRemaining")
    public AjaxResult getTimeSlotRemaining(String appointmentDate)
    {
        try {
            if (appointmentDate == null || appointmentDate.isEmpty()) {
                return AjaxResult.error("请选择日期");
            }

            System.out.println("查询日期: " + appointmentDate);

            // 将字符串日期转换为Date对象
            Date date = java.sql.Date.valueOf(appointmentDate);

            // 定义各时间段的最大容量
            int maxCapacity = 20; // 每个时间段最多20人

            // 查询各时间段的预约数量
            List<Map<String, Object>> result = new ArrayList<>();

            // 定义时间段
            String[] timeSlots = {"1", "2", "3"};
            String[] timeSlotNames = {"上午", "下午", "晚上"};
            String[] timeSlotFullNames = {"上午 09:00-11:00", "下午 14:00-16:00", "晚上 18:00-20:00"};

            for (int i = 0; i < timeSlots.length; i++) {
                String slot = timeSlots[i];

                // ⭐️ 关键修复：只查询指定日期的预约
                VaccineAppointment queryParam = new VaccineAppointment();
                queryParam.setAppointmentDate(date);
                queryParam.setTimeSlot(slot);

                // 获取该日期该时间段的所有预约
                List<VaccineAppointment> appointments = vaccineAppointmentService.selectVaccineAppointmentList(queryParam);

                System.out.println("时间段 " + slot + " 预约总数: " + appointments.size());

                // 过滤掉已取消的预约（状态为3）
                long bookedCount = appointments.stream()
                        .filter(a -> !"3".equals(a.getStatus())) // 排除已取消
                        .count();

                System.out.println("有效预约数: " + bookedCount);

                int remaining = maxCapacity - (int) bookedCount;

                Map<String, Object> slotInfo = new HashMap<>();
                slotInfo.put("value", slot);
                slotInfo.put("name", timeSlotNames[i]);
                slotInfo.put("fullName", timeSlotFullNames[i]);
                slotInfo.put("booked", bookedCount);
                slotInfo.put("remaining", Math.max(remaining, 0));
                slotInfo.put("maxCapacity", maxCapacity);
                slotInfo.put("available", remaining > 0);

                result.add(slotInfo);

                System.out.println("时间段 " + slot + " 剩余: " + Math.max(remaining, 0));
            }

            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("获取时间段剩余名额失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 确认预约（待确认 → 已确认）
     */
    @Log(title = "疫苗预约", businessType = BusinessType.UPDATE)
    @PutMapping("/confirm/{id}")
    public AjaxResult confirm(@PathVariable Long id)
    {
        try {
            int result = vaccineAppointmentService.confirmAppointment(id);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("确认预约失败", e);
            return AjaxResult.error("确认失败：" + e.getMessage());
        }
    }

    /**
     * 完成接种（已确认 → 已完成）
     */
    @Log(title = "疫苗预约", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{id}")
    public AjaxResult complete(@PathVariable Long id)
    {
        try {
            int result = vaccineAppointmentService.completeAppointment(id);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("完成接种失败", e);
            return AjaxResult.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户某疫苗的接种进度
     */
    @GetMapping("/myProgress")
    public AjaxResult getMyVaccineProgress(@RequestParam Long vaccineId)
    {
        try {
            Long userId = SecurityUtils.getUserId();

            // 获取用户已完成剂次数
            int completedDoses = vaccineAppointmentService.getUserCompletedDoses(userId, vaccineId);

            // 获取疫苗信息
            VaccineInformation vaccine = vaccineInformationService.selectVaccineInformationById(vaccineId);

            Map<String, Object> result = new HashMap<>();
            result.put("completedDoses", completedDoses);

            if (vaccine != null && vaccine.getIsMultiDose() != null && vaccine.getIsMultiDose() == 1) {
                int totalDoses = vaccine.getTotalDoses() != null ? vaccine.getTotalDoses() : 1;
                int nextDose = completedDoses + 1;

                if (nextDose <= totalDoses) {
                    result.put("nextDose", nextDose);

                    // 获取最新预约记录，计算最早可约日期
                    VaccineAppointment latest = vaccineAppointmentService.getUserLatestAppointment(userId, vaccineId);
                    if (latest != null && latest.getIsCompleted() == 1 && latest.getNextDoseDate() != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        result.put("earliestDate", sdf.format(latest.getNextDoseDate()));
                    }
                }
            }

            return success(result);
        } catch (Exception e) {
            logger.error("获取接种进度失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户待接种提醒列表
     */
    @GetMapping("/myPendingDoses")
    public AjaxResult getMyPendingDoses()
    {
        try {
            Long userId = SecurityUtils.getUserId();
            List<VaccineAppointment> list = vaccineAppointmentService.getUserPendingDoses(userId);
            return success(list);
        } catch (Exception e) {
            logger.error("获取待接种提醒失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取今日待接种提醒（管理员用）
     */
    @GetMapping("/todayReminders")
    public AjaxResult getTodayReminders()
    {
        try {
            List<Map<String, Object>> list = vaccineAppointmentService.getTodayReminders();
            return success(list);
        } catch (Exception e) {
            logger.error("获取今日提醒失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取疫苗消耗统计（用于仪表盘图表）
     */
    @GetMapping("/consumptionStats")
    public AjaxResult getConsumptionStats()
    {
        try {
            List<Map<String, Object>> list = vaccineAppointmentService.getVaccineConsumptionStats();
            return success(list);
        } catch (Exception e) {
            logger.error("获取消耗统计失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取接种覆盖率统计
     */
    @GetMapping("/coverageStats")
    public AjaxResult getCoverageStats()
    {
        try {
            List<Map<String, Object>> list = vaccineAppointmentService.getVaccinationCoverage();
            return success(list);
        } catch (Exception e) {
            logger.error("获取覆盖率统计失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取各剂次完成情况统计
     */
    @GetMapping("/doseStats/{vaccineId}")
    public AjaxResult getDoseStats(@PathVariable Long vaccineId)
    {
        try {
            List<Map<String, Object>> list = vaccineAppointmentService.getDoseCompletionStats(vaccineId);
            return success(list);
        } catch (Exception e) {
            logger.error("获取剂次统计失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取预约趋势（按日期统计，支持多剂次区分）
     */
    @GetMapping("/trend")
    public AjaxResult getAppointmentTrend(@RequestParam(required = false) String beginDate,
                                          @RequestParam(required = false) String endDate)
    {
        try {
            // 如果没有传日期，默认最近14天
            if (beginDate == null || endDate == null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                endDate = sdf.format(cal.getTime());
                cal.add(java.util.Calendar.DAY_OF_MONTH, -13);
                beginDate = sdf.format(cal.getTime());
            }

            List<Map<String, Object>> list = vaccineAppointmentService.getAppointmentTrendByDate(beginDate, endDate);
            return success(list);
        } catch (Exception e) {
            logger.error("获取预约趋势失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取疫苗总预约数
     */
    @GetMapping("/totalBooked/{vaccineId}")
    public AjaxResult getTotalBooked(@PathVariable Long vaccineId)
    {
        try {
            int count = vaccineAppointmentService.getTotalBookedByVaccine(vaccineId);
            return success(count);
        } catch (Exception e) {
            logger.error("获取总预约数失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }
}