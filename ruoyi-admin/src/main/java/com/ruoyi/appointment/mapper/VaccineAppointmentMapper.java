package com.ruoyi.appointment.mapper;

import java.util.List;
import com.ruoyi.appointment.domain.VaccineAppointment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.Map;

/**
 * 疫苗预约Mapper接口
 * @date 2026-02-08
 */
public interface VaccineAppointmentMapper 
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
    List<VaccineAppointment> selectMyAppointmentList(VaccineAppointment vaccineAppointment);

    List<VaccineAppointment> selectVaccineAppointmentListWithInfo(VaccineAppointment vaccineAppointment);
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
     * 删除疫苗预约
     * 
     * @param id 疫苗预约主键
     * @return 结果
     */
    public int deleteVaccineAppointmentById(Long id);

    /**
     * 批量删除疫苗预约
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVaccineAppointmentByIds(Long[] ids);

    public int selectPendingCount();
    @Select("SELECT COUNT(*) FROM vaccine_appointment WHERE vaccine_id = #{vaccineId} AND status IN ('0', '1')")
    public int countUnfinishedByVaccineId(@Param("vaccineId") Long vaccineId);
    public int selectCountByVaccineAndDate(@Param("vaccineId") Long vaccineId, @Param("appointmentDate") java.util.Date appointmentDate);

    /**
     * 根据用户ID和疫苗ID查询所有未取消的预约记录（按剂次升序）
     *
     * @param userId 用户ID
     * @param vaccineId 疫苗ID
     * @return 预约记录列表
     */
    public List<VaccineAppointment> selectUserVaccineAppointments(@Param("userId") Long userId, @Param("vaccineId") Long vaccineId);

    /**
     * 查询用户未完成的剂次（用于提醒）
     *
     * @param userId 用户ID
     * @return 未完成剂次列表
     */
    public List<VaccineAppointment> selectUserPendingDoses(@Param("userId") Long userId);

    /**
     * 查询今日待接种提醒（后台管理员用）
     *
     * @return 今日提醒列表
     */
    public List<Map<String, Object>> selectTodayReminders();

    /**
     * 统计疫苗消耗情况（用于仪表盘图表）
     *
     * @return 消耗统计
     */
    public List<Map<String, Object>> selectVaccineConsumptionStats();

    /**
     * 统计接种覆盖率（按用户）
     *
     * @return 覆盖率统计
     */
    public List<Map<String, Object>> selectVaccinationCoverage();

    /**
     * 统计各剂次完成情况（用于多剂次进度展示）
     *
     * @param vaccineId 疫苗ID
     * @return 剂次统计
     */
    public List<Map<String, Object>> selectDoseCompletionStats(@Param("vaccineId") Long vaccineId);

    /**
     * 检查用户是否已完成全部剂次
     *
     * @param userId 用户ID
     * @param vaccineId 疫苗ID
     * @return 已完成剂次数
     */
    public Integer selectUserCompletedDoses(@Param("userId") Long userId, @Param("vaccineId") Long vaccineId);

    /**
     * 获取用户某疫苗的最新预约记录
     *
     * @param userId 用户ID
     * @param vaccineId 疫苗ID
     * @return 最新预约
     */
    public VaccineAppointment selectUserLatestAppointment(@Param("userId") Long userId, @Param("vaccineId") Long vaccineId);

    /**
     * 按日期统计预约数量（用于预约趋势图，支持多剂次）
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    public List<Map<String, Object>>selectAppointmentTrendByDate(@Param("beginDate") String beginDate, @Param("endDate") String endDate);

    /**
     * 统计疫苗总预约数（用于库存计算）
     *
     * @param vaccineId 疫苗ID
     * @return 总预约数
     */
    public Integer selectTotalBookedByVaccine(@Param("vaccineId") Long vaccineId);
    /**
     * 查询用户已完成的预约记录（排除指定ID）
     * @param userId 用户ID
     * @param vaccineId 疫苗ID
     * @param excludeId 排除的预约ID
     * @return 已完成预约列表
     */
    List<VaccineAppointment> selectUserCompletedAppointments(@Param("userId") Long userId,
                                                             @Param("vaccineId") Long vaccineId,
                                                             @Param("excludeId") Long excludeId);
}
