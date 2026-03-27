package com.ruoyi.appointment.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.appointment.mapper.VaccineAppointmentMapper;
import com.ruoyi.appointment.domain.VaccineAppointment;
import com.ruoyi.appointment.service.IVaccineAppointmentService;
import com.ruoyi.vaccine.domain.VaccineInformation;
import com.ruoyi.vaccine.mapper.VaccineInformationMapper;

/**
 * 疫苗预约Service业务层处理
 * @date 2026-02-08
 */
@Service
public class VaccineAppointmentServiceImpl implements IVaccineAppointmentService
{
    @Autowired
    private VaccineAppointmentMapper vaccineAppointmentMapper;

    @Autowired
    private VaccineInformationMapper vaccineInformationMapper;

    /**
     * 查询疫苗预约
     *
     * @param id 疫苗预约主键
     * @return 疫苗预约
     */
    @Override
    public VaccineAppointment selectVaccineAppointmentById(Long id)
    {
        return vaccineAppointmentMapper.selectVaccineAppointmentById(id);
    }

    /**
     * 查询疫苗预约列表
     *
     * @param vaccineAppointment 疫苗预约
     * @return 疫苗预约集合
     */
    @Override
    public List<VaccineAppointment> selectVaccineAppointmentList(VaccineAppointment vaccineAppointment)
    {
        return vaccineAppointmentMapper.selectVaccineAppointmentList(vaccineAppointment);
    }

    /**
     * 查询疫苗预约列表（关联用户和疫苗信息）
     *
     * @param vaccineAppointment 疫苗预约
     * @return 疫苗预约集合
     */
    @Override
    public List<VaccineAppointment> selectVaccineAppointmentListWithInfo(VaccineAppointment vaccineAppointment)
    {
        return vaccineAppointmentMapper.selectVaccineAppointmentListWithInfo(vaccineAppointment);
    }

    /**
     * 查询我的预约列表
     *
     * @param vaccineAppointment 疫苗预约
     * @return 预约集合
     */
    @Override
    public List<VaccineAppointment> selectMyAppointmentList(VaccineAppointment vaccineAppointment)
    {
        return vaccineAppointmentMapper.selectMyAppointmentList(vaccineAppointment);
    }

    /**
     * 新增疫苗预约（支持多剂次疫苗）
     *
     * @param vaccineAppointment 疫苗预约
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertVaccineAppointment(VaccineAppointment vaccineAppointment)
    {
        // 1. 基础校验
        if (vaccineAppointment.getUserId() == null) {
            throw new ServiceException("用户信息不能为空");
        }
        if (vaccineAppointment.getVaccineId() == null) {
            throw new ServiceException("疫苗信息不能为空");
        }
        if (vaccineAppointment.getAppointmentDate() == null) {
            throw new ServiceException("预约日期不能为空");
        }
        if (vaccineAppointment.getTimeSlot() == null || vaccineAppointment.getTimeSlot().isEmpty()) {
            throw new ServiceException("预约时间段不能为空");
        }

        // 2. 获取疫苗信息
        VaccineInformation vaccine = vaccineInformationMapper.selectVaccineInformationById(vaccineAppointment.getVaccineId());
        if (vaccine == null) {
            throw new ServiceException("疫苗不存在");
        }
        if (!"0".equals(vaccine.getStatus())) {
            throw new ServiceException("该疫苗已停用，无法预约");
        }

        // 3. 检查当日是否已约满（按天统计）
        int todayBooked = vaccineAppointmentMapper.selectCountByVaccineAndDate(
                vaccineAppointment.getVaccineId(),
                vaccineAppointment.getAppointmentDate());
        if (vaccine.getStock() <= todayBooked) {
            throw new ServiceException("该日期预约已满，请选择其他日期");
        }

        // 4. 检查重复预约（同一用户同一天同一疫苗）
        VaccineAppointment checkParam = new VaccineAppointment();
        checkParam.setUserId(vaccineAppointment.getUserId());
        checkParam.setVaccineId(vaccineAppointment.getVaccineId());
        checkParam.setAppointmentDate(vaccineAppointment.getAppointmentDate());
        List<VaccineAppointment> existingList = vaccineAppointmentMapper.selectVaccineAppointmentList(checkParam);
        for (VaccineAppointment existing : existingList) {
            if (!"3".equals(existing.getStatus())) {
                throw new ServiceException("您已预约过该疫苗，请勿重复预约");
            }
        }

        // 5. 多剂次疫苗逻辑处理
        boolean isMultiDose = vaccine.getIsMultiDose() != null && vaccine.getIsMultiDose() == 1;
        int currentDose = 1;
        Date nextDoseDate = null;

        if (isMultiDose) {
            // 查询用户对该疫苗的所有未取消预约（按剂次升序）
            List<VaccineAppointment> userAppointments = vaccineAppointmentMapper.selectUserVaccineAppointments(
                    vaccineAppointment.getUserId(), vaccineAppointment.getVaccineId());

            int lastDose = userAppointments.isEmpty() ? 0 : userAppointments.get(userAppointments.size() - 1).getDoseNumber();

            // 如果已经完成了最后一剂，不允许再预约
            Integer totalDoses = vaccine.getTotalDoses();
            if (totalDoses == null) totalDoses = 1;
            if (lastDose >= totalDoses) {
                throw new ServiceException("您已完成该疫苗的全部" + totalDoses + "剂次接种，无需再次预约");
            }

            // 当前剂次 = lastDose + 1
            currentDose = lastDose + 1;

            // 检查间隔时间：如果有上一剂且上一剂未完成，需要满足间隔天数
            if (lastDose > 0) {
                VaccineAppointment lastApp = userAppointments.get(userAppointments.size() - 1);
                // 上一剂是否已完成接种
                if (lastApp.getIsCompleted() == null || lastApp.getIsCompleted() == 0) {
                    throw new ServiceException("请先完成第" + lastDose + "剂次的接种，再预约第" + currentDose + "剂次");
                }

                // 检查时间间隔
                Integer intervalDays = vaccine.getIntervalDays();
                if (intervalDays != null && intervalDays > 0) {
                    Date lastDate = lastApp.getAppointmentDate();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(lastDate);
                    cal.add(Calendar.DAY_OF_MONTH, intervalDays);
                    Date earliestNextDate = cal.getTime();

                    if (vaccineAppointment.getAppointmentDate().before(earliestNextDate)) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        throw new ServiceException("距离上一剂接种时间不足" + intervalDays + "天，请选择" + dateFormat.format(earliestNextDate) + "之后的日期");
                    }
                }
            }

            // 计算下一针日期（当前预约日期 + 间隔天数）
            Integer intervalDays = vaccine.getIntervalDays();
            if (intervalDays != null && intervalDays > 0 && currentDose < vaccine.getTotalDoses()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(vaccineAppointment.getAppointmentDate());
                cal.add(Calendar.DAY_OF_MONTH, intervalDays);
                nextDoseDate = cal.getTime();
            }
        }

        // 6. 设置预约信息
        vaccineAppointment.setDoseNumber(currentDose);
        vaccineAppointment.setNextDoseDate(nextDoseDate);
        vaccineAppointment.setIsCompleted(0);
        vaccineAppointment.setStatus("0"); // 默认待确认
        vaccineAppointment.setCreateTime(new Date());

        // 7. 插入预约记录
        int rows = vaccineAppointmentMapper.insertVaccineAppointment(vaccineAppointment);

        // 8. 更新疫苗库存（减库存）
        vaccineInformationMapper.updateStock(vaccineAppointment.getVaccineId(), -1);

        return rows;
    }

    /**
     * 修改疫苗预约
     *
     * @param vaccineAppointment 疫苗预约
     * @return 结果
     */
    @Override
    public int updateVaccineAppointment(VaccineAppointment vaccineAppointment)
    {
        vaccineAppointment.setUpdateTime(new Date());
        return vaccineAppointmentMapper.updateVaccineAppointment(vaccineAppointment);
    }

    /**
     * 批量删除疫苗预约
     *
     * @param ids 需要删除的疫苗预约主键集合
     * @return 结果
     */
    @Override
    public int deleteVaccineAppointmentByIds(Long[] ids)
    {
        // 检查是否有正在处理中的预约
        for (Long id : ids) {
            VaccineAppointment appointment = vaccineAppointmentMapper.selectVaccineAppointmentById(id);
            if (appointment != null && !"3".equals(appointment.getStatus()) && !"2".equals(appointment.getStatus())) {
                throw new ServiceException("只能删除已取消或已完成的预约");
            }
        }
        return vaccineAppointmentMapper.deleteVaccineAppointmentByIds(ids);
    }

    /**
     * 删除预约的规则：
     * 1. 只有状态为"已取消"的预约才能删除
     * 2. 多剂次疫苗的已完成记录不能删除（由规则1已覆盖，因为已完成≠已取消）
     * 3. 进行中的预约（待确认、已确认）应先取消，再删除
     */
    @Override
    public int deleteVaccineAppointmentById(Long id) {
        VaccineAppointment appointment = vaccineAppointmentMapper.selectVaccineAppointmentById(id);
        if (appointment == null) {
            throw new ServiceException("预约记录不存在");
        }

        // 🔥 只有已取消的预约才能删除
        if (!"3".equals(appointment.getStatus())) {
            throw new ServiceException("只有已取消的预约才能删除");
        }

        // 🔥 多剂次疫苗检查（可选，实际上已取消的预约没有接种记录）
        VaccineInformation vaccine = vaccineInformationMapper.selectVaccineInformationById(appointment.getVaccineId());
        if (vaccine != null && vaccine.getIsMultiDose() == 1) {
            // 如果已取消的预约之前有过已完成记录，理论上不会发生
            // 但为了安全，可以检查
            List<VaccineAppointment> completedList = vaccineAppointmentMapper.selectUserCompletedAppointments(
                    appointment.getUserId(), appointment.getVaccineId(), id);
            if (!completedList.isEmpty()) {
                throw new ServiceException("该疫苗已有接种记录，相关已取消记录不能删除");
            }
        }

        return vaccineAppointmentMapper.deleteVaccineAppointmentById(id);
    }


    /**
     * 获取待处理预约数量（用于小红点）
     */
    @Override
    public int selectPendingCount() {
        try {
            int count = vaccineAppointmentMapper.selectPendingCount();
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 确认预约（待确认 → 已确认）
     *
     * @param id 预约ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmAppointment(Long id) {
        VaccineAppointment appointment = vaccineAppointmentMapper.selectVaccineAppointmentById(id);
        if (appointment == null) {
            throw new ServiceException("预约记录不存在");
        }
        if (!"0".equals(appointment.getStatus())) {
            throw new ServiceException("当前状态无法确认，只有待确认的预约才能确认");
        }

        appointment.setStatus("1"); // 已确认
        appointment.setUpdateTime(new Date());

        return vaccineAppointmentMapper.updateVaccineAppointment(appointment);
    }

    /**
     * 完成接种（已确认 → 已完成）
     *
     * @param id 预约ID
     * @return 结果
     */
    @Override
    @Transactional
    public int completeAppointment(Long id) {
        VaccineAppointment appointment = vaccineAppointmentMapper.selectVaccineAppointmentById(id);
        if (appointment == null) {
            throw new ServiceException("预约记录不存在");
        }
        if (!"1".equals(appointment.getStatus())) {
            throw new ServiceException("当前状态无法完成，只有已确认的预约才能完成接种");
        }

        appointment.setStatus("2"); // 已完成
        appointment.setIsCompleted(1); // 🔥 关键：标记该剂次已完成
        appointment.setUpdateTime(new Date());

        int rows = vaccineAppointmentMapper.updateVaccineAppointment(appointment);
        return rows;
    }

    /**
     * 取消预约
     *
     * @param id 预约ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelAppointment(Long id) {
        VaccineAppointment appointment = vaccineAppointmentMapper.selectVaccineAppointmentById(id);
        if (appointment == null) {
            throw new ServiceException("预约记录不存在");
        }
        if ("2".equals(appointment.getStatus())) {
            throw new ServiceException("已完成接种的预约无法取消");
        }
        if ("3".equals(appointment.getStatus())) {
            throw new ServiceException("该预约已取消，请勿重复操作");
        }

        String oldStatus = appointment.getStatus();
        appointment.setStatus("3"); // 已取消
        appointment.setUpdateTime(new Date());

        int rows = vaccineAppointmentMapper.updateVaccineAppointment(appointment);

        // 如果是待确认或已确认状态取消，需要恢复库存
        if ("0".equals(oldStatus) || "1".equals(oldStatus)) {
            vaccineInformationMapper.updateStock(appointment.getVaccineId(), +1);
        }

        return rows;
    }

    // ==================== 多剂次疫苗新增方法 ====================

    /**
     * 获取用户待接种提醒列表
     *
     * @param userId 用户ID
     * @return 待接种列表
     */
    @Override
    public List<VaccineAppointment> getUserPendingDoses(Long userId) {
        return vaccineAppointmentMapper.selectUserPendingDoses(userId);
    }

    /**
     * 获取今日待接种提醒（管理员用）
     *
     * @return 今日提醒列表
     */
    @Override
    public List<Map<String, Object>> getTodayReminders() {
        return vaccineAppointmentMapper.selectTodayReminders();
    }

    /**
     * 获取疫苗消耗统计
     *
     * @return 消耗统计
     */
    @Override
    public List<Map<String, Object>> getVaccineConsumptionStats() {
        return vaccineAppointmentMapper.selectVaccineConsumptionStats();
    }

    /**
     * 获取接种覆盖率统计
     *
     * @return 覆盖率统计
     */
    @Override
    public List<Map<String, Object>> getVaccinationCoverage() {
        return vaccineAppointmentMapper.selectVaccinationCoverage();
    }

    /**
     * 获取各剂次完成情况统计
     *
     * @param vaccineId 疫苗ID
     * @return 剂次统计
     */
    @Override
    public List<Map<String, Object>> getDoseCompletionStats(Long vaccineId) {
        return vaccineAppointmentMapper.selectDoseCompletionStats(vaccineId);
    }

    /**
     * 获取用户某疫苗的已完成剂次数
     *
     * @param userId    用户ID
     * @param vaccineId 疫苗ID
     * @return 已完成剂次数
     */
    @Override
    public int getUserCompletedDoses(Long userId, Long vaccineId) {
        Integer count = vaccineAppointmentMapper.selectUserCompletedDoses(userId, vaccineId);
        return count != null ? count : 0;
    }

    /**
     * 获取用户某疫苗的最新预约记录
     *
     * @param userId    用户ID
     * @param vaccineId 疫苗ID
     * @return 最新预约
     */
    @Override
    public VaccineAppointment getUserLatestAppointment(Long userId, Long vaccineId) {
        return vaccineAppointmentMapper.selectUserLatestAppointment(userId, vaccineId);
    }

    /**
     * 获取预约趋势（按日期统计，支持多剂次区分）
     *
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据
     */
    @Override
    public List<Map<String, Object>> getAppointmentTrendByDate(String beginDate, String endDate) {
        return vaccineAppointmentMapper.selectAppointmentTrendByDate(beginDate, endDate);
    }

    /**
     * 获取疫苗总预约数
     *
     * @param vaccineId 疫苗ID
     * @return 总预约数
     */
    @Override
    public int getTotalBookedByVaccine(Long vaccineId) {
        Integer count = vaccineAppointmentMapper.selectTotalBookedByVaccine(vaccineId);
        return count != null ? count : 0;
    }
}