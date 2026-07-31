import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import MainLayout from '../layout/MainLayout.vue'
import '../styles/main.css'

// 患者相关组件
import PatientIndex from '../views/patient/index.vue'
import PatientBooking from '../views/patient/Booking.vue'
import PatientAiTriage from '../views/patient/AiTriage.vue'
import PatientProfile from '../views/patient/Profile.vue'
import PatientNoticeList from '../views/patient/NoticeList.vue'
import PatientChat from '../views/patient/PatientConsultation.vue'


// 医生相关组件
import DoctorPatientList from '../views/doctor/PatientList.vue'
import DoctorAiAssistant from '../views/doctor/AiAssistant.vue'
import DoctorOnlineConsult from '../views/doctor/OnlineConsult.vue'

// 管理员相关组件
import AdminStats from '../views/admin/Stats.vue'
import AdminUserManage from '../views/admin/UserManage.vue'
import AdminDeptManage from '../views/admin/DeptManage.vue'
import AdminDoctorManage from '../views/admin/DoctorManage.vue'
import AdminTriageDb from '../views/admin/TriageDb.vue'


// 2. 公共路由
const publicRoutes = [
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: Register,
        meta: { requiresAuth: false }
    },
    {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: ForgotPassword,
        meta: { requiresAuth: false }
    },
]

// 3. 认证路由
const protectedRoutes = [
    {
        path: '/',
        component: MainLayout,
        redirect: to => {
            const user = JSON.parse(sessionStorage.getItem('user'))
            if (!user) return { name: 'Login' }

            switch(user.role) {
                case 'ADMIN': return '/admin/stats'
                case 'DOCTOR': return '/doctor/workspace'
                case 'PATIENT': return '/patient/index'
                default: return '/login'
            }
        },
        meta: { requiresAuth: true },
        children: [
            //  患者路由组
            {
                path: '/patient',
                name: 'PatientRoot',
                redirect: '/patient/index',
                meta: { role: 'PATIENT' },
                children: [
                    {
                        path: 'index',
                        name: 'PatientIndex',
                        component: PatientIndex,
                        meta: { title: '首页浏览', role: 'PATIENT' }
                    },
                    {
                        path: 'booking',
                        name: 'PatientBooking',
                        component: PatientBooking,
                        meta: { title: '预约挂号', role: 'PATIENT' }
                    },
                    {
                        path: 'triage',
                        name: 'PatientTriage',
                        component: PatientAiTriage,
                        meta: { title: 'AI智能导诊', role: 'PATIENT' }
                    },
                    {
                        path: 'chat',
                        name: 'PatientOnlineChat',
                        component: PatientChat,
                        meta: { title: '在线咨询', role: 'PATIENT' }
                    },
                    {
                        path: 'profile',
                        name: 'PatientProfile',
                        component: PatientProfile,
                        meta: { title: '个人中心', role: 'PATIENT' }
                    },
                    {
                        path: 'notices',
                        name: 'PatientNotices',
                        component: PatientNoticeList,
                        meta: { title: '系统公告', role: 'PATIENT' }
                    }
                ]
            },

            //  医生路由组
            {
                path: '/doctor',
                name: 'DoctorRoot',
                redirect: '/doctor/patients',
                meta: { role: 'DOCTOR' },
                children: [
                    {
                        path: 'patients',
                        name: 'DoctorPatients',
                        component: DoctorPatientList,
                        meta: { title: '患者管理', role: 'DOCTOR' }
                    },
                    {
                        path: 'ai-help',
                        name: 'DoctorAi',
                        component: DoctorAiAssistant,
                        meta: { title: 'AI辅助诊断', role: 'DOCTOR' }
                    },
                    {
                        path: 'consult',
                        name: 'DoctorConsult',
                        component: DoctorOnlineConsult,
                        meta: { title: '在线咨询', role: 'DOCTOR' }
                    }
                ]
            },

            //  管理员路由组
            {
                path: '/admin',
                name: 'AdminRoot',
                redirect: '/admin/stats',
                meta: { role: 'ADMIN' },
                children: [
                    {
                        path: 'stats',
                        name: 'AdminStats',
                        component: AdminStats,
                        meta: { title: '数据分析', role: 'ADMIN' }
                    },
                    {
                        path: 'users',
                        name: 'AdminUsers',
                        component: AdminUserManage,
                        meta: { title: '用户管理', role: 'ADMIN' }
                    },
                    {
                        path: 'depts',
                        name: 'AdminDepts',
                        component: AdminDeptManage,
                        meta: { title: '科室管理', role: 'ADMIN' }
                    },
                    {
                        path: 'doctors',
                        name: 'AdminDoctors',
                        component: AdminDoctorManage,
                        meta: { title: '医生排班', role: 'ADMIN' }
                    },
                    {
                        path: 'triage-db',
                        name: 'AdminTriageDb',
                        component: AdminTriageDb,
                        meta: { title: '导诊规则管理', role: 'ADMIN' }
                    }
                ]
            }
        ]
    }
]

// 4. 创建路由实例
const router = createRouter({
    history: createWebHistory(),
    routes: [...publicRoutes, ...protectedRoutes]
})

// 5. 路由守卫
router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth !== false) {
        const user = JSON.parse(sessionStorage.getItem('user'))
        if (!user) {
            return next({ name: 'Login', query: { redirect: to.fullPath } })
        }

        if (to.meta.role && to.meta.role !== user.role) {
            return next({ name: 'Login' })
        }
    }
    document.title = to.meta.title || '智能医疗系统'
    next()
})

export default router
