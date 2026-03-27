package com.ruoyi.appointment.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.appointment.domain.VaccineAppointment;

/**
 * 疫苗预约Service接口
 * @date 2026-02-08
 */
public interface IVaccineAppointmentService
{
    /**
     * 查询疫苗预约
     *
     * @param id 疫苗预约主键
     * @return 疫苗预约
     */
    public VaccineAppointment selectVaccineAppointmentById(Long id);

    /**
     * 查询疫苗预约列表
     *
     * @param vaccineAppointment 疫苗预约
     * @return 疫苗预约集合
     */
    public List<VaccineAppointment> selectVaccineAppointmentList(VaccineAppointment vaccineAppointment);

    /**
     * 查询疫苗预约列表（关联用户和疫苗信息）
     *
     * @param vaccineAppointment 疫苗预约
     * @return 疫苗预约集合
     */
    public List<VaccineAppointment> selectVaccineAppointmentListWithInfo(VaccineAppointment vaccineAppointment);

    /**
     * 查询我的预约列表
     *
     * @param vaccineAppointment 疫苗预约
     * @return 预约集合
     */
    public List<VaccineAppointment> selectMyAppointmentList(VaccineAppointment vaccineAppointment);

    /**
     * 新增疫苗预约
     *
     * @param vaccineAppointment 疫苗预约
     * @return 结果
     */
    public int insertVaccineAppointment(VaccineAppointment vaccineAppointment);

    /**
     * 修改疫苗预约
     *
     * @param vaccineAppointment 疫苗预约
     * @return 结果
     */
    public int updateVaccineAppointment(VaccineAppointment vaccineAppointment);

    /**
     * 批量删除疫苗预约
     *
     * @param ids 需要删除的疫苗预约主键集合
     * @return 结果
     */
    public int deleteVaccineAppointmentByIds(Long[] ids);

    /**
     * 删除疫苗预约信息
     *
     * @param id 疫苗预约主键
     * @return 结果
     */
    public int deleteVaccineAppointmentById(Long id);
    public int selectPendingCount();
    int confirmAppointment(Long id);
    int completeAppointment(Long id);
    int cancelAppointment(Long id);

    // ==================== 多剂次疫苗新增方法 ====================
    List<VaccineAppointment> getUserPendingDoses(Long userId);
    List<Map<String, Object>> getTodayReminders();
    List<Map<String, Object>> getVaccineConsumptionStats();
    List<Map<String, Object>> getVaccinationCoverage();
    List<Map<String, Object>> getDoseCompletionStats(Long vaccineId);
    int getUserCompletedDoses(Long userId, Long vaccineId);
    VaccineAppointment getUserLatestAppointment(Long userId, Long vaccineId);
    List<Map<String, Object>> getAppointmentTrendByDate(String beginDate, String endDate);
    int getTotalBookedByVaccine(Long vaccineId);

}