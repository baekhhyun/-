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
      row-key="id"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="预约编号" align="center" prop="id" width="100" />
      <el-table-column
        label="用户信息"
        align="center"
        prop="userName"
        width="120"
      />
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

      <!-- 多剂次疫苗 - 剂次列 -->
      <el-table-column label="剂次" align="center" width="100">
        <template slot-scope="scope">
          <!--  多剂次疫苗：有 doseNumber -->
          <el-tag
            v-if="scope.row.doseNumber"
            size="small"
            :type="getDoseTagType(scope.row)"
          >
            多剂次-第{{ scope.row.doseNumber }}剂
          </el-tag>
          <!--  单剂次疫苗：没有 doseNumber -->
          <el-tag v-else size="small"> 单剂次 </el-tag>
        </template>
      </el-table-column>

      <!-- 多剂次疫苗 - 接种状态列 -->
      <el-table-column label="接种状态" align="center" width="100">
        <template slot-scope="scope">
          <!-- 已完成接种 -->
          <el-tag v-if="scope.row.status === '2'" type="success" size="small">
            已完成接种
          </el-tag>
          <!-- 已取消 -->
          <el-tag v-else-if="scope.row.status === '3'" type="info" size="small">
            已取消
          </el-tag>
          <!-- 待接种 -->
          <el-tag v-else type="warning" size="small"> 待接种 </el-tag>
        </template>
      </el-table-column>

      <!-- 多剂次疫苗 - 下一针日期列 -->
      <el-table-column label="下一针日期" align="center" width="120">
        <template slot-scope="scope">
          <span v-if="scope.row.nextDoseDate">{{
            parseDate(scope.row.nextDoseDate)
          }}</span>
          <span v-else>-</span>
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

          <!-- 查看接种进度按钮（多剂次疫苗） -->
          <el-button
            v-if="scope.row.doseNumber"
            size="mini"
            type="text"
            icon="el-icon-data-line"
            @click="handleViewProgress(scope.row)"
            >进度</el-button
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

        <!-- 多剂次疫苗信息展示 -->
        <el-row v-if="form.doseNumber">
          <el-col :span="12">
            <el-form-item label="当前剂次">
              <el-tag :type="getDoseTagType(form)">
                第{{ form.doseNumber }}剂
              </el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="接种状态">
              <el-tag v-if="form.isCompleted === 1" type="success"
                >已完成接种</el-tag
              >
              <el-tag v-else type="warning">待接种</el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.nextDoseDate">
          <el-col :span="12">
            <el-form-item label="下一针日期">{{
              parseDate(form.nextDoseDate)
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

    <!-- 接种进度详情弹窗 -->
    <el-dialog
      :title="progressTitle"
      :visible.sync="progressOpen"
      width="500px"
      append-to-body
    >
      <div v-if="progressAppointment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户">{{
            progressAppointment.userName
          }}</el-descriptions-item>
          <el-descriptions-item label="疫苗">{{
            progressAppointment.vaccineName
          }}</el-descriptions-item>
          <el-descriptions-item label="当前剂次">
            <el-tag :type="getDoseTagType(progressAppointment)">
              第{{ progressAppointment.doseNumber }}剂
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="接种状态">
            <el-tag v-if="progressAppointment.isCompleted === 1" type="success"
              >已完成</el-tag
            >
            <el-tag
              v-else-if="progressAppointment.status === '2'"
              type="success"
              >已完成</el-tag
            >
            <el-tag v-else type="warning">待接种</el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            label="下一针日期"
            v-if="progressAppointment.nextDoseDate"
          >
            {{ parseDate(progressAppointment.nextDoseDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="预约日期">{{
            parseDate(progressAppointment.appointmentDate)
          }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{
            progressAppointment.userPhone || progressAppointment.phone
          }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{
            progressAppointment.remark || "无"
          }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="progressOpen = false">关 闭</el-button>
      </span>
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
        <!-- 多剂次疫苗提示 -->
        <el-form-item v-if="currentAppointment.doseNumber" label="当前剂次">
          <el-tag size="small" :type="getDoseTagType(currentAppointment)">
            第{{ currentAppointment.doseNumber }}剂
          </el-tag>
          <span
            v-if="currentAppointment.nextDoseDate"
            style="margin-left: 10px; color: #909399"
          >
            完成后下一针日期：{{ parseDate(currentAppointment.nextDoseDate) }}
          </span>
        </el-form-item>
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
  confirmAppointment,
  completeAppointment,
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

      // 接种进度弹窗
      progressOpen: false,
      progressTitle: "",
      progressAppointment: {},

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
      this.ids = selection.map((item) => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },

    /** 查看按钮操作 */
    handleView(id) {
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

    /** 查看接种进度 */
    handleViewProgress(row) {
      this.progressAppointment = row;
      this.progressTitle = `${row.userName} - ${row.vaccineName} 接种进度`;
      this.progressOpen = true;
    },

    /** 获取剂次标签类型 */
    getDoseTagType(row) {
      if (row.isMultiDose === 1) {
        return "warning"; // 多剂次 - 橙色
      }
      return "success"; // 单剂次 - 绿色
    },

    /** 确认预约操作 */
    handleConfirm(row) {
      this.currentAppointment = row;
      this.statusOperation = "确认预约";
      this.statusTitle = "确认预约";
      this.statusForm.remark = "";
      this.statusOpen = true;
    },

    /** 完成接种操作 */
    /** 完成接种操作 */
    handleComplete(row) {
      //  检查预约日期
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const appointmentDate = new Date(row.appointmentDate);
      appointmentDate.setHours(0, 0, 0, 0);

      // 计算日期差
      const diffDays = Math.ceil(
        (appointmentDate - today) / (1000 * 60 * 60 * 24)
      );

      let confirmMessage = "";
      let confirmTitle = "";
      let confirmType = "";

      // 格式化日期
      const formatDate = (date) => {
        const d = new Date(date);
        return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`;
      };

      //  构建疫苗和剂次信息
      let vaccineInfo = row.vaccineName;
      if (row.doseNumber) {
        vaccineInfo = `${row.vaccineName}（第${row.doseNumber}剂）`;
      }

      if (diffDays > 0) {
        // 预约日期在未来（还没到接种日期）
        confirmTitle = "⚠️ 提前完成提醒";
        confirmType = "warning";
        confirmMessage = `
      <div style="line-height: 1.8;">
        <p><strong style="color: #e6a23c;">接种日期还未到！</strong></p>
        <p>预约接种日期：${formatDate(row.appointmentDate)}</p>
        <p>当前日期：${formatDate(today)}</p>
        <p>距离接种还有 <strong style="color: #e6a23c; font-size: 16px;">${diffDays}</strong> 天</p>
        <p>疫苗：${vaccineInfo}</p>
        <p>用户：${row.userName}</p>
        <p style="color: #909399; margin-top: 10px;">确定要提前完成接种吗？</p>
      </div>
    `;
      } else if (diffDays < 0) {
        // 预约日期已过期
        confirmTitle = "⚠️ 过期接种提醒";
        confirmType = "warning";
        confirmMessage = `
      <div style="line-height: 1.8;">
        <p><strong style="color: #f56c6c;">预约日期已过期！</strong></p>
        <p>预约接种日期：${formatDate(row.appointmentDate)}</p>
        <p>当前日期：${formatDate(today)}</p>
        <p>已过期 <strong style="color: #f56c6c; font-size: 16px;">${Math.abs(
          diffDays
        )}</strong> 天</p>
        <p>疫苗：${vaccineInfo}</p>
        <p>用户：${row.userName}</p>
        <p style="color: #909399; margin-top: 10px;">确定要完成这个过期的接种吗？</p>
      </div>
    `;
      } else {
        // 预约日期就是今天
        confirmTitle = "完成接种";
        confirmType = "info";
        confirmMessage = `
      <div style="line-height: 1.8;">
        <p>接种日期：${formatDate(row.appointmentDate)}</p>
        <p>疫苗：${vaccineInfo}</p>
        <p>用户：${row.userName}</p>
        <p style="color: #67c23a;">确认已完成接种吗？</p>
      </div>
    `;
      }

      this.$confirm(confirmMessage, confirmTitle, {
        confirmButtonText: "确认完成",
        cancelButtonText: "取消",
        type: confirmType,
        dangerouslyUseHTMLString: true,
        distinguishCancelAndClose: true,
      })
        .then(() => {
          this.$set(row, "loading", true);
          completeAppointment(row.id)
            .then(() => {
              this.$message.success("完成接种成功");
              this.getList();
            })
            .finally(() => {
              this.$set(row, "loading", false);
            });
        })
        .catch(() => {});
    },

    /** 取消预约操作 */
    handleCancel(row) {
      let message = "确定要取消此预约吗？";
      if (row.doseNumber) {
        message = `确定要取消第${row.doseNumber}剂次的预约吗？取消后需要重新预约。`;
      }

      this.$confirm(message, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          const id = row.id;
          const data = {
            id: id,
            status: "3",
            remark: row.remark + "（管理员取消）",
          };
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
      // console.log("===== 提交状态变更 =====");
      // console.log("当前操作:", this.statusOperation);
      // console.log("当前预约:", this.currentAppointment);

      // 根据操作类型调用不同的API
      if (this.statusOperation === "确认预约") {
        // console.log("调用确认API，ID:", this.currentAppointment.id);
        confirmAppointment(this.currentAppointment.id)
          .then((response) => {
            // console.log("确认API响应:", response);
            if (response.code === 200) {
              this.$message.success("确认成功");
              this.statusOpen = false;
              this.getList();
            } else {
              this.$message.error(response.msg || "操作失败");
            }
          })
          .catch((error) => {
            console.error("确认失败:", error);
            this.$message.error("操作失败");
          });
      } else if (this.statusOperation.includes("完成")) {
        // console.log("调用完成接种API，ID:", this.currentAppointment.id);
        completeAppointment(this.currentAppointment.id)
          .then((response) => {
            // console.log("完成接种API响应:", response);
            if (response.code === 200) {
              this.$message.success("完成接种成功");
              this.statusOpen = false;
              this.getList();
              this.$emit("refresh-pending-count");
            } else {
              this.$message.error(response.msg || "操作失败");
            }
          })
          .catch((error) => {
            console.error("完成接种失败:", error);
            this.$message.error("操作失败");
          });
      } else {
        // 取消或其他操作
        const nextStatus = this.statusMap[this.currentAppointment.status]?.next;
        if (!nextStatus) {
          this.$message.error("当前状态不可操作");
          return;
        }

        const data = {
          id: this.currentAppointment.id,
          status: nextStatus,
          remark: this.currentAppointment.remark || "",
        };
        if (this.statusForm.remark) {
          data.remark += ` [${this.statusOperation}: ${this.statusForm.remark}]`;
        }
        updateAppointment(data)
          .then((response) => {
            if (response.code === 200) {
              this.$message.success(`${this.statusOperation}成功`);
              this.statusOpen = false;
              this.getList();
            } else {
              this.$message.error(response.msg || "操作失败");
            }
          })
          .catch((error) => {
            console.error("操作失败:", error);
            this.$message.error("操作失败");
          });
      }
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const appointmentId = row.id;
      const vaccineName = row.vaccineName;

      //  根据状态给出不同的提示
      let message = "";
      let title = "提示";

      if (row.status === "3") {
        message = `确认删除已取消的预约（${vaccineName}）吗？删除后不可恢复。`;
        title = "删除已取消预约";
      } else {
        // 如果不是已取消状态，提示用户先取消
        message = `该预约状态为"${this.formatStatus(
          row.status
        )}"，只有已取消的预约才能删除。请先取消预约后再删除。`;
        this.$message.warning(message);
        return;
      }

      this.$confirm(message, title, {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          return delAppointment(appointmentId);
        })
        .then(() => {
          this.getList();
          this.$message.success("删除成功");
        })
        .catch((error) => {
          console.error("删除失败:", error);
          if (error !== "cancel") {
            this.$message.error(error.msg || "删除失败");
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
      // 处理 ISO 8601 格式
      if (dateStr.includes("T")) {
        const date = new Date(dateStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
      }
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

.el-tag {
  margin: 2px;
}

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
