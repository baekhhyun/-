<template>
  <div class="admin-dashboard">
    <!-- 欢迎语 -->
    <div class="welcome-section">
      <h1 class="welcome-title">👋 下午好，{{ userName }}</h1>
      <p class="welcome-subtitle">
        今天是 {{ currentDate }}，有
        <span class="highlight">{{ pendingCount }}</span> 个预约待处理
      </p>
    </div>

    <!-- 统计卡片行 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in statsCards" :key="index">
        <el-card
          class="stat-card"
          :style="{ background: stat.color }"
          shadow="hover"
          :body-style="{ padding: '20px' }"
        >
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-trend" v-if="stat.trend !== 0">
                <i
                  :class="stat.trend > 0 ? 'el-icon-top' : 'el-icon-bottom'"
                ></i>
                {{ Math.abs(stat.trend) }}% 较昨日
              </div>
            </div>
            <div class="stat-icon">
              <i :class="stat.icon"></i>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!--  多剂次统计卡片行（新增） -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="multi-dose-card" shadow="hover">
          <div class="multi-dose-content">
            <div class="multi-dose-icon">
              <i class="el-icon-s-data"></i>
            </div>
            <div class="multi-dose-info">
              <div class="multi-dose-label">多剂次疫苗数</div>
              <div class="multi-dose-value">{{ multiDoseCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="multi-dose-card" shadow="hover">
          <div class="multi-dose-content">
            <div class="multi-dose-icon">
              <i class="el-icon-success"></i>
            </div>
            <div class="multi-dose-info">
              <div class="multi-dose-label">首剂接种率</div>
              <div class="multi-dose-value">{{ firstDoseRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="multi-dose-card" shadow="hover">
          <div class="multi-dose-content">
            <div class="multi-dose-icon">
              <i class="el-icon-refresh"></i>
            </div>
            <div class="multi-dose-info">
              <div class="multi-dose-label">加强剂接种率</div>
              <div class="multi-dose-value">{{ boosterDoseRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="multi-dose-card" shadow="hover">
          <div class="multi-dose-content">
            <div class="multi-dose-icon">
              <i class="el-icon-bell"></i>
            </div>
            <div class="multi-dose-info">
              <div class="multi-dose-label">今日待接种</div>
              <div class="multi-dose-value">{{ todayRemindersCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 预约趋势图 -->
      <el-col :span="16">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-data-line" style="color: #409eff"></i>
              预约趋势</span
            >
            <el-radio-group
              size="small"
              v-model="trendType"
              @change="handleTrendChange"
            >
              <el-radio-button label="week">近7天</el-radio-button>
              <el-radio-button label="month">近30天</el-radio-button>
            </el-radio-group>
          </div>
          <div id="trendChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>

      <!-- 预约状态分布 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-pie-chart" style="color: #67c23a"></i>
              预约状态分布</span
            >
          </div>
          <div id="pieChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 热门疫苗预约榜 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-medal" style="color: #e6a23c"></i>
              热门疫苗预约榜</span
            >
          </div>
          <div id="barChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>

      <!--  疫苗消耗统计（新增） -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-s-data" style="color: #409eff"></i>
              疫苗消耗统计</span
            >
          </div>
          <div id="consumptionChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>

      <!--  接种覆盖率统计（新增） -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-pie-chart" style="color: #67c23a"></i>
              接种覆盖率</span
            >
          </div>
          <div id="coverageChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 待处理预约列表 -->
      <el-col :span="8">
        <el-card
          class="pending-card"
          shadow="hover"
          :body-style="{ padding: '0' }"
        >
          <div slot="header" class="pending-header">
            <span
              ><i class="el-icon-warning" style="color: #e6a23c"></i> 待处理预约
              ({{ pendingCount }})</span
            >
            <el-button type="text" @click="goToPendingList">
              查看全部 <i class="el-icon-arrow-right"></i>
            </el-button>
          </div>

          <!-- 待处理列表 -->
          <div class="pending-list" v-loading="loading">
            <div v-if="pendingList.length === 0" class="empty-state">
              <i class="el-icon-check"></i>
              <p>暂无待处理预约</p>
            </div>

            <div
              v-for="(item, index) in pendingList"
              :key="index"
              class="pending-item"
            >
              <div class="item-header">
                <span class="item-user">{{ item.userName || "未知用户" }}</span>
                <el-tag size="mini" type="warning" effect="light"
                  >待确认</el-tag
                >
              </div>
              <div class="item-info">
                <div class="info-line">
                  疫苗名称：
                  <span style="font-weight: bold">{{
                    item.vaccineName || "未知疫苗"
                  }}</span>
                  <el-tag
                    v-if="item.doseNumber && item.doseNumber > 0"
                    size="mini"
                    type="info"
                    style="margin-left: 8px"
                  >
                    第{{ item.doseNumber }}剂
                  </el-tag>
                  <el-tag
                    v-else
                    size="mini"
                    type="success"
                    style="margin-left: 8px"
                  >
                    单剂次
                  </el-tag>
                </div>
                <div class="info-line">
                  <i class="el-icon-date"></i>
                  <span
                    >{{ item.appointmentDate }} {{ formatTimeSlot(item) }}</span
                  >
                </div>
                <div class="info-line">
                  <i class="el-icon-phone"></i>
                  <span>{{ item.phone }}</span>
                </div>
              </div>
              <div class="item-actions">
                <el-button
                  type="primary"
                  size="mini"
                  @click="handleConfirm(item)"
                  :loading="item.loading"
                  style="flex: 1; margin-right: 5px"
                >
                  确认
                </el-button>
                <el-button
                  type="danger"
                  size="mini"
                  @click="handleCancel(item)"
                  :loading="item.loading"
                  style="flex: 1"
                >
                  取消
                </el-button>
              </div>
            </div>
            <div v-if="pendingList.length > 3" class="more-tip">
              <i class="el-icon-more"></i> 还有
              {{ pendingList.length - 3 }} 条待处理，<el-link
                type="primary"
                @click="goToPendingList"
                >查看全部</el-link
              >
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 时间段预约分布 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <div slot="header" class="chart-header">
            <span
              ><i class="el-icon-time" style="color: #f56c6c"></i>
              时间段预约分布</span
            >
          </div>
          <div id="timeSlotChart" style="height: 350px; width: 100%"></div>
        </el-card>
      </el-col>

      <!-- 今日待接种提醒列表（新增） -->
      <el-col :span="8">
        <el-card class="reminder-card" shadow="hover">
          <div slot="header" class="reminder-header">
            <span
              ><i class="el-icon-bell" style="color: #e6a23c"></i>
              今日待接种提醒 ({{ todayRemindersCount }})</span
            >
          </div>
          <div class="reminder-list" v-loading="reminderLoading">
            <div v-if="todayReminders.length === 0" class="empty-state">
              <i class="el-icon-check"></i>
              <p>今日无待接种提醒</p>
            </div>
            <div
              v-for="item in todayReminders"
              :key="item.id"
              class="reminder-item"
            >
              <div class="reminder-user">{{ item.userName }}</div>
              <div class="reminder-info">
                <el-tag size="mini" type="warning">
                  {{ item.vaccineName }}
                </el-tag>
                <el-tag v-if="item.doseNumber" size="mini" type="info">
                  第{{ item.doseNumber }}剂
                </el-tag>
              </div>
              <div class="reminder-date">
                <i class="el-icon-date"></i> {{ item.nextDoseDate }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from "echarts";
import {
  getDashboardStats,
  getTrendData,
  getVaccineRanking,
  getPendingList,
} from "@/api/vaccine/dashboard";
import {
  updateAppointment,
  getConsumptionStats,
  getCoverageStats,
  getTodayReminders,
  listAppointment,
} from "@/api/vaccine/appointment";
import { listVaccine } from "@/api/vaccine/vaccine";
export default {
  name: "AdminDashboard",
  data() {
    return {
      refreshTimer: null,
      refreshInterval: 60000,
      isRefreshing: false,
      resizeTimer: null,
      observer: null,
      trendData: null,
      pieData: null,
      barData: null,
      timeSlotData: null,
      userName: "管理员",
      currentDate: "",
      pendingCount: 0,
      pendingList: [],
      loading: false,
      trendType: "week",
      //  多剂次统计数据（新增）
      multiDoseCount: 0,
      firstDoseRate: 0,
      boosterDoseRate: 0,
      todayReminders: [],
      todayRemindersCount: 0,
      reminderLoading: false,
      consumptionStats: [],
      coverageStats: [],
      statsCards: [
        {
          label: "总预约数",
          value: 0,
          icon: "el-icon-s-order",
          color: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
          trend: 0,
        },
        {
          label: "今日预约",
          value: 0,
          icon: "el-icon-date",
          color: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
          trend: 0,
        },
        {
          label: "已完成",
          value: 0,
          icon: "el-icon-success",
          color: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
          trend: 0,
        },
        {
          label: "疫苗种类",
          value: 0,
          icon: "el-icon-shopping-bag-1",
          color: "linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)",
          trend: 0,
        },
      ],
      trendChart: null,
      pieChart: null,
      barChart: null,
      timeSlotChart: null,
      consumptionChart: null,
      coverageChart: null,
      trendDates: [],
      trendValues: [],
    };
  },
  created() {
    this.initData();
    this.getUserName();
    this.formatCurrentDate();
    this.startAutoRefresh();
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts();
    });
    window.addEventListener("resize", this.handleResize);
    this.observeContainer();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.handleResize);
    this.disposeCharts();
  },
  methods: {
    startAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer);
      }
      this.refreshTimer = setInterval(() => {
        this.refreshDashboard();
      }, this.refreshInterval);
    },

    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer);
        this.refreshTimer = null;
      }
    },

    refreshDashboard() {
      if (this.isRefreshing) return;
      this.isRefreshing = true;
      Promise.all([
        this.getStats(),
        this.getTrend(),
        this.getRanking(),
        this.getPendingList(),
        this.getMultiDoseStats(),
        this.getTodayRemindersList(),
      ])
        .catch((error) => {
          // console.error("刷新失败:", error);
        })
        .finally(() => {
          this.isRefreshing = false;
        });
    },

    initData() {
      this.getStats();
      this.getTrend();
      this.getRanking();
      this.getPendingList();
      this.getMultiDoseStats();
      this.getTodayRemindersList();
    },

    getUserName() {
      this.userName = this.$store.getters.name || "管理员";
    },

    formatCurrentDate() {
      const date = new Date();
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const weekdays = [
        "星期日",
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
      ];
      const weekday = weekdays[date.getDay()];
      this.currentDate = `${year}年${month}月${day}日 ${weekday}`;
    },

    // ==========  多剂次统计方法==========
    getMultiDoseStats() {
      // console.log("========== 开始计算多剂次疫苗统计 ==========");
      // 第一步：获取疫苗列表，统计多剂次疫苗数
      listVaccine({ status: "0", pageSize: 999 })
        .then((vaccineRes) => {
          const vaccines = vaccineRes.rows || [];

          // 多剂次疫苗数（is_multi_dose === 1）
          this.multiDoseCount = vaccines.filter(
            (v) => v.isMultiDose === 1
          ).length;
          // console.log("多剂次疫苗数:", this.multiDoseCount);
        })
        .catch((err) => {
          // console.error("获取疫苗列表失败:", err);
        });

      // 第二步：获取所有预约记录，手动计算首剂接种率
      listAppointment({ pageNum: 1, pageSize: 999 })
        .then((appointmentRes) => {
          const appointments = appointmentRes.rows || [];
          // console.log("预约记录总数:", appointments.length);

          if (appointments.length === 0) {
            // console.log("没有预约记录，接种率为 0");
            this.firstDoseRate = 0;
            this.boosterDoseRate = 0;
            return;
          }

          // 按用户+疫苗分组，统计每个疫苗的接种进度
          const userVaccineMap = new Map();

          appointments.forEach((app) => {
            if (app.status === "3") return;

            const key = `${app.userId}_${app.vaccineId}`;

            if (!userVaccineMap.has(key)) {
              userVaccineMap.set(key, {
                userId: app.userId,
                vaccineId: app.vaccineId,
                doses: [],
              });
            }

            const record = userVaccineMap.get(key);
            record.doses.push({
              doseNumber: app.doseNumber || 1,
              isCompleted: app.isCompleted || 0,
              status: app.status,
            });
          });

          // console.log("用户-疫苗组合数:", userVaccineMap.size);

          // 统计首剂和加强剂
          let totalFirstDose = 0; // 已开始首剂的人数（有第1剂记录）
          let completedFirstDose = 0; // 已完成首剂的人数
          let totalBooster = 0; // 需要接种加强剂的人数（有第1剂完成记录）
          let completedBooster = 0; // 已完成全部剂次的人数

          userVaccineMap.forEach((record, key) => {
            const doses = record.doses;

            // 检查是否有第1剂记录
            const firstDose = doses.find((d) => d.doseNumber === 1);
            if (firstDose) {
              totalFirstDose++;
              // 第1剂已完成（isCompleted=1 或 status=已完成）
              if (firstDose.isCompleted === 1 || firstDose.status === "2") {
                completedFirstDose++;

                // 有第1剂完成记录，说明需要接种加强剂
                // 检查是否有其他剂次（第2、3...剂）
                const otherDoses = doses.filter((d) => d.doseNumber > 1);

                if (otherDoses.length > 0) {
                  totalBooster++;
                  // 检查最后一剂是否完成
                  const maxDose = Math.max(...doses.map((d) => d.doseNumber));
                  const lastDose = doses.find((d) => d.doseNumber === maxDose);
                  if (
                    lastDose &&
                    (lastDose.isCompleted === 1 || lastDose.status === "2")
                  ) {
                    completedBooster++;
                  }
                } else {
                }
              }
            }
          });

          // 计算接种率（保留整数）
          this.firstDoseRate =
            totalFirstDose > 0
              ? Math.round((completedFirstDose / totalFirstDose) * 100)
              : 0;

          this.boosterDoseRate =
            totalBooster > 0
              ? Math.round((completedBooster / totalBooster) * 100)
              : 0;

          // console.log("首剂接种率计算:", {
          //   totalFirstDose,
          //   completedFirstDose,
          //   rate: this.firstDoseRate + "%",
          // });

          // console.log("加强剂接种率计算:", {
          //   totalBooster,
          //   completedBooster,
          //   rate: this.boosterDoseRate + "%",
          // });

          // console.log("========== 多剂次疫苗统计计算完成 ==========");
        })
        .catch((err) => {
          // 出错时使用默认值
          this.firstDoseRate = 0;
          this.boosterDoseRate = 0;
        });

      // 第三步：获取消耗统计（用于图表）
      getConsumptionStats()
        .then((response) => {
          if (response.code === 200) {
            this.consumptionStats = response.data || [];
            this.updateConsumptionChart(this.consumptionStats);
          }
        })
        .catch((error) => {
          // console.error("获取消耗统计失败:", error);
        });

      // 第四步：获取覆盖率统计（用于图表）
      getCoverageStats()
        .then((response) => {
          if (response.code === 200) {
            this.coverageStats = response.data || [];
            this.updateCoverageChart(this.coverageStats);
          }
        })
        .catch((error) => {
          // console.error("获取覆盖率统计失败:", error);
        });

      // 第五步：获取今日提醒
      this.getTodayRemindersList();
    },

    getTodayRemindersList() {
      this.reminderLoading = true;
      getTodayReminders()
        .then((response) => {
          if (response.code === 200) {
            this.todayReminders = response.data || [];
            this.todayRemindersCount = this.todayReminders.length;
          }
          this.reminderLoading = false;
        })
        .catch((error) => {
          // console.error("获取今日提醒失败:", error);
          this.reminderLoading = false;
        });
    },

    getStats() {
      getDashboardStats()
        .then((response) => {
          if (response.code === 200) {
            const data = response.data || {};
            this.statsCards[0].value = data.totalAppointments || 0;
            this.statsCards[1].value = data.todayAppointments || 0;
            this.statsCards[2].value = data.completedCount || 0;
            this.statsCards[3].value = data.vaccineCount || 0;
            this.pendingCount = data.pendingCount || 0;

            this.updatePieChart(
              data.statusData || [
                { name: "待确认", value: 0 },
                { name: "已确认", value: 0 },
                { name: "已完成", value: 0 },
                { name: "已取消", value: 0 },
              ]
            );
          }
        })
        .catch((error) => {
          // console.error("获取统计数据失败", error);
        });
    },

    getTrend() {
      getTrendData({ type: this.trendType })
        .then((response) => {
          const data = response.data;
          if (Array.isArray(data) && data.length > 0) {
            this.trendDates = data.map((item) => item.date);
            this.trendValues = data.map((item) => item.count);
            this.$nextTick(() => {
              this.updateTrendChart();
            });
          } else {
            this.trendDates = [];
            this.trendValues = [];
          }
        })
        .catch((error) => {
          // console.error("获取趋势数据失败:", error);
        });
    },

    getRanking() {
      getVaccineRanking()
        .then((response) => {
          if (response.code === 200) {
            const data = response.data || {};
            this.updateBarChart(data.vaccineRank || []);
            this.updateTimeSlotChart(data.timeSlotData || []);
          }
        })
        .catch((error) => {
          // console.error("获取排行榜数据失败", error);
        });
    },

    getPendingList() {
      this.loading = true;
      getPendingList({ pageNum: 1, pageSize: 5 })
        .then((response) => {
          this.pendingList = response.rows || [];
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },

    // ========== 图表初始化 ==========

    initCharts() {
      const trendElement = document.getElementById("trendChart");
      if (trendElement) this.trendChart = echarts.init(trendElement);

      const pieElement = document.getElementById("pieChart");
      if (pieElement) this.pieChart = echarts.init(pieElement);

      const barElement = document.getElementById("barChart");
      if (barElement) this.barChart = echarts.init(barElement);

      const timeSlotElement = document.getElementById("timeSlotChart");
      if (timeSlotElement) this.timeSlotChart = echarts.init(timeSlotElement);

      const consumptionElement = document.getElementById("consumptionChart");
      if (consumptionElement)
        this.consumptionChart = echarts.init(consumptionElement);

      const coverageElement = document.getElementById("coverageChart");
      if (coverageElement) this.coverageChart = echarts.init(coverageElement);
    },

    // ========== 更新图表 ==========

    updateTrendChart() {
      if (!this.trendChart) return;
      const today = new Date();
      const todayStr = `${String(today.getMonth() + 1).padStart(
        2,
        "0"
      )}-${String(today.getDate()).padStart(2, "0")}`;
      const todayIndex = this.trendDates.findIndex((date) => date === todayStr);

      const option = {
        tooltip: { trigger: "axis", confine: true },
        grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
        xAxis: {
          type: "category",
          data: this.trendDates,
          axisLabel: {
            color: (value) => (value === todayStr ? "#ff4d4f" : "#666"),
            fontWeight: (value) => (value === todayStr ? "bold" : "normal"),
          },
        },
        yAxis: {
          type: "value",
          name: "预约数量",
          splitLine: { lineStyle: { type: "dashed", color: "#eee" } },
        },
        series: [
          {
            name: "预约数",
            type: "line",
            data: this.trendValues.map((value, index) => ({
              value: value,
              itemStyle:
                index === todayIndex
                  ? {
                      color: "#ff4d4f",
                      borderColor: "#ff4d4f",
                      borderWidth: 2,
                      shadowBlur: 10,
                    }
                  : null,
            })),
            smooth: true,
            symbol: "circle",
            symbolSize: 8,
            lineStyle: { color: "#409EFF", width: 3 },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "rgba(64,158,255,0.3)" },
                { offset: 1, color: "rgba(64,158,255,0.1)" },
              ]),
            },
          },
        ],
      };
      this.trendChart.setOption(option);
    },

    updatePieChart(data) {
      if (!this.pieChart) return;
      const option = {
        tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
        legend: { orient: "vertical", left: "left" },
        series: [
          {
            type: "pie",
            radius: ["50%", "70%"],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: "#fff",
              borderWidth: 2,
            },
            data: data,
            color: ["#E6A23C", "#409EFF", "#67C23A", "#F56C6C"],
            label: { show: false },
            emphasis: {
              label: { show: true, fontSize: 16, fontWeight: "bold" },
            },
          },
        ],
      };
      this.pieChart.setOption(option);
    },

    updateBarChart(data) {
      if (!this.barChart) return;
      const option = {
        tooltip: { trigger: "axis" },
        grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
        xAxis: {
          type: "category",
          data: data.map((item) => item.name) || [],
          axisLabel: { rotate: 20 },
        },
        yAxis: { type: "value", name: "预约次数" },
        series: [
          {
            name: "预约数",
            type: "bar",
            data: data.map((item) => item.count) || [],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#409EFF" },
                { offset: 1, color: "#36CFC9" },
              ]),
            },
            barWidth: 30,
          },
        ],
      };
      this.barChart.setOption(option);
    },

    updateTimeSlotChart(data) {
      if (!this.timeSlotChart) return;
      const option = {
        tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
        legend: { orient: "vertical", left: "left" },
        series: [
          {
            type: "pie",
            radius: ["50%", "70%"],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: "#fff",
              borderWidth: 2,
            },
            data: data.length
              ? data
              : [
                  { name: "上午", value: 0 },
                  { name: "下午", value: 0 },
                  { name: "晚上", value: 0 },
                ],
            label: { show: false },
            emphasis: {
              label: { show: true, fontSize: 16, fontWeight: "bold" },
            },
            color: ["#36C2CE", "#77E4C8", "#FFC700"],
          },
        ],
      };
      this.timeSlotChart.setOption(option);
    },

    //  疫苗消耗统计图
    updateConsumptionChart(data) {
      if (!this.consumptionChart) return;
      const option = {
        tooltip: {
          trigger: "axis",
          axisPointer: { type: "shadow" },
          formatter: function (params) {
            const item = params[0];
            return `${item.name}<br/>库存消耗率: ${item.value}%`;
          },
        },
        grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
        xAxis: {
          type: "category",
          data: data.map((item) => item.vaccineName) || [],
          axisLabel: { rotate: 20, interval: 0 },
        },
        yAxis: { type: "value", name: "使用率 (%)" },
        series: [
          {
            name: "使用率",
            type: "bar",
            data: data.map((item) => item.usageRate || 0) || [],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#67C23A" },
                { offset: 1, color: "#409EFF" },
              ]),
              borderRadius: [4, 4, 0, 0],
            },

            label: {
              show: true,
              position: "top",
              formatter: "{c}%",
            },
          },
        ],
      };
      this.consumptionChart.setOption(option);
    },

    //  接种覆盖率图表
    updateCoverageChart(data) {
      if (!this.coverageChart) return;

      //  过滤掉使用率为 0 或没有数据的疫苗
      const filteredData = (data || []).filter((item) => {
        // 只显示有数据的疫苗
        return (
          item.coverageRate > 0 || item.usedCount > 0 || item.totalStock > 0
        );
      });

      // 如果过滤后没有数据，显示提示
      if (filteredData.length === 0) {
        this.coverageChart.setOption({
          title: {
            show: true,
            text: "暂无接种数据",
            left: "center",
            top: "center",
            textStyle: {
              color: "#909399",
              fontSize: 14,
            },
          },
          series: [],
        });
        return;
      }

      const option = {
        tooltip: {
          trigger: "item",
          formatter: function (params) {
            return `${params.name}<br/>接种覆盖率: ${params.value}%`;
          },
        },
        legend: {
          orient: "vertical",
          left: "left",
          data: filteredData.map((item) => item.vaccineName),
        },
        series: [
          {
            name: "接种覆盖率",
            type: "pie",
            radius: ["50%", "70%"],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: "#fff",
              borderWidth: 2,
            },
            data: filteredData.map((item) => ({
              name: item.vaccineName,
              value: item.coverageRate || 0,
            })),
            label: {
              show: true,
              formatter: "{b}: {d}%",
              color: "#333",
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: "bold",
              },
            },
          },
        ],
      };

      this.coverageChart.setOption(option);
    },

    formatTimeSlot(row) {
      if (!row || !row.timeSlot) return "未选择";
      const map = {
        1: "上午 09:00-11:00",
        2: "下午 14:00-16:00",
        3: "晚上 18:00-20:00",
      };
      return map[row.timeSlot] || row.timeSlot;
    },

    handleConfirm(row) {
      this.$confirm("确认该预约吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.$set(row, "loading", true);
        updateAppointment({ id: row.id, status: "1" })
          .then(() => {
            this.$message.success("已确认");
            this.getPendingList();
            this.getStats();
            this.getTrend();
            this.getMultiDoseStats();
          })
          .finally(() => {
            this.$set(row, "loading", false);
          });
      });
    },

    handleCancel(row) {
      this.$confirm("取消该预约吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.$set(row, "loading", true);
        updateAppointment({ id: row.id, status: "3" })
          .then(() => {
            this.$message.success("已取消");
            this.getPendingList();
            this.getStats();
          })
          .finally(() => {
            this.$set(row, "loading", false);
          });
      });
    },

    handleTrendChange() {
      this.getTrend();
    },

    goToPendingList() {
      this.$router.push({ name: "VaccineAppointments" });
    },

    handleResize() {
      if (this.resizeTimer) clearTimeout(this.resizeTimer);
      this.resizeTimer = setTimeout(() => {
        this.trendChart?.resize();
        this.pieChart?.resize();
        this.barChart?.resize();
        this.timeSlotChart?.resize();
        this.consumptionChart?.resize();
        this.coverageChart?.resize();
      }, 100);
    },

    observeContainer() {
      if (window.ResizeObserver) {
        this.observer = new ResizeObserver(() => {
          window.requestAnimationFrame(() => this.handleResize());
        });
        const containers = [
          "trendChart",
          "pieChart",
          "barChart",
          "timeSlotChart",
          "consumptionChart",
          "coverageChart",
        ];
        containers.forEach((id) => {
          const element = document.getElementById(id);
          if (element && element.parentElement) {
            this.observer.observe(element.parentElement);
          }
        });
      }
    },

    disposeCharts() {
      this.trendChart?.dispose();
      this.pieChart?.dispose();
      this.barChart?.dispose();
      this.timeSlotChart?.dispose();
      this.consumptionChart?.dispose();
      this.coverageChart?.dispose();
    },
  },
};
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.welcome-section {
  margin-bottom: 30px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 500;
  color: #2c3e50;
  margin: 0 0 10px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
}

.highlight {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  color: white;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2) !important;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-trend {
  font-size: 12px;
  opacity: 0.9;
}

.stat-icon i {
  font-size: 48px;
  opacity: 0.8;
}

/*  多剂次统计卡片样式（新增） */
.multi-dose-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transition: all 0.3s;
}

.multi-dose-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.multi-dose-content {
  display: flex;
  align-items: center;
  padding: 15px;
}

.multi-dose-icon i {
  font-size: 40px;
  margin-right: 15px;
  opacity: 0.9;
}

.multi-dose-info {
  flex: 1;
}

.multi-dose-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.multi-dose-value {
  font-size: 28px;
  font-weight: bold;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
}

.chart-card >>> .el-card__header {
  padding: 15px 20px !important;
  border-bottom: 1px solid #f0f0f0;
  height: 51px !important;
  box-sizing: border-box !important;
  line-height: 21px !important;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  width: 100%;
}

.chart-header span {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  line-height: 1;
}

/*  待处理列表卡片样式 */
.pending-card {
  border-radius: 12px;
}

.pending-card >>> .el-card__header {
  padding: 15px 20px !important;
  border-bottom: 1px solid #f0f0f0;
  height: 51px !important;
  box-sizing: border-box !important;
  line-height: 21px !important;
  background-color: #f8fafc;
}

.pending-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  width: 100%;
}

.pending-header span {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  line-height: 1;
}

.pending-card >>> .el-card__body {
  padding: 0 !important;
}

.pending-list {
  height: 385px;
  overflow-y: auto;
  padding: 10px;
}

.pending-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  transition: all 0.3s;
  background-color: #fff;
}

.pending-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-user {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.info-line {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 13px;
  color: #606266;
}

.info-line i {
  width: 20px;
  color: #409eff;
  font-size: 14px;
}

.item-actions {
  display: flex;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #e4e7ed;
}

/*  今日待接种提醒卡片样式（新增） */
.reminder-card {
  border-radius: 12px;
}

.reminder-card >>> .el-card__header {
  padding: 15px 20px !important;
  border-bottom: 1px solid #f0f0f0;
  height: 51px !important;
  box-sizing: border-box !important;
  line-height: 21px !important;
  background-color: #f8fafc;
}

.reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  width: 100%;
}

.reminder-header span {
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  line-height: 1;
}

.reminder-card >>> .el-card__body {
  padding: 0 !important;
}

.reminder-list {
  height: 385px;
  overflow-y: auto;
  padding: 10px;
}

.reminder-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  background-color: #fff;
  transition: all 0.3s;
}

.reminder-item:hover {
  border-color: #e6a23c;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
}

.reminder-user {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
  margin-bottom: 8px;
}

.reminder-info {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.reminder-date {
  font-size: 12px;
  color: #909399;
}

.reminder-date i {
  margin-right: 4px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.empty-state i {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 10px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.more-tip {
  text-align: center;
  padding: 10px;
  color: #909399;
  font-size: 12px;
}

/* 滚动条美化 */
.pending-list::-webkit-scrollbar,
.reminder-list::-webkit-scrollbar {
  width: 6px;
}

.pending-list::-webkit-scrollbar-thumb,
.reminder-list::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.pending-list::-webkit-scrollbar-track,
.reminder-list::-webkit-scrollbar-track {
  background: #f5f7fa;
}
</style>
