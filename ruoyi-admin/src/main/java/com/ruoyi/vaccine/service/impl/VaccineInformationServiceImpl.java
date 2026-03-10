package com.ruoyi.vaccine.service.impl;

import java.util.List;

import com.ruoyi.appointment.mapper.VaccineAppointmentMapper;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.vaccine.mapper.VaccineInformationMapper;
import com.ruoyi.vaccine.domain.VaccineInformation;
import com.ruoyi.vaccine.service.IVaccineInformationService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 疫苗信息Service业务层处理
 * 
 * @author jzx
 * @date 2026-02-08
 */
@Service
public class VaccineInformationServiceImpl implements IVaccineInformationService 
{
    @Autowired
    private VaccineInformationMapper vaccineInformationMapper;
    @Autowired
    private VaccineAppointmentMapper vaccineAppointmentMapper;    /**
     * 查询疫苗信息
     * 
     * @param id 疫苗信息主键
     * @return 疫苗信息
     */
    @Override
    public VaccineInformation selectVaccineInformationById(Long id)
    {
        return vaccineInformationMapper.selectVaccineInformationById(id);
    }

    /**
     * 查询疫苗信息列表
     * 
     * @param vaccineInformation 疫苗信息
     * @return 疫苗信息
     */
    @Override
    public List<VaccineInformation> selectVaccineInformationList(VaccineInformation vaccineInformation)
    {
        return vaccineInformationMapper.selectVaccineInformationList(vaccineInformation);
    }

    /**
     * 新增疫苗信息
     * 
     * @param vaccineInformation 疫苗信息
     * @return 结果
     */
    @Override
    public int insertVaccineInformation(VaccineInformation vaccineInformation)
    {
        vaccineInformation.setCreateTime(DateUtils.getNowDate());
        return vaccineInformationMapper.insertVaccineInformation(vaccineInformation);
    }

    /**
     * 修改疫苗信息
     * 
     * @param vaccineInformation 疫苗信息
     * @return 结果
     */
    @Override
    public int updateVaccineInformation(VaccineInformation vaccineInformation)
    {
        vaccineInformation.setUpdateTime(DateUtils.getNowDate());
        return vaccineInformationMapper.updateVaccineInformation(vaccineInformation);
    }

    /**
     * 批量删除疫苗信息
     * 
     * @param ids 需要删除的疫苗信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteVaccineInformationByIds(Long[] ids) {  // 注意参数是 Long[]

        for (Long id : ids) {
            // 检查该疫苗是否有未完成的预约（待确认'0'、已确认'1'）
            int count = vaccineAppointmentMapper.countUnfinishedByVaccineId(id);
            if (count > 0) {
                throw new ServiceException(String.format("疫苗【ID: %s】存在未完成的预约，无法删除", id));
            }
        }

        return vaccineInformationMapper.deleteVaccineInformationByIds(ids);
    }

    /**
     * 删除疫苗信息信息
     * 
     * @param id 疫苗信息主键
     * @return 结果
     */
    @Override
    public int deleteVaccineInformationById(Long id)
    {
        return vaccineInformationMapper.deleteVaccineInformationById(id);
    }
}
