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
      <!-- <el-form-item label="适用年龄" prop="suitableAge">
        <el-input
          v-model="queryParams.suitableAge"
          placeholder="请输入适用年龄"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item> -->
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
      <!-- <el-form-item label="库存数量" prop="stock">
        <el-input
          v-model="queryParams.stock"
          placeholder="请输入库存数量"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item> -->
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
      <el-table-column label="疫苗ID" align="center" prop="id" />
      <el-table-column label="疫苗名称" align="center" prop="name" />
      <el-table-column label="生产厂家" align="center" prop="manufacturer" />
      <el-table-column label="疫苗描述" align="center" prop="description" />
      <!-- <el-table-column label="适用年龄" align="center" prop="suitableAge" /> -->
      <el-table-column label="适用年龄" align="center" width="120">
        <template slot-scope="scope">
          {{ formatSuitableAge(scope.row.suitableAge) }}
        </template>
      </el-table-column>
      <el-table-column label="库存数量" align="center" prop="stock" />
      <el-table-column label="状态" align="center" prop="status">
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
      width="500px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
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
            placeholder="请输入内容"
          />
        </el-form-item>
        <!-- <el-form-item label="适用年龄" prop="suitableAge">
          <el-input v-model="form.suitableAge" placeholder="请输入适用年龄" />
        </el-form-item> -->
        <el-form-item label="适用年龄" prop="suitableAge">
          <el-select v-model="form.suitableAge" placeholder="请选择适用年龄">
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
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option
              v-for="dict in dict.type.vaccine_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
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
        suitableAge: undefined, // 添加这一行
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

        // stock: [
        //   { required: true, message: "库存数量不能为空", trigger: "blur" },
        //   { type: "number", message: "库存数量必须为数字", trigger: "blur" },
        // ],
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
    /** 查询疫苗信息列表 */
    getList() {
      this.loading = true;
      listVaccine(this.queryParams).then((response) => {
        this.vaccineList = response.rows;
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
        suitableAge: null,
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
        this.open = true;
        this.title = "修改疫苗信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
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
      this.$modal
        .confirm('是否确认删除疫苗信息编号为"' + ids + '"的数据项？')
        .then(function () {
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
      this.download(
        "system/vaccine/export",
        {
          ...this.queryParams,
        },
        `vaccine_${new Date().getTime()}.xlsx`
      );
    },
  },
};
</script>
