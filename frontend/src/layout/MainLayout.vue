<template>
  <el-container class="app-layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="aside">
      <div class="logo">
        <el-icon><Monitor /></el-icon> 智能医疗系统
      </div>

      <el-menu
          router
          :default-active="$route.path"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF">

        <!-- 患者菜单 -->
        <template v-if="userRole === 'PATIENT'">
          <el-menu-item index="/patient/index">
            <el-icon><Service /></el-icon>
            <span>首页浏览</span>
          </el-menu-item>
          <el-menu-item index="/patient/booking">
            <el-icon><Calendar /></el-icon>
            <span>预约挂号</span>
          </el-menu-item>
          <el-menu-item index="/patient/triage">
            <el-icon><List /></el-icon>
            <span>AI智能导诊</span>
          </el-menu-item>
          <el-menu-item index="/patient/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>在线问诊</span>
          </el-menu-item>
          <el-menu-item index="/patient/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/patient/notices">
            <el-icon><Bell /></el-icon>
            <span>系统公告</span>
          </el-menu-item>

        </template>


        <!--  医生菜单 -->
        <template v-if="userRole === 'DOCTOR'">
          <el-menu-item index="/doctor/patients">
            <el-icon><UserFilled /></el-icon>
            <span>患者管理</span>
          </el-menu-item>
          <el-menu-item index="/doctor/ai-help">
            <el-icon><Cpu /></el-icon>
            <span>AI辅助诊断</span>
          </el-menu-item>
          <el-menu-item index="/doctor/consult">
            <el-icon><ChatDotRound /></el-icon>
            <span>在线咨询</span>
          </el-menu-item>
        </template>

        <!-- 管理员菜单 -->
        <template v-if="userRole === 'ADMIN'">
          <el-menu-item index="/admin/stats">
            <el-icon><TrendCharts /></el-icon>
            <span>数据分析</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/depts">
            <el-icon><OfficeBuilding /></el-icon>
            <span>科室管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/doctors">
            <el-icon><Avatar /></el-icon>
            <span>医生管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/triage-db">
            <el-icon><Reading /></el-icon>
            <span>导诊规则管理</span>
          </el-menu-item>
        </template>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
          当前位置: {{ $route.meta.title || '首页' }}
        </div>
        <div class="user-info">
          <span>欢迎您，{{ realName }}</span>
          <el-button type="danger" plain size="small" @click="handleLogout" style="margin-left: 15px">
            退出
          </el-button>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {ElContainer, ElAside, ElMenu, ElMenuItem, ElHeader, ElMain, ElIcon, ElButton} from 'element-plus'
import {Monitor, Service, Calendar, List, Bell, User, UserFilled, Cpu, ChatDotRound, TrendCharts, OfficeBuilding, Avatar, Reading} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const userStr = sessionStorage.getItem('user')
const user = userStr ? JSON.parse(userStr) : {}

const userRole = ref(user.role || '')
const realName = ref(user.username || '用户')

// 2. 角色中文显示
computed(() => {
  switch (userRole.value) {
    case 'ADMIN': return '管理员'
    case 'DOCTOR': return '医生'
    case 'PATIENT': return '患者'
  }
});

// 3. 退出登录
const handleLogout = () => {
  if(confirm("确定要退出登录吗？")) {
    // 清除本地存储的用户信息
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
    // 跳转到登录页
    router.push('/login')
  }
}
</script>

<style scoped>
.app-layout { height: 100vh; display: flex; }
.aside { background-color: #304156; color: white; display: flex; flex-direction: column; }

.logo {
  height: 50px;
  line-height: 50px;
  text-align: center;
  font-weight: bold;
  font-size: 18px;
  background-color: #2b3649;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.header {
  background: white;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

</style>