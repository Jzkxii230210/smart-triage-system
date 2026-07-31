<template>
  <div class="patient-consult-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="flex-between">
          <span>💬 我的在线问诊</span>
          <el-button type="primary" @click="openCreateDialog">发起咨询</el-button>
        </div>
      </template>

      <el-table :data="list" stripe v-loading="loading" height="calc(100vh - 180px)">
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="content" label="病情描述" show-overflow-tooltip>
          <template #default="{row}">
            {{ parseMessage(row.content).text }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{row}">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
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
          :title="'与医生 ' + currentDoctorName + ' 的对话'"
          width="650px"
          :close-on-click-modal="false"
          @close="closeChat"
          top="5vh"
      >
        <div class="chat-box">
          <div class="msg-container" ref="msgContainer" @scroll="handleScroll">
            <div v-for="(msg, i) in messages" :key="i"
                 class="msg-row"
                 :class="msg.sender === 'patient' ? 'row-patient' : 'row-doctor'">

              <div class="avatar">{{ msg.sender === 'patient' ? '我' : '医' }}</div>

              <div class="bubble-group">
                <div v-if="msg.parsedImage" class="msg-img-box">
                  <el-image
                      :src="msg.parsedImage"
                      :preview-src-list="[msg.parsedImage]"
                      fit="cover"
                      class="bubble-img"
                  />
                </div>
                <div v-if="msg.parsedText" class="bubble">{{ msg.parsedText }}</div>

                <div class="time">{{ msg.time ? formatTime(msg.time, true) : '' }}</div>
              </div>
            </div>
          </div>

          <div class="input-panel">
            <div v-if="previewImage" class="preview-bar">
              <div class="preview-item">
                <el-image :src="previewImage" fit="cover" />
                <span class="img-size">Base64: {{ (imageBase64.length/1024).toFixed(0) }}KB</span>
                <el-icon class="del-icon" @click="clearImage"><CloseBold /></el-icon>
              </div>
            </div>

            <el-input
                v-model="replyContent"
                type="textarea"
                :rows="3"
                resize="none"
                placeholder="请输入病情描述，支持语音和图片..."
                :disabled="sending"
            />

            <div class="toolbar">
              <div class="left-tools">
                <el-button
                    :type="isRecording ? 'danger' : 'info'"
                    plain
                    size="small"
                    @click="toggleVoice"
                    :loading="voiceLoading"
                >
                  <el-icon class="mr-1"><Microphone /></el-icon>
                  {{ isRecording ? `停止录音 (${recordingTime}s)` : '点击说话' }}
                </el-button>

                <el-button type="success" plain size="small" @click="triggerUpload">
                  <el-icon class="mr-1"><Picture /></el-icon> 图片
                </el-button>
                <input
                    type="file"
                    ref="fileInput"
                    accept="image/jpeg, image/png"
                    style="display: none"
                    @change="handleFileChange"
                />
              </div>

              <div class="right-tools">
                <el-button
                    type="danger"
                    size="small"
                    @click="endConsultation"
                    v-if="currentConsultationStatus === 1"
                >
                  结束会诊
                </el-button>
                <el-button @click="chatVisible = false" size="small">关闭</el-button>
                <el-button
                    type="primary"
                    size="small"
                    @click="doSend"
                    :loading="sending"
                    :disabled="!replyContent.trim() && !imageBase64"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>

      <el-dialog v-model="createVisible" title="发起新咨询" width="500px">
        <el-form :model="createForm" label-width="80px">
          <el-form-item label="选择医生">
            <el-select v-model="createForm.doctorId" placeholder="请选择医生" style="width: 100%">
              <el-option
                  v-for="item in doctorOptions"
                  :key="item.id"
                  :label="item.name + ' (' + item.dept + ')'"
                  :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="病情描述">
            <el-input
                v-model="createForm.content"
                type="textarea"
                rows="4"
                placeholder="请详细描述您的症状..."
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="doCreate" :loading="creating">提交</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Microphone, Picture, CloseBold } from '@element-plus/icons-vue' // 需安装图标库

const SILICONFLOW_API_KEY = import.meta.env.VITE_SILICONFLOW_API_KEY
const SILICONFLOW_API_URL = 'https://api.siliconflow.cn/v1/audio/transcriptions'
const BASE_URL = '/api/patient/consultation'
const IMG_SEPARATOR = '<<<IMG>>>' // 文本与图片的分割符

const list = ref([])
const loading = ref(false)
const chatVisible = ref(false)
const currentId = ref(null)
const currentDoctorName = ref('')
const currentConsultationStatus = ref(0) // 当前会诊状态
const messages = ref([])
const replyContent = ref('')
const sending = ref(false)
const msgContainer = ref(null)
let pollTimer = null
const userScrolled = ref(false)

// 图片相关
const fileInput = ref(null)
const previewImage = ref('')
const imageBase64 = ref('')

// 语音相关
const isRecording = ref(false)
const voiceLoading = ref(false)
const recordingTime = ref(0)
let mediaRecorder = null
let audioChunks = []
let recordTimer = null

// 发起咨询相关
const createVisible = ref(false)
const createForm = ref({ doctorId: '', content: '' })
const doctorOptions = ref([])
const creating = ref(false)


const parseMessage = (contentRaw) => {
  if (!contentRaw) return { text: '', img: null }

  if (contentRaw.includes(IMG_SEPARATOR)) {
    const [txt, img] = contentRaw.split(IMG_SEPARATOR)
    return { text: txt || '', img: img || null }
  }

  if (contentRaw.trim().startsWith('data:image')) {
    return { text: '', img: contentRaw }
  }

  return { text: contentRaw, img: null }
}

const triggerUpload = () => fileInput.value.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return

  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    return ElMessage.error('仅支持 JPG/PNG 格式')
  }
  if (file.size > 2 * 1024 * 1024) {
    return ElMessage.error('为了传输速度，图片请小于 2MB')
  }

  const reader = new FileReader()
  reader.onload = (evt) => {
    previewImage.value = evt.target.result
    imageBase64.value = evt.target.result
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

const clearImage = () => {
  previewImage.value = ''
  imageBase64.value = ''
}

const toggleVoice = () => {
  if (isRecording.value) stopVoiceInput()
  else startVoiceInput()
}

const startVoiceInput = async () => {
  if (!SILICONFLOW_API_KEY) return ElMessage.warning('未配置语音API Key')

  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    const mimeType = MediaRecorder.isTypeSupported('audio/webm') ? 'audio/webm' : 'audio/mp4'

    mediaRecorder = new MediaRecorder(stream, { mimeType })
    audioChunks = []

    mediaRecorder.ondataavailable = (e) => {
      if (e.data.size > 0) audioChunks.push(e.data)
    }

    mediaRecorder.onstop = () => {
      const blob = new Blob(audioChunks, { type: mimeType })
      stream.getTracks().forEach(t => t.stop()) // 关麦
      transcribeAudio(blob, mimeType)
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0

    // 60秒自动停止
    recordTimer = setInterval(() => {
      recordingTime.value++
      if (recordingTime.value >= 60) {
        stopVoiceInput()
        ElMessage.warning('单次录音最长60秒')
      }
    }, 1000)
    ElMessage.success('开始录音，请说话...')
  } catch (err) {
    ElMessage.error('麦克风权限获取失败')
  }
}

const stopVoiceInput = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  }
  isRecording.value = false
  clearInterval(recordTimer)
}

const transcribeAudio = async (blob, mimeType) => {
  if (blob.size < 100) return ElMessage.warning('录音时间太短')

  voiceLoading.value = true
  try {
    const ext = mimeType.includes('mp4') ? 'mp4' : 'webm'
    const formData = new FormData()
    formData.append('file', new File([blob], `voice.${ext}`, { type: mimeType }))
    formData.append('model', 'FunAudioLLM/SenseVoiceSmall')

    const res = await fetch(SILICONFLOW_API_URL, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${SILICONFLOW_API_KEY}` },
      body: formData
    })

    const data = await res.json()
    if (data.text) {
      replyContent.value += (replyContent.value ? ' ' : '') + data.text
      ElMessage.success('语音识别成功')
    } else {
      ElMessage.warning('未识别出有效内容')
    }
  } catch (e) {
    ElMessage.error('语音转写服务异常')
  } finally {
    voiceLoading.value = false
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const userStr = localStorage.getItem('user')

    if (!userStr) {
      console.warn("未找到登录用户信息")
      return
    }

    const user = JSON.parse(userStr)
    const pid = user.specificId
    console.log("当前登录的特定角色ID (PatientID):", pid)

    if (!pid) {
      console.error("无法获取患者ID，请检查后端登录接口是否返回了 specificId")
      return
    }

    const res = await axios.get(`${BASE_URL}/list?patientId=${pid}`)
    if(res.data.code === 200) {
      list.value = res.data.data
    }
  } catch (err) {
    console.error("获取列表失败", err)
  } finally {
    loading.value = false
  }
}

const fetchMessages = async () => {
  if(!currentId.value) return
  const res = await axios.get(`${BASE_URL}/history/${currentId.value}`)
  if(res.data.code === 200) {
    const rawData = res.data.data || []
    messages.value = rawData.map(msg => {
      const { text, img } = parseMessage(msg.content)
      return {
        ...msg,
        parsedText: text,
        parsedImage: img
      }
    })
    scrollToBottom()
  }
}

const doSend = async () => {
  const text = replyContent.value.trim()
  const img = imageBase64.value

  if(!text && !img) return ElMessage.warning("内容不能为空")

  sending.value = true
  try {
    let finalContent = text
    if (img) {
      finalContent = text + IMG_SEPARATOR + img
    }

    await axios.post(`${BASE_URL}/reply`, {
      id: currentId.value,
      content: finalContent
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

const openChat = (row) => {
  currentId.value = row.id
  currentDoctorName.value = row.doctorName
  currentConsultationStatus.value = row.status
  chatVisible.value = true
  messages.value = []
  clearImage()
  replyContent.value = ''

  fetchMessages()
  pollTimer = setInterval(fetchMessages, 3000)
}

const closeChat = () => {
  chatVisible.value = false
  clearInterval(pollTimer)
  stopVoiceInput()
  fetchList()
}

// 结束会诊
const endConsultation = async () => {
  try {
    await ElMessageBox.confirm('确定要结束本次会诊吗？结束后的会诊将无法继续对话。', '确认结束', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await axios.post(`${BASE_URL}/end/${currentId.value}`)
    if(res.data.code === 200) {
      ElMessage.success('会诊已结束')
      chatVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.data.msg || '结束会诊失败')
    }
  } catch(e) {
    if(e !== 'cancel') {
      ElMessage.error('网络错误')
    }
  }
}

const fetchDoctors = async () => {
  const res = await axios.get(`${BASE_URL}/doctors`);
  if(res.data.code === 200) doctorOptions.value = res.data.data;
}
const openCreateDialog = () => {
  createForm.value = { doctorId: null, content: '' };
  fetchDoctors();
  createVisible.value = true;
}
const doCreate = async () => {
  if(!createForm.value.doctorId || !createForm.value.content) return ElMessage.warning('请填写完整');
  creating.value = true;
  try {
    await axios.post(`${BASE_URL}/create`, {
      patientId: 1,
      patientName: '当前用户',
      doctorId: createForm.value.doctorId,
      content: createForm.value.content
    });
    createVisible.value = false;
    fetchList();
    ElMessage.success('提交成功');
  } finally { creating.value = false; }
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  userScrolled.value = scrollHeight - scrollTop - clientHeight > 100
}

// 修改滚动逻辑
const scrollToBottom = () => {
  nextTick(() => {
    if(msgContainer.value && !userScrolled.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

const formatTime = (val, timeOnly=false) => {
  if (!val) return ''
  const t = val.replace('T', ' ')
  return timeOnly ? t.split(' ')[1].slice(0, 5) : t
}

// 状态文本映射
const getStatusText = (status) => {
  if(status === 0) return '待接诊'
  if(status === 1) return '诊疗中'
  if(status === 2) return '已完成'
  return '未知'
}

const getStatusType = (status) => {
  if(status === 0) return 'warning'
  if(status === 1) return 'primary'
  if(status === 2) return 'success'
  return 'info'
}

onMounted(fetchList)
onBeforeUnmount(() => {
  clearInterval(pollTimer)
  stopVoiceInput()
})
</script>

<style scoped>
.patient-consult-page { padding: 20px; height: 100vh; box-sizing: border-box; background: #f0f2f5; }
.main-card { height: 100%; display: flex; flex-direction: column; }

.flex-between { display: flex; justify-content: space-between; align-items: center; }

/* 聊天窗口样式 */
.chat-box {
  display: flex; flex-direction: column; height: 500px;
  background: #f5f7fa; border: 1px solid #e4e7ed; border-radius: 4px;
}

.msg-container {
  flex: 1; overflow-y: auto; padding: 20px;
  display: flex; flex-direction: column; gap: 15px;
}

.msg-row { display: flex; gap: 12px; max-width: 85%; }
.row-patient { align-self: flex-end; flex-direction: row-reverse; }
.row-doctor { align-self: flex-start; }

.avatar {
  width: 38px; height: 38px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: bold; flex-shrink: 0;
}
.row-patient .avatar { background: #67C23A; }
.row-doctor .avatar { background: #409EFF; }

.bubble-group { display: flex; flex-direction: column; max-width: 100%; }

.bubble {
  padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5;
  background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  word-break: break-all; white-space: pre-wrap;
}
.row-patient .bubble { background: #95ec69; }

.msg-img-box { margin-bottom: 5px; }
.bubble-img {
  max-width: 200px; max-height: 200px;
  border-radius: 4px; border: 1px solid #ddd; background: #fff;
}

.time { font-size: 12px; color: #999; margin-top: 5px; }
.row-patient .time { text-align: right; }

/* 输入与工具栏 */
.input-panel {
  background: #fff; border-top: 1px solid #ddd; padding: 12px; position: relative;
}

/* 图片预览条 */
.preview-bar {
  position: absolute; bottom: 100%; left: 0; right: 0;
  background: rgba(255,255,255,0.95); border-bottom: 1px solid #ebeef5;
  padding: 8px 12px; z-index: 10;
}
.preview-item {
  display: inline-flex; align-items: center; gap: 10px;
  background: #f0f9eb; padding: 4px 8px; border-radius: 4px; border: 1px solid #e1f3d8;
}

.img-size { font-size: 12px; color: #67c23a; }
.del-icon { cursor: pointer; color: #f56c6c; margin-left: 5px; }

.toolbar {
  display: flex; justify-content: space-between; align-items: center; margin-top: 10px;
}
.left-tools, .right-tools { display: flex; gap: 10px; align-items: center; }
.mr-1 { margin-right: 4px; }
</style>
