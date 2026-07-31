<template>
  <div class="triage-db" style="padding: 20px;">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 16px;">🧠 智能导诊知识库管理</span>
          <el-button
              v-if="activeTab === 'rules'"
              type="primary"
              icon="Plus"
              size="small"
              @click="openDialog()"
          >添加规则</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">

        <el-tab-pane label="症状-科室映射" name="rules">
          <el-table :data="rules" border stripe v-loading="loading">
            <el-table-column prop="keyword" label="症状关键词" width="180">
              <template #default="{ row }">
                <el-tag effect="plain">{{ row.keyword }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetDept" label="推荐科室" width="180" />
            <el-table-column prop="weight" label="权重(1-10)" width="120" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.weight" :max="10" disabled text-color="#ff9900" />
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="最后更新" min-width="160" />

            <el-table-column label="操作" width="180" align="center">
              <template #default="{ row }">
                <el-button link type="primary" icon="Edit" @click="openDialog(row)">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="AI模型提示词配置" name="prompt">
          <el-alert
              title="提示：System Message 是设定 AI 行为基准的核心指令，请谨慎修改。"
              type="warning"
              show-icon
              :closable="false"
              style="margin-bottom: 20px;"
          />
          <el-form label-position="top">
            <el-form-item label="系统预设 Prompt (System Message)">
              <el-input
                  type="textarea"
                  :rows="12"
                  v-model="systemPrompt"
                  placeholder="正在加载配置..."
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="promptLoading" @click="savePrompt">更新模型配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑规则' : '添加规则'" width="450px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="症状关键词">
          <el-input v-model="form.keyword" placeholder="例如：胸痛" />
        </el-form-item>
        <el-form-item label="推荐科室">
          <el-input v-model="form.targetDept" placeholder="例如：心血管内科" />
        </el-form-item>
        <el-form-item label="权重">
          <el-slider v-model="form.weight" :min="1" :max="10" show-input />
          <div style="font-size: 12px; color: #999;">权重越高，AI 在匹配时越优先考虑此规则</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 状态定义
const activeTab = ref('rules')
const loading = ref(false)
const promptLoading = ref(false)
const rules = ref([])
const systemPrompt = ref('')

const dialogVisible = ref(false)
const form = reactive({
  id: null,
  keyword: '',
  targetDept: '',
  weight: 5
})

// 1. 获取规则列表
const fetchRules = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/triage/rules')
    if (res.data.code === 200) {
      rules.value = res.data.data
    }
  } finally {
    loading.value = false
  }
}

// 2. 获取Prompt
const fetchPrompt = async () => {
  try {
    const res = await axios.get('/api/admin/triage/prompt')
    if (res.data.code === 200) {
      systemPrompt.value = res.data.data
    }
  } catch(e) {}
}

// 3. 提交规则
const submitRule = async () => {
  if(!form.keyword || !form.targetDept) return ElMessage.warning('请填写完整信息')
  try {
    await axios.post('/api/admin/triage/rule/save', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchRules()
  } catch(e) { ElMessage.error('失败') }
}

// 4. 删除规则
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该条规则吗？', '提示', { type: 'warning' })
      .then(async () => {
        await axios.delete(`/api/admin/triage/rule/${row.id}`)
        ElMessage.success('删除成功')
        fetchRules()
      })
}

// 5. 保存 Prompt
const savePrompt = async () => {
  promptLoading.value = true
  try {
    await axios.post('/api/admin/triage/prompt/save', { prompt: systemPrompt.value })
    ElMessage.success('AI 模型配置已更新')
  } catch(e) {
    ElMessage.error('更新失败')
  } finally {
    promptLoading.value = false
  }
}

// 交互辅助
const openDialog = (row) => {
  dialogVisible.value = true
  if (row) {
    Object.assign(form, row)
  } else {
    form.id = null
    form.keyword = ''
    form.targetDept = ''
    form.weight = 5
  }
}

const handleTabChange = (tab) => {
  if (tab === 'rules') fetchRules()
  if (tab === 'prompt') fetchPrompt()
}

// 初始化
onMounted(() => {
  fetchRules()
  fetchPrompt() // 预加载
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>