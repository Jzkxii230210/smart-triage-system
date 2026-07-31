<template>
  <el-card shadow="never" style="margin: 20px;">
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <span style="font-weight: bold; font-size: 16px;">🏥 科室管理</span>
      <el-button type="primary" icon="Plus" @click="openDialog()">新增科室</el-button>
    </div>

    <el-table :data="depts" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="科室名称" width="200" font-weight="bold" />
      <el-table-column prop="description" label="科室简介" min-width="300" show-overflow-tooltip />

      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑科室' : '新增科室'" width="500px">
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：心血管内科" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              rows="4"
              placeholder="请输入科室主要诊疗范围"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const depts = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
}

// 获取列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/dept/list')
    if (res.data.code === 200) {
      depts.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 保存
const save = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.post('/api/admin/dept/save', form)
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除科室【${row.name}】吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/api/admin/dept/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

const openDialog = (row = null) => {
  dialogVisible.value = true
  if(formRef.value) formRef.value.resetFields()

  if (row) {
    form.id = row.id
    form.name = row.name
    form.description = row.description
  } else {
    form.id = null
    form.name = ''
    form.description = ''
  }
}

onMounted(() => fetchData())
</script>