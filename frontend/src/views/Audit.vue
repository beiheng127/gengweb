<template>
  <div class="audit-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <span class="title-icon">🔍</span>
          内容审核中心
        </h2>
        <p class="page-subtitle">共 <strong>{{ pendingList.length }}</strong> 条内容待审核</p>
      </div>
      <div class="header-right">
        <button class="refresh-btn" @click="loadPendingList" :class="{ spinning: loading }">
          <span class="refresh-icon">↻</span>
          刷新
        </button>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton-card">
        <div class="skeleton-cover shimmer"></div>
        <div class="skeleton-body">
          <div class="skeleton-title shimmer"></div>
          <div class="skeleton-line shimmer"></div>
          <div class="skeleton-line short shimmer"></div>
          <div class="skeleton-meta">
            <div class="skeleton-tag shimmer"></div>
            <div class="skeleton-tag shimmer"></div>
          </div>
          <div class="skeleton-actions">
            <div class="skeleton-btn shimmer"></div>
            <div class="skeleton-btn shimmer"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <Transition name="fade-up">
      <div v-if="!loading && pendingList.length === 0" class="empty-state">
        <div class="empty-icon">✅</div>
        <h3>全部审核完毕</h3>
        <p>暂时没有需要审核的内容，休息一下吧~</p>
      </div>
    </Transition>

    <!-- 审核列表 -->
    <TransitionGroup
      v-if="!loading && pendingList.length > 0"
      name="audit-list"
      tag="div"
      class="audit-list"
    >
      <div
        v-for="(meme, index) in pendingList"
        :key="meme.id"
        class="audit-card"
        :style="{ animationDelay: index * 0.08 + 's' }"
      >
        <!-- 序号徽章 -->
        <div class="card-index">{{ index + 1 }}</div>

        <div class="card-body">
          <!-- 封面媒体 -->
          <div class="card-cover">
            <LazyImage
              v-if="meme.mediaType === 1"
              :src="meme.mediaUrl"
              :alt="meme.title"
              class="cover-img"
            />
            <div v-else-if="meme.mediaType === 2" class="cover-media">
              <video :src="meme.mediaUrl" controls class="cover-video" />
            </div>
            <div v-else-if="meme.mediaType === 3" class="cover-audio">
              <div class="audio-icon">🎵</div>
              <audio :src="meme.mediaUrl" controls />
            </div>
            <div v-else class="cover-empty">
              <span>📄</span>
              <p>纯文字内容</p>
            </div>
            <!-- 媒体类型标签 -->
            <div class="media-badge" v-if="meme.mediaType">
              <span v-if="meme.mediaType === 1">🖼️ 图片</span>
              <span v-else-if="meme.mediaType === 2">🎬 视频</span>
              <span v-else-if="meme.mediaType === 3">🎵 音频</span>
            </div>
          </div>

          <!-- 内容信息 -->
          <div class="card-info">
            <div class="info-header">
              <h3 class="meme-title">{{ meme.title }}</h3>
              <span class="meme-id">#{{ meme.id }}</span>
            </div>

            <div class="meme-content-preview">
              {{ truncateContent(meme.content) }}
            </div>

            <div class="meme-meta">
              <div class="meta-item">
                <span class="meta-icon">📅</span>
                <span class="meta-label">起源时间</span>
                <span class="meta-value">{{ formatDate(meme.originTime) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-icon">🔥</span>
                <span class="meta-label">爆火时间</span>
                <span class="meta-value">{{ formatDate(meme.boomTime) }}</span>
              </div>
            </div>

            <div class="card-actions">
              <button
                class="btn-approve"
                @click="approveMeme(meme.id)"
                :disabled="processingIds.has(meme.id)"
              >
                <span v-if="processingIds.has(meme.id)" class="btn-spinner"></span>
                <span v-else>✓</span>
                审核通过
              </button>
              <button
                class="btn-reject"
                @click="showRejectModal(meme.id)"
                :disabled="processingIds.has(meme.id)"
              >
                <span>✗</span>
                驳回
              </button>
            </div>
          </div>
        </div>
      </div>
    </TransitionGroup>

    <!-- 驳回弹窗 -->
    <Transition name="modal-fade">
      <div v-if="showReject" class="modal-overlay" @click.self="closeRejectModal">
        <div class="modal-box">
          <div class="modal-header">
            <div class="modal-title-wrap">
              <span class="modal-icon">✗</span>
              <h3>填写驳回原因</h3>
            </div>
            <button class="modal-close-btn" @click="closeRejectModal">✕</button>
          </div>

          <div class="modal-body">
            <div class="reject-tips">
              <span>💡</span>
              <span>请填写清晰具体的驳回原因，帮助创作者改进内容</span>
            </div>
            <textarea
              v-model="rejectReason"
              class="reject-textarea"
              placeholder="例如：内容违规、质量不达标、信息不完整..."
              rows="5"
              maxlength="500"
            ></textarea>
            <div class="char-count">{{ rejectReason.length }} / 500</div>
          </div>

          <div class="modal-footer">
            <button class="modal-cancel-btn" @click="closeRejectModal">取消</button>
            <button
              class="modal-confirm-btn"
              @click="rejectMeme"
              :disabled="!rejectReason.trim()"
            >
              确认驳回
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { memeApi } from "@/api";
import LazyImage from "@/components/LazyImage.vue";
import type { Meme } from "@/types";

const loading = ref(false);
const pendingList = ref<Meme[]>([]);
const showReject = ref(false);
const rejectReason = ref("");
const currentMemeId = ref<number | null>(null);
const processingIds = ref<Set<number>>(new Set());

const loadPendingList = async () => {
  loading.value = true;
  try {
    const res = await memeApi.list({ current: 1, size: 100, status: 1 });
    pendingList.value = res.records;
  } catch (e) {
    console.error(e);
    ElMessage.error("加载失败，请重试");
  } finally {
    loading.value = false;
  }
};

const approveMeme = async (id: number) => {
  processingIds.value.add(id);
  try {
    await memeApi.audit(id, { status: 2, rejectReason: "" });
    ElMessage.success("✅ 审核通过");
    pendingList.value = pendingList.value.filter(m => m.id !== id);
  } catch (e) {
    console.error(e);
    ElMessage.error("操作失败");
  } finally {
    processingIds.value.delete(id);
  }
};

const showRejectModal = (id: number) => {
  currentMemeId.value = id;
  rejectReason.value = "";
  showReject.value = true;
};

const closeRejectModal = () => {
  showReject.value = false;
  rejectReason.value = "";
  currentMemeId.value = null;
};

const rejectMeme = async () => {
  if (!currentMemeId.value || !rejectReason.value.trim()) return;
  const id = currentMemeId.value;
  processingIds.value.add(id);
  try {
    await memeApi.audit(id, {
      status: 3,
      rejectReason: rejectReason.value,
    });
    ElMessage.success("已驳回");
    pendingList.value = pendingList.value.filter(m => m.id !== id);
    closeRejectModal();
  } catch (e) {
    console.error(e);
    ElMessage.error("操作失败");
  } finally {
    processingIds.value.delete(id);
  }
};

const truncateContent = (content: string, len = 120) => {
  if (!content) return "（暂无内容描述）";
  return content.length > len ? content.slice(0, len) + "..." : content;
};

const formatDate = (date: string) => {
  if (!date) return "未填写";
  return new Date(date).toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

onMounted(() => {
  loadPendingList();
});
</script>

<style scoped>
/* ===== 页面基础 ===== */
.audit-page {
  padding: 24px 0 60px;
  min-height: 60vh;
}

/* ===== 页头 ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 28px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #8c8c8c;
}

.page-subtitle strong {
  color: #ff6b35;
  font-size: 18px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: white;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
  color: #555;
  cursor: pointer;
  transition: all 0.25s;
}

.refresh-btn:hover {
  border-color: #667eea;
  color: #667eea;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.15);
}

.refresh-icon {
  font-size: 18px;
  display: inline-block;
  transition: transform 0.4s;
}

.refresh-btn.spinning .refresh-icon {
  animation: spinOnce 0.6s linear infinite;
}

@keyframes spinOnce {
  to { transform: rotate(360deg); }
}

/* ===== 骨架屏 ===== */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.skeleton-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  gap: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.skeleton-cover {
  width: 220px;
  height: 160px;
  border-radius: 12px;
  flex-shrink: 0;
}

.skeleton-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-title {
  height: 24px;
  border-radius: 6px;
  width: 60%;
}

.skeleton-line {
  height: 14px;
  border-radius: 4px;
  width: 90%;
}

.skeleton-line.short {
  width: 70%;
}

.skeleton-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.skeleton-tag {
  height: 28px;
  width: 100px;
  border-radius: 20px;
}

.skeleton-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.skeleton-btn {
  height: 40px;
  width: 120px;
  border-radius: 10px;
}

/* shimmer 动画 */
.shimmer {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 37%, #f0f0f0 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease infinite;
}

@keyframes shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 72px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.empty-state h3 {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.empty-state p {
  margin: 0;
  font-size: 15px;
  color: #8c8c8c;
}

/* ===== 审核列表动画 ===== */
.audit-list-enter-active {
  animation: slideInUp 0.4s ease both;
}

.audit-list-leave-active {
  animation: slideOutRight 0.35s ease both;
}

.audit-list-move {
  transition: transform 0.4s ease;
}

@keyframes slideInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideOutRight {
  from { opacity: 1; transform: translateX(0) scale(1); }
  to { opacity: 0; transform: translateX(60px) scale(0.95); }
}

/* ===== 审核卡片 ===== */
.audit-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.audit-card {
  position: relative;
  background: white;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.07);
  border: 1px solid rgba(0,0,0,0.05);
  transition: box-shadow 0.3s, transform 0.3s;
}

.audit-card:hover {
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  transform: translateY(-2px);
}

.card-index {
  position: absolute;
  top: 16px;
  left: 16px;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.card-body {
  display: flex;
  gap: 24px;
  padding: 24px;
}

/* ===== 封面区域 ===== */
.card-cover {
  position: relative;
  width: 220px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f8f8;
  min-height: 155px;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-media {
  width: 100%;
}

.cover-video {
  width: 100%;
  max-height: 200px;
  display: block;
}

.cover-audio {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 12px;
  min-height: 155px;
  background: linear-gradient(135deg, #f8f9ff, #e8ecff);
}

.audio-icon {
  font-size: 40px;
}

.cover-audio audio {
  width: 100%;
}

.cover-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 155px;
  color: #bbb;
  gap: 8px;
  font-size: 14px;
}

.cover-empty span {
  font-size: 40px;
}

.media-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(0,0,0,0.65);
  color: white;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  backdrop-filter: blur(4px);
}

/* ===== 信息区域 ===== */
.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.info-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.meme-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.3;
  flex: 1;
}

.meme-id {
  font-size: 13px;
  color: #bbb;
  white-space: nowrap;
  font-family: monospace;
  margin-top: 4px;
}

.meme-content-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.meme-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f8f9fe;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
}

.meta-icon {
  font-size: 14px;
}

.meta-label {
  color: #999;
}

.meta-value {
  color: #555;
  font-weight: 500;
}

/* ===== 操作按钮 ===== */
.card-actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #f5f5f5;
}

.btn-approve,
.btn-reject {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
}

.btn-approve {
  background: linear-gradient(135deg, #56ccf2, #2f80ed);
  color: white;
  box-shadow: 0 4px 15px rgba(47, 128, 237, 0.3);
}

.btn-approve:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(47, 128, 237, 0.45);
}

.btn-approve:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-reject {
  background: linear-gradient(135deg, #ff6b6b, #ee0979);
  color: white;
  box-shadow: 0 4px 15px rgba(238, 9, 121, 0.25);
}

.btn-reject:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(238, 9, 121, 0.4);
}

.btn-reject:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 驳回弹窗 ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(4px);
}

.modal-box {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 520px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee0979 100%);
  color: white;
}

.modal-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-icon {
  font-size: 22px;
  font-weight: 700;
  width: 36px;
  height: 36px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.modal-close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255,255,255,0.2);
  color: white;
  border-radius: 50%;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.modal-close-btn:hover {
  background: rgba(255,255,255,0.35);
}

.modal-body {
  padding: 24px 28px;
}

.reject-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff7ec;
  border: 1px solid #ffe0b2;
  border-radius: 10px;
  font-size: 13px;
  color: #e67e22;
  margin-bottom: 16px;
}

.reject-textarea {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #eee;
  border-radius: 12px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
  transition: border-color 0.2s;
  line-height: 1.6;
}

.reject-textarea:focus {
  outline: none;
  border-color: #ee0979;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #bbb;
  margin-top: 6px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 28px 24px;
}

.modal-cancel-btn {
  padding: 11px 24px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.modal-cancel-btn:hover {
  background: #ebebeb;
}

.modal-confirm-btn {
  padding: 11px 28px;
  background: linear-gradient(135deg, #ff6b6b, #ee0979);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(238, 9, 121, 0.3);
  transition: all 0.25s;
}

.modal-confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(238, 9, 121, 0.45);
}

.modal-confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

/* ===== 弹窗过渡 ===== */
.modal-fade-enter-active {
  animation: modalIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-fade-leave-active {
  animation: modalOut 0.25s ease;
}

@keyframes modalIn {
  from { opacity: 0; transform: scale(0.88) translateY(-20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

@keyframes modalOut {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(0.9) translateY(-10px); }
}

/* ===== 淡入上移 ===== */
.fade-up-enter-active {
  animation: fadeUp 0.5s ease;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .card-body {
    flex-direction: column;
    padding: 16px;
    gap: 16px;
  }

  .card-cover {
    width: 100%;
    height: 200px;
  }

  .cover-img {
    height: 200px;
  }

  .card-index {
    top: 12px;
    left: 12px;
  }

  .card-actions {
    flex-wrap: wrap;
  }

  .btn-approve,
  .btn-reject {
    flex: 1;
    justify-content: center;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .skeleton-card {
    flex-direction: column;
  }

  .skeleton-cover {
    width: 100%;
    height: 160px;
  }
}
</style>
