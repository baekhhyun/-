package com.ruoyi.appointment.controller;

import com.ruoyi.appointment.domain.VaccineConsultation;
import com.ruoyi.appointment.service.IVaccineConsultationService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vaccine/consultation")
public class VaccineConsultationController extends BaseController {

    @Autowired
    private IVaccineConsultationService vaccineConsultationService;

    @GetMapping("/list")
    public TableDataInfo list(VaccineConsultation vaccineConsultation) {
        startPage();
        List<VaccineConsultation> list = vaccineConsultationService.selectVaccineConsultationList(vaccineConsultation);
        return getDataTable(list);
    }

    @GetMapping("/myList")
    public TableDataInfo myList(VaccineConsultation vaccineConsultation) {
        vaccineConsultation.setUserId(SecurityUtils.getUserId());
        startPage();
        List<VaccineConsultation> list = vaccineConsultationService.selectVaccineConsultationList(vaccineConsultation);
        return getDataTable(list);
    }

    @PostMapping
    public AjaxResult add(@RequestBody VaccineConsultation vaccineConsultation) {
        if (vaccineConsultation.getTitle() == null || vaccineConsultation.getTitle().trim().isEmpty()) {
            return AjaxResult.error("咨询标题不能为空");
        }
        if (vaccineConsultation.getQuestionContent() == null || vaccineConsultation.getQuestionContent().trim().isEmpty()) {
            return AjaxResult.error("咨询内容不能为空");
        }

        vaccineConsultation.setUserId(SecurityUtils.getUserId());
        vaccineConsultation.setUserName(SecurityUtils.getUsername());
        vaccineConsultation.setCreateBy(SecurityUtils.getUsername());
        return toAjax(vaccineConsultationService.insertVaccineConsultation(vaccineConsultation));
    }

    @PutMapping("/reply")
    public AjaxResult reply(@RequestBody VaccineConsultation vaccineConsultation) {
        if (vaccineConsultation.getId() == null) {
            return AjaxResult.error("咨询ID不能为空");
        }
        if (vaccineConsultation.getAnswerContent() == null || vaccineConsultation.getAnswerContent().trim().isEmpty()) {
            return AjaxResult.error("回复内容不能为空");
        }

        vaccineConsultation.setAnswerBy(SecurityUtils.getUsername());
        vaccineConsultation.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(vaccineConsultationService.replyConsultation(vaccineConsultation));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(vaccineConsultationService.deleteVaccineConsultationByIds(ids));
    }
}
