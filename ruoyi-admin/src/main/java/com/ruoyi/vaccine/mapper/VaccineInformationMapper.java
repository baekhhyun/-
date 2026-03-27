package com.ruoyi.vaccine.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.vaccine.domain.VaccineInformation;
import org.apache.ibatis.annotations.Param;

/**
 * 疫苗信息Mapper接口
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
    /**
     * 更新疫苗库存
     *
     * @param vaccineId 疫苗ID
     * @param delta 库存变化量（正数增加，负数减少）
     * @return 影响行数
     */
    public int updateStock(@Param("vaccineId") Long vaccineId, @Param("delta") int delta);

    /**
     * 查询疫苗库存
     *
     * @param vaccineId 疫苗ID
     * @return 库存数量
     */
    public Integer selectStock(@Param("vaccineId") Long vaccineId);

    /**
     * 检查库存是否足够
     *
     * @param vaccineId 疫苗ID
     * @param needCount 需要的数量
     * @return 是否足够（>0表示足够）
     */
    public int checkStock(@Param("vaccineId") Long vaccineId, @Param("needCount") int needCount);

    /**
     * 查询所有多剂次疫苗
     *
     * @return 多剂次疫苗列表
     */
    public List<VaccineInformation> selectMultiDoseVaccines();

    /**
     * 根据疫苗名称查询
     *
     * @param name 疫苗名称
     * @return 疫苗信息
     */
    public VaccineInformation selectVaccineByName(@Param("name") String name);

    /**
     * 查询疫苗总数
     *
     * @return 疫苗数量
     */
    public int selectVaccineCount();

    /**
     * 查询在售疫苗数量
     *
     * @return 在售疫苗数量
     */
    public int selectActiveVaccineCount();

    /**
     * 查询低库存疫苗（库存小于阈值）
     *
     * @param threshold 阈值
     * @return 低库存疫苗列表
     */
    public List<VaccineInformation> selectLowStockVaccines(@Param("threshold") int threshold);

    /**
     * 统计各年龄段的疫苗数量
     *
     * @return 年龄段统计
     */
    public List<Map<String, Object>> selectVaccineCountByAge();

    /**
     * 批量更新疫苗状态
     *
     * @param ids 疫苗ID数组
     * @param status 状态
     * @return 影响行数
     */
    public int batchUpdateStatus(@Param("ids") Long[] ids, @Param("status") String status);

    /**
     * 查询热门疫苗TopN（按预约量）
     *
     * @param limit 数量限制
     * @return 热门疫苗列表
     */
    public List<Map<String, Object>> selectHotVaccines(@Param("limit") int limit);

    /**
     * 查询疫苗消耗统计（用于仪表盘）
     *
     * @return 消耗统计
     */
    public List<Map<String, Object>> selectVaccineConsumption();

    /**
     * 根据适用年龄分类查询疫苗
     *
     * @param ageType 年龄类型（1婴儿,2儿童,3青少年,4成人,5老人）
     * @return 疫苗列表
     */
    public List<VaccineInformation> selectVaccineByAgeType(@Param("ageType") Integer ageType);

    /**
     * 批量插入疫苗
     *
     * @param list 疫苗列表
     * @return 影响行数
     */
    public int batchInsert(@Param("list") List<VaccineInformation> list);

    /**
     * 按生产厂家统计疫苗数量
     *
     * @return 厂家统计
     */
    public List<Map<String, Object>> selectVaccineCountByManufacturer();

    /**
     * 搜索疫苗（支持多字段模糊匹配）
     *
     * @param keyword 搜索关键词
     * @param limit 数量限制
     * @return 疫苗列表
     */
    public List<VaccineInformation> searchVaccine(@Param("keyword") String keyword, @Param("limit") int limit);

    /**
     * 获取疫苗预约统计（按日期范围）
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 预约统计
     */
    public List<Map<String, Object>> selectVaccineAppointmentStats(@Param("beginDate") String beginDate, @Param("endDate") String endDate);}
