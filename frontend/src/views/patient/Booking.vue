<template>
  <div class="booking-container">
    <el-card shadow="never">
      <template #header>
        <div class="flex-between">
          <span>📅 自助挂号大厅</span>
          <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择就诊日期"
              :disabled-date="disabledDate"
              :clearable="false"
              value-format="YYYY-MM-DD"
              @change="loadDoctorSchedule"
          />
        </div>
      </template>

      <el-tabs v-model="activeDept" type="card" @tab-change="handleDeptChange">
        <el-tab-pane label="全部科室" name="0" />
        <el-tab-pane
            v-for="dept in deptList"
            :key="dept.id"
            :label="dept.name"
            :name="dept.name"
        />
      </el-tabs>

      <div v-loading="loading" style="min-height: 200px">
        <el-empty v-if="doctorList.length === 0" description="该条件下暂无排班" />

        <el-row :gutter="20" v-else>
          <el-col :span="8" v-for="doc in doctorList" :key="doc.id" class="mb-20">
            <el-card shadow="hover" :body-style="{ padding: '0px' }" class="doc-card">
              <div class="doc-header">
                <el-avatar :size="70" :src="doc.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                <div class="doc-titling">
                  <div class="name">{{ doc.realName }}</div>
                  <el-tag size="small" type="success">{{ doc.title }}</el-tag>
                  <div class="dept-text">{{ doc.deptName }}</div>
                </div>
              </div>
              <div class="doc-body">
                <p class="intro" :title="doc.expertise">擅长: {{ doc.expertise || '暂无详细介绍' }}</p>
                <div class="fee-row">
                  <span>挂号费: <b class="price">￥{{ doc.fee }}</b></span>
                  <span>剩余号源: <b :class="doc.rest > 0 ? 'stock-ok':'stock-no'">{{ doc.rest }}</b></span>
                </div>
              </div>
              <el-button
                  type="primary"
                  class="book-btn"
                  :disabled="doc.rest <= 0"
                  @click="openBookDialog(doc)"
              >
                {{ doc.rest > 0 ? '立即预约' : '号源已满' }}
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="确认挂号信息" width="400px">
      <el-form label-width="80px">
        <el-form-item label="就诊医生">
          <strong>{{ currentDoc.realName }}</strong> ({{ currentDoc.deptName }})
        </el-form-item>
        <el-form-item label="就诊日期">
          <el-input v-model="bookForm.dateStr" readonly />
        </el-form-item>
        <el-form-item label="就诊时段">
          <el-radio-group v-model="bookForm.slot">
            <el-radio value="AM" label="AM" border>上午 08:00-11:30</el-radio>
            <el-radio value="PM" label="PM" border class="mt-2">下午 14:00-17:00</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="confirmBooking">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import dayjs from 'dayjs'

const route = useRoute()

const activeDept = ref('0')
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const loading = ref(false)
const submitting = ref(false)
const deptList = ref([])
const doctorList = ref([])
const dialogVisible = ref(false)
const currentDoc = ref({})
const bookForm = ref({ dateStr: '', slot: 'AM' })

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 1. 获取科室
const fetchDepartments = async () => {
  try {
    const res = await axios.get('/api/patient/appointment/depts')
    if(res.data.code === 200) {
      deptList.value = res.data.data
    }
  } catch (err) {
    console.error(err)
  }
}

// 2. 加载医生
const loadDoctorSchedule = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/patient/appointment/doctors', {
      params: {
        deptName: activeDept.value,
        date: selectedDate.value
      }
    })
    if (res.data.code === 200) {
      doctorList.value = res.data.data
    } else {
      doctorList.value = []
    }
  } catch (err) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const handleDeptChange = () => {
  loadDoctorSchedule()
}

const openBookDialog = (doc) => {
  currentDoc.value = doc
  bookForm.value.dateStr = selectedDate.value
  bookForm.value.slot = 'AM'
  dialogVisible.value = true
}

const timeSlotMap = {
  'AM': '08:00-11:30',
  'PM': '14:00-17:00'
};

// 3. 确认预约
const confirmBooking = async () => {
  submitting.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录系统')
      submitting.value = false
      return
    }
    const res = await axios.post('/api/patient/appointment', {
      doctorId: currentDoc.value.id,
      appointmentDate: bookForm.value.dateStr,
      slot: bookForm.value.slot,
      timeSlot: timeSlotMap[bookForm.value.slot]
    }, {
      headers: {
        'Authorization': token
      }
    })

    if (res.data.code === 200) {
      ElMessage.success('预约成功')
      dialogVisible.value = false
      loadDoctorSchedule()
    } else {
      ElMessage.error(res.data.msg || '预约失败')
    }
  } catch (err) {
    console.error(err)
    if (err.response && err.response.status === 401) {
      ElMessage.error('登录过期，请重新登录')
    } else {
      ElMessage.error('系统繁忙，请稍后再试')
    }
  } finally {
    submitting.value = false
  }
}


onMounted(async () => {
  if (route.query.deptName) activeDept.value = route.query.deptName
  await fetchDepartments()
  loadDoctorSchedule()
})
</script>

<style scoped>
.booking-container { padding: 20px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb-20 { margin-bottom: 20px; }
.mt-2 { margin-top: 10px; }

.doc-card { position: relative; overflow: hidden; transition: 0.3s; }
.doc-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.doc-header { background: #f5f7fa; padding: 15px; display: flex; align-items: center; gap: 15px; }
.doc-titling .name { font-size: 16px; font-weight: bold; margin-bottom: 5px; }
.doc-titling .dept-text { font-size: 12px; color: #909399; margin-top: 3px; }

.doc-body { padding: 15px; }
.intro {
  color: #606266; font-size: 13px; height: 40px;
  overflow: hidden; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  margin-bottom: 15px;
}
.fee-row { display: flex; justify-content: space-between; font-size: 13px; color: #606266; }
.price { color: #f56c6c; font-size: 16px; font-weight: bold;}
.stock-ok { color: #67c23a; font-weight: bold;}
.stock-no { color: #909399; font-weight: bold;}

.book-btn { width: 100%; border-radius: 0 0 4px 4px; }
</style>