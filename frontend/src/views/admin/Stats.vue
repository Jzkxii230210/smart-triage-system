<template>
  <div class="stats-container" style="padding: 20px;">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">📊 科室就诊占比</div>
          </template>
          <div ref="pieChartRef" style="height: 320px;" v-loading="loading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">📈 近7日挂号趋势</div>
          </template>
          <div ref="lineChartRef" style="height: 320px;" v-loading="loading"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="chart-header">🏆 热门医生排名 (Top 5)</div>
      </template>
      <div ref="barChartRef" style="height: 350px;" v-loading="loading"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const pieChartRef = ref(null)
const lineChartRef = ref(null)
const barChartRef = ref(null)

let pieChart = null
let lineChart = null
let barChart = null

const fetchDataAndRender = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/stats/dashboard')
    if (res.data.code === 200) {
      const data = res.data.data
      renderPie(data.deptStats || [])
      renderLine(data.trendStats || [])
      renderBar(data.doctorStats || [])
    }
  } catch (e) {
    ElMessage.error('无法加载统计数据')
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 1. 渲染饼图
const renderPie = (data) => {
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)

  const chartData = data.length > 0 ? data : [{name: '暂无数据', value: 0}]

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center' },
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
    series: [
      {
        name: '就诊人数',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: true, formatter: '{b}: {c}人' },
        data: chartData
      }
    ]
  })
}

// 2. 渲染折线图
const renderLine = (data) => {
  if (lineChart) lineChart.dispose()
  lineChart = echarts.init(lineChartRef.value)

  const dates = data.map(item => item.name) // 日期
  const counts = data.map(item => item.value) // 数量

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: { type: 'value' },
    series: [{
      name: '挂号量',
      data: counts,
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.5)' },
          { offset: 1, color: 'rgba(64,158,255,0.1)' }
        ])
      },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

// 3. 渲染柱状图
const renderBar = (data) => {
  if (barChart) barChart.dispose()
  barChart = echarts.init(barChartRef.value)

  const names = data.map(item => item.name)
  const counts = data.map(item => item.value)

  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names, inverse: true }, // inverse让第一名在上面
    series: [{
      name: '接诊量',
      type: 'bar',
      data: counts,
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right' }
    }]
  })
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  pieChart && pieChart.resize()
  lineChart && lineChart.resize()
  barChart && barChart.resize()
}

onMounted(() => {
  fetchDataAndRender()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.chart-header {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}
</style>