<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="疫苗名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入疫苗名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生产厂家" prop="manufacturer">
        <el-input
          v-model="queryParams.manufacturer"
          placeholder="请输入生产厂家"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="适用年龄" prop="suitableAge">
        <el-select
          v-model="queryParams.suitableAge"
          placeholder="请选择适用年龄"
          clearable
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        >
          <el-option label="全部" value="" />
          <el-option label="婴儿" value="1" />
          <el-option label="儿童" value="2" />
          <el-option label="青少年" value="3" />
          <el-option label="成人" value="4" />
          <el-option label="老人" value="5" />
        </el-select>
      </el-form-item>
      <!--  多剂次筛选（新增） -->
      <el-form-item label="疫苗类型" prop="isMultiDose">
        <el-select
          v-model="queryParams.isMultiDose"
          placeholder="请选择"
          clearable
          style="width: 240px"
        >
          <el-option label="单剂次" value="0" />
          <el-option label="多剂次" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
        >
          <el-option
            v-for="dict in dict.type.vaccine_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          >导出</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="vaccineList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" width="60">
        <template slot-scope="scope">
          {{
            (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1
          }}
        </template>
      </el-table-column>
      <el-table-column label="疫苗名称" align="center" prop="name" />
      <el-table-column label="生产厂家" align="center" prop="manufacturer" />
      <el-table-column
        label="疫苗描述"
        align="center"
        prop="description"
        show-overflow-tooltip
      />
      <el-table-column label="适用年龄" align="center" width="120">
        <template slot-scope="scope">
          {{ formatSuitableAge(scope.row.suitableAge) }}
        </template>
      </el-table-column>
      <el-table-column
        label="库存数量"
        align="center"
        prop="stock"
        width="100"
      />
      <el-table-column label="已预约总数" align="center" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.totalBooked || 0 }}</span>
        </template>
      </el-table-column>
      <!--  多剂次疫苗列（新增） -->
      <el-table-column label="疫苗类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag
            v-if="scope.row.isMultiDose === 1"
            type="warning"
            size="small"
          >
            多剂次
          </el-tag>
          <el-tag v-else type="info" size="small"> 单剂次 </el-tag>
        </template>
      </el-table-column>
      <!--  多剂次信息列（新增） -->
      <el-table-column label="接种计划" align="center" width="150">
        <template slot-scope="scope">
          <span v-if="scope.row.isMultiDose === 1">
            {{ scope.row.totalDoses || "-" }}剂 /
            {{ scope.row.intervalDays || "-" }}天
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <dict-tag
            :options="dict.type.vaccine_status"
            :value="scope.row.status"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
        width="120"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            >修改</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            :disabled="scope.row.totalBooked > 0"
            :title="
              scope.row.totalBooked > 0
                ? `已有${scope.row.totalBooked}条预约，无法删除`
                : ''
            "
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
    <!-- 添加或修改疫苗信息对话框 -->
    <el-dialog
      :title="title"
      :visible.sync="open"
      width="600px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="疫苗名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入疫苗名称" />
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="疫苗描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入内容"
          />
        </el-form-item>
        <el-form-item label="适用年龄" prop="suitableAge">
          <el-select
            v-model="form.suitableAge"
            placeholder="请选择适用年龄"
            style="width: 100%"
          >
            <el-option label="婴儿(0-2岁)" value="1" />
            <el-option label="儿童(3-12岁)" value="2" />
            <el-option label="青少年(13-17岁)" value="3" />
            <el-option label="成人(18-59岁)" value="4" />
            <el-option label="老人(60岁以上)" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            :max="999999"
            :precision="0"
            label="库存数量"
            style="width: 100%"
          />
        </el-form-item>

        <!--  新增时显示疫苗类型配置，修改时隐藏 -->
        <template v-if="title === '添加疫苗信息'">
          <el-form-item label="疫苗类型" prop="isMultiDose">
            <el-radio-group
              v-model="form.isMultiDose"
              @change="handleVaccineTypeChange"
            >
              <el-radio :label="0">单剂次疫苗（只需接种一次）</el-radio>
              <el-radio :label="1">多剂次疫苗（需要接种多剂）</el-radio>
            </el-radio-group>
            <div class="tip-text" style="margin-top: 5px">
              <span v-if="form.isMultiDose === 1" style="color: #e6a23c">
                📋 多剂次疫苗需要设置总剂次数和间隔天数
              </span>
              <span v-else style="color: #67c23a">
                ✅ 单剂次疫苗只需接种一次
              </span>
            </div>
          </el-form-item>

          <!-- 多剂次疫苗详细配置（选择多剂次时显示） -->
          <template v-if="form.isMultiDose === 1">
            <el-form-item label="总剂次数" prop="totalDoses">
              <el-input-number
                v-model="form.totalDoses"
                :min="2"
                :max="10"
                style="width: 100%"
                placeholder="请输入总剂次数"
              />
              <div class="tip-text">例如：乙肝疫苗3剂次，HPV疫苗3剂次</div>
            </el-form-item>
            <el-form-item label="间隔天数" prop="intervalDays">
              <el-input-number
                v-model="form.intervalDays"
                :min="0"
                :max="365"
                style="width: 100%"
                placeholder="请输入剂次间隔天数"
              />
              <div class="tip-text">
                相邻两剂之间的间隔天数，如：28天、180天
              </div>
            </el-form-item>
            <el-form-item label="接种计划描述" prop="doseSchedule">
              <el-input
                v-model="form.doseSchedule"
                placeholder="例如：0,1,6月龄 或 第0、2、6个月"
                maxlength="100"
                show-word-limit
              />
              <div class="tip-text">选填，描述接种时间安排</div>
            </el-form-item>
          </template>
        </template>

        <!--  修改时显示提示信息（可选） -->
        <template v-else>
          <el-alert
            title="提示"
            type="info"
            description="疫苗类型已在新增时确定，修改请删除后重新添加"
            :closable="false"
            show-icon
            style="margin-bottom: 15px"
          />
        </template>

        <el-form-item label="状态" prop="status">
          <el-select
            v-model="form.status"
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option
              v-for="dict in dict.type.vaccine_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listVaccine,
  getVaccine,
  delVaccine,
  addVaccine,
  updateVaccine,
} from "@/api/vaccine/vaccine";
import { getTotalBooked } from "@/api/vaccine/appointment";

export default {
  name: "Vaccine",
  dicts: ["vaccine_status"],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 疫苗信息表格数据
      vaccineList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        manufacturer: null,
        description: null,
        suitableAge: null,
        stock: null,
        status: null,
        isMultiDose: null, //  新增
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "请输入疫苗名称", trigger: "blur" }],
        manufacturer: [
          { required: true, message: "请输入生产厂家", trigger: "blur" },
        ],
        suitableAge: [
          { required: true, message: "请选择适用年龄", trigger: "change" },
        ],
        status: [{ required: true, message: "请选择状态", trigger: "change" }],
        //  多剂次疫苗校验（新增）
        totalDoses: [
          { required: true, message: "请输入总剂次数", trigger: "blur" },
          {
            type: "number",
            min: 2,
            max: 10,
            message: "剂次数应在2-10之间",
            trigger: "blur",
          },
        ],
        intervalDays: [
          { required: true, message: "请输入间隔天数", trigger: "blur" },
          {
            type: "number",
            min: 0,
            max: 365,
            message: "间隔天数应在0-365之间",
            trigger: "blur",
          },
        ],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 适用年龄
    formatSuitableAge(value) {
      const ageMap = {
        1: "婴儿(0-2岁)",
        2: "儿童(3-12岁)",
        3: "青少年(13-17岁)",
        4: "成人(18-59岁)",
        5: "老人(60岁以上)",
      };
      return ageMap[value] || value || "-";
    },
    handleVaccineTypeChange(val) {
      if (val === 1) {
        // 多剂次疫苗，初始化默认值
        this.form.totalDoses = 3;
        this.form.intervalDays = 28;
        this.form.doseSchedule = "";
      } else {
        // 单剂次疫苗，清空多剂次字段
        this.form.totalDoses = null;
        this.form.intervalDays = null;
        this.form.doseSchedule = "";
      }
      // 触发表单校验
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate();
        }
      });
    },
    getList() {
      listVaccine(this.queryParams).then((response) => {
        this.vaccineList = response.rows;
        // 获取每个疫苗的总预约数
        this.vaccineList.forEach((vaccine) => {
          getTotalBooked(vaccine.id).then((res) => {
            if (res.code === 200) {
              // 使用 Vue.set 确保响应式更新
              this.$set(vaccine, "totalBooked", res.data);
            }
          });
        });
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        name: null,
        manufacturer: null,
        description: null,
        suitableAge: null,
        stock: null,
        status: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        // 多剂次字段
        isMultiDose: 0,
        totalDoses: null,
        intervalDays: null,
        doseSchedule: "",
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加疫苗信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getVaccine(id).then((response) => {
        this.form = response.data;
        //  确保多剂次字段有默认值
        if (this.form.isMultiDose !== 1) {
          this.form.isMultiDose = 0;
        }
        this.open = true;
        this.title = "修改疫苗信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          //  多剂次疫苗额外校验
          if (this.form.isMultiDose === 1) {
            if (!this.form.totalDoses || this.form.totalDoses < 2) {
              this.$message.error("多剂次疫苗总剂次数不能少于2");
              return;
            }
            if (!this.form.intervalDays || this.form.intervalDays < 0) {
              this.$message.error("请填写正确的间隔天数");
              return;
            }
          }

          if (this.form.id != null) {
            updateVaccine(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addVaccine(this.form).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      const name = row.name || "选中的疫苗";

      //  检查是否有预约记录
      if (row.totalBooked && row.totalBooked > 0) {
        this.$message.warning(
          `疫苗【${name}】已有 ${row.totalBooked} 条预约记录，无法删除！`
        );
        return;
      }

      this.$modal
        .confirm(`是否确认删除疫苗【${name}】？删除后无法恢复。`, "警告", {
          confirmButtonText: "确定删除",
          cancelButtonText: "取消",
          type: "warning",
        })
        .then(() => {
          return delVaccine(ids);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      import("@/utils/request").then((module) => {
        const request = module.default;

        request({
          url: "/vaccine/vaccine/export",
          method: "post",
          data: this.queryParams,
          responseType: "blob",
          headers: {
            "Content-Type": "application/json",
          },
        })
          .then((response) => {
            const blob = new Blob([response], {
              type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            });
            const link = document.createElement("a");
            link.href = window.URL.createObjectURL(blob);
            link.download = `疫苗数据_${new Date().getTime()}.xlsx`;

            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            window.URL.revokeObjectURL(link.href);

            this.$modal.msgSuccess("导出成功");
          })
          .catch((error) => {
            console.error("导出失败:", error);
            this.$modal.msgError("导出失败");
          });
      });
    },
  },
};
</script>

<style scoped>
.tip-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.2;
}
</style>
