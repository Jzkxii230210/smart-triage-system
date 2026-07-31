<template>
  <el-card shadow="never" style="margin: 20px;">
    <template #header>
      <div style="font-weight: bold; font-size: 16px;">📅 医生排班与信息管理</div>
    </template>

    <el-table :data="doctors" border stripe v-loading="loading">
      <el-table-column prop="realName" label="医生姓名" width="120" />
      <el-table-column prop="deptName" label="所属科室" width="120" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="fee" label="挂号费" width="100">
        <template #default="{ row }">￥{{ row.fee }}</template>
      </el-table-column>

      <el-table-column label="近期排班 (点击 X 取消)" min-width="280">
        <template #default="{ row }">
          <div v-if="row.schedules && row.schedules.length > 0">
            <el-tag
                v-for="sche in row.schedules"
                :key="sche.id"
                :type="sche.slot === 'AM' ? '' : 'success'"
                class="ml-2"
                closable
                style="margin-right: 5px; margin-bottom: 5px;"
                @close="cancelSchedule(sche, row.realName)"
            >
              {{ formatDate(sche.workDate) }}({{ sche.slot === 'AM' ? '上午' : '下午' }})
            </el-tag>
          </div>
          <span v-else style="color: #999; font-size: 12px;">暂无排班</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openScheduleDialog(row)">排班</el-button>
          <el-button size="small" type="info" link @click="openDetailDialog(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="scheduleVisible" title="新增排班" width="400px">
      <div v-if="currentDoc" style="margin-bottom: 15px;">
        为 <el-tag>{{ currentDoc.realName }}</el-tag> 设置排班：
      </div>
      <el-form :model="scheduleForm" label-width="80px">
        <el-form-item label="日期">
          <el-date-picker
              v-model="scheduleForm.workDate"
              type="date"
              value-format="YYYY-MM-DD"
              :disabled-date="(t) => t.getTime() < Date.now() - 8.64e7"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="scheduleForm.slot" style="width: 100%">
            <el-option label="上午 (AM)" value="AM" />
            <el-option label="下午 (PM)" value="PM" />
          </el-select>
        </el-form-item>
        <el-form-item label="号源数">
          <el-input-number v-model="scheduleForm.maxNum" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSchedule">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="医生详细资料" width="500px">
      <el-descriptions :column="2" border v-if="currentDoc">
        <el-descriptions-item label="姓名">{{ currentDoc.realName }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ currentDoc.deptName }}</el-descriptions-item>
        <el-descriptions-item label="职称">
          <el-tag size="small">{{ currentDoc.title }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="挂号费">{{ currentDoc.fee }} 元</el-descriptions-item>
        <el-descriptions-item label="默认号源">{{ currentDoc.stock }} / 天</el-descriptions-item>
        <el-descriptions-item label="擅长领域" :span="2">
          {{ currentDoc.expertise || '该医生暂未填写擅长领域信息。' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const doctors = ref([])

// 排班相关
const scheduleVisible = ref(false)
const scheduleForm = reactive({ doctorId: null, workDate: '', slot: 'AM', maxNum: 30 })

// 详情相关
const detailVisible = ref(false)
const currentDoc = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/doctor/list')
    doctors.value = res.data.data
  } finally { loading.value = false }
}

// 打开详情
const openDetailDialog = (row) => {
  currentDoc.value = row
  detailVisible.value = true
}

// 打开排班
const openScheduleDialog = (row) => {
  currentDoc.value = row
  scheduleForm.doctorId = row.id
  scheduleForm.workDate = ''
  scheduleForm.maxNum = row.stock || 30
  scheduleVisible.value = true
}

// 提交排班
const submitSchedule = async () => {
  if(!scheduleForm.workDate) return ElMessage.warning('请选择日期')
  try {
    await axios.post('/api/admin/doctor/schedule/save', scheduleForm)
    ElMessage.success('排班成功')
    scheduleVisible.value = false
    fetchData()
  } catch(e) { ElMessage.error('失败') }
}

// 取消排班
const cancelSchedule = (schedule, doctorName) => {
  const dateStr = formatDate(schedule.workDate)
  const slotStr = schedule.slot === 'AM' ? '上午' : '下午'

  ElMessageBox.confirm(
      `确定要取消【${doctorName}】在 ${dateStr} ${slotStr} 的排班吗？`,
      '取消确认',
      { type: 'warning' }
  ).then(async () => {
    try {
      await axios.delete(`/api/admin/doctor/schedule/${schedule.id}`)
      ElMessage.success('已取消排班')
      fetchData()
    } catch(e) { ElMessage.error('取消失败') }
  })
}

const formatDate = (dateStr) => {
  if(!dateStr) return ''
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const d = new Date(dateStr)
  return `${d.toISOString().split('T')[0]} (${weekDay[d.getDay()]})`
}

onMounted(() => fetchData())
</script>