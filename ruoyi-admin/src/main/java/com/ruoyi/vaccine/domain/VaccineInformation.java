package com.ruoyi.vaccine.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class VaccineInformation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 疫苗ID */
    private Long id;

    /** 疫苗名称 */
    @Excel(name = "疫苗名称")
    private String name;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 适用年龄（1婴儿,2儿童,3青少年,4成人,5老人） */
    @Excel(name = "适用年龄", readConverterExp = "1=婴儿,2=儿童,3=青少年,4=成人,5=老人")
    private String suitableAge;

    /** 库存 */
    @Excel(name = "库存")
    private Integer stock;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 描述 */
    @Excel(name = "描述")
    private String description;

    // ========== 新增多剂次字段 ==========
    /** 是否多剂次疫苗（0否 1是） */
    @Excel(name = "是否多剂次", readConverterExp = "0=否,1=是")
    private Integer isMultiDose;

    /** 总剂次数 */
    @Excel(name = "总剂次数")
    private Integer totalDoses;

    /** 剂次间隔天数 */
    @Excel(name = "间隔天数")
    private Integer intervalDays;

    /** 接种计划描述 */
    @Excel(name = "接种计划")
    private String doseSchedule;

    // getter/setter...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getSuitableAge() { return suitableAge; }
    public void setSuitableAge(String suitableAge)
    {
        this.suitableAge = suitableAge;
    }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getIsMultiDose() { return isMultiDose; }
    public void setIsMultiDose(Integer isMultiDose) { this.isMultiDose = isMultiDose; }
    public Integer getTotalDoses() { return totalDoses; }
    public void setTotalDoses(Integer totalDoses) { this.totalDoses = totalDoses; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
    public String getDoseSchedule() { return doseSchedule; }
    public void setDoseSchedule(String doseSchedule) { this.doseSchedule = doseSchedule; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("manufacturer", getManufacturer())
                .append("suitableAge", getSuitableAge())
                .append("stock", getStock())
                .append("status", getStatus())
                .append("description", getDescription())
                .append("isMultiDose", getIsMultiDose())
                .append("totalDoses", getTotalDoses())
                .append("intervalDays", getIntervalDays())
                .append("doseSchedule", getDoseSchedule())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}