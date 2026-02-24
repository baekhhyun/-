package com.ruoyi.appointment.service;

import java.util.List;
import com.ruoyi.appointment.domain.VaccineAppointment;

/**
 * 疫苗预约Service接口
 *
 * @author jzx
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
}