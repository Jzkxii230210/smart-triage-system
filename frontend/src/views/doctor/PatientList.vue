<template>
  <el-card>
    <template #header>
      <div class="flex-between">
        <span>📂 患者档案管理</span>
        <div style="width: 300px">
          <el-input v-model="keyword" placeholder="输入患者姓名搜索..." clearable @clear="fetchList" @keyup.enter="fetchList">
            <template #append><el-button :icon="Search" @click="fetchList" /></template>
          </el-input>
        </div>
      </div>
    </template>

    <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="lastVisit" label="最近就诊" width="180" />

      <el-table-column label="档案操作">
        <template #default="{ row }">
          <el-button type="primary" link @click="openProfile(row)">查看详细档案</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawerVisible" title="患者全景视图" size="50%">
      <div v-if="currentProfile">
        <el-descriptions title="基本信息" :column="2" border class="mb-4">
          <el-descriptions-item label="姓名">{{ currentProfile.name }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentProfile.phone }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentProfile.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentProfile.age }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs type="border-card">
          <el-tab-pane label="📅 预约/挂号记录">
            <el-timeline>
              <el-timeline-item
                  v-for="(appt, index) in currentProfile.appointments"
                  :key="index"
                  :timestamp="appt.date"
                  placement="top"
                  :type="getStatusType(appt.status)"
              >
                <el-card>
                  <div class="flex-between">
                    <div>
                      <h4>{{ appt.realName }} 医生 ({{ appt.timeSlot }})</h4>
                      <p>当前状态：
                        <el-tag :type="getStatusType(appt.status)">
                          {{ getStatusText(appt.status) }}
                        </el-tag>
                      </p>
                    </div>
                    <div v-if="appt.status !== 2">
                      <el-button
                          v-if="appt.status === 0"
                          type="primary" size="small"
                          @click="changeStatus(appt.id, 1)"
                      >接诊</el-button>

                      <el-button
                          v-if="appt.status === 1"
                          type="success" size="small"
                          @click="openFinishDialog(appt.id)"
                      >完成就诊</el-button>
                      <el-button
                          type="info" link size="small"
                          @click="changeStatus(appt.id, -1)"
                      >取消预约</el-button>
                    </div>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>

          <el-tab-pane label="📋 电子病历归档">
            <el-collapse accordion>
              <el-collapse-item
                  v-for="(record, index) in currentProfile.medicalRecords"
                  :key="record.id"
                  :title="record.createTime + ' - 诊断医师：' + record.realName"
              >
                <div v-if="!record.editing">
                  <p><strong>诊断结果：</strong> {{ record.diagnosis }}</p>
                  <el-divider />
                  <p><strong>处方医嘱：</strong> {{ record.prescription }}</p>
                  <div style="text-align: right; margin-top: 10px;">
                    <el-button size="small" @click="editMedicalRecord(record)">编辑</el-button>
                  </div>
                </div>
                <div v-else>
                  <el-form :model="record" label-position="top">
                    <el-form-item label="诊断结果">
                      <el-input
                          v-model="record.diagnosis"
                          type="textarea"
                          :rows="3"
                          placeholder="请输入诊断结果"
                      />
                    </el-form-item>
                    <el-form-item label="处方医嘱">
                      <el-input
                          v-model="record.prescription"
                          type="textarea"
                          :rows="3"
                          placeholder="请输入处方医嘱"
                      />
                    </el-form-item>
                    <div style="text-align: right; margin-top: 10px;">
                      <el-button size="small" @click="cancelEdit(record)">取消</el-button>
                      <el-button size="small" type="primary" @click="saveMedicalRecord(record)">保存</el-button>
                    </div>
                  </el-form>
                </div>
              </el-collapse-item>
            </el-collapse>
            <el-empty v-if="!currentProfile.medicalRecords?.length" description="暂无历史病历" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <el-dialog v-model="finishDialogVisible" title="填写诊断结果" width="400px">
      <el-form label-position="top">
        <el-form-item label="诊断建议">
          <el-input type="textarea" v-model="finishForm.diagnosis" placeholder="例如：上呼吸道感染，建议休息" />
        </el-form-item>
        <el-form-item label="处方/医嘱">
          <el-input type="textarea" v-model="finishForm.prescription" placeholder="例如：阿莫西林 x 1盒" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFinish">确认完成</el-button>
      </template>
    </el-dialog>

  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const keyword = ref('')
const tableData = ref([])
const drawerVisible = ref(false)
const currentProfile = ref(null)
const loading = ref(false)
const drawerLoading = ref(false)
const finishDialogVisible = ref(false)
const finishForm = ref({ appointmentId: null, diagnosis: '', prescription: '' })

const getStatusType = (status) => {
  if (status === 2) return '已完成'
  if (status === 1) return '就诊中'
  if (status === 0) return '待就诊'
  if (status === -1) return '已取消'
}

const getStatusText = (status) => {
  const map = { 0: '待就诊', 2: '已完成', 1: '就诊中', '-1': '已取消' }
  return map[status] || '未知'
}

// 1. 加载患者列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/doctor/patient-manager/list', {
      params: { keyword: keyword.value }
    })

    if (res.data.code === 200) {
      tableData.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '获取列表失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络请求异常')
  } finally {
    loading.value = false
  }
}

// 2. 打开详情并加载数据
const openProfile = async (row) => {
  drawerVisible.value = true
  drawerLoading.value = true

  currentProfile.value = null

  try {
    const res = await axios.get(`/api/doctor/patient-manager/profile/${row.id}`)

    if (res.data.code === 200) {
      currentProfile.value = res.data.data
    } else {
      ElMessage.error(res.data.msg || '获取档案失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('无法加载档案详情')
  } finally {
    drawerLoading.value = false
  }
}
// 3. 删除患者
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要删除患者 "${row.name}" 吗？此操作将同时删除其所有预约和病历记录！`,
      '警告',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      const res = await axios.delete(`/api/doctor/patient-manager/delete/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchList() // 刷新列表
      } else {
        ElMessage.error(res.data.msg)
      }
    } catch (e) {
      ElMessage.error('删除请求失败')
    }
  })
}

// 4. 简单更改状态

const changeStatus = async (apptId, newStatus) => {
  try {
    const res = await axios.post('/api/doctor/patient-manager/appointment/status', {
      appointmentId: apptId,
      status: newStatus
    })

    if (res.data.code === 200) {
      if (newStatus === 1) {
        ElMessage.success('接诊成功！已自动创建在线问诊会话，请前往“在线问诊”查看。')
      } else {
        ElMessage.success('状态更新成功')
      }

      // 刷新详情页数据
      if (currentProfile.value) refreshProfile(currentProfile.value.patientId)
      // 刷新列表
      fetchList()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('网络请求异常')
  }
}

// 5. 打开完成就诊弹窗
const openFinishDialog = (apptId) => {
  finishForm.value = { appointmentId: apptId, diagnosis: '', prescription: '' }
  finishDialogVisible.value = true
}

// 6. 提交完成就诊
const submitFinish = async () => {
  if (!finishForm.value.diagnosis) return ElMessage.warning('请填写诊断建议')

  try {
    const res = await axios.post('/api/doctor/patient-manager/appointment/status', {
      appointmentId: finishForm.value.appointmentId,
      status: 2,
      diagnosis: finishForm.value.diagnosis,
      prescription: finishForm.value.prescription
    })

    if (res.data.code === 200) {
      ElMessage.success('就诊已完成，病历已归档')
      finishDialogVisible.value = false
      if (currentProfile.value) refreshProfile(currentProfile.value.patientId)
    }
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

// 添加编辑相关方法
const editMedicalRecord = (record) => {
  // 保存原始数据，用于取消编辑
  record.originalDiagnosis = record.diagnosis;
  record.originalPrescription = record.prescription;
  record.editing = true;
}

const cancelEdit = (record) => {
  // 恢复原始数据
  record.diagnosis = record.originalDiagnosis;
  record.prescription = record.originalPrescription;
  record.editing = false;
}

const saveMedicalRecord = async (record) => {
  try {
    const res = await axios.put(`/api/doctor/patient-manager/medical-record/${record.id}`, {
      diagnosis: record.diagnosis,
      prescription: record.prescription,
      patientId: record.patientId,
      doctorId: record.doctorId,
      appointmentId: record.appointmentId
    });

    if (res.data.code === 200) {
      ElMessage.success('病历更新成功');
      record.editing = false;
      // 刷新数据
      if (currentProfile.value) {
        refreshProfile(currentProfile.value.patientId);
      }
    } else {
      ElMessage.error(res.data.msg || '更新失败');
    }
  } catch (error) {
    ElMessage.error('网络请求失败');
  }
}


// 辅助：刷新详情数据
const refreshProfile = async (pid) => {
  const res = await axios.get(`/api/doctor/patient-manager/profile/${pid}`)
  if (res.data.code === 200) currentProfile.value = res.data.data
}

// 页面加载时自动查一次
onMounted(() => {
  fetchList()
})
</script>
<style scoped>
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb-4 { margin-bottom: 20px; }
</style>