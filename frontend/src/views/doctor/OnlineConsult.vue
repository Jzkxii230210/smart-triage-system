<template>
  <div class="doctor-consult-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="flex-between">
          <span>💬 医生在线问诊</span>
          <el-button @click="fetchList" :icon="Refresh" circle size="small" />
        </div>
      </template>

      <el-table :data="list" stripe v-loading="loading" height="calc(100vh - 180px)">
        <el-table-column prop="patientName" label="患者" width="120" />
        <el-table-column prop="content" label="最新消息/病情" show-overflow-tooltip>
          <template #default="{row}">
            {{ parseMessage(row.content).text }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button
                size="small"
                type="primary"
                @click="openChat(row)"
                :disabled="row.status === 2"
            >
              进入对话
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog
          v-model="chatVisible"
          :title="'与 ' + currentPatientName + ' 的诊疗对话'"
          width="650px"
          :close-on-click-modal="false"
          @close="closeChat"
          top="5vh"
      >
        <div class="chat-box">
          <div class="msg-container" ref="msgContainer">
            <div v-for="(msg, i) in messages" :key="i"
                 class="msg-row"
                 :class="msg.sender === 'doctor' ? 'row-doctor' : 'row-patient'">

              <div class="avatar">{{ msg.sender === 'doctor' ? '医' : '患' }}</div>

              <div class="bubble-group">
                <!-- 图片 -->
                <div v-if="msg.parsedImage" class="msg-img-box">
                  <el-image
                      :src="msg.parsedImage"
                      :preview-src-list="[msg.parsedImage]"
                      fit="cover" class="bubble-img"
                  />
                </div>
                <div v-if="msg.parsedText" class="bubble">{{ msg.parsedText }}</div>
                <div class="time">{{ formatTime(msg.time, true) }}</div>
              </div>
            </div>
          </div>

          <div class="input-panel">
            <div v-if="previewImage" class="preview-bar">
              <div class="preview-item">
                <el-image :src="previewImage" fit="cover" />
                <el-icon class="del-icon" @click="clearImage"><CloseBold /></el-icon>
              </div>
            </div>

            <el-input
                v-model="replyContent"
                type="textarea" :rows="3" resize="none"
                placeholder="请输入回复内容..."
                :disabled="sending"
                @keyup.enter.ctrl="doSend"
            />

            <div class="toolbar">
              <div class="left-tools">
                <el-button type="success" plain size="small" @click="triggerUpload">
                  <el-icon class="mr-1"><Picture /></el-icon> 图片
                </el-button>
                <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFileChange" />
              </div>
              <div class="right-tools">
                <el-button type="danger" size="small" @click="endConsultation">结束此次问诊</el-button>
                <el-button type="primary" size="small" @click="doSend" :loading="sending">发送 (Ctrl+Enter)</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, CloseBold, Refresh } from '@element-plus/icons-vue'

const list = ref([])
const loading = ref(false)
const chatVisible = ref(false)
const currentId = ref(null)
const currentPatientName = ref('')
const messages = ref([])
const replyContent = ref('')
const sending = ref(false)
const msgContainer = ref(null)
const fileInput = ref(null)
const previewImage = ref('')
const imageBase64 = ref('')
let pollTimer = null

const IMG_SEPARATOR = '<<<IMG>>>'

const parseMessage = (contentRaw) => {
  if (!contentRaw) return { text: '', img: null }
  if (contentRaw.includes(IMG_SEPARATOR)) {
    const [txt, img] = contentRaw.split(IMG_SEPARATOR)
    return { text: txt || '', img: img || null }
  }
  if (contentRaw.trim().startsWith('data:image')) return { text: '', img: contentRaw }
  return { text: contentRaw, img: null }
}

const formatTime = (val, timeOnly=false) => {
  if (!val) return ''
  const t = val.replace('T', ' ')
  return timeOnly ? t.split(' ')[1].substring(0,5) : t
}

const getStatusType = (s) => (s===2 ? 'success' : (s===1 ? 'primary' : 'warning'))
const getStatusText = (s) => ({0:'待接诊', 1:'诊疗中', 2:'已完成'}[s] || '未知')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/doctor/consultation/list')
    if (res.data.code === 200) list.value = res.data.data
  } finally {
    loading.value = false
  }
}

const fetchMessages = async () => {
  if (!currentId.value) return
  const res = await axios.get(`/api/doctor/consultation/history/${currentId.value}`)
  if (res.data.code === 200) {
    messages.value = res.data.data.map(m => {
      const { text, img } = parseMessage(m.content)
      return { ...m, parsedText: text, parsedImage: img }
    })
    scrollToBottom()
  }
}

const openChat = (row) => {
  currentId.value = row.id
  currentPatientName.value = row.patientName
  chatVisible.value = true
  replyContent.value = ''
  clearImage()
  messages.value = []

  fetchMessages()
  pollTimer = setInterval(fetchMessages, 3000) // 3秒轮询新消息
}

const closeChat = () => {
  chatVisible.value = false
  clearInterval(pollTimer)
  fetchList()
}

const doSend = async () => {
  const text = replyContent.value.trim()
  const img = imageBase64.value
  if (!text && !img) return ElMessage.warning("请输入内容")

  sending.value = true
  try {
    let content = text
    if (img) content = text + IMG_SEPARATOR + img

    await axios.post('/api/doctor/consultation/reply', {
      id: currentId.value,
      content: content
    })

    replyContent.value = ''
    clearImage()
    fetchMessages()
  } catch(e) {
    ElMessage.error("发送失败")
  } finally {
    sending.value = false
  }
}

const endConsultation = () => {
  ElMessageBox.confirm('结束会诊后将无法继续对话，确定结束吗？', '提示', { type: 'warning' })
      .then(async () => {
        await axios.post(`/api/doctor/consultation/end/${currentId.value}`)
        ElMessage.success('会诊已结束')
        closeChat()
      })
      .catch(() => {})
}

// 图片上传
const triggerUpload = () => fileInput.value.click()
const handleFileChange = (e) => {
  const f = e.target.files[0]
  if (!f) return
  const reader = new FileReader()
  reader.onload = (evt) => {
    previewImage.value = evt.target.result
    imageBase64.value = evt.target.result
  }
  reader.readAsDataURL(f)
  e.target.value = ''
}
const clearImage = () => { previewImage.value = ''; imageBase64.value = '' }

const scrollToBottom = () => {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

onMounted(fetchList)
onBeforeUnmount(() => clearInterval(pollTimer))
</script>

<style scoped>
.doctor-consult-page { padding: 20px; height: 100vh; background: #f0f2f5; }
.main-card { height: 100%; display: flex; flex-direction: column; }

.flex-between { display: flex; justify-content: space-between; align-items: center; }

.chat-box { display: flex; flex-direction: column; height: 500px; background: #f5f7fa; border: 1px solid #e4e7ed; }
.msg-container { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 15px; }
.msg-row { display: flex; gap: 10px; max-width: 80%; }
.row-patient { align-self: flex-start; }
.row-doctor { align-self: flex-end; flex-direction: row-reverse; }
.avatar { width: 36px; height: 36px; border-radius: 4px; display: flex; align-items: center; justify-content: center; color:#fff; font-size:12px; flex-shrink: 0; }
.row-patient .avatar { background: #67C23A; }
.row-doctor .avatar { background: #409EFF; }
.bubble-group { display: flex; flex-direction: column; }
.bubble { padding: 8px 12px; border-radius: 6px; background: #fff; font-size: 14px; white-space: pre-wrap; word-break: break-all; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
.row-doctor .bubble { background: #95ec69; }
.bubble-img { max-width: 150px; max-height: 150px; border-radius: 4px; border: 1px solid #eee; margin-bottom: 4px; background: #fff; }
.time { font-size: 11px; color: #aaa; margin-top: 4px; }
.row-doctor .time { text-align: right; }

.input-panel { background: #fff; border-top: 1px solid #ddd; padding: 10px; position: relative; }
.preview-bar { position: absolute; bottom: 100%; left: 0; padding: 5px 10px; background: rgba(255,255,255,0.9); width: 100%; border-bottom: 1px solid #eee; }
.preview-item { position: relative; display: inline-block; }

.del-icon { position: absolute; top: -5px; right: -5px; background: #f56c6c; color: #fff; border-radius: 50%; cursor: pointer; padding: 1px; }

.toolbar { display: flex; justify-content: space-between; margin-top: 8px; }
.mr-1 { margin-right: 4px; }
</style>