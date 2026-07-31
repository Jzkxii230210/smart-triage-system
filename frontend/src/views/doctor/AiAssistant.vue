<template>
  <div class="ai-doc-container">
    <div class="intro">
      <h2>🧠 临床决策辅助系统 (CDSS)</h2>
      <p>仅供参考，不可完全替代医生判断。</p>
      <el-tag type="danger" effect="dark" round>多轮对话模式：可追问病史补充、检查结果或治疗反馈</el-tag>
    </div>

    <div class="chat-container">
      <div class="messages-area" ref="messagesArea">
        <div v-if="messages.length === 0" class="welcome-tip">
          <p>请输入患者的病例特征，上传检查报告图片，或点击下方语音按钮描述病情。</p>
        </div>

        <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
          <div class="message-avatar">
            <span v-if="msg.role === 'ai'">🤖</span>
            <span v-else>👨‍⚕️</span>
          </div>
          <div class="message-content">
            <div v-if="msg.image" class="msg-image">
              <el-image
                  :src="msg.image"
                  :preview-src-list="[msg.image]"
                  fit="cover"
                  style="max-width: 200px; border-radius: 4px;"
              />
            </div>
            <div v-if="msg.content" v-html="msg.content"></div>
          </div>
        </div>

        <div v-if="loading" class="message-row ai">
          <div class="message-avatar">🤖</div>
          <div class="message-content loading">
            <el-icon class="is-loading"><Loading /></el-icon> 正在分析病情...
          </div>
        </div>

        <div v-if="transcribing" class="message-row user">
          <div class="message-avatar">👨‍⚕️</div>
          <div class="message-content">
            <el-icon class="is-loading"><Loading /></el-icon> 正在识别语音...
          </div>
        </div>
      </div>

      <div class="input-area">
        <div v-if="previewUrl" class="image-preview-wrapper">
          <el-image :src="previewUrl" class="preview-img" fit="cover" />
          <div class="close-btn" @click="removeImage">
            <el-icon><CircleCloseFilled /></el-icon>
          </div>
        </div>

        <el-input
            v-model="currentQuery"
            type="textarea"
            :rows="3"
            placeholder="请输入病例信息或追问..."
            :disabled="loading || isRecording || transcribing"
            @keyup.enter.prevent="sendMessage"
        />

        <div class="input-controls">
          <div class="left-controls">
            <el-button
                :type="isRecording ? 'danger' : 'primary'"
                :class="{ 'recording-animate': isRecording }"
                @click="handleVoiceClick"
                :disabled="transcribing || loading"
            >
              <el-icon v-if="!isRecording"><Microphone /></el-icon>
              <el-icon v-else><Mute /></el-icon>
              {{ isRecording ? `停止 (${recordingTime}s)` : '语音输入' }}
            </el-button>

            <el-button
                type="success"
                @click="triggerUpload"
                :disabled="loading || isRecording || transcribing"
            >
              <el-icon><Picture /></el-icon> 上传图片
            </el-button>

            <input
                type="file"
                ref="fileInput"
                accept="image/*"
                style="display: none"
                @change="handleFileChange"
            />
          </div>

          <el-button
              type="primary"
              @click="sendMessage"
              :loading="loading"
              :disabled="(!currentQuery.trim() && !selectedImage) || isRecording || transcribing"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUpdated, onBeforeUnmount, nextTick } from 'vue'
import { marked } from 'marked'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Microphone, Mute, Loading, Picture, CircleCloseFilled } from '@element-plus/icons-vue'

const currentQuery = ref('')
const messages = ref([])
const loading = ref(false)
const transcribing = ref(false)
const messagesArea = ref(null)

//  图片上传
const fileInput = ref(null)
const selectedImage = ref(null)
const previewUrl = ref('')

//  录音
const isRecording = ref(false)
const recordingTime = ref(0)
let mediaRecorder = null
let audioChunks = []
let timerInterval = null
const MAX_RECORD_SECONDS = 60 // 延长到60秒

const SILICONFLOW_API_KEY = import.meta.env.VITE_SILICONFLOW_API_KEY
const SILICONFLOW_API_URL = 'https://api.siliconflow.cn/v1/audio/transcriptions'
const API_URL = '/api/doctor/ai/diagnosis/chat' // 后端接口

// 1. 图片上传
const triggerUpload = () => fileInput.value.click()

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) return ElMessage.error('请上传图片文件')
  if (file.size > 5 * 1024 * 1024) return ElMessage.error('图片大小不能超过 5MB')

  selectedImage.value = file
  previewUrl.value = URL.createObjectURL(file)
  e.target.value = ''
}

const removeImage = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  selectedImage.value = null
  previewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

//  2. 语音逻辑
const handleVoiceClick = () => isRecording.value ? stopVoiceInput() : startVoiceInput()

const startVoiceInput = async () => {
  if (!SILICONFLOW_API_KEY) return ElMessage.error('未配置 API Key')
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = e => audioChunks.push(e.data)
    mediaRecorder.onstop = () => {
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      stream.getTracks().forEach(t => t.stop())
      transcribe(blob)
    }

    mediaRecorder.start()
    isRecording.value = true
    recordingTime.value = 0
    timerInterval = setInterval(() => {
      recordingTime.value++
      if(recordingTime.value >= MAX_RECORD_SECONDS) stopVoiceInput()
    }, 1000)
    ElMessage.success('开始录音...')
  } catch(e) {
    ElMessage.error('无法访问麦克风')
  }
}

const stopVoiceInput = () => {
  if(mediaRecorder) mediaRecorder.stop()
  isRecording.value = false
  clearInterval(timerInterval)
}

const transcribe = async (blob) => {
  transcribing.value = true
  try {
    const formData = new FormData()
    formData.append('file', new File([blob], 'voice.webm', { type: 'audio/webm' }))
    formData.append('model', 'FunAudioLLM/SenseVoiceSmall')

    const res = await fetch(SILICONFLOW_API_URL, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${SILICONFLOW_API_KEY}` },
      body: formData
    })
    const data = await res.json()
    if(data.text) {
      currentQuery.value += data.text
      ElMessage.success('识别成功')
    }
  } catch(e) {
    ElMessage.error('识别失败')
  } finally {
    transcribing.value = false
  }
}

// 3. 发送逻辑
const sendMessage = async () => {
  const text = currentQuery.value.trim()
  const imageFileToSend = selectedImage.value
  const imagePreviewUrl = previewUrl.value

  if (!text && !imageFileToSend) return
  const userMsg = { role: 'user', content: text }
  if (imagePreviewUrl) {
    userMsg.image = imagePreviewUrl
  }
  messages.value.push(userMsg)

  const contextPrompt = messages.value.slice(-6, -1).map(m =>
      `${m.role === 'user' ? '医生' : 'AI'}: ${m.content || '[图片信息]'}`
  ).join('\n')

  currentQuery.value = ''
  selectedImage.value = null
  previewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''

  loading.value = true
  nextTick(scrollToBottom)

  try {
    let res;
    if (imageFileToSend) {
      const formData = new FormData()
      formData.append('message', text || '请分析这张图片')
      formData.append('context', contextPrompt)
      formData.append('imageFile', imageFileToSend) // 使用暂存的文件对象

      res = await axios.post(API_URL, formData)
    } else {
      res = await axios.post(API_URL, {
        message: text,
        context: contextPrompt
      }, {
        headers: { 'Content-Type': 'application/json' }
      })
    }

    if (res.data.code === 200) {
      messages.value.push({
        role: 'ai',
        content: marked.parse(res.data.data)
      })
      sessionStorage.setItem('aiChatHistory', JSON.stringify(messages.value));
    } else {
      ElMessage.error(res.data.msg || 'AI 响应异常')
    }

  } catch (err) {
    console.error(err)
    ElMessage.error('网络请求失败')
  } finally {
    loading.value = false
    nextTick(scrollToBottom)
  }
}

const scrollToBottom = () => {
  if (messagesArea.value) {
    messagesArea.value.scrollTop = messagesArea.value.scrollHeight
  }
}

const loadHistory = () => {
  const savedHistory = sessionStorage.getItem('aiChatHistory');
  if (savedHistory) {
    try {
      messages.value = JSON.parse(savedHistory);
    } catch (e) {
      console.error('加载聊天历史失败:', e);
      messages.value = [];
    }
  } else {
    messages.value = [];
  }
};

onBeforeUnmount(() => {
  sessionStorage.setItem('aiChatHistory', JSON.stringify(messages.value));
  stopVoiceInput();
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  messages.value.forEach(msg => {
    if (msg.image && msg.image.startsWith('blob:')) {
      URL.revokeObjectURL(msg.image)
    }
  })
});

onMounted(() => {
  loadHistory();
  scrollToBottom();
});

onUpdated(scrollToBottom)
onBeforeUnmount(() => {
  stopVoiceInput()
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  messages.value.forEach(msg => {
    if (msg.image && msg.image.startsWith('blob:')) {
      URL.revokeObjectURL(msg.image)
    }
  })
})
</script>

<style scoped>
.ai-doc-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
.intro { text-align: center; margin-bottom: 20px; }
.chat-container { height: calc(100vh - 220px); display: flex; flex-direction: column; background: #fff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.messages-area { flex: 1; padding: 20px; overflow-y: auto; background: #f5f7fa; }
.welcome-tip { text-align: center; color: #909399; margin-top: 40px; }


.message-row { display: flex; margin-bottom: 20px; gap: 12px; }
.message-avatar { width: 40px; height: 40px; background: #e0e0e0; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; cursor: default; }
.message-content { max-width: 75%; padding: 12px 16px; border-radius: 8px; font-size: 15px; line-height: 1.6; background: #fff; border: 1px solid #ebeef5; overflow-wrap: break-word; }
.message-row.user { flex-direction: row-reverse; }
.message-row.user .message-content { background: #409EFF; color: #fff; border: none; }
.message-row.ai .message-avatar { background: #e1f3d8; }

.msg-image { margin-bottom: 8px; }

.input-area { padding: 15px; background: #fff; border-top: 1px solid #ebeef5; gap: 10px; display: flex; flex-direction: column; position: relative; }

.image-preview-wrapper {
  display: inline-block;
  position: relative;
  width: 80px;
  height: 80px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}
.preview-img { width: 100%; height: 100%; }
.close-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  color: #F56C6C;
  cursor: pointer;
  background: rgba(255,255,255,0.8);
  border-radius: 50%;
  font-size: 16px;
  line-height: 1;
}

.input-controls { display: flex; justify-content: space-between; margin-top: 10px; }
.left-controls { display: flex; gap: 10px; }

.recording-animate {
  animation: breathing 1.5s infinite;
  background-color: #F56C6C !important;
  border-color: #F56C6C !important;
}
@keyframes breathing {
  0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(245, 108, 108, 0); }
  100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}
</style>