package com.ruoyi.appointment.service.impl;

import com.ruoyi.appointment.domain.VaccineConsultation;
import com.ruoyi.appointment.mapper.VaccineConsultationMapper;
import com.ruoyi.appointment.service.IVaccineConsultationService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class VaccineConsultationServiceImpl implements IVaccineConsultationService {

    @Autowired
    private VaccineConsultationMapper vaccineConsultationMapper;

    @Override
    public VaccineConsultation selectVaccineConsultationById(Long id) {
        return vaccineConsultationMapper.selectVaccineConsultationById(id);
    }

    @Override
    public List<VaccineConsultation> selectVaccineConsultationList(VaccineConsultation vaccineConsultation) {
        return vaccineConsultationMapper.selectVaccineConsultationList(vaccineConsultation);
    }

    @Override
    public int insertVaccineConsultation(VaccineConsultation vaccineConsultation) {
        vaccineConsultation.setCreateTime(DateUtils.getNowDate());
        vaccineConsultation.setStatus("0");
        if (vaccineConsultation.getPriority() == null) {
            vaccineConsultation.setPriority("1");
        }
        return vaccineConsultationMapper.insertVaccineConsultation(vaccineConsultation);
    }

    @Override
    public int replyConsultation(VaccineConsultation vaccineConsultation) {
        vaccineConsultation.setStatus("1");
        vaccineConsultation.setAnswerTime(new Date());
        vaccineConsultation.setUpdateTime(DateUtils.getNowDate());
        return vaccineConsultationMapper.updateVaccineConsultation(vaccineConsultation);
    }

    @Override
    public int deleteVaccineConsultationByIds(Long[] ids) {
        return vaccineConsultationMapper.deleteVaccineConsultationByIds(ids);
    }
}
