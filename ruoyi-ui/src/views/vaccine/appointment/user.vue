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
        <el-form-item>
          <el-button type="primary" @click="searchVaccines">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 疫苗列表 -->
      <div class="vaccine-list">
        <el-row :gutter="20">
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
          <el-descriptions-item label="疫苗名称">{{
            detailDialog.vaccine.name
          }}</el-descriptions-item>
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
      // 筛选条件
      filter: {
        name: "",
        suitableAge: "",
      },
      // 疫苗列表
      vaccineList: [],
      // 分页参数
      queryParams: {
        pageNum: 1,
        pageSize: 10, // 3x3网格
        name: undefined,
        suitableAge: undefined,
        status: "0", // 只显示正常状态的疫苗
      },
      total: 0,
      loading: false,
      // 详情对话框
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
    // 获取疫苗列表
    getVaccineList() {
      this.loading = true;
      // 合并筛选条件
      const params = {
        ...this.queryParams,
        name: this.filter.name || undefined,
        suitableAge: this.filter.suitableAge || undefined,
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

    // 搜索疫苗
    searchVaccines() {
      this.queryParams.pageNum = 1;
      this.getVaccineList();
    },

    // 重置筛选
    resetFilter() {
      this.filter = {
        name: "",
        suitableAge: "",
      };
      this.searchVaccines();
    },

    // 查看疫苗详情
    viewVaccineDetail(vaccine) {
      this.detailDialog = {
        visible: true,
        title: vaccine.name + " - 详细信息",
        vaccine: vaccine,
      };
    },

    // 前往预约页面
    goToAppointment(vaccine) {
      // 保存选中的疫苗信息到本地存储或路由参数
      sessionStorage.setItem("selectedVaccine", JSON.stringify(vaccine));
      // 跳转到预约表单页面
      this.$router.push({
        path: "/vaccine/appointment/form",
        query: { vaccineId: vaccine.id },
      });
    },

    // 前往我的预约
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

.vaccine-card-col {
  margin-bottom: 20px;
}

.vaccine-card {
  height: 100%;
}

.vaccine-name {
  font-size: 16px;
  font-weight: bold;
}

.vaccine-info {
  margin-bottom: 15px;
}

.vaccine-info p {
  margin: 5px 0;
  font-size: 13px;
  color: #666;
}

.vaccine-actions {
  text-align: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
</style>
