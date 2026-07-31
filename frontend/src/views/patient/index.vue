<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <h2>🏥 欢迎来到智慧医疗服务平台</h2>
            <p>基于 Spring AI Alibaba 的新一代智能导诊系统，为您提供精准的就医指引。</p>
            <div class="action-buttons">
              <el-button type="primary" size="large" @click="$router.push('/patient/triage')">
                <el-icon class="mr-1"><Cpu /></el-icon> 开始 AI 智能导诊
              </el-button>
              <el-button type="success" size="large" @click="$router.push('/patient/booking')">
                <el-icon class="mr-1"><Calendar /></el-icon> 预约挂号
              </el-button>
            </div>
          </div>
        </el-card>

        <h3 class="section-title">科室导航</h3>
        <el-card shadow="never" v-loading="loading">
          <el-row :gutter="20">
            <el-col :span="6" v-for="dept in dashboardData.departments" :key="dept.name" class="mb-3">
              <div class="dept-item" @click="handleDeptClick(dept)">
                <div class="dept-icon">{{ dept.icon }}</div>
                <span class="dept-name">{{ dept.name }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card header="📢 最新公告" class="notice-card" v-loading="loading">
          <ul class="notice-list">
            <li v-for="item in dashboardData.notices" :key="item.id">
              <el-tag size="small" :type="item.type" effect="plain">{{ item.tag }}</el-tag>
              <span class="notice-title">{{ item.title }}</span>
            </li>
          </ul>
        </el-card>

        <el-card header="👨‍⚕️ 专家推荐" class="mt-3" v-loading="loading">
          <div v-for="doc in dashboardData.recommendDoctors" :key="doc.id" class="doc-item">
            <el-avatar :size="40" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <div class="doc-info">
              <div class="name">{{ doc.name }} <el-tag size="small">{{ doc.title }}</el-tag></div>
              <div class="dept">{{ doc.deptName }} | {{ doc.expertise }}</div>
            </div>
            <el-button link type="primary" @click="$router.push('/patient/booking')">预约</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Cpu, Calendar } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const dashboardData = ref({
  departments: [],
  notices: [],
  recommendDoctors: []
})

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/patient/dashboard')
    if(res.data.code === 200) {
      dashboardData.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('请求错误:', error)
    if (error.response) {
      ElMessage.error(`请求失败: ${error.response.status} - ${error.response.statusText}`)
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }
  } finally {
    loading.value = false
  }
}

const handleDeptClick = (dept) => {
  router.push({ path: '/patient/booking', query: { deptName: dept.name }})
}

// 页面加载时执行
onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container { padding: 20px; }
.welcome-card {
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  color: #2c3e50;
  border: none;
  margin-bottom: 25px;
}
.welcome-content h2 { margin-top: 0; }
.action-buttons { margin-top: 20px; display: flex; gap: 15px; }
.mr-1 { margin-right: 5px; }
.section-title { margin: 0 0 15px 5px; font-weight: 600; color: #303133; border-left: 4px solid #409EFF; padding-left: 10px; }

.dept-item {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 15px; border: 1px solid #EBEEF5; border-radius: 8px; cursor: pointer;
  transition: all 0.3s; background: white;
}
.dept-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); transform: translateY(-2px); border-color: #409EFF; }
.dept-icon { font-size: 28px; margin-bottom: 8px; }
.mb-3 { margin-bottom: 20px; }
.mt-3 { margin-top: 20px; }

.notice-list { list-style: none; padding: 0; margin: 0; }
.notice-list li { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px dashed #eee; cursor: pointer; gap: 8px;}
.notice-list li:hover .notice-title { color: #409EFF; }
.notice-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 14px;}

.doc-item { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.doc-info { flex: 1; }
.doc-info .name { font-weight: bold; font-size: 14px; }
.doc-info .dept { font-size: 12px; color: #909399; margin-top: 2px; }
</style>