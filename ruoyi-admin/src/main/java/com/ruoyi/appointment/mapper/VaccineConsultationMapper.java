package com.ruoyi.appointment.mapper;

import com.ruoyi.appointment.domain.VaccineConsultation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VaccineConsultationMapper {

    VaccineConsultation selectVaccineConsultationById(Long id);

    List<VaccineConsultation> selectVaccineConsultationList(VaccineConsultation vaccineConsultation);

    int insertVaccineConsultation(VaccineConsultation vaccineConsultation);

    int updateVaccineConsultation(VaccineConsultation vaccineConsultation);

    int deleteVaccineConsultationByIds(@Param("ids") Long[] ids);
}
