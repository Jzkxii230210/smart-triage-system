<template>
  <el-card shadow="never" class="user-card">
    <template #header>
      <div class="card-header">
        <span class="title">👤 系统账号管理</span>
        <div class="right-actions">
          <el-input
              v-model="queryParams.username"
              placeholder="输入用户名搜索"
              clearable
              style="width: 200px; margin-right: 10px"
              @keyup.enter="handleQuery"
          />
          <el-select
              v-model="queryParams.role"
              placeholder="按角色筛选"
              clearable
              style="width: 140px; margin-right: 10px"
              @change="handleQuery"
          >
            <el-option label="管理员" value="admin" />
            <el-option label="医生" value="doctor" />
            <el-option label="患者" value="patient" />
          </el-select>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button type="success" icon="Plus" @click="openDialog()">新增账号</el-button>
        </div>
      </div>
    </template>

    <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120">
        <template #default="{ row }">
          <el-tag v-if="!row.realName" type="info" size="small">未完善信息</el-tag>
          <span v-else>{{ row.realName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="身份角色" width="150" align="center">
        <template #default="{ row }">
          <el-tag :type="getRoleTag(row.role)" effect="dark">
            {{ formatRole(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" icon="Edit" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="warning" icon="RefreshLeft" @click="handleResetPwd(row)">重置密码</el-button>
          <el-button size="small" type="danger" icon="Delete" v-if="row.username !== 'admin'" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; text-align: right">
      <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="10"
          @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑账号' : '新增账号'" width="450px">
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="初始密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="默认123456" show-password />
        </el-form-item>
        <el-form-item label="角色身份" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio label="doctor">医生</el-radio>
            <el-radio label="patient">患者</el-radio>
            <el-radio label="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
            v-if="form.role === 'doctor' && !form.id"
            title="提示：创建医生账号后，请前往【医生管理】完善排班和科室信息。"
            type="info"
            show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>


<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = reactive({ username: '', role: '' })
const form = reactive({ id: null, username: '', password: '', role: 'doctor' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}
const formatRole = (role) => ({ admin: '管理员', doctor: '医生', patient: '患者' }[role] || role)
const getRoleTag = (role) => ({ admin: 'danger', doctor: 'success', patient: '' }[role] || 'info')
const handleQuery = () => fetchData()
const handlePageChange = (page) => fetchData(page)

const fetchData = async (page = 1) => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/user/list', {
      params: { ...queryParams, page }
    })
    if (res.data.code === 200) {
      tableData.value = res.data.data.list
      total.value = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await axios.post('/api/admin/user/save', form)
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除 ${row.username}？`, '警告', { type: 'warning' })
  try {
    await axios.delete(`/api/admin/user/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleResetPwd = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
    inputPattern: /\S{6,}/,
    inputErrorMessage: '密码长度需大于6位'
  })
  try {
    await axios.post('/api/admin/user/reset-pwd', { id: row.id, password: value })
    ElMessage.success('密码重置成功')
  } catch (e) {
    ElMessage.error('重置失败')
  }
}

const openDialog = (row = null) => {
  if (row) {
    form.id = row.id
    form.username = row.username
    form.role = row.role
    form.password = ''
  } else {
    form.id = null
    form.username = ''
    form.password = ''
    form.role = 'doctor'
  }
  dialogVisible.value = true
}



onMounted(() => fetchData())
</script>

<style scoped>
.user-card { margin: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: bold; border-left: 4px solid #409EFF; padding-left: 10px; }
.right-actions { display: flex; align-items: center; }
</style>