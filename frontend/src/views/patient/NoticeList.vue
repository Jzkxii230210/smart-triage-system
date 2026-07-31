<template>
  <div class="notice-container">
    <el-card>
      <template #header>📢 医院公告 & 新闻</template>
      <el-table :data="newsList" style="width: 100%" @row-click="openDetail" highlight-current-row class="cursor-pointer" v-loading="loading">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type" size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="发布日期" width="150" sortable />
      </el-table>
    </el-card>

    <el-drawer v-model="drawer" :title="currentNews.title" size="50%">
      <div class="news-meta">
        <span>发布人: {{ currentNews.author }}</span> | <span>{{ currentNews.date }}</span>
      </div>
      <el-divider />
      <div class="news-content">
        {{ currentNews.content || '暂无内容' }}
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const newsList = ref([])
const currentNews = ref({})
const drawer = ref(false)
const loading = ref(false)

// 页面加载时请求数据
onMounted(() => {
  fetchNoticeList()
})

// 请求公告列表
const fetchNoticeList = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/notice/list')
    if (res.data.code === 200) {
      newsList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取公告失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 打开详情抽屉
const openDetail = async (row) => {
   try {
     const res = await axios.get(`/api/notice/detail/${row.id}`)
     currentNews.value = res.data.data
     drawer.value = true
   } catch (error) {
     ElMessage.error('获取详情失败')
   }
}
</script>

<style scoped>
.notice-container { padding: 20px; }
.cursor-pointer { cursor: pointer; }
.news-meta { color: #999; font-size: 13px; margin-bottom: 20px; }
.news-content { line-height: 1.8; font-size: 15px; color: #333; }
</style>