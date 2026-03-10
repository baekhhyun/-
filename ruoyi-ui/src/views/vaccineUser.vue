<!-- ==================== 文件路径：ruoyi-ui/src/views/dashboard/user.vue ==================== -->

<template>
  <div class="user-dashboard">
    <!-- 顶部横幅 -->
    <div class="hero-banner">
      <div class="banner-content">
        <h1>疫苗接种预约平台</h1>
        <p>安全、便捷、高效的疫苗接种服务</p>
        <el-button type="primary" size="large" @click="goToVaccineList" round>
          立即预约 <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>
      <div class="banner-image">
        <i
          class="el-icon-medicine"
          style="font-size: 120px; color: rgba(255, 255, 255, 0.3)"
        ></i>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <h2 class="section-title">快捷服务</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(action, index) in quickActions" :key="index">
          <el-card
            class="action-card"
            shadow="hover"
            @click.native="handleAction(action)"
            :body-style="{ padding: '20px' }"
          >
            <div
              class="action-icon"
              :style="{ background: action.color + '20' }"
            >
              <i :class="action.icon" :style="{ color: action.color }"></i>
            </div>
            <div class="action-title">{{ action.title }}</div>
            <div class="action-desc">{{ action.desc }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 我的预约 -->
    <div class="my-appointments">
      <div class="section-header">
        <h2 class="section-title">我的预约</h2>
        <el-button type="text" @click="goToMyAppointments">
          查看全部 <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col :span="8" v-for="(item, index) in myAppointments" :key="index">
          <el-card
            class="appointment-card"
            shadow="hover"
            :body-style="{ padding: '15px' }"
          >
            <div class="card-header">
              <span class="vaccine-name">{{
                item.vaccineName || "未知疫苗"
              }}</span>
              <el-tag
                :type="getStatusType(item.status)"
                size="small"
                effect="dark"
              >
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
            <div class="card-body">
              <p>
                <i class="el-icon-date"></i> 预约日期：{{
                  item.appointmentDate || "未设置"
                }}
              </p>
              <p>
                <i class="el-icon-time"></i> 时间段：{{
                  formatTimeSlot(item.timeSlot)
                }}
              </p>
              <p><i class="el-icon-location"></i> 接种点：社区卫生服务中心</p>
            </div>
            <!-- <div
              class="card-footer"
              v-if="item.status === '0' || item.status === '1'"
            >
              <el-button type="text" @click="cancelAppointment(item)"
                >取消预约</el-button
              >
            </div> -->
          </el-card>
        </el-col>

        <!-- 空状态 -->
        <el-col :span="24" v-if="myAppointments.length === 0">
          <el-card class="empty-card">
            <div class="empty-state">
              <i class="el-icon-info"></i>
              <p>暂无预约记录，快去预约吧！</p>
              <el-button type="primary" size="small" @click="goToVaccineList"
                >立即预约</el-button
              >
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 底部信息 -->
    <el-row :gutter="20" class="bottom-info">
      <el-col :span="16">
        <el-card class="info-card" shadow="hover">
          <div slot="header">
            <span
              ><i class="el-icon-info" style="color: #409eff"></i>
              接种须知</span
            >
          </div>
          <div class="notice-list">
            <div class="notice-item">
              <i class="el-icon-check" style="color: #67c23a"></i>
              <span>请提前10分钟到达接种点，携带身份证原件</span>
            </div>
            <div class="notice-item">
              <i class="el-icon-check" style="color: #67c23a"></i>
              <span>接种前请勿空腹，穿着宽松衣物</span>
            </div>
            <div class="notice-item">
              <i class="el-icon-check" style="color: #67c23a"></i>
              <span>如有发热、急性疾病请暂缓接种</span>
            </div>
            <div class="notice-item">
              <i class="el-icon-check" style="color: #67c23a"></i>
              <span>接种后需留观30分钟，无异常方可离开</span>
            </div>
            <div class="notice-item">
              <i class="el-icon-check" style="color: #67c23a"></i>
              <span>24小时内接种部位避免沾水，清淡饮食</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="info-card" shadow="hover">
          <div slot="header">
            <span
              ><i class="el-icon-bell" style="color: #e6a23c"></i>
              最新公告</span
            >
          </div>
          <div class="notice-list">
            <div
              v-for="(notice, index) in notices"
              :key="index"
              class="notice-item"
              @click="viewNoticeDetail(notice)"
              style="cursor: pointer"
            >
              <span class="notice-title">{{
                notice.noticeTitle || "系统公告"
              }}</span>
              <span class="notice-date">{{ notice.createTime || "刚刚" }}</span>
            </div>

            <div v-if="notices.length === 0" class="notice-item">
              <span class="notice-title">欢迎使用疫苗接种预约平台</span>
              <span class="notice-date">今天</span>
            </div>
            <div v-if="notices.length === 0" class="notice-item">
              <span class="notice-title">春节期间正常接种</span>
              <span class="notice-date">昨天</span>
            </div>
            <div v-if="notices.length === 0" class="notice-item">
              <span class="notice-title">流感疫苗到货通知</span>
              <span class="notice-date">3天前</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getMyAppointments, getNotices } from "@/api/vaccine/dashboard";
import { cancelAppointment } from "@/api/vaccine/appointment";

export default {
  name: "UserDashboard",
  data() {
    return {
      myAppointments: [],
      notices: [],
      quickActions: [
        {
          title: "疫苗预约",
          desc: "选择疫苗立即预约",
          icon: "el-icon-edit",
          color: "#409EFF",
          name: "vaccineAppointment", // 改成路由 name
        },
        {
          title: "我的预约",
          desc: "查看预约记录",
          icon: "el-icon-s-order",
          color: "#67C23A",
          name: "myappointment", // 改成路由 name
        },
        {
          title: "接种指南",
          desc: "了解接种流程",
          icon: "el-icon-guide",
          color: "#E6A23C",
          action: "showGuide",
        },
        {
          title: "联系我们",
          desc: "获取帮助支持",
          icon: "el-icon-phone",
          color: "#F56C6C",
          action: "showContact",
        },
      ],
    };
  },
  created() {
    this.getMyAppointments();
    this.getNotices();
  },
  methods: {
    viewNoticeDetail(notice) {
      // 格式化公告内容，如果是HTML就原样显示
      const content = notice.noticeContent || "暂无内容";

      this.$alert(
        `<div class="notice-detail-content">
      <div style="margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0;">
        <span style="display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; background: #e6a23c20; color: #e6a23c;">
          ${this.getNoticeTypeText(notice.noticeType)}
        </span>
        <span style="margin-left: 10px; color: #999; font-size: 12px;">
          发布时间：${this.formatDateTime(notice.createTime)}
        </span>
      </div>
      <div style="font-size: 20px; font-weight: 600; color: #2c3e50; margin-bottom: 15px;">
        ${notice.noticeTitle || "系统公告"}
      </div>
      <div style="font-size: 16px; color: #606266; line-height: 1.8; max-height: 400px; overflow-y: auto; padding-right: 10px;">
        ${content}
      </div>
    </div>`,
        "公告详情",
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: "关闭",
          showCancelButton: false,
          closeOnClickModal: true,
          customClass: "notice-detail-dialog",
          callback: (action) => {
            console.log("已读公告:", notice.noticeTitle);
          },
        }
      );
    },
    getNoticeTypeText(type) {
      const texts = {
        1: "系统公告",
        2: "疫苗动态",
        3: "节假日",
        4: "温馨提示",
        5: "功能更新",
      };
      return texts[type] || "系统公告";
    },
    formatDateTime(date) {
      if (!date) return "未知时间";
      const d = new Date(date);
      const year = d.getFullYear();
      const month = (d.getMonth() + 1).toString().padStart(2, "0");
      const day = d.getDate().toString().padStart(2, "0");
      const hours = d.getHours().toString().padStart(2, "0");
      const minutes = d.getMinutes().toString().padStart(2, "0");
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    getMyAppointments() {
      getMyAppointments({ pageNum: 1, pageSize: 3 }).then((response) => {
        this.myAppointments = response.rows || [];
      });
    },

    getNotices() {
      getNotices({ pageNum: 1, pageSize: 5 }).then((response) => {
        this.notices = response.rows || [];
        console.log(this.notices);
      });
    },

    handleAction(action) {
      if (action.name) {
        this.$router.push({ name: action.name });
      } else if (action.action === "showGuide") {
        this.$alert(
          `<div style="padding: 10px;font-size: 16px;">
            <p><strong>接种流程：</strong></p>
            <p>1. 在线预约 - 选择疫苗和时间</p>
            <p>2. 现场确认 - 提前10分钟到达</p>
            <p>3. 健康咨询 - 医生评估</p>
            <p>4. 疫苗接种 - 专业护士接种</p>
            <p>5. 留观30分钟 - 无异常方可离开</p>
          </div>`,
          "接种指南",
          { dangerouslyUseHTMLString: true, confirmButtonText: "知道了" }
        );
      } else if (action.action === "showContact") {
        this.$alert(
          `<div style="padding: 10px;font-size: 16px;">
            <p><i class="el-icon-phone"></i> 服务热线：0769-12345678</p>
            <p><i class="el-icon-time"></i> 工作时间：周一至周五 9:00-17:00</p>
            <p><i class="el-icon-location"></i> 接种地址：社区卫生服务中心</p>
          </div>`,
          "联系我们",
          { dangerouslyUseHTMLString: true, confirmButtonText: "知道了" }
        );
      }
    },

    cancelAppointment(row) {
      this.$confirm("确定取消该预约吗？", "提示", { type: "warning" }).then(
        () => {
          cancelAppointment(row.id).then(() => {
            this.$message.success("取消成功");
            this.getMyAppointments();
          });
        }
      );
    },

    goToVaccineList() {
      this.$router.push({ name: "vaccineAppointment" });
    },

    goToMyAppointments() {
      this.$router.push({ name: "myappointment" });
    },

    formatTimeSlot(slot) {
      if (!slot) return "未选择";
      const map = {
        1: "上午 09:00-11:00",
        2: "下午 14:00-16:00",
        3: "晚上 18:00-20:00",
      };
      return map[slot] || slot;
    },

    getStatusType(status) {
      const map = { 0: "warning", 1: "success", 2: "info", 3: "danger" };
      return map[status] || "info";
    },

    getStatusText(status) {
      const map = { 0: "待确认", 1: "已确认", 2: "已完成", 3: "已取消" };
      return map[status] || status;
    },
  },
};
</script>

<style scoped>
.user-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 40px 60px;
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: white;
  position: relative;
  overflow: hidden;
}

.banner-content h1 {
  font-size: 36px;
  margin: 0 0 15px;
  font-weight: 600;
}

.banner-content p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 25px;
}

.banner-content .el-button {
  padding: 12px 30px;
  font-size: 16px;
  background: white;
  color: #667eea;
  border: none;
}

.banner-content .el-button:hover {
  transform: scale(1.05);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.section-title {
  font-size: 22px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0 0 20px;
  padding-left: 15px;
  position: relative;
}

.section-title::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.quick-actions {
  margin-bottom: 40px;
}

.action-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  border-radius: 12px;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1) !important;
}

.action-icon {
  width: 60px;
  height: 60px;
  border-radius: 30px;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon i {
  font-size: 30px;
}

.action-title {
  font-size: 18px;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 5px;
}

.action-desc {
  font-size: 13px;
  color: #7f8c8d;
}

.my-appointments {
  margin-bottom: 40px;
}

.appointment-card {
  border-radius: 12px;
  border: none;
  height: 100%;
  transition: all 0.3s;
}

.appointment-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.vaccine-name {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
}

.card-body p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

.card-body i {
  margin-right: 5px;
  color: #409eff;
  width: 18px;
}

.card-footer {
  text-align: right;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  margin-top: 10px;
}

.empty-card {
  border-radius: 12px;
  border: 2px dashed #dcdfe6;
  background-color: #fafafa;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.empty-state i {
  font-size: 48px;
  color: #909399;
  margin-bottom: 10px;
}

.empty-state p {
  margin: 0 0 15px;
}

.bottom-info {
  margin-top: 20px;
}

.info-card {
  border-radius: 12px;
  border: none;
  height: 100%;
}

.info-card >>> .el-card__header {
  padding: 15px 20px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e4e7ed;
}

.notice-list {
  padding: 10px 0;
}

.notice-item {
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item i {
  margin-right: 10px;
  color: #67c23a;
  font-size: 16px;
}

.notice-title {
  flex: 1;
  color: #2c3e50;
  font-size: 16px;
}

.notice-date {
  color: #999;
  font-size: 12px;
  margin-left: 10px;
}
.info-card >>> .el-card__body {
  padding: 0 !important;
}
/* ========== 公告详情弹窗样式 ========== */
.notice-detail-dialog {
  max-width: 600px;
}

.notice-detail-dialog .el-message-box__content {
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}

.notice-detail-content {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
    "Helvetica Neue", Arial, sans-serif;
}

.notice-detail-content p {
  margin: 10px 0;
}

.notice-detail-content ul,
.notice-detail-content ol {
  padding-left: 20px;
  margin: 10px 0;
}

.notice-detail-content li {
  margin: 5px 0;
}

.notice-detail-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.notice-detail-content th,
.notice-detail-content td {
  padding: 8px;
  border: 1px solid #e4e7ed;
  text-align: left;
}

.notice-detail-content th {
  background-color: #f5f7fa;
  font-weight: 500;
}

/* 鼠标悬停效果 */
.notice-item {
  transition: all 0.3s;
}

.notice-item:hover {
  background-color: #f5f7fa;
  transform: translateX(5px);
}

.notice-item:hover .notice-title {
  color: #409eff;
}
</style>
