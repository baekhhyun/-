<template>
  <div class="app-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/vaccine/appointment/user' }"
        >疫苗预约</el-breadcrumb-item
      >
      <el-breadcrumb-item>预约接种</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 预约表单卡片 -->
    <el-card class="form-card">
      <div slot="header" class="clearfix">
        <span style="font-size: 18px; font-weight: bold">疫苗预约表单</span>
      </div>

      <!-- 疫苗信息展示 -->
      <div v-if="vaccine" class="vaccine-info">
        <el-descriptions title="所选疫苗信息" :column="2">
          <el-descriptions-item label="疫苗名称">{{
            vaccine.name
          }}</el-descriptions-item>
          <el-descriptions-item label="生产厂家">{{
            vaccine.manufacturer
          }}</el-descriptions-item>
          <el-descriptions-item label="适用人群">
            <el-tag :type="getAgeTagType(vaccine.suitableAge)">
              {{ formatAge(vaccine.suitableAge) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前库存">
            <span :class="{ 'stock-low': vaccine.stock < 10 }">
              {{ vaccine.stock || 0 }} 剂
              <el-tag v-if="vaccine.stock < 10" size="mini" type="danger"
                >库存紧张</el-tag
              >
            </span>
          </el-descriptions-item>
          <!--  多剂次疫苗信息展示（新增） -->
          <el-descriptions-item
            v-if="vaccine.isMultiDose === 1"
            label="接种计划"
            :span="2"
          >
            <div class="dose-plan">
              <el-tag type="info" size="small"
                >共 {{ vaccine.totalDoses }} 剂次</el-tag
              >
              <el-tag type="warning" size="small" style="margin-left: 8px"
                >间隔 {{ vaccine.intervalDays }} 天</el-tag
              >
              <span v-if="vaccine.doseSchedule" style="margin-left: 8px"
                >（{{ vaccine.doseSchedule }}）</span
              >
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <!--  用户接种进度提示（新增） -->
        <el-alert
          v-if="vaccine.isMultiDose === 1 && userProgress"
          :title="getProgressTitle()"
          :type="getProgressType()"
          :closable="false"
          show-icon
          style="margin-top: 15px"
        >
          <template slot="default">
            <div class="progress-detail">
              <span
                >已完成：第 {{ userProgress.completedDoses }} /
                {{ vaccine.totalDoses }} 剂</span
              >
              <span v-if="userProgress.nextDose" style="margin-left: 20px">
                下一剂：第 {{ userProgress.nextDose }} 剂
                <span v-if="userProgress.earliestDate">
                  （最早可约 {{ userProgress.earliestDate }}）
                </span>
              </span>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 预约表单 -->
      <el-form
        ref="appointmentForm"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="appointment-form"
        v-loading="loading"
      >
        <!-- 用户信息（从登录信息获取） -->
        <el-form-item label="预约人">
          <span style="color: #409eff; font-weight: bold">
            {{ userName || "获取中..." }}
          </span>
          <el-button
            v-if="!userName"
            type="text"
            size="mini"
            @click="refreshUserInfo"
            style="margin-left: 10px"
          >
            刷新
          </el-button>
        </el-form-item>

        <!-- 预约日期 -->
        <el-form-item label="预约日期" prop="appointmentDate">
          <el-date-picker
            v-model="formData.appointmentDate"
            type="date"
            placeholder="选择预约日期"
            format="yyyy年MM月dd日"
            value-format="yyyy-MM-dd"
            :picker-options="datePickerOptions"
            style="width: 100%"
            @change="handleDateChange"
          >
          </el-date-picker>
          <div class="tip-text">请选择未来7天内的日期</div>
        </el-form-item>

        <!-- 时间段选择 -->
        <el-form-item label="预约时段" prop="timeSlot">
          <el-radio-group v-model="formData.timeSlot" :disabled="slotLoading">
            <el-radio
              v-for="slot in timeSlots"
              :key="slot.value"
              :label="slot.value"
              :disabled="!slot.available"
            >
              {{ slot.label }}
              <span
                v-if="slot.available"
                style="color: #67c23a; margin-left: 10px"
              >
                (剩余{{ slot.remaining }}个名额)
              </span>
              <span v-else style="color: #f56c6c; margin-left: 10px">
                (已满)
              </span>
            </el-radio>
          </el-radio-group>

          <div class="time-slots-info" v-if="formData.appointmentDate">
            <el-alert
              v-if="slotLoading"
              type="info"
              :closable="false"
              show-icon
              title="正在查询剩余名额..."
            />
            <el-alert
              v-else-if="!hasAvailableSlot"
              type="warning"
              :closable="false"
              show-icon
              title="当天所有时间段均已约满，请选择其他日期"
            />
          </div>
        </el-form-item>

        <!-- 联系电话 -->
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入联系电话"
            maxlength="11"
            show-word-limit
          />
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注信息" prop="remark">
          <el-input
            type="textarea"
            v-model="formData.remark"
            :rows="3"
            placeholder="请输入其他说明（如身体状况、特殊需求等）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 按钮组 -->
        <el-form-item class="form-buttons">
          <el-button
            type="primary"
            @click="submitAppointment"
            :loading="submitting"
            :disabled="!userName || !canSubmit"
          >
            <i class="el-icon-check"></i> 提交预约
          </el-button>
          <el-button @click="goBack">
            <i class="el-icon-back"></i> 返回疫苗列表
          </el-button>
          <el-button type="info" @click="resetForm">
            <i class="el-icon-refresh"></i> 重置表单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预约须知 -->
    <el-card class="notice-card">
      <div slot="header">
        <span style="font-weight: bold">📋 预约须知</span>
      </div>
      <ul class="notice-list">
        <li>1. 预约成功后，请按时到达接种点，迟到超过30分钟预约自动取消</li>
        <li>2. 请携带有效身份证件和健康码</li>
        <li>3. 如有发热、咳嗽等症状，请暂缓接种并及时取消预约</li>
        <li>4. 如需取消预约，请在"我的预约"页面操作</li>
        <li>5. 咨询电话：0769-88888888</li>
        <li v-if="vaccine && vaccine.isMultiDose === 1">
          6. 多剂次疫苗提醒：请按照接种计划完成全部剂次接种
        </li>
      </ul>
    </el-card>
  </div>
</template>

<script>
import { getVaccine, getMyVaccineProgress } from "@/api/vaccine/vaccine";
import {
  addAppointment,
  getTimeSlotRemaining, //  新增API
} from "@/api/vaccine/appointment";
import { getUserProfile } from "@/api/system/user";

export default {
  name: "VaccineAppointmentForm",
  data() {
    // 电话号码验证规则
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入联系电话"));
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error("请输入正确的11位手机号码"));
      } else {
        callback();
      }
    };

    // 日期验证规则（禁止选择过去和周末）
    const validateDate = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请选择预约日期"));
      } else {
        const selectedDate = new Date(value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // 计算日期差
        const diffDays = Math.floor(
          (selectedDate - today) / (1000 * 60 * 60 * 24)
        );

        // 检查是否为过去日期
        if (diffDays < 0) {
          callback(new Error("不能选择过去的日期"));
          return;
        }

        // 检查是否超过7天
        if (diffDays > 6) {
          callback(new Error("只能预约未来7天内的日期"));
          return;
        }

        callback();
      }
    };

    return {
      // 疫苗信息
      vaccine: null,
      vaccineId: null,
      // 用户信息
      userName: "",
      userInfo: {},
      //  用户接种进度（新增）
      userProgress: null,

      // 表单数据
      formData: {
        vaccineId: null,
        appointmentDate: "",
        timeSlot: "1",
        phone: "",
        remark: "",
      },

      // 表单验证规则
      formRules: {
        appointmentDate: [
          { required: true, validator: validateDate, trigger: "change" },
        ],
        timeSlot: [
          { required: true, message: "请选择预约时段", trigger: "change" },
        ],
        phone: [{ required: true, validator: validatePhone, trigger: "blur" }],
      },

      // 时间段数据
      timeSlots: [
        {
          value: "1",
          label: "上午 09:00-11:00",
          remaining: 0,
          booked: 0,
          maxCapacity: 20,
          available: true,
        },
        {
          value: "2",
          label: "下午 14:00-16:00",
          remaining: 0,
          booked: 0,
          maxCapacity: 20,
          available: true,
        },
        {
          value: "3",
          label: "晚上 18:00-20:00",
          remaining: 0,
          booked: 0,
          maxCapacity: 20,
          available: true,
        },
      ],
      slotLoading: false,

      // 日期选择限制
      datePickerOptions: {
        disabledDate(time) {
          const now = new Date();
          now.setHours(0, 0, 0, 0);
          const maxDate = new Date(now);
          maxDate.setDate(now.getDate() + 6);
          return (
            time.getTime() < now.getTime() || time.getTime() > maxDate.getTime()
          );
        },
      },
      // 状态标志
      loading: false,
      submitting: false,
    };
  },
  computed: {
    // 是否有可用时间段
    hasAvailableSlot() {
      return this.timeSlots.some((slot) => slot.available);
    },

    //  是否可以提交（多剂次疫苗额外校验，新增）
    canSubmit() {
      if (!this.vaccine || this.vaccine.isMultiDose !== 1) {
        return true;
      }
      if (!this.userProgress) {
        return true;
      }
      // 已完成全部剂次不可再约
      if (this.userProgress.completedDoses >= this.vaccine.totalDoses) {
        return false;
      }
      return true;
    },
  },
  beforeRouteEnter(to, from, next) {
    next((vm) => {
      vm.initFromRoute(to);
    });
  },

  beforeRouteUpdate(to, from, next) {
    const newVaccineId = to.query.vaccineId;
    const oldVaccineId = from.query.vaccineId;

    if (newVaccineId && newVaccineId !== oldVaccineId) {
      this.handleVaccineChange(newVaccineId);
    }

    next();
  },

  created() {
    this.initFromRoute(this.$route);
    this.$nextTick(() => {
      this.initPage();
    });
  },
  watch: {
    // 监听预约日期变化，动态获取名额
    "formData.appointmentDate": {
      handler(newVal) {
        if (newVal) {
          this.getTimeSlotRemaining(newVal);
        } else {
          this.resetTimeSlots();
        }
      },
      immediate: false,
    },
  },

  methods: {
    //  获取进度提示标题
    getProgressTitle() {
      if (!this.userProgress) return "";
      const completed = this.userProgress.completedDoses;
      const total = this.vaccine.totalDoses;
      if (completed === 0) {
        return `📢 您还未接种过该疫苗，请预约第1剂`;
      }
      if (completed === total) {
        return `✅ 您已完成全部 ${total} 剂次接种，无需再次预约`;
      }
      return `📢 您已完成 ${completed}/${total} 剂次，请继续预约第 ${
        completed + 1
      } 剂`;
    },

    //  获取进度提示类型（新增）
    getProgressType() {
      if (!this.userProgress) return "info";
      const completed = this.userProgress.completedDoses;
      const total = this.vaccine.totalDoses;
      if (completed === 0) return "warning";
      if (completed === total) return "success";
      return "info";
    },

    initFromRoute(route) {
      const vaccineId = route.query.vaccineId;
      if (!vaccineId) {
        this.$message.error("未选择疫苗");
        this.$router.push("/vaccine/appointment/user");
        return;
      }

      const numericId = parseInt(vaccineId);
      if (this.vaccineId !== numericId) {
        this.vaccineId = numericId;
        this.loadVaccineInfo();
      }
    },

    handleVaccineChange(newVaccineId) {
      if (!newVaccineId) return;
      const numericId = parseInt(newVaccineId);
      if (this.vaccineId === numericId) return;

      this.vaccineId = numericId;
      this.resetForNewVaccine();
      this.loadVaccineInfo();
    },

    resetForNewVaccine() {
      this.vaccine = null;
      this.userProgress = null;
      this.formData = {
        vaccineId: this.vaccineId,
        appointmentDate: "",
        timeSlot: "1",
        phone: this.formData.phone,
        remark: "",
      };
      if (this.userInfo?.phonenumber) {
        this.formData.phone = this.userInfo.phonenumber;
      }
    },

    //  加载用户接种进度（新增）
    loadUserProgress() {
      if (!this.vaccineId || this.vaccine?.isMultiDose !== 1) {
        this.userProgress = null;
        return;
      }

      getMyVaccineProgress(this.vaccineId)
        .then((response) => {
          if (response.code === 200 && response.data) {
            this.userProgress = response.data;
          }
        })
        .catch((error) => {
          console.error("获取接种进度失败:", error);
          this.userProgress = null;
        });
    },

    loadVaccineInfo() {
      if (!this.vaccineId) return;

      this.loading = true;
      getVaccine(this.vaccineId)
        .then((response) => {
          if (response.code === 200) {
            this.vaccine = response.data;
            this.formData.vaccineId = this.vaccineId;
            document.title = `预约${this.vaccine.name} - 疫苗接种预约平台`;

            if (response.data.stock <= 0) {
              this.$message.warning("该疫苗库存不足");
            }

            //  加载用户接种进度
            this.loadUserProgress();
          } else {
            this.$message.error(response.msg || "获取疫苗信息失败");
          }
        })
        .catch((error) => {
          console.error("加载疫苗信息失败:", error);
          this.$message.error("加载疫苗信息失败");
        })
        .finally(() => {
          this.loading = false;
        });
    },
    handleDateChange() {},
    getTimeSlotRemaining(date) {
      if (!date) return;

      this.slotLoading = true;
      getTimeSlotRemaining(date)
        .then((response) => {
          if (response.code === 200 && response.data) {
            const slotData = response.data;

            this.timeSlots = this.timeSlots.map((slot) => {
              const found = slotData.find((s) => s.value === slot.value);
              if (found) {
                return {
                  ...slot,
                  remaining: found.remaining,
                  booked: found.booked,
                  maxCapacity: found.maxCapacity,
                  available: found.available,
                };
              }
              return slot;
            });

            const currentSlot = this.formData.timeSlot;
            if (currentSlot) {
              const selectedSlot = this.timeSlots.find(
                (s) => s.value === currentSlot
              );
              if (selectedSlot && !selectedSlot.available) {
                this.formData.timeSlot = "";
                this.$message.warning("您选择的时间段已满，请重新选择");
              }
            }
          }
        })
        .catch((error) => {
          console.error("获取时间段剩余名额失败:", error);
          this.resetTimeSlots();
        })
        .finally(() => {
          this.slotLoading = false;
        });
    },

    resetTimeSlots() {
      this.timeSlots = [
        {
          value: "1",
          label: "上午 09:00-11:00",
          remaining: 20,
          booked: 0,
          maxCapacity: 20,
          available: true,
        },
        {
          value: "2",
          label: "下午 14:00-16:00",
          remaining: 20,
          booked: 0,
          maxCapacity: 20,
          available: true,
        },
        {
          value: "3",
          label: "晚上 18:00-20:00",
          remaining: 20,
          booked: 0,
          maxCapacity: 20,
          available: true,
        },
      ];
    },

    checkTimeSlotAvailability() {
      if (!this.formData.appointmentDate) {
        this.$message.warning("请先选择预约日期");
        return false;
      }

      const selectedSlot = this.timeSlots.find(
        (s) => s.value === this.formData.timeSlot
      );
      if (selectedSlot && !selectedSlot.available) {
        this.$message.error("该时间段已满，请选择其他时间段");
        return false;
      }
      return true;
    },

    initPage() {
      this.loadUserInfo();
      this.vaccineId =
        this.$route.query.vaccineId || this.$route.params.vaccineId;

      if (!this.vaccineId) {
        this.$message.error("未选择疫苗，请返回重新选择");
        this.goBack();
        return;
      }

      this.loadVaccineInfo();
    },

    loadUserInfo() {
      getUserProfile()
        .then((response) => {
          if (response.code === 200 && response.data) {
            this.userInfo = response.data;
            this.userName =
              response.data.nickName || response.data.userName || "用户";

            if (response.data.phonenumber && !this.formData.phone) {
              this.formData.phone = response.data.phonenumber;
            }
          } else {
            this.userName = "用户";
            console.warn("获取用户信息失败:", response.msg);
          }
        })
        .catch((error) => {
          console.error("获取用户信息异常:", error);
          this.userName = "用户";
        });
    },

    refreshUserInfo() {
      this.userName = "获取中...";
      this.loadUserInfo();
    },

    formatAge(ageCode) {
      const ageMap = {
        1: "婴儿",
        2: "儿童",
        3: "青少年",
        4: "成人",
        5: "老人",
      };
      return ageMap[ageCode] || "-";
    },

    getAgeTagType(ageCode) {
      const typeMap = {
        1: "success",
        2: "primary",
        3: "warning",
        4: "info",
        5: "danger",
      };
      return typeMap[ageCode] || "info";
    },

    submitAppointment() {
      // 检查用户信息
      if (!this.userName || this.userName === "获取中...") {
        this.$message.warning("正在获取用户信息，请稍后重试");
        this.refreshUserInfo();
        return;
      }

      //  多剂次疫苗额外校验
      if (this.vaccine && this.vaccine.isMultiDose === 1 && this.userProgress) {
        const totalDoses = this.vaccine.totalDoses;
        if (this.userProgress.completedDoses >= totalDoses) {
          this.$message.error(
            `您已完成该疫苗的全部${totalDoses}剂次接种，无需再次预约`
          );
          return;
        }

        // 检查间隔时间（前端提醒，后端会再次校验）
        if (this.userProgress.earliestDate) {
          const selectedDate = this.formData.appointmentDate;
          const earliestDate = this.userProgress.earliestDate;
          if (selectedDate && selectedDate < earliestDate) {
            this.$message.error(
              `距离上一剂接种时间不足${this.vaccine.intervalDays}天，请选择${earliestDate}之后的日期`
            );
            return;
          }
        }
      }

      this.$refs.appointmentForm.validate((valid) => {
        if (valid) {
          this.submitting = true;

          const appointmentData = {
            vaccineId: this.formData.vaccineId,
            appointmentDate: this.formData.appointmentDate,
            timeSlot: this.formData.timeSlot,
            phone: this.formData.phone,
            remark: this.formData.remark,
          };

          addAppointment(appointmentData)
            .then((response) => {
              this.submitting = false;
              if (response.code === 200) {
                this.$message.success("预约成功！请按时前往接种点");
                setTimeout(() => {
                  this.$router.push("/vaccine/appointment/my");
                }, 1500);
              } else {
                let errorMsg = response.msg || "预约失败";
                if (errorMsg.includes("当天已有有效预约")) {
                  errorMsg = "您当天已有其他预约，如需重新预约请先取消原有预约";
                } else if (errorMsg.includes("已预约过此疫苗")) {
                  errorMsg = "您已预约过此疫苗，如需重新预约请先取消原有预约";
                } else if (errorMsg.includes("间隔")) {
                  errorMsg = errorMsg;
                }
                this.$message.error(errorMsg);
              }
            })
            .catch((error) => {
              console.error("提交预约失败:", error);
              this.submitting = false;
              this.$message.error("提交预约失败，请稍后重试");
            });
        } else {
          this.$message.warning("请完善表单信息");
          return false;
        }
      });
    },

    goBack() {
      this.$router.push("/vaccine/appointment/user");
    },

    resetForm() {
      this.formData = {
        vaccineId: this.vaccineId,
        appointmentDate: "",
        timeSlot: "1",
        phone: "",
        remark: "",
      };

      this.resetTimeSlots();

      if (this.userInfo.phonenumber) {
        this.formData.phone = this.userInfo.phonenumber;
      }

      this.$message.info("请重新选择预约日期");
    },
  },
};
</script>

<style scoped>
/* 原有样式保持不变 */
.app-container {
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.vaccine-info {
  margin-bottom: 30px;
}

.appointment-form {
  margin-top: 20px;
  max-width: 800px;
}

.tip-text {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.time-slots {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}

.time-slot-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px 15px;
  text-align: center;
  min-width: 150px;
  background-color: #fafafa;
}

.time-slot-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.slot-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.slot-info {
  font-size: 12px;
  color: #67c23a;
}

.form-buttons {
  text-align: center;
  margin-top: 30px;
}

.notice-card {
  margin-top: 20px;
}

.notice-list {
  padding-left: 20px;
}

.notice-list li {
  margin-bottom: 8px;
  line-height: 1.5;
}

.stock-low {
  color: #f56c6c;
  font-weight: bold;
}

.time-slots-info {
  margin-top: 15px;
}

.el-radio {
  margin-bottom: 10px;
  display: block !important;
  line-height: 2 !important;
}

.el-radio.is-disabled {
  opacity: 0.6;
}

.el-radio.is-disabled span {
  color: #f56c6c !important;
}

/*  新增样式 */
.dose-plan {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.progress-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

@media screen and (max-width: 768px) {
  .time-slots {
    flex-direction: column;
    gap: 10px;
  }

  .time-slot-item {
    min-width: 100%;
  }
}
</style>
