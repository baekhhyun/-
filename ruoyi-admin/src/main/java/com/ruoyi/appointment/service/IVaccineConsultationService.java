package com.ruoyi.appointment.service;

import com.ruoyi.appointment.domain.VaccineConsultation;

import java.util.List;

public interface IVaccineConsultationService {
    VaccineConsultation selectVaccineConsultationById(Long id);

    List<VaccineConsultation> selectVaccineConsultationList(VaccineConsultation vaccineConsultation);

    int insertVaccineConsultation(VaccineConsultation vaccineConsultation);

    int replyConsultation(VaccineConsultation vaccineConsultation);

    int deleteVaccineConsultationByIds(Long[] ids);
}
