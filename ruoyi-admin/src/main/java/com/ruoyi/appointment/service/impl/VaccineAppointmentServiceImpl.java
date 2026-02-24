package com.ruoyi.appointment.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.appointment.mapper.VaccineAppointmentMapper;
import com.ruoyi.appointment.domain.VaccineAppointment;
import com.ruoyi.appointment.service.IVaccineAppointmentService;

/**
 * 疫苗预约Service业务层处理
 *
 * @author jzx
 * @date 2026-02-08
 */
@Service
public class VaccineAppointmentServiceImpl implements IVaccineAppointmentService
{
    @Autowired
    private VaccineAppointmentMapper vaccineAppointmentMapper;

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
     * 新增疫苗预约
     *
     * @param vaccineAppointment 疫苗预约
     * @return 结果
     */
    @Override
    public int insertVaccineAppointment(VaccineAppointment vaccineAppointment)
    {
        return vaccineAppointmentMapper.insertVaccineAppointment(vaccineAppointment);
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
        return vaccineAppointmentMapper.deleteVaccineAppointmentByIds(ids);
    }

    /**
     * 删除疫苗预约信息
     *
     * @param id 疫苗预约主键
     * @return 结果
     */
    @Override
    public int deleteVaccineAppointmentById(Long id)
    {
        return vaccineAppointmentMapper.deleteVaccineAppointmentById(id);
    }
}