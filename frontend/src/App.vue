<template>
  <div id="app">
    <div class="app-container">
      <!-- 顶部导航栏 -->
      <header class="app-header" :class="{ scrolled: isScrolled }">
        <div class="header-inner">
          <div class="logo" @click="goHome">
            <span class="logo-icon">🎭</span>
            <span class="logo-text">梗百科</span>
            <span class="logo-badge">BETA</span>
          </div>
          <nav class="nav">
            <a @click="goHome" class="nav-link" :class="{ active: $route.path === '/' }">
              <span class="nav-icon">🏠</span>首页
            </a>
            <a @click="goUpload" class="nav-link" :class="{ active: $route.path === '/upload' }">
              <span class="nav-icon">✏️</span>发布梗
            </a>
            <a v-if="isAuditor" @click="goAudit" class="nav-link" :class="{ active: $route.path === '/audit' }">
              <span class="nav-icon">🔍</span>审核
            </a>
            <a v-if="isAdmin" @click="goAdmin" class="nav-link" :class="{ active: $route.path === '/admin' }">
              <span class="nav-icon">⚙️</span>管理后台
            </a>
          </nav>
          <div class="user-section">
            <template v-if="userStore.user">
              <div class="user-info">
                <div class="user-avatar-mini">{{ (userStore.user.nickname || userStore.user.username).charAt(0).toUpperCase() }}</div>
                <span class="user-name">{{ userStore.user.nickname }}</span>
                <span class="role-badge" :class="'role-' + userStore.user.role">
                  {{ getRoleLabel(userStore.user.role) }}
                </span>
              </div>
              <button class="btn-logout" @click="logout">
                <span>退出</span>
              </button>
            </template>
            <template v-else>
              <button class="btn-login" @click="showLogin = true">登录</button>
              <button class="btn-register" @click="showRegister = true">注册</button>
            </template>
          </div>
        </div>
      </header>

      <main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <footer class="app-footer">
        <p>🎭 梗百科 &nbsp;|&nbsp; 记录互联网文化遗产 &nbsp;|&nbsp; <span class="footer-link">关于我们</span></p>
      </footer>
    </div>

    <!-- 登录弹窗 -->
    <transition name="modal-fade">
      <div v-if="showLogin" class="modal-overlay" @click.self="showLogin = false">
        <div class="modal">
          <div class="modal-header">
            <h2>🔑 欢迎回来</h2>
            <button class="modal-close" @click="showLogin = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>用户名</label>
              <div class="input-wrapper">
                <span class="input-icon">👤</span>
                <input v-model="loginForm.username" type="text" placeholder="请输入用户名" @keyup.enter="handleLogin" />
              </div>
            </div>
            <div class="form-group">
              <label>密码</label>
              <div class="input-wrapper">
                <span class="input-icon">🔒</span>
                <input v-model="loginForm.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
              </div>
            </div>
            <div class="form-hint">
              <span>测试账号：admin / 123456</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-secondary" @click="showLogin = false">取消</button>
            <button class="btn-primary" @click="handleLogin" :disabled="loginLoading">
              <span v-if="loginLoading" class="btn-spinner"></span>
              {{ loginLoading ? "登录中..." : "登录" }}
            </button>
          </div>
          <div class="modal-switch">
            还没有账号？
            <span class="switch-link" @click="switchToRegister">立即注册</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 注册弹窗 -->
    <transition name="modal-fade">
      <div v-if="showRegister" class="modal-overlay" @click.self="showRegister = false">
        <div class="modal">
          <div class="modal-header">
            <h2>🌟 创建账号</h2>
            <button class="modal-close" @click="showRegister = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>用户名</label>
              <div class="input-wrapper">
                <span class="input-icon">👤</span>
                <input v-model="registerForm.username" type="text" placeholder="设置用户名" />
              </div>
            </div>
            <div class="form-group">
              <label>密码</label>
              <div class="input-wrapper">
                <span class="input-icon">🔒</span>
                <input v-model="registerForm.password" type="password" placeholder="设置密码（至少6位）" />
              </div>
            </div>
            <div class="form-group">
              <label>邮箱 <span class="optional">（可选）</span></label>
              <div class="input-wrapper">
                <span class="input-icon">📧</span>
                <input v-model="registerForm.email" type="email" placeholder="输入邮箱地址" />
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-secondary" @click="showRegister = false">取消</button>
            <button class="btn-primary" @click="handleRegister" :disabled="registerLoading">
              <span v-if="registerLoading" class="btn-spinner"></span>
              {{ registerLoading ? "注册中..." : "注册" }}
            </button>
          </div>
          <div class="modal-switch">
            已有账号？
            <span class="switch-link" @click="switchToLogin">去登录</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user";
import { userApi } from "@/api";

const router = useRouter();
const userStore = useUserStore();

const showLogin = ref(false);
const showRegister = ref(false);
const loginLoading = ref(false);
const registerLoading = ref(false);
const isScrolled = ref(false);

const loginForm = ref({ username: "", password: "" });
const registerForm = ref({ username: "", password: "", email: "" });

const isAdmin = computed(() => userStore.user?.role === 3);
const isAuditor = computed(() => (userStore.user?.role ?? 0) >= 2);

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20;
};

onMounted(() => {
  window.addEventListener("scroll", handleScroll);
  userStore.loadUserFromStorage();
});
onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});

const goHome = () => router.push("/");
const goUpload = () => router.push("/upload");
const goAudit = () => router.push("/audit");
const goAdmin = () => router.push("/admin");

const getRoleLabel = (role: number) => {
  const map: Record<number, string> = { 1: "用户", 2: "审核", 3: "管理员" };
  return map[role] || "";
};

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning("请输入用户名和密码");
    return;
  }
  loginLoading.value = true;
  try {
    const res = await userApi.login(loginForm.value);
    userStore.setUser(res.user);
    localStorage.setItem("token", res.token);
    showLogin.value = false;
    loginForm.value = { username: "", password: "" };
    ElMessage.success(`欢迎回来，${res.user.nickname || res.user.username}！`);
  } catch (e) {
    console.error(e);
  } finally {
    loginLoading.value = false;
  }
};

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning("请填写完整信息");
    return;
  }
  if (registerForm.value.password.length < 6) {
    ElMessage.warning("密码至少6位");
    return;
  }
  registerLoading.value = true;
  try {
    await userApi.register(registerForm.value);
    showRegister.value = false;
    const registerUsername = registerForm.value.username;
    const registerPassword = registerForm.value.password;
    registerForm.value = { username: "", password: "", email: "" };
    
    ElMessage.success("注册成功，正在自动登录...");
    
    try {
      const loginRes = await userApi.login({ username: registerUsername, password: registerPassword });
      userStore.setUser(loginRes.user);
      localStorage.setItem("token", loginRes.token);
      ElMessage.success(`欢迎，${loginRes.user.nickname || loginRes.user.username}！`);
    } catch (loginError) {
      console.error(loginError);
      showLogin.value = true;
      ElMessage.warning("自动登录失败，请手动登录");
    }
  } catch (e) {
    console.error(e);
  } finally {
    registerLoading.value = false;
  }
};

const logout = () => {
  userStore.setUser(null);
  localStorage.removeItem("token");
  router.push("/");
  ElMessage.info("已退出登录");
};

const switchToRegister = () => {
  showLogin.value = false;
  setTimeout(() => (showRegister.value = true), 300);
};
const switchToLogin = () => {
  showRegister.value = false;
  setTimeout(() => (showLogin.value = true), 300);
};
</script>

<style scoped>
#app {
  min-height: 100vh;
  background: #f0f2f5;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* ======= 导航栏 ======= */
.app-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: box-shadow 0.3s ease, background 0.3s ease;
}
.app-header.scrolled {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  cursor: pointer;
  text-decoration: none;
  transition: opacity 0.2s;
  flex-shrink: 0;
}
.logo:hover { opacity: 0.8; }
.logo-icon { font-size: 26px; }
.logo-badge {
  font-size: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
  letter-spacing: 0.5px;
  margin-top: -2px;
}

.nav {
  display: flex;
  gap: 4px;
  flex: 1;
  justify-content: center;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.nav-link:hover {
  background: #f0f4ff;
  color: #4361ee;
}
.nav-link.active {
  background: #f0f4ff;
  color: #4361ee;
  font-weight: 600;
}
.nav-icon { font-size: 15px; }

.user-section {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-avatar-mini {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}
.user-name {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}
.role-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}
.role-1 { background: #f0f0f0; color: #666; }
.role-2 { background: #e6f4ff; color: #1890ff; }
.role-3 { background: #fff2e8; color: #fa541c; }

.btn-login, .btn-register, .btn-logout {
  padding: 8px 18px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-login {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}
.btn-login:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
}

.btn-register {
  background: transparent;
  color: #667eea;
  border: 1.5px solid #667eea;
}
.btn-register:hover {
  background: #f0f4ff;
}

.btn-logout {
  background: #f5f5f5;
  color: #666;
}
.btn-logout:hover { background: #ebebeb; }

/* ======= 主内容区 ======= */
.app-main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 24px;
  box-sizing: border-box;
}

/* ======= Footer ======= */
.app-footer {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;
  border-top: 1px solid #e8e8e8;
  background: white;
}
.footer-link {
  color: #667eea;
  cursor: pointer;
}
.footer-link:hover { text-decoration: underline; }

/* ======= 页面切换动画 ======= */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.25s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ======= Modal ======= */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-from .modal {
  transform: scale(0.92) translateY(-20px);
}
.modal-fade-leave-to .modal {
  transform: scale(0.92) translateY(-20px);
}

.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 0;
}
.modal-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
}

.modal-close {
  width: 32px; height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.modal-close:hover { background: #ebebeb; color: #333; }

.modal-body { padding: 20px 24px; }

.form-group { margin-bottom: 18px; }
.form-group:last-child { margin-bottom: 0; }
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.input-wrapper {
  display: flex;
  align-items: center;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.input-wrapper:focus-within { border-color: #667eea; }

.input-icon {
  padding: 0 12px;
  font-size: 16px;
  background: #fafafa;
  height: 44px;
  display: flex;
  align-items: center;
  border-right: 1px solid #e8e8e8;
}

.input-wrapper input {
  flex: 1;
  height: 44px;
  padding: 0 14px;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
}

.optional { color: #999; font-weight: 400; font-size: 12px; }
.form-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  background: #f9f9f9;
  padding: 8px 12px;
  border-radius: 6px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 20px;
}

.btn-primary, .btn-secondary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}
.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);
}
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-secondary { background: #f5f5f5; color: #666; }
.btn-secondary:hover { background: #ebebeb; }

.btn-spinner {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  display: inline-block;
}

@keyframes spin { to { transform: rotate(360deg); } }

.modal-switch {
  text-align: center;
  padding: 0 24px 20px;
  font-size: 13px;
  color: #999;
}
.switch-link {
  color: #667eea;
  cursor: pointer;
  font-weight: 600;
}
.switch-link:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .header-inner {
    padding: 0 16px;
    height: 56px;
  }
  .logo-text { display: none; }
  .logo-badge { display: none; }
  .nav { gap: 2px; }
  .nav-link { padding: 7px 10px; font-size: 13px; }
  .nav-icon { display: none; }
  .user-name { display: none; }
  .role-badge { display: none; }
  .app-main { padding: 16px; }
}

@media (max-width: 480px) {
  .nav { display: none; }
  .btn-login, .btn-register, .btn-logout {
    padding: 7px 12px;
    font-size: 13px;
  }
}
</style>
