<template>
  <div class="admin-page">
    <!-- 页头 -->
    <div class="page-header">
      <div class="header-title">
        <span class="title-icon">⚙️</span>
        <div>
          <h2>管理后台</h2>
          <p>梗百科平台数据管理中心</p>
        </div>
      </div>
    </div>

    <!-- Tab 导航 -->
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
        <span v-if="tab.badge" class="tab-badge">{{ tab.badge }}</span>
      </button>
    </div>

    <!-- 内容区 -->
    <div class="tab-content">
      <Transition name="tab-fade" mode="out-in">

        <!-- ====== 梗管理 ====== -->
        <div v-if="activeTab === 'meme'" key="meme" class="section-panel">
          <div class="section-header">
            <h3>梗内容管理</h3>
            <div class="filter-bar">
              <select v-model="statusFilter" class="filter-select" @change="fetchMemeList">
                <option :value="null">全部状态</option>
                <option :value="0">草稿</option>
                <option :value="1">待审核</option>
                <option :value="2">已通过</option>
                <option :value="3">已驳回</option>
              </select>
            </div>
          </div>

          <div v-if="memeLoading" class="table-skeleton">
            <div v-for="i in 5" :key="i" class="table-skeleton-row shimmer"></div>
          </div>

          <div v-else class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>标题</th>
                  <th>状态</th>
                  <th>浏览数</th>
                  <th>点赞数</th>
                  <th>操作</th>
                </tr>
              </thead>
              <TransitionGroup name="row-list" tag="tbody">
                <tr v-for="meme in memeList" :key="meme.id" class="table-row">
                  <td class="id-cell">{{ meme.id }}</td>
                  <td class="title-cell">
                    <span class="title-text" :title="meme.title">{{ meme.title }}</span>
                  </td>
                  <td>
                    <span class="status-badge" :class="'status-' + getStatusClass(meme.status)">
                      {{ getStatusText(meme.status) }}
                    </span>
                  </td>
                  <td class="num-cell">{{ formatNum(meme.viewCount || 0) }}</td>
                  <td class="num-cell">{{ formatNum(meme.likeCount || 0) }}</td>
                  <td>
                    <div class="action-row">
                      <button
                        v-if="meme.status === 1"
                        class="act-btn approve"
                        @click="handleApprove(meme.id)"
                      >通过</button>
                      <button
                        v-if="meme.status === 1"
                        class="act-btn reject"
                        @click="showRejectModal(meme.id)"
                      >驳回</button>
                      <button class="act-btn view" @click="viewDetail(meme.id)">查看</button>
                    </div>
                  </td>
                </tr>
              </TransitionGroup>
            </table>
            <div v-if="memeList.length === 0" class="table-empty">
              <span>📭</span>
              <p>暂无数据</p>
            </div>
          </div>
        </div>

        <!-- ====== 用户管理 ====== -->
        <div v-else-if="activeTab === 'user'" key="user" class="section-panel">
          <div class="section-header">
            <h3>用户管理</h3>
            <div class="filter-bar">
              <div class="search-wrap">
                <span class="search-icon-inner">🔍</span>
                <input
                  v-model="userSearch"
                  type="text"
                  class="search-input"
                  placeholder="搜索用户名或昵称..."
                  @input="handleUserSearch"
                />
              </div>
            </div>
          </div>

          <div v-if="userLoading" class="table-skeleton">
            <div v-for="i in 5" :key="i" class="table-skeleton-row shimmer"></div>
          </div>

          <div v-else class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <TransitionGroup name="row-list" tag="tbody">
                <tr v-for="user in filteredUserList" :key="user.id" class="table-row">
                  <td class="id-cell">{{ user.id }}</td>
                  <td>
                    <div class="user-info">
                      <div class="user-avatar">{{ (user.nickname || user.username || '?')[0].toUpperCase() }}</div>
                      <span>{{ user.username }}</span>
                    </div>
                  </td>
                  <td>{{ user.nickname || '-' }}</td>
                  <td>
                    <span class="role-badge" :class="'role-' + user.role">
                      {{ getRoleText(user.role) }}
                    </span>
                  </td>
                  <td>
                    <span class="status-badge" :class="user.status === 1 ? 'status-approved' : 'status-rejected'">
                      {{ user.status === 1 ? '正常' : '禁用' }}
                    </span>
                  </td>
                  <td>
                    <div class="action-row">
                      <button
                        v-if="user.role === 1"
                        class="act-btn promote"
                        @click="promoteUser(user.id)"
                      >提升审核员</button>
                      <button
                        class="act-btn"
                        :class="user.status === 1 ? 'toggle-disable' : 'toggle-enable'"
                        @click="toggleUserStatus(user)"
                      >{{ user.status === 1 ? '禁用' : '启用' }}</button>
                    </div>
                  </td>
                </tr>
              </TransitionGroup>
            </table>
            <div v-if="filteredUserList.length === 0 && !userLoading" class="table-empty">
              <span>👤</span>
              <p>{{ userSearch ? '未找到匹配用户' : '暂无用户数据' }}</p>
            </div>
          </div>
        </div>

        <!-- ====== 数据分析 ====== -->
        <div v-else-if="activeTab === 'stats'" key="stats" class="section-panel">
          <div class="section-header">
            <h3>数据概览</h3>
            <span class="update-time">数据实时更新</span>
          </div>

          <div class="stats-grid">
            <div
              v-for="(card, i) in statsCards"
              :key="card.key"
              class="stat-card"
              :style="{ animationDelay: i * 0.1 + 's', '--card-color': card.color }"
            >
              <div class="stat-card-bg"></div>
              <div class="stat-icon">{{ card.icon }}</div>
              <div class="stat-body">
                <div class="stat-value">
                  <AnimatedNumber :target="stats[card.key]" />
                </div>
                <div class="stat-label">{{ card.label }}</div>
              </div>
              <div class="stat-trend" v-if="card.trend">
                <span class="trend-arrow">↑</span>
                {{ card.trend }}
              </div>
            </div>
          </div>

          <!-- 状态分布 -->
          <div class="distribution-section">
            <h4>梗内容状态分布</h4>
            <div class="dist-bars">
              <div
                v-for="item in statusDistribution"
                :key="item.label"
                class="dist-item"
              >
                <div class="dist-label-row">
                  <span class="dist-dot" :style="{ background: item.color }"></span>
                  <span class="dist-label">{{ item.label }}</span>
                  <span class="dist-count">{{ item.count }}</span>
                </div>
                <div class="dist-bar-track">
                  <div
                    class="dist-bar-fill"
                    :style="{
                      width: totalMemeCount ? (item.count / totalMemeCount * 100) + '%' : '0%',
                      background: item.color
                    }"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 热门梗 TOP5 -->
          <div class="top-section">
            <h4>点赞榜 TOP 5</h4>
            <div class="top-list">
              <div
                v-for="(meme, i) in topMemes"
                :key="meme.id"
                class="top-item"
                @click="viewDetail(meme.id)"
              >
                <div class="top-rank" :class="'rank-' + (i+1)">{{ i + 1 }}</div>
                <div class="top-title">{{ meme.title }}</div>
                <div class="top-likes">👍 {{ formatNum(meme.likeCount || 0) }}</div>
              </div>
              <div v-if="topMemes.length === 0" class="top-empty">暂无数据</div>
            </div>
          </div>
        </div>

      </Transition>
    </div>

    <!-- 驳回弹窗 -->
    <Transition name="modal-fade">
      <div v-if="showReject" class="modal-overlay" @click.self="closeRejectModal">
        <div class="modal-box">
          <div class="modal-header">
            <h3>驳回原因</h3>
            <button class="modal-close-btn" @click="closeRejectModal">✕</button>
          </div>
          <div class="modal-body">
            <textarea
              v-model="rejectReason"
              class="reject-textarea"
              placeholder="请填写驳回原因（必填）"
              rows="4"
            ></textarea>
          </div>
          <div class="modal-footer">
            <button class="modal-cancel-btn" @click="closeRejectModal">取消</button>
            <button
              class="modal-confirm-btn"
              @click="handleReject"
              :disabled="!rejectReason.trim()"
            >确认驳回</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { memeApi, userApi } from "@/api";
import type { Meme, User } from "@/types";
import { ElMessage } from "element-plus";

// 动画数字组件
import { defineComponent, h, ref as vRef, watch as vWatch } from "vue";
const AnimatedNumber = defineComponent({
  props: { target: { type: Number, default: 0 } },
  setup(props) {
    const current = vRef(0);
    let timer: ReturnType<typeof setInterval> | null = null;

    vWatch(
      () => props.target,
      (end) => {
        if (timer) clearInterval(timer);
        const startVal = current.value;
        const diff = end - startVal;
        const steps = 30;
        const stepSize = diff / steps;
        let step = 0;
        timer = setInterval(() => {
          step++;
          if (step >= steps) {
            current.value = end;
            if (timer) clearInterval(timer);
          } else {
            current.value = Math.round(startVal + stepSize * step);
          }
        }, 25);
      },
      { immediate: true }
    );

    return () => h('span', current.value.toLocaleString());
  }
});

const router = useRouter();

const activeTab = ref("meme");
const memeLoading = ref(false);
const userLoading = ref(false);
const memeList = ref<Meme[]>([]);
const userList = ref<User[]>([]);
const statusFilter = ref<number | null>(null);
const userSearch = ref("");
const showReject = ref(false);
const rejectReason = ref("");
const currentMemeId = ref<number | null>(null);

const tabs = [
  { key: "meme", icon: "📋", label: "梗管理", badge: null as null | number },
  { key: "user", icon: "👥", label: "用户管理", badge: null as null | number },
  { key: "stats", icon: "📊", label: "数据分析", badge: null as null | number },
];

const stats = ref({
  totalMemes: 0,
  totalUsers: 0,
  totalViews: 0,
  totalLikes: 0,
});

const statsCards = [
  { key: "totalMemes" as keyof typeof stats.value, icon: "📝", label: "总梗数", color: "#667eea", trend: null },
  { key: "totalUsers" as keyof typeof stats.value, icon: "👥", label: "注册用户", color: "#f093fb", trend: null },
  { key: "totalViews" as keyof typeof stats.value, icon: "👁️", label: "总浏览量", color: "#4facfe", trend: null },
  { key: "totalLikes" as keyof typeof stats.value, icon: "❤️", label: "总点赞数", color: "#f5576c", trend: null },
];

// 状态分布计算
const statusDistribution = computed(() => [
  { label: "草稿", count: memeList.value.filter(m => m.status === 0).length, color: "#bbb" },
  { label: "待审核", count: memeList.value.filter(m => m.status === 1).length, color: "#f5a623" },
  { label: "已通过", count: memeList.value.filter(m => m.status === 2).length, color: "#67c23a" },
  { label: "已驳回", count: memeList.value.filter(m => m.status === 3).length, color: "#f56c6c" },
]);

const totalMemeCount = computed(() => memeList.value.length);

// 点赞榜 TOP5
const topMemes = computed(() =>
  [...memeList.value]
    .filter(m => m.status === 2)
    .sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
    .slice(0, 5)
);

// 用户搜索
const filteredUserList = computed(() => {
  if (!userSearch.value.trim()) return userList.value;
  const kw = userSearch.value.toLowerCase();
  return userList.value.filter(u =>
    (u.username || "").toLowerCase().includes(kw) ||
    (u.nickname || "").toLowerCase().includes(kw)
  );
});

const handleUserSearch = () => {
  // 响应式 computed 已处理
};

const switchTab = (key: string) => {
  activeTab.value = key;
};

const fetchMemeList = async () => {
  memeLoading.value = true;
  try {
    const params: any = { current: 1, size: 200 };
    if (statusFilter.value !== null) params.status = statusFilter.value;
    const res = await memeApi.list(params);
    memeList.value = res.records;
    // 更新待审核 badge
    const pendingCount = memeList.value.filter(m => m.status === 1).length;
    tabs[0].badge = pendingCount > 0 ? pendingCount : null;
    updateStats();
  } catch (e) {
    console.error(e);
  } finally {
    memeLoading.value = false;
  }
};

const fetchUserList = async () => {
  userLoading.value = true;
  try {
    const res = await userApi.list();
    userList.value = res || [];
    stats.value.totalUsers = userList.value.length;
  } catch (e) {
    console.error(e);
  } finally {
    userLoading.value = false;
  }
};

const updateStats = () => {
  stats.value.totalMemes = memeList.value.length;
  stats.value.totalViews = memeList.value.reduce((s, m) => s + (m.viewCount || 0), 0);
  stats.value.totalLikes = memeList.value.reduce((s, m) => s + (m.likeCount || 0), 0);
};

const handleApprove = async (id: number) => {
  try {
    await memeApi.audit(id, { status: 2, rejectReason: "" });
    ElMessage.success("✅ 审核通过");
    fetchMemeList();
  } catch (e) { console.error(e); }
};

const showRejectModal = (id: number) => {
  currentMemeId.value = id;
  rejectReason.value = "";
  showReject.value = true;
};

const closeRejectModal = () => {
  showReject.value = false;
  currentMemeId.value = null;
  rejectReason.value = "";
};

const handleReject = async () => {
  if (!currentMemeId.value || !rejectReason.value.trim()) return;
  try {
    await memeApi.audit(currentMemeId.value, { status: 3, rejectReason: rejectReason.value });
    ElMessage.success("已驳回");
    closeRejectModal();
    fetchMemeList();
  } catch (e) { console.error(e); }
};

const viewDetail = (id: number) => router.push(`/meme/${id}`);

const promoteUser = async (id: number) => {
  try {
    await userApi.updateRole(id, 2);
    ElMessage.success("✅ 已提升为审核员");
    fetchUserList();
  } catch (e) {
    ElMessage.error("操作失败");
  }
};

const toggleUserStatus = async (user: User) => {
  const newStatus = user.status === 1 ? 0 : 1;
  try {
    await userApi.updateStatus(user.id, newStatus);
    user.status = newStatus;
    ElMessage.success(newStatus === 1 ? "已启用" : "已禁用");
  } catch (e) {
    ElMessage.error("操作失败");
  }
};

const getStatusClass = (status: number) => ({ 0: "draft", 1: "pending", 2: "approved", 3: "rejected" }[status] || "draft");
const getStatusText = (status: number) => ({ 0: "草稿", 1: "待审核", 2: "已通过", 3: "已驳回" }[status] || "未知");
const getRoleText = (role: number) => ({ 1: "普通用户", 2: "审核员", 3: "管理员" }[role] || "未知");
const formatNum = (n: number) => n >= 10000 ? (n / 10000).toFixed(1) + "w" : n.toString();

onMounted(() => {
  fetchMemeList();
  fetchUserList();
});
</script>

<style scoped>
/* ===== 基础 ===== */
.admin-page {
  padding: 24px 0 60px;
}

/* ===== 页头 ===== */
.page-header {
  margin-bottom: 28px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 14px;
}

.title-icon {
  font-size: 36px;
}

.header-title h2 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
}

.header-title p {
  margin: 0;
  font-size: 13px;
  color: #999;
}

/* ===== Tab 导航 ===== */
.tab-nav {
  display: flex;
  gap: 6px;
  margin-bottom: 24px;
  padding: 6px;
  background: #f0f2f8;
  border-radius: 14px;
  width: fit-content;
  flex-wrap: wrap;
}

.tab-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s;
}

.tab-btn:hover {
  background: rgba(255,255,255,0.7);
}

.tab-btn.active {
  background: white;
  color: #667eea;
  font-weight: 600;
  box-shadow: 0 3px 12px rgba(0,0,0,0.1);
}

.tab-icon {
  font-size: 15px;
}

.tab-badge {
  position: absolute;
  top: 4px;
  right: 6px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: #f56c6c;
  color: white;
  border-radius: 9px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

/* ===== Tab 过渡 ===== */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: all 0.25s ease;
}

.tab-fade-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.tab-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ===== 面板 ===== */
.section-panel {
  background: white;
  border-radius: 18px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.07);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
}

.update-time {
  font-size: 12px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 4px 12px;
  border-radius: 20px;
}

/* ===== 过滤栏 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-select {
  padding: 8px 14px;
  border: 2px solid #ebebeb;
  border-radius: 10px;
  font-size: 14px;
  color: #555;
  background: #fafafa;
  cursor: pointer;
  transition: border-color 0.2s;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
}

.search-wrap {
  position: relative;
}

.search-icon-inner {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 14px;
}

.search-input {
  padding: 9px 14px 9px 36px;
  border: 2px solid #ebebeb;
  border-radius: 10px;
  font-size: 14px;
  width: 220px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
}

/* ===== 骨架屏 ===== */
.table-skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.table-skeleton-row {
  height: 52px;
  border-radius: 8px;
}

.shimmer {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 37%, #f0f0f0 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease infinite;
}

@keyframes shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* ===== 表格 ===== */
.table-wrap {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 640px;
}

.data-table thead {
  background: linear-gradient(135deg, #f8f9ff, #f0f2ff);
}

.data-table th {
  padding: 13px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  white-space: nowrap;
  border-bottom: 2px solid #eef0ff;
}

.data-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: #555;
  border-bottom: 1px solid #f8f8f8;
}

.table-row {
  transition: background 0.15s;
}

.table-row:hover {
  background: #f8f9ff;
}

.table-row:last-child td {
  border-bottom: none;
}

.id-cell {
  font-family: monospace;
  color: #aaa;
  font-size: 13px;
}

.title-cell {
  max-width: 200px;
}

.title-text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
  font-weight: 500;
}

.num-cell {
  text-align: center;
  font-weight: 600;
  color: #444;
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-draft { background: #f5f5f5; color: #999; }
.status-pending { background: #fff7e6; color: #fa8c16; }
.status-approved { background: #f0f9eb; color: #52c41a; }
.status-rejected { background: #fff2f0; color: #f5222d; }

/* 角色徽章 */
.role-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.role-1 { background: #f5f5f5; color: #888; }
.role-2 { background: #e6f7ff; color: #1890ff; }
.role-3 { background: #fff7e6; color: #fa8c16; }

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 操作按钮 */
.action-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.act-btn {
  padding: 5px 14px;
  border: none;
  border-radius: 7px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.act-btn.approve { background: #52c41a; color: white; }
.act-btn.approve:hover { background: #73d13d; transform: translateY(-1px); }

.act-btn.reject { background: #f5222d; color: white; }
.act-btn.reject:hover { background: #ff4d4f; transform: translateY(-1px); }

.act-btn.view { background: #667eea; color: white; }
.act-btn.view:hover { background: #7b93f5; transform: translateY(-1px); }

.act-btn.promote { background: #fa8c16; color: white; }
.act-btn.promote:hover { background: #ffa940; transform: translateY(-1px); }

.act-btn.toggle-disable { background: #f5f5f5; color: #666; }
.act-btn.toggle-disable:hover { background: #ffeaea; color: #f5222d; }

.act-btn.toggle-enable { background: #f0f9eb; color: #52c41a; }
.act-btn.toggle-enable:hover { background: #52c41a; color: white; }

/* 表格空态 */
.table-empty {
  text-align: center;
  padding: 40px 20px;
  color: #bbb;
  font-size: 14px;
}

.table-empty span {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

/* 行列表动画 */
.row-list-enter-active,
.row-list-leave-active {
  transition: all 0.3s ease;
}

.row-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.row-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* ===== 数据分析 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 32px;
}

.stat-card {
  position: relative;
  padding: 22px 20px;
  border-radius: 16px;
  background: var(--card-color, #667eea);
  color: white;
  overflow: hidden;
  animation: cardIn 0.5s ease both;
  cursor: default;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0,0,0,0.15);
}

.stat-card-bg {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 12px;
  position: relative;
}

.stat-body {
  position: relative;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(20px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* 分布条 */
.distribution-section {
  margin-bottom: 28px;
  padding: 22px;
  background: #f9f9ff;
  border-radius: 14px;
}

.distribution-section h4,
.top-section h4 {
  margin: 0 0 18px;
  font-size: 15px;
  font-weight: 700;
  color: #333;
}

.dist-bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dist-item {}

.dist-label-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.dist-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dist-label {
  font-size: 13px;
  color: #555;
  flex: 1;
}

.dist-count {
  font-size: 13px;
  font-weight: 700;
  color: #333;
}

.dist-bar-track {
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}

.dist-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 1s cubic-bezier(0.23, 1, 0.32, 1);
}

/* TOP 列表 */
.top-section {
  padding: 22px;
  background: #fff9f0;
  border-radius: 14px;
}

.top-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
}

.top-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.top-rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #ddd;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.top-rank.rank-1 { background: linear-gradient(135deg, #ffd700, #ff9500); color: white; }
.top-rank.rank-2 { background: linear-gradient(135deg, #c0c0c0, #909090); color: white; }
.top-rank.rank-3 { background: linear-gradient(135deg, #cd7f32, #a0522d); color: white; }

.top-title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-likes {
  font-size: 13px;
  color: #f5222d;
  font-weight: 600;
  white-space: nowrap;
}

.top-empty {
  text-align: center;
  padding: 20px;
  color: #bbb;
  font-size: 14px;
}

/* ===== 弹窗 ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(3px);
}

.modal-box {
  background: white;
  border-radius: 18px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #1a1a2e;
}

.modal-close-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.modal-close-btn:hover { background: #eee; }

.modal-body { padding: 22px 24px; }

.reject-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #eee;
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.reject-textarea:focus {
  outline: none;
  border-color: #667eea;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.modal-cancel-btn {
  padding: 10px 22px;
  background: #f5f5f5;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: background 0.2s;
}

.modal-cancel-btn:hover { background: #ebebeb; }

.modal-confirm-btn {
  padding: 10px 26px;
  background: linear-gradient(135deg, #f5222d, #ff4d4f);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(245,34,45,0.3);
  transition: all 0.25s;
}

.modal-confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(245,34,45,0.4);
}

.modal-confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

/* ===== 弹窗过渡 ===== */
.modal-fade-enter-active { animation: modalIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.modal-fade-leave-active { animation: modalOut 0.2s ease; }

@keyframes modalIn {
  from { opacity: 0; transform: scale(0.88) translateY(-15px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

@keyframes modalOut {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(0.9); }
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .section-panel { padding: 18px; }
  .tab-nav { width: 100%; }
  .tab-btn { padding: 9px 16px; font-size: 13px; }
  .search-input { width: 180px; }
  .section-header { flex-direction: column; align-items: flex-start; }
}

@media (max-width: 480px) {
  .stats-grid { grid-template-columns: 1fr 1fr; gap: 12px; }
  .stat-value { font-size: 22px; }
  .search-input { width: 100%; }
}
</style>
