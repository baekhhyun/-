<template>
  <div class="app-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/vaccine/appointment/user' }"
        >疫苗浏览</el-breadcrumb-item
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
        <el-descriptions title="所选疫苗信息" :column="2" border>
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
        </el-descriptions>
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
            :disabled="!userName"
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
      </ul>
    </el-card>
  </div>
</template>

<script>
import { getVaccine } from "@/api/vaccine/vaccine"; // 使用疫苗模块的API
import {
  addAppointment,
  getTimeSlotRemaining,
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

        // 检查是否为周末（0=周日,6=周六）
        // const day = selectedDate.getDay();
        // if (day === 0 || day === 6) {
        //   callback(new Error("周末不可预约，请选择工作日"));
        //   return;
        // }

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
          // 计算7天后的日期（包含今天）
          const maxDate = new Date(now);
          maxDate.setDate(now.getDate() + 6); // 今天是第1天，+6天是第7天
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
  beforeRouteEnter(to, from, next) {
    console.log("beforeRouteEnter", to.query);
    next((vm) => {
      vm.initFromRoute(to);
    });
  },

  beforeRouteUpdate(to, from, next) {
    console.log(
      "beforeRouteUpdate",
      from.query.vaccineId,
      "->",
      to.query.vaccineId
    );

    const newVaccineId = to.query.vaccineId;
    const oldVaccineId = from.query.vaccineId;

    if (newVaccineId && newVaccineId !== oldVaccineId) {
      this.handleVaccineChange(newVaccineId);
    }

    next();
  },

  created() {
    // this.initPage();
    this.initFromRoute(this.$route);
    this.$nextTick(() => {
      this.initPage();
    });
  },
  computed: {
    // 是否有可用时间段
    hasAvailableSlot() {
      return this.timeSlots.some((slot) => slot.available);
    },

    // 格式化显示
    formattedTimeSlots() {
      return this.timeSlots.map((slot) => ({
        ...slot,
        displayLabel: `${slot.label} ${
          slot.available
            ? `(剩余${slot.remaining}/${slot.maxCapacity})`
            : "(已满)"
        }`,
      }));
    },
  },
  watch: {
    // 监听预约日期变化，动态获取名额
    "formData.appointmentDate": {
      handler(newVal) {
        if (newVal) {
          this.getTimeSlotRemaining(newVal);
        } else {
          // 重置所有时间段剩余名额
          this.resetTimeSlots();
        }
      },
      immediate: false,
    },
  },

  methods: {
    initFromRoute(route) {
      const vaccineId = route.query.vaccineId;
      console.log("从路由初始化, vaccineId:", vaccineId);

      if (!vaccineId) {
        this.$message.error("未选择疫苗");
        this.$router.push("/vaccine/appointment/user");
        return;
      }

      // 转换为数字
      const numericId = parseInt(vaccineId);

      // 如果ID不同才重新加载
      if (this.vaccineId !== numericId) {
        this.vaccineId = numericId;
        this.loadVaccineInfo();
      }
    },

    // 处理疫苗切换
    handleVaccineChange(newVaccineId) {
      console.log("处理疫苗切换:", newVaccineId);

      if (!newVaccineId) return;

      // 转换为数字
      const numericId = parseInt(newVaccineId);

      // 如果相同就不处理
      if (this.vaccineId === numericId) {
        console.log("疫苗ID相同，不处理");
        return;
      }

      // 更新ID
      this.vaccineId = numericId;

      // 重置所有数据
      this.resetForNewVaccine();

      // 加载新疫苗信息
      this.loadVaccineInfo();
    },

    // 为新疫苗重置数据
    resetForNewVaccine() {
      console.log("为新疫苗重置数据");

      // 清空疫苗信息
      this.vaccine = null;
      this.vaccineName = "";

      // 重置表单
      this.formData = {
        vaccineId: this.vaccineId,
        appointmentDate: "",
        timeSlot: "1",
        phone: this.formData.phone, // 保留电话
        remark: "",
      };

      // 如果有用户电话，保留
      if (this.userInfo?.phonenumber) {
        this.formData.phone = this.userInfo.phonenumber;
      }
    },

    // 加载疫苗信息
    loadVaccineInfo() {
      if (!this.vaccineId) {
        console.error("疫苗ID为空");
        return;
      }

      this.loading = true;
      console.log("加载疫苗信息, ID:", this.vaccineId);

      getVaccineInfo(this.vaccineId)
        .then((response) => {
          console.log("疫苗信息响应:", response);

          if (response.code === 200 && response.data) {
            this.vaccine = response.data;
            this.vaccineName = response.data.name;
            this.formData.vaccineId = this.vaccineId;

            // 更新页面标题
            document.title = `预约${this.vaccineName} - 疫苗接种预约平台`;

            // 检查库存
            if (response.data.stock <= 0) {
              this.$message.warning("该疫苗库存不足");
            }
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

    // 返回疫苗列表
    goBack() {
      this.$router.push("/vaccine/appointment/user");
    },
    getTimeSlotRemaining(date) {
      if (!date) return;

      this.slotLoading = true;
      getTimeSlotRemaining(date)
        .then((response) => {
          if (response.code === 200 && response.data) {
            const slotData = response.data;

            // 更新时间段数据
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

            // 如果当前选中的时间段不可用，清空选择
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
          // 出错时使用默认值
          this.resetTimeSlots();
        })
        .finally(() => {
          this.slotLoading = false;
        });
    },

    // 重置时间段数据
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

    // 检查时间段是否可选
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

    // 提交预约前检查
    submitAppointment() {
      // 先检查时间段是否可用
      if (!this.checkTimeSlotAvailability()) {
        return;
      }
    },
    // 初始化页面
    initPage() {
      // 先获取用户信息
      this.loadUserInfo();

      // 获取URL参数中的疫苗ID
      this.vaccineId =
        this.$route.query.vaccineId || this.$route.params.vaccineId;

      if (!this.vaccineId) {
        this.$message.error("未选择疫苗，请返回重新选择");
        this.goBack();
        return;
      }

      // 加载疫苗信息
      this.loadVaccineInfo();
    },

    // 加载用户信息
    loadUserInfo() {
      getUserProfile()
        .then((response) => {
          if (response.code === 200 && response.data) {
            this.userInfo = response.data;
            // 优先显示昵称，没有则显示用户名
            this.userName =
              response.data.nickName || response.data.userName || "用户";

            // 如果有用户手机号，可以自动填充
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

    // 刷新用户信息
    refreshUserInfo() {
      this.userName = "获取中...";
      this.loadUserInfo();
    },

    // 加载疫苗信息
    loadVaccineInfo() {
      this.loading = true;
      getVaccine(this.vaccineId)
        .then((response) => {
          if (response.code === 200) {
            this.vaccine = response.data;
            this.formData.vaccineId = this.vaccineId;
            this.formData.appointmentDate = "";
            this.formData.timeSlot = "1";

            // 重置时间段剩余名额
            this.resetTimeSlots();
            // 如果库存为0，禁止预约
            if (this.vaccine.stock !== undefined && this.vaccine.stock <= 0) {
              this.$message.error("该疫苗库存不足，暂时无法预约");
              this.goBack();
            }
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

    // 格式化适用年龄
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

    // 获取年龄标签类型
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

    // 提交预约
    submitAppointment() {
      // 先检查用户信息是否加载完成
      if (!this.userName || this.userName === "获取中...") {
        this.$message.warning("正在获取用户信息，请稍后重试");
        this.refreshUserInfo();
        return;
      }

      this.$refs.appointmentForm.validate((valid) => {
        if (valid) {
          this.submitting = true;

          // 构建预约数据
          const appointmentData = {
            vaccineId: this.formData.vaccineId,
            appointmentDate: this.formData.appointmentDate,
            timeSlot: this.formData.timeSlot,
            phone: this.formData.phone,
            remark: this.formData.remark,
          };

          // 调用API提交预约
          addAppointment(appointmentData)
            .then((response) => {
              this.submitting = false;
              if (response.code === 200) {
                this.$message.success("预约成功！请按时前往接种点");
                setTimeout(() => {
                  this.$router.push("/vaccine/appointment/my");
                }, 1500);
              } else {
                // 根据不同的错误信息给出更友好的提示
                let errorMsg = response.msg || "预约失败";
                if (errorMsg.includes("当天已有有效预约")) {
                  errorMsg = "您当天已有其他预约，如需重新预约请先取消原有预约";
                } else if (errorMsg.includes("已预约过此疫苗")) {
                  errorMsg = "您已预约过此疫苗，如需重新预约请先取消原有预约";
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

    // 返回疫苗列表
    goBack() {
      this.$router.push("/vaccine/appointment/user");
    },

    // 重置表单
    resetForm() {
      this.formData = {
        vaccineId: this.vaccineId, // 保留疫苗ID
        appointmentDate: "",
        timeSlot: "1", // 重置为默认值
        phone: "",
        remark: "",
      };

      // 重置时间段数据
      this.resetTimeSlots();

      // 如果有用户手机号，重新填充
      if (this.userInfo.phonenumber) {
        this.formData.phone = this.userInfo.phonenumber;
      }

      this.$message.info("请重新选择预约日期");
    },
  },
};
</script>

<style scoped>
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
/* 原有的样式保持不变 */
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

/* 响应式调整 */
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
