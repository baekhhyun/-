<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form
      :model="queryParams"
      ref="queryForm"
      :inline="true"
      label-width="100px"
    >
      <el-form-item label="预约日期" prop="appointmentDate">
        <el-date-picker
          v-model="queryParams.appointmentDate"
          type="date"
          placeholder="选择预约日期"
          format="yyyy年MM月dd日"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item label="预约状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="全部状态"
          clearable
        >
          <el-option label="待确认" value="0" />
          <el-option label="已确认" value="1" />
          <el-option label="已完成" value="2" />
          <el-option label="已取消" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="疫苗名称" prop="vaccineName">
        <el-input
          v-model="queryParams.vaccineName"
          placeholder="请输入疫苗名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="用户名称" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入用户名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
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

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
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

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="appointmentList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="预约编号" align="center" prop="id" width="100" />
      <el-table-column
        label="用户信息"
        align="center"
        prop="userName"
        width="120"
      >
      </el-table-column>
      <el-table-column
        label="疫苗信息"
        align="center"
        prop="vaccineName"
        width="150"
      >
        <template slot-scope="scope">
          <div>{{ scope.row.vaccineName }}</div>
          <el-tag size="mini" :type="getAgeTagType(scope.row.suitableAge)">
            {{ formatAge(scope.row.suitableAge) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预约时间" align="center" width="180">
        <template slot-scope="scope">
          <div>{{ parseDate(scope.row.appointmentDate) }}</div>
          <div style="font-size: 12px">
            {{ formatTimeSlot(scope.row.timeSlot) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column
        label="联系电话"
        align="center"
        prop="userPhone"
        width="120"
      />
      <el-table-column
        label="预约状态"
        align="center"
        prop="status"
        width="100"
      >
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ formatStatus(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="160"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="备注"
        align="center"
        prop="remark"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
        width="220"
      >
        <template slot-scope="scope">
          <!-- 查看详情 -->
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row.id)"
            >详情</el-button
          >

          <!-- 状态操作按钮 -->
          <template v-if="scope.row.status === '0'">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-check"
              style="color: #67c23a"
              @click="handleConfirm(scope.row)"
              >确认</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-close"
              style="color: #f56c6c"
              @click="handleCancel(scope.row)"
              >取消</el-button
            >
          </template>

          <template v-if="scope.row.status === '1'">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-success"
              style="color: #67c23a"
              @click="handleComplete(scope.row)"
              >完成</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-close"
              style="color: #f56c6c"
              @click="handleCancel(scope.row)"
              >取消</el-button
            >
          </template>

          <!-- 删除按钮（仅管理员） -->
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            style="color: #f56c6c"
            @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 详情对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="预约编号">{{ form.id }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预约状态">
              <el-tag :type="getStatusTagType(form.status)">
                {{ formatStatus(form.status) }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="用户信息">{{ form.userName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">{{ form.userPhone }}</el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="疫苗名称">{{ form.vaccineName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产厂家">{{
              form.manufacturer
            }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="适用人群">
              <el-tag :type="getAgeTagType(form.suitableAge)">
                {{ formatAge(form.suitableAge) }}
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预约日期">{{
              parseDate(form.appointmentDate)
            }}</el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="预约时段">{{
              formatTimeSlot(form.timeSlot)
            }}</el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="创建时间">{{
          parseTime(form.createTime)
        }}</el-form-item>
        <el-form-item label="更新时间">{{
          parseTime(form.updateTime)
        }}</el-form-item>

        <el-form-item label="备注信息">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="无备注"
            :disabled="true"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 状态操作确认对话框 -->
    <el-dialog
      :title="statusTitle"
      :visible.sync="statusOpen"
      width="500px"
      append-to-body
    >
      <el-form ref="statusForm" :model="statusForm" label-width="100px">
        <el-form-item label="操作类型">
          <span style="font-weight: bold">{{ statusOperation }}</span>
        </el-form-item>
        <el-form-item label="预约编号">{{
          currentAppointment.id
        }}</el-form-item>
        <el-form-item label="用户信息">{{
          currentAppointment.userName
        }}</el-form-item>
        <el-form-item label="疫苗名称">{{
          currentAppointment.vaccineName
        }}</el-form-item>
        <el-form-item label="预约时间">
          {{ parseDate(currentAppointment.appointmentDate) }}
          {{ formatTimeSlot(currentAppointment.timeSlot) }}
        </el-form-item>
        <el-form-item label="操作备注" prop="remark">
          <el-input
            v-model="statusForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入操作备注（选填）"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitStatusChange">确 定</el-button>
        <el-button @click="statusOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listAppointment,
  getAppointment,
  updateAppointment,
  delAppointment,
  exportAppointment,
} from "@/api/vaccine/appointment";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "VaccineAppointment",
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
      // 预约表格数据
      appointmentList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        appointmentDate: null,
        status: null,
        vaccineName: null,
        userName: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {},

      // 状态操作相关
      statusOpen: false,
      statusTitle: "",
      statusForm: {
        remark: "",
      },
      currentAppointment: {},
      statusOperation: "",

      // 状态映射
      statusMap: {
        0: { text: "待确认", type: "warning", next: "1", operation: "确认" },
        1: { text: "已确认", type: "primary", next: "2", operation: "完成" },
        2: { text: "已完成", type: "success", next: null, operation: "" },
        3: { text: "已取消", type: "danger", next: null, operation: "" },
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询预约列表 */
    getList() {
      this.loading = true;
      listAppointment(this.queryParams)
        .then((response) => {
          console.log("获取到的数据:", response); // 调试用
          console.log("第一条数据详情:", response.rows[0]); // 查看数据结构
          this.appointmentList = response.rows;
          this.total = response.total;
          this.loading = false;
        })
        .catch((error) => {
          console.error("获取列表失败:", error);
          this.loading = false;
        });
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

    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.appointmentId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },

    /** 查看按钮操作 */
    handleView(id) {
      console.log("查看详情，ID:", id); // 调试用
      if (!id) {
        this.$message.error("无效的预约ID");
        return;
      }
      getAppointment(id)
        .then((response) => {
          this.form = response.data;
          this.open = true;
          this.title = "预约详情";
        })
        .catch((error) => {
          console.error("获取详情失败:", error);
          this.$message.error("获取详情失败");
        });
    },

    /** 确认预约操作 */
    handleConfirm(row) {
      this.currentAppointment = row;
      this.statusOperation = "确认预约";
      this.statusTitle = "确认预约";
      this.statusForm.remark = "";
      this.statusOpen = true;
    },

    /** 完成预约操作 */
    handleComplete(row) {
      this.currentAppointment = row;
      this.statusOperation = "完成预约";
      this.statusTitle = "完成预约";
      this.statusForm.remark = "";
      this.statusOpen = true;
    },

    /** 取消预约操作（管理员取消） */
    handleCancel(row) {
      this.$confirm("确定要取消此预约吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          const id = row.id || row.appointmentId;
          const data = {
            id: id, // 提交时使用 id
            status: "3",
            remark: row.remark + "（管理员取消）",
          };
          console.log("取消预约数据:", data); // 调试用
          updateAppointment(data).then((response) => {
            if (response.code === 200) {
              this.$message.success("取消成功");
              this.getList();
            }
          });
        })
        .catch(() => {});
    },

    /** 提交状态变更 */
    submitStatusChange() {
      console.log("=== 状态变更调试 ===");
      console.log("当前预约对象:", this.currentAppointment);
      console.log("当前状态:", this.currentAppointment.status);
      console.log(
        "下一状态:",
        this.statusMap[this.currentAppointment.status]?.next
      );

      const nextStatus = this.statusMap[this.currentAppointment.status].next;
      if (!nextStatus) {
        this.$message.error("当前状态不可操作");
        return;
      }

      const data = {
        id: this.currentAppointment.id,
        status: nextStatus,
        remark: this.currentAppointment.remark || "",
      };

      // 添加操作备注
      if (this.statusForm.remark) {
        data.remark += ` [${this.statusOperation}: ${this.statusForm.remark}]`;
      }

      console.log("提交的数据:", JSON.stringify(data));

      updateAppointment(data)
        .then((response) => {
          console.log("API响应:", response);
          if (response.code === 200) {
            this.$message.success(`${this.statusOperation}成功`);
            this.statusOpen = false;
            this.getList();
          } else {
            this.$message.error(response.msg || "操作失败");
          }
        })
        .catch((error) => {
          console.error("API错误详情:", error);
          console.error("错误响应:", error.response);
          this.$message.error(`操作失败: ${error.message}`);
        });
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      console.log("删除预约，ID:", row.id); // 调试用
      const appointmentId = row.id;
      this.$confirm(
        '是否确认删除预约编号为"' + appointmentId + '"的数据项？',
        "警告",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(() => {
          // 注意：delAppointment方法应该接收单个id
          return delAppointment(appointmentId);
        })
        .then(() => {
          this.getList();
          this.$message.success("删除成功");
        })
        .catch((error) => {
          console.error("删除失败:", error);
          if (error !== "cancel") {
            this.$message.error("删除失败");
          }
        });
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "vaccine/appointment/export",
        {
          ...this.queryParams,
        },
        `预约数据_${new Date().getTime()}.xlsx`
      );
    },

    // ============ 工具方法 ============

    /** 格式化状态 */
    formatStatus(status) {
      return this.statusMap[status]?.text || "未知";
    },

    /** 获取状态标签类型 */
    getStatusTagType(status) {
      return this.statusMap[status]?.type || "info";
    },

    /** 格式化时间段 */
    formatTimeSlot(slot) {
      const slotMap = {
        1: "上午 09:00-11:00",
        2: "下午 14:00-16:00",
        3: "晚上 18:00-20:00",
      };
      return slotMap[slot] || slot;
    },

    /** 格式化适用年龄 */
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

    /** 获取年龄标签类型 */
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

    /** 解析日期 */
    parseDate(dateStr) {
      if (!dateStr) return "";
      return dateStr.substring(0, 10);
    },

    /** 解析时间 */
    parseTime(time) {
      return parseTime(time);
    },
  },
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mb8 {
  margin-bottom: 8px;
}

.small-padding .cell {
  padding: 8px 0;
}

.fixed-width {
  width: 220px;
}

/* 状态标签样式 */
.el-tag {
  margin: 2px;
}

/* 表格行状态颜色 */
.el-table .warning-row {
  background: #fdf6ec;
}

.el-table .success-row {
  background: #f0f9eb;
}

.el-table .danger-row {
  background: #fef0f0;
}
</style>
