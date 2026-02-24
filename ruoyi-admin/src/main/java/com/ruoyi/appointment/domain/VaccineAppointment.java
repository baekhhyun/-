package com.ruoyi.appointment.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 疫苗预约对象 vaccine_appointment
 *
 * @author jzx
 * @date 2026-02-08
 */
public class VaccineAppointment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预约ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 疫苗ID */
    @Excel(name = "疫苗ID")
    private Long vaccineId;

    /** 预约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预约日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date appointmentDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 时间段 */
    @Excel(name = "时间段")
    private String timeSlot;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 用户名称（关联查询） */
    private String userName;

    /** 用户手机号（关联查询） */
    private String userPhone;

    /** 疫苗名称（关联查询） */
    private String vaccineName;

    /** 生产厂家（关联查询） */
    private String manufacturer;

    /** 适用年龄（关联查询） */
    private String suitableAge;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setVaccineId(Long vaccineId)
    {
        this.vaccineId = vaccineId;
    }

    public Long getVaccineId()
    {
        return vaccineId;
    }

    public void setAppointmentDate(Date appointmentDate)
    {
        this.appointmentDate = appointmentDate;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getAppointmentDate()
    {
        return appointmentDate;
    }
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setTimeSlot(String timeSlot)
    {
        this.timeSlot = timeSlot;
    }

    public String getTimeSlot()
    {
        return timeSlot;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getStatus()
    {
        return status;
    }



    public String getPhone()
    {
        return phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getUserPhone() {
        return userPhone;
    }



    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getManufacturer() {
        return manufacturer;
    }




    public String getSuitableAge() {
        return suitableAge;
    }

    public void setSuitableAge(String suitableAge) {
        this.suitableAge = suitableAge;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("vaccineId", getVaccineId())
                .append("appointmentDate", getAppointmentDate())
                .append("timeSlot", getTimeSlot())
                .append("status", getStatus())
                .append("phone", getPhone())
                .append("remark", getRemark())
                .append("userName", getUserName())
                .append("userPhone", getUserPhone())
                .append("vaccineName", getVaccineName())
                .append("manufacturer", getManufacturer())
                .append("suitableAge", getSuitableAge())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }

}