package com.ruoyi.vaccine.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.vaccine.domain.VaccineInformation;
import com.ruoyi.vaccine.service.IVaccineInformationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 疫苗信息Controller
 * @date 2026-02-08
 */
@RestController
@RequestMapping("/vaccine/vaccine")
public class VaccineInformationController extends BaseController
{
    @Autowired
    private IVaccineInformationService vaccineInformationService;

    /**
     * 查询疫苗信息列表
     */
//    @PreAuthorize("@ss.hasPermi('vaccine:vaccine:list')")
    @GetMapping("/list")
    public TableDataInfo list(VaccineInformation vaccineInformation)
    {
        startPage();
        List<VaccineInformation> list = vaccineInformationService.selectVaccineInformationList(vaccineInformation);
        return getDataTable(list);
    }

    /**
     * 导出疫苗列表
     */
    @Log(title = "疫苗信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VaccineInformation vaccineInformation) throws IOException
    {
        List<VaccineInformation> list = vaccineInformationService.selectVaccineInformationList(vaccineInformation);
        ExcelUtil<VaccineInformation> util = new ExcelUtil<VaccineInformation>(VaccineInformation.class);
        util.exportExcel(response, list, "疫苗数据");
    }
    /**
     * 获取疫苗信息详细信息
     */
//    @PreAuthorize("@ss.hasPermi('vaccine:vaccine:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(vaccineInformationService.selectVaccineInformationById(id));
    }

    /**
     * 新增疫苗信息
     */
//    @PreAuthorize("@ss.hasPermi('vaccine:vaccine:add')")
    @Log(title = "疫苗信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VaccineInformation vaccineInformation)
    {
        return toAjax(vaccineInformationService.insertVaccineInformation(vaccineInformation));
    }

    /**
     * 修改疫苗信息
     */
//    @PreAuthorize("@ss.hasPermi('vaccine:vaccine:edit')")
    @Log(title = "疫苗信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VaccineInformation vaccineInformation)
    {
        return toAjax(vaccineInformationService.updateVaccineInformation(vaccineInformation));
    }

    /**
     * 删除疫苗信息
     */
//    @PreAuthorize("@ss.hasPermi('vaccine:vaccine:remove')")
    @Log(title = "疫苗信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(vaccineInformationService.deleteVaccineInformationByIds(ids));
    }
}
