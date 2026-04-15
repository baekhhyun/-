package com.ruoyi.appointment.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 在线咨询对象 vaccine_consultation
 */
public class VaccineConsultation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "用户ID")
    private Long userId;

    @Excel(name = "用户名称")
    private String userName;

    @Excel(name = "标题")
    private String title;

    @Excel(name = "咨询内容")
    private String questionContent;

    @Excel(name = "回复内容")
    private String answerContent;

    /** 0待回复 1已回复 */
    @Excel(name = "状态", readConverterExp = "0=待回复,1=已回复")
    private String status;

    private String priority;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date answerTime;

    private String answerBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(Date answerTime) {
        this.answerTime = answerTime;
    }

    public String getAnswerBy() {
        return answerBy;
    }

    public void setAnswerBy(String answerBy) {
        this.answerBy = answerBy;
    }
}
