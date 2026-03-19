package com.ruoyi.vaccine.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import javax.validation.constraints.Size;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 疫苗信息对象 vaccine_information
 *
 * @author jzx
 * @date 2026-02-08
 */
public class VaccineInformation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 疫苗ID */
    private Long id;

    /** 疫苗名称 */
    @Excel(name = "疫苗名称")
    private String name;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 疫苗描述 */
    @Excel(name = "疫苗描述")
    private String description;

    /** 适用年龄 */
    @Excel(name = "适用年龄", readConverterExp = "1=婴儿,2=儿童,3=青少年,4=成人,5=老人")
    @Size(min = 1, max = 10, message = "适用年龄长度必须在1到10个字符之间")
    private String suitableAge;

    /** 库存数量 */
    @Excel(name = "库存数量")
    private Long stock;

    /** 状态 */
    @Excel(name = "状态",readConverterExp = "0=正常,1=停用")
    private String status;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer()
    {
        return manufacturer;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setSuitableAge(String suitableAge)
    {
        this.suitableAge = suitableAge;
    }

    public String getSuitableAge()
    {
        return suitableAge;
    }

    public void setStock(Long stock)
    {
        this.stock = stock;
    }

    public Long getStock()
    {
        return stock;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("manufacturer", getManufacturer())
            .append("description", getDescription())
            .append("suitableAge", getSuitableAge())
            .append("stock", getStock())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
