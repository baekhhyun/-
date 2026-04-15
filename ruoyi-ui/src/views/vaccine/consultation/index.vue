<template>
  <div class="consultation-page app-container">
    <el-row :gutter="20">
      <el-col :span="isAdmin ? 10 : 24">
        <el-card shadow="never" class="mb16">
          <div slot="header"><span>发起在线咨询</span></div>
          <el-form ref="form" :model="form" :rules="rules" label-width="100px">
            <el-form-item label="咨询标题" prop="title">
              <el-input v-model="form.title" maxlength="128" show-word-limit />
            </el-form-item>
            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="form.priority">
                <el-radio label="0">普通</el-radio>
                <el-radio label="1">较急</el-radio>
                <el-radio label="2">紧急</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="咨询内容" prop="questionContent">
              <el-input v-model="form.questionContent" type="textarea" :rows="6" maxlength="1000" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitForm">提交咨询</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="isAdmin ? 14 : 24">
        <el-card shadow="never">
          <div slot="header" class="header-actions">
            <span>{{ isAdmin ? "咨询工单管理" : "我的咨询记录" }}</span>
            <el-tag size="small" type="info">待回复 {{ pendingCount }} 条</el-tag>
          </div>

          <el-table :data="consultationList" v-loading="loading" border>
            <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
            <el-table-column prop="userName" label="咨询人" width="100" v-if="isAdmin" />
            <el-table-column label="内容" min-width="200">
              <template slot-scope="scope">
                <div class="line2">{{ scope.row.questionContent }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === '1' ? 'success' : 'warning'">
                  {{ scope.row.status === "1" ? "已回复" : "待回复" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="优先级" width="90">
              <template slot-scope="scope">
                {{ formatPriority(scope.row.priority) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="165" />
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" @click="showDetail(scope.row)">详情</el-button>
                <el-button v-if="isAdmin && scope.row.status === '0'" type="text" @click="openReply(scope.row)">回复</el-button>
                <el-button v-if="isAdmin" type="text" style="color:#f56c6c" @click="remove(scope.row)">删除</el-button>
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
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="咨询详情" :visible.sync="detailOpen" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="咨询内容">{{ detail.questionContent }}</el-descriptions-item>
        <el-descriptions-item label="回复内容">{{ detail.answerContent || '暂无回复' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog title="回复咨询" :visible.sync="replyOpen" width="600px">
      <el-form :model="replyForm" label-width="90px">
        <el-form-item label="咨询标题">{{ replyForm.title }}</el-form-item>
        <el-form-item label="回复内容">
          <el-input type="textarea" :rows="5" v-model="replyForm.answerContent" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="replyOpen = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确认回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addConsultation,
  delConsultation,
  listConsultation,
  listMyConsultation,
  replyConsultation,
} from "@/api/vaccine/consultation";

export default {
  name: "VaccineConsultation",
  data() {
    return {
      loading: false,
      total: 0,
      pendingCount: 0,
      consultationList: [],
      detailOpen: false,
      replyOpen: false,
      detail: {},
      form: {
        title: "",
        priority: "1",
        questionContent: "",
      },
      replyForm: {
        id: undefined,
        title: "",
        answerContent: "",
      },
      rules: {
        title: [{ required: true, message: "请输入咨询标题", trigger: "blur" }],
        questionContent: [{ required: true, message: "请输入咨询内容", trigger: "blur" }],
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
    };
  },
  computed: {
    isAdmin() {
      const roles = this.$store.getters.roles || [];
      return Array.isArray(roles)
        ? roles.includes("admin") || roles.includes("vaccineAdmin")
        : roles === "admin" || roles === "vaccineAdmin";
    },
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      const req = this.isAdmin ? listConsultation : listMyConsultation;
      req(this.queryParams)
        .then((res) => {
          this.consultationList = res.rows || [];
          this.total = res.total || 0;
          this.pendingCount = this.consultationList.filter((item) => item.status === "0").length;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        addConsultation(this.form).then(() => {
          this.$modal.msgSuccess("咨询提交成功");
          this.resetForm();
          this.queryParams.pageNum = 1;
          this.getList();
        });
      });
    },
    resetForm() {
      this.form = { title: "", priority: "1", questionContent: "" };
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate());
    },
    showDetail(row) {
      this.detail = { ...row };
      this.detailOpen = true;
    },
    openReply(row) {
      this.replyForm = { id: row.id, title: row.title, answerContent: "" };
      this.replyOpen = true;
    },
    submitReply() {
      if (!this.replyForm.answerContent) {
        this.$modal.msgError("请输入回复内容");
        return;
      }
      replyConsultation(this.replyForm).then(() => {
        this.$modal.msgSuccess("回复成功");
        this.replyOpen = false;
        this.getList();
      });
    },
    remove(row) {
      this.$modal
        .confirm(`确认删除咨询「${row.title}」吗？`)
        .then(() => delConsultation(row.id))
        .then(() => {
          this.$modal.msgSuccess("删除成功");
          this.getList();
        });
    },
    formatPriority(priority) {
      return { "0": "普通", "1": "较急", "2": "紧急" }[priority] || "普通";
    },
  },
};
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.line2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
