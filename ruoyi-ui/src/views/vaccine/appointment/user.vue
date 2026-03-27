<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>疫苗预约</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="goToMyAppointment"
        >
          查看我的预约
        </el-button>
      </div>

      <!-- 疫苗筛选 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="疫苗名称">
          <el-input
            v-model="filter.name"
            placeholder="输入疫苗名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="适用年龄">
          <el-select v-model="filter.suitableAge" placeholder="全部" clearable>
            <el-option label="婴儿(0-2岁)" value="1" />
            <el-option label="儿童(3-12岁)" value="2" />
            <el-option label="青少年(13-17岁)" value="3" />
            <el-option label="成人(18-59岁)" value="4" />
            <el-option label="老人(60岁以上)" value="5" />
          </el-select>
        </el-form-item>
        <!-- 疫苗类型筛选 -->
        <el-form-item label="疫苗类型">
          <el-select v-model="filter.vaccineType" placeholder="全部" clearable>
            <el-option label="单剂次疫苗" value="0" />
            <el-option label="多剂次疫苗" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchVaccines">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 疫苗列表 -->
      <div class="vaccine-list">
        <el-row :gutter="20" type="flex" justify="start">
          <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="8"
            :xl="8"
            v-for="vaccine in vaccineList"
            :key="vaccine.id"
            class="vaccine-card-col"
          >
            <el-card class="vaccine-card" shadow="hover">
              <div slot="header">
                <span class="vaccine-name">{{ vaccine.name }}</span>
                <el-tag
                  v-if="vaccine.isMultiDose === 1"
                  type="warning"
                  size="small"
                  style="margin-left: 8px"
                >
                  多剂次
                </el-tag>
                <el-tag
                  v-else
                  type="info"
                  size="small"
                  style="margin-left: 8px"
                >
                  单剂次
                </el-tag>
                <el-tag
                  size="small"
                  :type="vaccine.stock > 0 ? 'success' : 'danger'"
                  style="float: right"
                >
                  {{ vaccine.stock > 0 ? "有库存" : "缺货" }}
                </el-tag>
              </div>

              <div class="vaccine-info">
                <p><strong>生产厂家：</strong>{{ vaccine.manufacturer }}</p>
                <p>
                  <strong>适用年龄：</strong>
                  <span v-if="vaccine.suitableAge === '1'">婴儿(0-2岁)</span>
                  <span v-else-if="vaccine.suitableAge === '2'"
                    >儿童(3-12岁)</span
                  >
                  <span v-else-if="vaccine.suitableAge === '3'"
                    >青少年(13-17岁)</span
                  >
                  <span v-else-if="vaccine.suitableAge === '4'"
                    >成人(18-59岁)</span
                  >
                  <span v-else-if="vaccine.suitableAge === '5'"
                    >老人(60岁以上)</span
                  >
                </p>
                <p><strong>库存数量：</strong>{{ vaccine.stock }} 剂</p>
                <!-- 多剂次疫苗额外信息 -->
                <div v-if="vaccine.isMultiDose === 1" class="multi-dose-info">
                  <p>
                    <strong>接种计划：</strong
                    >{{ vaccine.totalDoses || "?" }} 剂次，间隔
                    {{ vaccine.intervalDays || "?" }} 天
                  </p>
                  <p v-if="vaccine.doseSchedule">
                    <strong>接种安排：</strong>{{ vaccine.doseSchedule }}
                  </p>
                </div>
                <!-- 单剂次疫苗提示 -->
                <p v-else class="single-dose-tip">
                  <strong>温馨提示：</strong>只需接种一次
                </p>
                <p>
                  <strong>疫苗描述：</strong
                  >{{ vaccine.description || "暂无描述" }}
                </p>
              </div>

              <div class="vaccine-actions">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="vaccine.stock <= 0"
                  @click="goToAppointment(vaccine)"
                >
                  {{ vaccine.stock > 0 ? "立即预约" : "暂时缺货" }}
                </el-button>
                <el-button size="small" @click="viewVaccineDetail(vaccine)"
                  >查看详情</el-button
                >
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getVaccineList"
      />
    </el-card>

    <!-- 疫苗详情对话框 -->
    <el-dialog
      :title="detailDialog.title"
      :visible.sync="detailDialog.visible"
      width="600px"
    >
      <div v-if="detailDialog.vaccine">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="疫苗名称">
            {{ detailDialog.vaccine.name }}
            <el-tag
              v-if="detailDialog.vaccine.isMultiDose === 1"
              type="warning"
              size="small"
              style="margin-left: 8px"
            >
              多剂次
            </el-tag>
            <el-tag v-else type="info" size="small" style="margin-left: 8px">
              单剂次
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生产厂家">{{
            detailDialog.vaccine.manufacturer
          }}</el-descriptions-item>
          <el-descriptions-item label="适用年龄">
            <span v-if="detailDialog.vaccine.suitableAge === '1'"
              >婴儿(0-2岁)</span
            >
            <span v-else-if="detailDialog.vaccine.suitableAge === '2'"
              >儿童(3-12岁)</span
            >
            <span v-else-if="detailDialog.vaccine.suitableAge === '3'"
              >青少年(13-17岁)</span
            >
            <span v-else-if="detailDialog.vaccine.suitableAge === '4'"
              >成人(18-59岁)</span
            >
            <span v-else-if="detailDialog.vaccine.suitableAge === '5'"
              >老人(60岁以上)</span
            >
          </el-descriptions-item>
          <el-descriptions-item label="当前库存"
            >{{ detailDialog.vaccine.stock }} 剂</el-descriptions-item
          >
          <el-descriptions-item
            v-if="detailDialog.vaccine.isMultiDose === 1"
            label="接种计划"
          >
            {{ detailDialog.vaccine.totalDoses || "?" }} 剂次，间隔
            {{ detailDialog.vaccine.intervalDays || "?" }} 天
            <span v-if="detailDialog.vaccine.doseSchedule"
              >（{{ detailDialog.vaccine.doseSchedule }}）</span
            >
          </el-descriptions-item>
          <el-descriptions-item label="疫苗描述">
            <div style="white-space: pre-line">
              {{ detailDialog.vaccine.description || "暂无描述" }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialog.visible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="goToAppointment(detailDialog.vaccine)"
          :disabled="!detailDialog.vaccine || detailDialog.vaccine.stock <= 0"
          >立即预约</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listVaccine } from "@/api/vaccine/vaccine";

export default {
  name: "VaccineAppointmentUser",
  data() {
    return {
      filter: {
        name: "",
        suitableAge: "",
        vaccineType: "",
      },
      vaccineList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 9,
        name: undefined,
        suitableAge: undefined,
        isMultiDose: undefined,
        status: "0",
      },
      total: 0,
      loading: false,
      detailDialog: {
        visible: false,
        title: "",
        vaccine: null,
      },
    };
  },
  created() {
    this.getVaccineList();
  },
  methods: {
    getVaccineList() {
      this.loading = true;
      const params = {
        ...this.queryParams,
        name: this.filter.name || undefined,
        suitableAge: this.filter.suitableAge || undefined,
        isMultiDose: this.filter.vaccineType || undefined,
      };

      listVaccine(params)
        .then((response) => {
          this.vaccineList = response.rows;
          this.total = response.total;
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },

    searchVaccines() {
      this.queryParams.pageNum = 1;
      this.getVaccineList();
    },

    resetFilter() {
      this.filter = {
        name: "",
        suitableAge: "",
        vaccineType: "",
      };
      this.searchVaccines();
    },

    viewVaccineDetail(vaccine) {
      this.detailDialog = {
        visible: true,
        title: vaccine.name + " - 详细信息",
        vaccine: vaccine,
      };
    },

    goToAppointment(vaccine) {
      sessionStorage.setItem("selectedVaccine", JSON.stringify(vaccine));
      this.$router.push({
        path: "/vaccine/appointment/form",
        query: { vaccineId: vaccine.id },
      });
    },

    goToMyAppointment() {
      this.$router.push({ name: "myappointment" });
    },
  },
};
</script>

<style scoped>
.filter-form {
  margin-bottom: 20px;
}

.vaccine-list {
  margin-top: 20px;
}

/*  修复布局：使用 flex 网格 */
.vaccine-list .el-row {
  display: flex;
  flex-wrap: wrap;
  margin-left: -10px !important;
  margin-right: -10px !important;
}

.vaccine-card-col {
  padding-left: 10px;
  padding-right: 10px;
  margin-bottom: 20px;
  display: flex;
}

.vaccine-card {
  width: 100%;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.vaccine-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.vaccine-card >>> .el-card__header {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
}

.vaccine-card >>> .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 15px;
}

.vaccine-name {
  font-size: 16px;
  font-weight: bold;
}

.vaccine-info {
  flex: 1;
  margin-bottom: 15px;
}

.vaccine-info p {
  margin: 8px 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.multi-dose-info {
  background-color: #fef5e8;
  border-radius: 4px;
  padding: 8px 10px;
  margin: 8px 0;
  border-left: 3px solid #e6a23c;
}

.multi-dose-info p {
  margin: 4px 0;
  color: #e6a23c;
}

.single-dose-tip {
  color: #67c23a;
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 6px 10px;
  margin: 8px 0;
}

.vaccine-actions {
  text-align: center;
  padding-top: 12px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  margin-top: auto;
}

.vaccine-actions .el-button {
  flex: 1;
}

/* 响应式：小屏幕时一行2个 */
@media (max-width: 768px) {
  .vaccine-card-col {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

/* 响应式：超小屏幕时一行1个 */
@media (max-width: 480px) {
  .vaccine-card-col {
    flex: 0 0 100%;
    max-width: 100%;
  }
}
</style>
