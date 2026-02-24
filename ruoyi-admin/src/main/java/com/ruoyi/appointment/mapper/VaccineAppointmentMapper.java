package com.ruoyi.appointment.mapper;

import java.util.List;
import com.ruoyi.appointment.domain.VaccineAppointment;

/**
 * 疫苗预约Mapper接口
 * 
 * @author jzx
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
}
