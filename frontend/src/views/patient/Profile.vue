<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="user-card" shadow="hover">
          <div class="user-header">
            <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <h2 class="username">{{ userInfo.realName}}</h2>
          </div>
          <el-descriptions :column="1" class="user-details" border>
            <el-descriptions-item label="手机号">{{ userInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="身份证">{{ userInfo.idCard }}</el-descriptions-item>
            <el-descriptions-item label="账户余额">
              <span class="balance">￥{{ userInfo.balance }}</span>
              <el-button link type="primary" size="small" style="margin-left: 10px">充值</el-button>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card shadow="never" class="tabs-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="📋 挂号记录 & 评价" name="records">
              <el-table :data="appointments" stripe style="width: 100%" v-loading="loading">
                <el-table-column prop="date" label="就诊日期" width="120" sortable />
                <el-table-column prop="timeSlot" label="就诊时间" width="120">
                  <template #default="{ row }">
                    {{ row.timeSlot === 'AM' ? '上午' : row.timeSlot === 'PM' ? '下午' : row.timeSlot }}
                  </template>
                </el-table-column>
                <el-table-column label="医生/科室" width="180">
                  <template #default="{ row }">
                    <div class="doc-info">
                      <strong>{{ row.realName }} / {{ row.deptName }}</strong>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="fee" label="挂号费" width="100">
                  <template #default="{ row }">￥{{ row.fee }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" min-width="180" align="right">
                  <template #default="{ row }">
                    <el-button
                        v-if="row.status === 0 || row.status === '待就诊'"
                        size="small"
                        type="danger"
                        plain
                        @click="handleCancel(row)">
                      取消预约
                    </el-button>
                    <el-button
                        v-else-if="!row.isEvaluated && (row.status === 2 || row.status === '已完成')"
                        size="small"
                        type="warning"
                        @click="openEvaluate(row)">
                      去评价
                    </el-button>
                    <el-button
                        v-else-if="row.isEvaluated"
                        size="small"
                        type="info"
                        @click="viewOrModifyEvaluation(row)">
                      查看/修改评价
                    </el-button>
                    <el-button
                        v-else-if="row.status === -1 || row.status === '已取消'"
                        size="small"
                        type="info"
                        disabled>
                      已取消
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="⚙️ 账号设置" name="settings">
              <el-form label-width="100px" style="max-width: 500px; margin-top: 20px;">
                <el-form-item label="联系电话">
                  <el-input v-model="formUser.phone" />
                </el-form-item>
                <el-form-item label="登录密码">
                  <el-input v-model="formUser.password" type="password" placeholder="不修改请留空" show-password/>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updateProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="evalDialogVisible" title="评价医生" width="500px">
      <el-form :model="evalForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="evalForm.rate" :max="5" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="evalForm.comment" type="textarea" :rows="4" placeholder="请输入您的评价..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeEvalDialog">取消</el-button>
          <el-button type="primary" @click="submitEvaluation">提交评价</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>



<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const activeTab = ref('records')
const loading = ref(false)
const evalDialogVisible = ref(false)
const currentEvalRow = ref(null)

const userInfo = ref({})
const formUser = reactive({ phone: '', password: ''})

const evalForm = reactive({ rate: 5, comment: '' })
const appointments = ref([])

onMounted(() => {
  loadUserInfo()
  loadAppointments()
})

const loadUserInfo = async () => {
  try {
    const res = await axios.get('/api/patient/profile/info')
    if(res.data.code === 200) {
      userInfo.value = res.data.data
      formUser.phone = res.data.data.phone
    }
  } catch(e) { console.error(e) }
}

const loadAppointments = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/patient/profile/appointments')
    if(res.data.code === 200) appointments.value = res.data.data
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

const getStatusText = (status) => {
  if(status === 0  || status === '待就诊') return '待就诊'
  if(status === 1  || status === '就诊中') return '就诊中'
  if(status === 2  || status === '已完成') return '已完成'
  if(status === -1  || status === '已取消') return '已取消'
  return status
}

const getStatusType = (status) => {
  if (status === 'pending' || status === '待就诊') return 'primary'
  if (status === 'completed' || status === '已完成') return 'success'
  if (status === 'cancelled' || status === '已取消') return 'info'
  if (status === 'in-progress' || status === '就诊中') return 'warning'
  return 'default'
}


const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消预约吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await axios.post(`/api/patient/profile/cancel/${row.id}`)
      if(res.data.code === 200) {
        ElMessage.success('预约已取消')
        loadAppointments()
      } else {
        ElMessage.error(res.data.msg)
      }
    } catch(e) { ElMessage.error('取消失败') }
  })
}

const openEvaluate = (row) => {
  currentEvalRow.value = row
  evalForm.rate = 5
  evalForm.comment = ''
  evalDialogVisible.value = true
}
const viewOrModifyEvaluation = (row) => {
  currentEvalRow.value = row
  evalForm.rate = row.rate || 5
  evalForm.comment = row.comment || ''
  evalDialogVisible.value = true
}

// 修改提交评价逻辑
const submitEvaluation = async () => {
  if (!evalForm.comment) return ElMessage.warning('请填写评价内容')
  try {
    const payload = {
      appointmentId: currentEvalRow.value.id,
      rate: evalForm.rate,
      comment: evalForm.comment
    }
    const res = await axios.post('/api/patient/profile/evaluate', payload)
    if(res.data.code === 200) {
      ElMessage.success('评价提交成功！')
      evalDialogVisible.value = false
      loadAppointments()
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch(e) {
    ElMessage.error('提交失败')
  }
}

const updateProfile = async () => {
  try {
    const res = await axios.post('/api/patient/profile/update', formUser)
    if(res.data.code === 200) {
      ElMessage.success('修改成功')
      loadUserInfo()
    } else ElMessage.error(res.data.msg)
  } catch(e) { ElMessage.error('请求失败') }
}



// 关闭评价对话框
const closeEvalDialog = () => {
  evalDialogVisible.value = false
}
</script>

<style scoped>
.profile-container { padding: 20px; }
.user-card { text-align: center; height: 100%; }
.user-header { padding: 20px 0; }
.username { margin: 10px 0 5px; color: #303133; }
.balance { font-size: 18px; color: #f56c6c; font-weight: bold; }
.doc-info { display: flex; flex-direction: column; }

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
