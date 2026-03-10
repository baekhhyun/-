package com.ruoyi.vaccine.mapper;

import java.util.List;
import com.ruoyi.vaccine.domain.VaccineInformation;

/**
 * 疫苗信息Mapper接口
 * 
 * @author jzx
 * @date 2026-02-08
 */
public interface VaccineInformationMapper 
{
    /**
     * 查询疫苗信息
     * 
     * @param id 疫苗信息主键
     * @return 疫苗信息
     */
    public VaccineInformation selectVaccineInformationById(Long id);

    /**
     * 查询疫苗信息列表
     * 
     * @param vaccineInformation 疫苗信息
     * @return 疫苗信息集合
     */
    public List<VaccineInformation> selectVaccineInformationList(VaccineInformation vaccineInformation);

    /**
     * 新增疫苗信息
     * 
     * @param vaccineInformation 疫苗信息
     * @return 结果
     */
    public int insertVaccineInformation(VaccineInformation vaccineInformation);

    /**
     * 修改疫苗信息
     * 
     * @param vaccineInformation 疫苗信息
     * @return 结果
     */
    public int updateVaccineInformation(VaccineInformation vaccineInformation);

    /**
     * 删除疫苗信息
     * 
     * @param id 疫苗信息主键
     * @return 结果
     */
    public int deleteVaccineInformationById(Long id);

    /**
     * 批量删除疫苗信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVaccineInformationByIds(Long[] ids);

}
