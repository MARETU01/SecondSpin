import { createRouter, createWebHashHistory } from 'vue-router'

import HomePage from '../views/HomePage.vue'
import LoginView from "../views/LoginView.vue"
import RegisterView from "@/views/RegisterView.vue"
import ResetPasswordView from "@/views/ResetPasswordView.vue"
import ProfilePage from "@/views/ProfilePage.vue"

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPasswordView
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfilePage
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
