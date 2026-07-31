<template>
  <div class="triage-wrapper">
    <!-- 头部引导区域 -->
    <div class="triage-header">
      <h1>🤖 AI 智能导诊助手</h1>
      <p>不知道挂哪个科？描述您的症状，AI 大模型为您精准分析。</p>
    </div>
    <el-card class="triage-card" shadow="always">
      <el-steps :active="activeStep" finish-status="success" align-center class="mb-4">
        <el-step title="症状描述" description="越详细越准确" />
        <el-step title="AI 分析中" description="基于 Spring AI" />
        <el-step title="分析结果" description="就医与挂号建议" />
      </el-steps>

      <div v-if="activeStep === 0" class="step-box fade-in">
        <el-form :model="form" class="triage-form" label-position="top">
          <el-form-item label="请详细描述您的身体不适（必填）：">
            <div class="textarea-with-voice">
              <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="5"
                  placeholder="例如：从昨天晚上开始左下腹剧烈疼痛，伴有恶心想吐，没有发烧..."
                  maxlength="300"
                  show-word-limit
              />
              <el-button
                  :type="isRecording ? 'danger' : 'primary'"
                  :class="{ 'recording-animate': isRecording }"
                  @click="handleVoiceClick"
                  class="voice-btn"
                  :icon="isRecording ? Mute : Microphone"
                  circle
              >
                <span v-if="isRecording">{{ recordingTime }}</span>
              </el-button>
            </div>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="持续时间">
                <el-select v-model="form.duration" style="width: 100%">
                  <el-option label="刚刚开始" value="now" />
                  <el-option label="几小时" value="hours" />
                  <el-option label="1-3天" value="days" />
                  <el-option label="一周以上" value="week" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否有过敏史">
                <el-radio-group v-model="form.allergy">
                  <el-radio label="无">无</el-radio>
                  <el-radio label="有">有</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-footer">
            <el-button
                type="primary"
                size="large"
                round
                :icon="Search"
                @click="startAnalysis"
                :disabled="!form.description"
            >
              开始智能分析
            </el-button>
          </div>
        </el-form>
      </div>

      <div v-if="activeStep === 1" class="step-box analyzing">
        <div class="pulse-container">
          <div class="pulse-ring"></div>
          <el-icon class="ai-icon" :size="50" color="#409EFF"><Cpu /></el-icon>
        </div>
        <h3 class="mt-20">正在分析您的症状...</h3>
        <p class="changing-text">{{ loadingText }}</p>
      </div>

      <div v-if="activeStep === 2" class="step-box result-container fade-in">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover" class="result-card recommend">
              <template #header>
                <div class="card-head">
                  <el-icon><OfficeBuilding /></el-icon> <span>推荐科室</span>
                </div>
              </template>
              <h2 class="result-dept">{{ analysisResult.deptName }}</h2>
              <p class="desc">{{ analysisResult.deptReason }}</p>
              <el-button type="primary" size="large" @click="goToBooking(analysisResult.deptName)">立即挂号</el-button>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="hover" class="result-card advice">
              <template #header>
                <div class="card-head">
                  <el-icon><FirstAidKit /></el-icon> <span>就医建议</span>
                </div>
              </template>
              <ul class="advice-list">
                <li v-for="(item, index) in analysisResult.advices" :key="index">{{ item }}</li>
              </ul>
            </el-card>
          </el-col>
        </el-row>

        <div class="doctor-rec mt-20" v-if="analysisResult.doctors && analysisResult.doctors.length > 0">
          <h4>为您匹配的专家（按对症匹配度排序）：</h4>
          <el-row :gutter="20">
            <el-col :span="8" v-for="doc in analysisResult.doctors" :key="doc.id">
              <el-card :body-style="{ padding: '15px' }" shadow="hover" class="doc-card-hover">
                <div class="doc-mini">
                  <el-avatar :size="40" :src="doc.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                  <div class="doc-info">
                    <div class="font-bold">{{ doc.name }}</div>
                    <div class="text-xs text-gray">{{ doc.title }}</div>
                    <div class="text-xs text-blue text-truncate">{{ doc.expertise }}</div>
                  </div>
                  <el-button link type="primary" @click="goToBooking(analysisResult.deptName)">预约</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div class="retry-btn">
          <el-divider border-style="dashed" />
          <el-button round icon="Refresh" @click="resetTriage">症状有变化？重新分析</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Cpu, Search, OfficeBuilding, FirstAidKit, Microphone, Mute } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeStep = ref(0)
const loadingText = ref('正在连接医疗知识库...')
const isRecording = ref(false)
const recordingTime = ref(0)
let mediaRecorder = null
let audioChunks = []
let timerInterval = null
const MAX_RECORD_SECONDS = 30 // 最大录音时长

// 用户输入表单数据
const form = reactive({
  description: '',
  duration: 'hours',
  allergy: '无'
})

// AI 分析结果数据
const analysisResult = reactive({
  deptName: '',
  deptReason: '',
  advices: [],
  doctors: []
})

// 1：获取与用户绑定的存储

const getStorageKey = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      if (user && user.id) {
        return `SMART_TRIAGE_STATE_${user.id}`
      }
    }
  } catch (e) {
    console.warn('获取用户信息失败', e)
  }
  return null
}

// 2：只恢复当前登录用户的状态
onMounted(() => {
  const key = getStorageKey()
  if (!key) return

  const savedState = localStorage.getItem(key)
  if (savedState) {
    try {
      const parsed = JSON.parse(savedState)
      if (parsed.activeStep === 2 && parsed.analysisResult.deptName) {
        Object.assign(form, parsed.form)
        Object.assign(analysisResult, parsed.analysisResult)
        activeStep.value = 2
      }
    } catch (e) {
      console.error('缓存解析失败', e)
      localStorage.removeItem(key)
    }
  }
})

// 开始 AI 分析
const startAnalysis = async () => {
  activeStep.value = 1
  const texts = ['正在提取关键症状...', '正在匹配可能的疾病...', '正在生成就医方案...', '分析完成']
  let i = 0
  const timer = setInterval(() => {
    loadingText.value = texts[i] || '正在整理数据...'
    i++
  }, 800)

  try {
    const response = await axios.post('/api/patient/triage/analyze', {
      description: form.description,
      duration: form.duration,
      allergy: form.allergy
    })

    clearInterval(timer)

    if (response.data.code === 200) {
      const data = response.data.data
      analysisResult.deptName = data.deptName
      analysisResult.deptReason = data.deptReason
      analysisResult.advices = data.advices || []
      analysisResult.doctors = data.doctors || []

      activeStep.value = 2
      saveStateToLocal()

    } else {
      ElMessage.error(response.data.msg || '分析失败，请重试')
      activeStep.value = 0
    }
  } catch (error) {
    clearInterval(timer)
    ElMessage.error('网络异常，无法连接到服务器')
    activeStep.value = 0
  }
}

// 3：保存时检查权限与 Key
const saveStateToLocal = () => {
  const key = getStorageKey()
  // 如果未登录，不保存状态
  if (!key) return

  const state = {
    activeStep: 2,
    form: { ...form },
    analysisResult: { ...analysisResult }
  }
  localStorage.setItem(key, JSON.stringify(state))
}

const goToBooking = (dept) => {
  router.push({ path: '/patient/booking', query: { dept: dept } })
}

// 4：重置时只清除当前用户的 Key
const resetTriage = () => {
  activeStep.value = 0
  const key = getStorageKey()
  if (key) {
    localStorage.removeItem(key) // 只删当前用户的缓存
  }

  form.description = ''
  analysisResult.deptName = ''
  analysisResult.advices = []
  analysisResult.doctors = []
}

// 语音输入点击处理
const handleVoiceClick = () => {
  isRecording.value ? stopVoiceInput() : startVoiceInput()
}

// 开始录音
const startVoiceInput = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = e => audioChunks.push(e.data)
    mediaRecorder.onstop = () => {
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      stream.getTracks().forEach(t => t.stop())
      transcribeVoice(blob)
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

// 停止录音
const stopVoiceInput = () => {
  if(mediaRecorder) mediaRecorder.stop()
  isRecording.value = false
  clearInterval(timerInterval)
}

// 语音转文字
const transcribeVoice = async (blob) => {
  try {
    const formData = new FormData()
    formData.append('file', new File([blob], 'voice.webm', { type: 'audio/webm' }))
    formData.append('model', 'FunAudioLLM/SenseVoiceSmall')

    const res = await fetch('https://api.siliconflow.cn/v1/audio/transcriptions', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${import.meta.env.VITE_SILICONFLOW_API_KEY}` },
      body: formData
    })

    const data = await res.json()
    if(data.text) {
      form.description += data.text
      ElMessage.success('语音识别成功')
    }
  } catch(e) {
    ElMessage.error('语音识别失败')
  }
}

</script>


<style scoped>
.triage-wrapper { max-width: 900px; margin: 0 auto; padding: 20px; font-family: 'Helvetica Neue', Arial, sans-serif; }
.triage-header { text-align: center; margin-bottom: 30px; }
.triage-header h1 { color: #303133; margin-bottom: 10px; }
.triage-header p { color: #606266; font-size: 16px; }

.triage-card { min-height: 550px; border-radius: 8px; }
.step-box { padding: 20px 0; }
.form-footer { text-align: center; margin-top: 40px; }
.analyzing { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 300px; }
.pulse-container { position: relative; width: 80px; height: 80px; display: flex; align-items: center; justify-content: center; margin-bottom: 20px;}
.pulse-ring { position: absolute; width: 100%; height: 100%; border-radius: 50%; border: 4px solid #409EFF; opacity: 0; animation: pulse 2s infinite; }
@keyframes pulse { 0% { transform: scale(0.8); opacity: 1; } 100% { transform: scale(1.5); opacity: 0; } }
.changing-text { color: #909399; margin-top: 10px; font-size: 14px; min-height: 20px; }
.mt-20 { margin-top: 20px; }
.result-card { height: 100%; display: flex; flex-direction: column; }
.card-head { display: flex; align-items: center; gap: 8px; font-weight: bold; font-size: 16px; }
.result-dept {
  font-size: 24px;
  color: #409EFF;
  margin: 15px 0;
  font-weight: bold;
}

.desc {
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.6;
  font-size: 16px; /* 与建议列表保持一致 */
  flex-grow: 1;
}
.advice-list {
  padding-left: 20px;
  color: #606266;
  font-size: 16px; /* 与推荐科室描述保持一致 */
  line-height: 1.6;
}
.advice-list li { margin-bottom: 8px; }
.doc-mini { display: flex; align-items: center; gap: 12px; }
.doc-info { flex: 1; overflow: hidden; }
.text-xs { font-size: 12px; }
.text-gray { color: #909399; }
.text-blue { color: #409EFF; margin-top: 2px; }
.text-truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.font-bold { font-weight: bold; color: #303133; }
.doc-card-hover { transition: transform 0.2s; cursor: pointer; }
.doc-card-hover:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.retry-btn { text-align: center; margin-top: 40px; }
.fade-in { animation: fadeIn 0.5s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.textarea-with-voice {
  position: relative;
  display: inline-block;
  width: 100%;
}

.textarea-with-voice .voice-btn {
  position: absolute;
  bottom: 25px;
  right: 10px;
  z-index: 10;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: auto;
  padding: 0;
  min-width: auto;
}

.textarea-with-voice .voice-btn span {
  font-size: 12px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}


/* 为文本域添加右侧内边距以避免文字被按钮遮挡 */

.voice-btn {
  position: absolute;
  bottom: 8px;
  right: 8px;
  padding: 5px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

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

/* 调整textarea的内边距以容纳按钮 */
</style>
