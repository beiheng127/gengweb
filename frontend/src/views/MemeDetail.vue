<template>
  <div class="detail-page">
    <button class="back-btn" @click="router.back()">
      <span>← 返回</span>
    </button>

    <div v-if="loading" class="loading">
      <div class="loading-skeleton">
        <div class="sk-title shimmer"></div>
        <div class="sk-meta shimmer"></div>
        <div class="sk-cover shimmer"></div>
        <div class="sk-content">
          <div class="sk-line shimmer" style="width:90%"></div>
          <div class="sk-line shimmer" style="width:75%"></div>
          <div class="sk-line shimmer" style="width:85%"></div>
          <div class="sk-line shimmer" style="width:60%"></div>
        </div>
      </div>
    </div>

    <template v-else-if="meme">
      <div class="detail-main">
        <!-- 左侧内容 -->
        <div class="detail-left">
          <!-- 头部 -->
          <div class="detail-header">
            <div class="meme-status" :class="'status-' + meme.status">
              {{ statusText }}
            </div>
            <h1 class="detail-title">{{ meme.title }}</h1>
            <div class="detail-meta">
              <span class="meta-item">
                <span class="meta-icon">📅</span>
                {{ formatDate(meme.createTime) }}
              </span>
              <span v-if="meme.createUserName" class="meta-item">
                <span class="meta-icon">👤</span>
                {{ meme.createUserName }}
              </span>
              <span class="meta-item">
                <span class="meta-icon">👁</span>
                {{ meme.viewCount || 0 }} 次浏览
              </span>
            </div>
          </div>

          <!-- 媒体展示 -->
          <div v-if="meme.mediaUrl" class="detail-media">
            <LazyImage
              v-if="meme.mediaType === 1"
              :src="meme.mediaUrl"
              :alt="meme.title"
            />
            <video
              v-else-if="meme.mediaType === 2"
              :src="meme.mediaUrl"
              controls
              class="media-video"
            />
            <div v-else-if="meme.mediaType === 3" class="audio-wrapper">
              <div class="audio-bg">
                <span class="audio-wave">🎵 {{ meme.title }}</span>
              </div>
              <audio :src="meme.mediaUrl" controls class="media-audio" />
            </div>
          </div>

          <!-- 正文内容 -->
          <div class="detail-content" v-html="renderedContent"></div>

          <!-- 点赞/点踩 -->
          <div class="detail-actions">
            <button
              class="action-btn like-btn"
              :class="{ active: userLike === 1 }"
              @click="handleLike"
              :disabled="actionLoading"
            >
              <span class="action-icon">{{ userLike === 1 ? '❤️' : '👍' }}</span>
              <span class="action-count">{{ meme.likeCount || 0 }}</span>
              <span class="action-label">点赞</span>
            </button>
            <button
              class="action-btn dislike-btn"
              :class="{ active: userLike === 2 }"
              @click="handleDislike"
              :disabled="actionLoading"
            >
              <span class="action-icon">👎</span>
              <span class="action-count">{{ meme.dislikeCount || 0 }}</span>
              <span class="action-label">点踩</span>
            </button>
            <div class="action-divider"></div>
            <button class="action-btn share-btn" @click="handleShare">
              <span class="action-icon">🔗</span>
              <span class="action-label">分享</span>
            </button>
          </div>
        </div>

        <!-- 右侧信息面板 -->
        <div class="detail-right">
          <!-- 基本信息 -->
          <div class="info-card">
            <h3 class="card-title">📋 基本信息</h3>
            <div class="info-list">
              <div class="info-row">
                <span class="info-key">起源时间</span>
                <span class="info-val">{{ formatDate(meme.originTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">爆火时间</span>
                <span class="info-val">{{ formatDate(meme.boomTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">媒体类型</span>
                <span class="info-val">{{ mediaTypeText }}</span>
              </div>
              <div class="info-row" v-if="meme.tags && meme.tags.length">
                <span class="info-key">标签</span>
                <div class="tag-list">
                  <span v-for="tag in meme.tags" :key="tag.id" class="tag-chip">{{ tag.name }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 数据统计 -->
          <div class="stats-card">
            <h3 class="card-title">📊 数据统计</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-icon">👁</span>
                <strong>{{ meme.viewCount || 0 }}</strong>
                <span>浏览</span>
              </div>
              <div class="stat-item">
                <span class="stat-icon">👍</span>
                <strong>{{ meme.likeCount || 0 }}</strong>
                <span>点赞</span>
              </div>
              <div class="stat-item">
                <span class="stat-icon">👎</span>
                <strong>{{ meme.dislikeCount || 0 }}</strong>
                <span>点踩</span>
              </div>
              <div class="stat-item">
                <span class="stat-icon">💬</span>
                <strong>{{ commentList.length }}</strong>
                <span>评论</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <div class="comment-header">
          <h3 class="section-title">
            💬 评论
            <span class="comment-badge">{{ commentList.length }}</span>
          </h3>
        </div>

        <div class="comment-form">
          <div class="comment-input-area">
            <textarea
              v-model="newComment"
              placeholder="写下你的评论，分享你的想法..."
              rows="3"
              class="comment-textarea"
            ></textarea>
          </div>
          <div class="comment-form-footer">
            <span class="char-count" :class="{ warning: newComment.length > 450 }">
              {{ newComment.length }}/500
            </span>
            <button
              class="submit-btn"
              @click="submitComment"
              :disabled="commentSubmitting || !newComment.trim() || newComment.length > 500"
            >
              <span v-if="commentSubmitting" class="btn-spinner"></span>
              {{ commentSubmitting ? "发布中..." : "发表评论" }}
            </button>
          </div>
        </div>

        <div v-if="commentLoading" class="comments-loading">
          <div class="spinner-mini"></div>
          <span>加载评论中...</span>
        </div>

        <div v-else-if="commentList.length === 0" class="empty-comments">
          <span class="empty-icon">💬</span>
          <p>暂无评论，快来抢沙发吧！</p>
        </div>

        <TransitionGroup name="comment-slide" tag="div" class="comment-list">
          <div v-for="comment in commentList" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              {{ (comment.userName || "U").charAt(0).toUpperCase() }}
            </div>
            <div class="comment-body">
              <div class="comment-header-row">
                <span class="comment-author">{{ comment.userName || "用户" }}</span>
                <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <button
                  class="comment-action-btn"
                  :class="{ liked: likedComments.has(comment.id!) }"
                  @click="likeComment(comment.id!)"
                >
                  {{ likedComments.has(comment.id!) ? '❤️' : '👍' }}
                  {{ comment.likeCount || 0 }}
                </button>
              </div>
            </div>
          </div>
        </TransitionGroup>
      </div>
    </template>

    <div v-else class="not-found">
      <div class="nf-icon">😕</div>
      <h3>未找到该梗</h3>
      <p>它可能已被删除或不存在</p>
      <button class="btn-back" @click="router.push('/')">返回首页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { marked } from "marked";
import { commentApi, memeApi } from "@/api";
import { ElMessage } from "element-plus";
import LazyImage from "@/components/LazyImage.vue";
import type { Meme, Comment } from "@/types";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const commentLoading = ref(false);
const commentSubmitting = ref(false);
const actionLoading = ref(false);
const meme = ref<Meme | null>(null);
const newComment = ref("");
const userLike = ref<number>(0);  // 0=无, 1=赞, 2=踩
const commentList = ref<Comment[]>([]);
const likedComments = ref(new Set<number>());

const renderedContent = computed(() => {
  if (!meme.value?.content) return "";
  return marked.parse(meme.value.content) as string;
});

const memeId = computed(() => Number(route.params.id));

const statusText = computed(() => {
  const map: Record<number, string> = { 0: "草稿", 1: "待审核", 2: "已发布", 3: "已驳回" };
  return map[meme.value?.status ?? 0] || "";
});

const mediaTypeText = computed(() => {
  const map: Record<number, string> = { 1: "🖼 图片", 2: "🎬 视频", 3: "🎵 音频" };
  return map[meme.value?.mediaType ?? 0] || "未知";
});

const fetchDetail = async () => {
  loading.value = true;
  try {
    const res = await memeApi.detail(memeId.value);
    meme.value = res;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const loadComments = async () => {
  commentLoading.value = true;
  try {
    const res = await commentApi.list(memeId.value);
    commentList.value = res;
  } catch (e) {
    console.error(e);
  } finally {
    commentLoading.value = false;
  }
};

const handleLike = async () => {
  if (!meme.value || actionLoading.value) return;
  actionLoading.value = true;
  try {
    if (userLike.value === 1) {
      // 取消点赞
      await memeApi.unlike(meme.value.id!);
      meme.value.likeCount = Math.max((meme.value.likeCount || 0) - 1, 0);
      userLike.value = 0;
    } else {
      if (userLike.value === 2) {
        // 先取消点踩
        await memeApi.undislike(meme.value.id!);
        meme.value.dislikeCount = Math.max((meme.value.dislikeCount || 0) - 1, 0);
      }
      await memeApi.like(meme.value.id!);
      meme.value.likeCount = (meme.value.likeCount || 0) + 1;
      userLike.value = 1;
    }
  } catch (e) {
    ElMessage.error("操作失败，请稍后重试");
  } finally {
    actionLoading.value = false;
  }
};

const handleDislike = async () => {
  if (!meme.value || actionLoading.value) return;
  actionLoading.value = true;
  try {
    if (userLike.value === 2) {
      await memeApi.undislike(meme.value.id!);
      meme.value.dislikeCount = Math.max((meme.value.dislikeCount || 0) - 1, 0);
      userLike.value = 0;
    } else {
      if (userLike.value === 1) {
        await memeApi.unlike(meme.value.id!);
        meme.value.likeCount = Math.max((meme.value.likeCount || 0) - 1, 0);
      }
      await memeApi.dislike(meme.value.id!);
      meme.value.dislikeCount = (meme.value.dislikeCount || 0) + 1;
      userLike.value = 2;
    }
  } catch (e) {
    ElMessage.error("操作失败，请稍后重试");
  } finally {
    actionLoading.value = false;
  }
};

const handleShare = () => {
  const url = window.location.href;
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url).then(() => ElMessage.success("链接已复制到剪贴板"));
  } else {
    ElMessage.info("请手动复制地址栏链接");
  }
};

const submitComment = async () => {
  if (!meme.value || !newComment.value.trim()) return;
  if (newComment.value.length > 500) {
    ElMessage.warning("评论不能超过500字");
    return;
  }
  commentSubmitting.value = true;
  try {
    await commentApi.create({
      memeId: meme.value.id!,
      content: newComment.value.trim(),
    });
    ElMessage.success("评论发布成功 🎉");
    newComment.value = "";
    await loadComments();
  } catch (e) {
    ElMessage.error("评论发布失败，请先登录");
  } finally {
    commentSubmitting.value = false;
  }
};

const likeComment = async (commentId: number) => {
  if (likedComments.value.has(commentId)) return;
  try {
    await commentApi.like(commentId);
    likedComments.value.add(commentId);
    const c = commentList.value.find((c) => c.id === commentId);
    if (c) c.likeCount = (c.likeCount || 0) + 1;
  } catch (e) {}
};

const formatDate = (dateStr?: string) => {
  if (!dateStr) return "-";
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return "-";
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return Math.floor(diff / 60000) + " 分钟前";
  if (diff < 86400000) return Math.floor(diff / 3600000) + " 小时前";
  return date.toLocaleDateString("zh-CN", { year: "numeric", month: "long", day: "numeric" });
};

onMounted(async () => {
  await Promise.all([fetchDetail(), loadComments()]);
});
</script>

<style scoped>
.detail-page { padding: 0 0 40px; }

.back-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; margin-bottom: 20px;
  border: 1.5px solid #e0e6f7; border-radius: 10px;
  background: white; color: #4361ee;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.back-btn:hover { background: #f0f4ff; border-color: #4361ee; transform: translateX(-2px); }

/* ========= 骨架屏 ========= */
.loading-skeleton { max-width: 900px; margin: 0 auto; }
.sk-title { height: 36px; border-radius: 8px; margin-bottom: 16px; width: 60%; }
.sk-meta { height: 20px; border-radius: 6px; margin-bottom: 24px; width: 40%; }
.sk-cover { height: 420px; border-radius: 16px; margin-bottom: 24px; }
.sk-content { }
.sk-line { height: 16px; border-radius: 6px; margin-bottom: 12px; }
.shimmer {
  background: linear-gradient(90deg, #f0f0f0 25%, #e4e4e4 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* ========= 主布局 ========= */
.detail-main {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  margin-bottom: 32px;
}

.detail-left { min-width: 0; }

/* ========= 头部 ========= */
.meme-status {
  display: inline-flex; align-items: center;
  padding: 4px 12px; border-radius: 20px;
  font-size: 12px; font-weight: 600; margin-bottom: 12px;
}
.status-0 { background: #f5f5f5; color: #999; }
.status-1 { background: #fff7e6; color: #d48806; }
.status-2 { background: #f0fff4; color: #389e0d; }
.status-3 { background: #fff1f0; color: #cf1322; }

.detail-title {
  margin: 0 0 16px; font-size: 28px; font-weight: 800;
  color: #1a1a2e; line-height: 1.3;
}

.detail-meta {
  display: flex; gap: 20px; flex-wrap: wrap; margin-bottom: 0;
}
.meta-item {
  display: flex; align-items: center; gap: 5px;
  color: #888; font-size: 13px;
}
.meta-icon { font-size: 14px; }

/* ========= 媒体 ========= */
.detail-media {
  margin: 20px 0 24px; border-radius: 16px;
  overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}
.media-video { width: 100%; display: block; background: #000; max-height: 500px; }

.audio-wrapper { }
.audio-bg {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 60px 40px 20px;
  text-align: center;
}
.audio-wave { color: white; font-size: 24px; font-weight: 700; }
.media-audio { width: 100%; padding: 0 20px 16px; background: #16213e; box-sizing: border-box; }

/* ========= 正文 ========= */
.detail-content {
  line-height: 1.9; font-size: 16px; color: #333;
  margin-bottom: 24px;
}
.detail-content :deep(img) { max-width: 100%; border-radius: 12px; margin: 16px 0; }
.detail-content :deep(h1), .detail-content :deep(h2), .detail-content :deep(h3) {
  color: #1a1a2e; margin-top: 28px; margin-bottom: 12px;
}
.detail-content :deep(code) {
  background: #f5f7fa; padding: 2px 8px;
  border-radius: 6px; font-size: 0.9em; color: #e74c3c;
}
.detail-content :deep(pre) {
  background: #1a1a2e; padding: 20px; border-radius: 12px;
  overflow-x: auto; margin: 16px 0;
}
.detail-content :deep(pre code) { background: none; color: #e0e0e0; padding: 0; }
.detail-content :deep(blockquote) {
  border-left: 4px solid #4361ee; padding-left: 16px;
  margin: 16px 0; color: #666; font-style: italic;
}
.detail-content :deep(a) { color: #4361ee; }
.detail-content :deep(p) { margin-bottom: 14px; }

/* ========= 操作栏 ========= */
.detail-actions {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 0; border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.action-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 20px; border-radius: 40px;
  border: 2px solid #e0e6f7; background: white;
  cursor: pointer; transition: all 0.2s;
  font-size: 14px; font-weight: 600; color: #555;
}
.action-btn:hover { border-color: #4361ee; color: #4361ee; transform: translateY(-2px); }
.action-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
.action-btn.active.like-btn { background: #fff0f0; border-color: #ff4757; color: #ff4757; }
.action-btn.active.dislike-btn { background: #f0f0ff; border-color: #a29bfe; color: #6c5ce7; }
.action-icon { font-size: 18px; }
.action-divider { flex: 1; }

/* ========= 右侧信息面板 ========= */
.detail-right { }

.info-card, .stats-card {
  background: white; border-radius: 16px; padding: 20px;
  margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.card-title { margin: 0 0 16px; font-size: 15px; font-weight: 700; color: #1a1a2e; }

.info-list { display: flex; flex-direction: column; gap: 14px; }
.info-row { display: flex; align-items: flex-start; gap: 8px; }
.info-key { width: 72px; font-size: 13px; color: #999; flex-shrink: 0; padding-top: 1px; }
.info-val { font-size: 14px; color: #333; font-weight: 500; flex: 1; }
.tag-list { display: flex; flex-wrap: wrap; gap: 6px; }
.tag-chip {
  padding: 3px 10px; border-radius: 12px;
  background: #f0f4ff; color: #4361ee;
  font-size: 12px; font-weight: 500;
}

.stats-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
}
.stat-item {
  background: #fafbff; border-radius: 12px; padding: 14px;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  text-align: center;
}
.stat-item .stat-icon { font-size: 20px; }
.stat-item strong { font-size: 22px; font-weight: 800; color: #1a1a2e; }
.stat-item span:last-child { font-size: 12px; color: #aaa; }

/* ========= 评论区 ========= */
.comment-section {
  background: white; border-radius: 20px; padding: 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.comment-header { margin-bottom: 24px; }
.section-title {
  margin: 0; font-size: 20px; font-weight: 700; color: #1a1a2e;
  display: flex; align-items: center; gap: 10px;
}
.comment-badge {
  background: #4361ee; color: white;
  font-size: 13px; padding: 2px 10px; border-radius: 20px;
  font-weight: 600;
}

.comment-form { margin-bottom: 28px; }
.comment-textarea {
  width: 100%; padding: 14px 16px;
  border: 2px solid #e8ecf4; border-radius: 12px;
  font-size: 15px; line-height: 1.6; resize: vertical;
  box-sizing: border-box; transition: border-color 0.2s;
  outline: none; font-family: inherit;
}
.comment-textarea:focus { border-color: #4361ee; }

.comment-form-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 10px;
}
.char-count { font-size: 12px; color: #bbb; }
.char-count.warning { color: #e6a23c; }

.submit-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 28px; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #4361ee 0%, #7209b7 100%);
  color: white; font-size: 14px; font-weight: 700; cursor: pointer;
  transition: all 0.2s; box-shadow: 0 3px 10px rgba(67, 97, 238, 0.35);
}
.submit-btn:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 5px 14px rgba(67, 97, 238, 0.45); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

.btn-spinner {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.comments-loading, .empty-comments {
  display: flex; flex-direction: column; align-items: center;
  padding: 40px; gap: 12px; color: #bbb;
}
.spinner-mini {
  width: 28px; height: 28px;
  border: 3px solid #e8e8e8; border-top-color: #4361ee;
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
.empty-icon { font-size: 40px; }

.comment-list { display: flex; flex-direction: column; gap: 20px; }

.comment-item { display: flex; gap: 14px; }

.comment-avatar {
  width: 44px; height: 44px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white; display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}

.comment-body { flex: 1; min-width: 0; }
.comment-header-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 6px;
}
.comment-author { font-size: 14px; font-weight: 700; color: #1a1a2e; }
.comment-time { font-size: 12px; color: #bbb; }
.comment-text { margin: 0 0 10px; font-size: 15px; color: #444; line-height: 1.6; }
.comment-actions { display: flex; gap: 10px; }
.comment-action-btn {
  padding: 4px 12px; border: 1.5px solid #e0e6f7;
  border-radius: 20px; background: white;
  font-size: 13px; cursor: pointer; color: #888;
  transition: all 0.2s;
}
.comment-action-btn:hover { background: #f0f4ff; border-color: #4361ee; color: #4361ee; }
.comment-action-btn.liked { background: #fff0f0; border-color: #ffb8c0; color: #ff4757; }

/* ========= 评论动画 ========= */
.comment-slide-enter-active { transition: all 0.3s ease; }
.comment-slide-enter-from { opacity: 0; transform: translateY(12px); }

/* ========= 404 ========= */
.not-found { text-align: center; padding: 80px 24px; }
.nf-icon { font-size: 60px; margin-bottom: 16px; }
.not-found h3 { font-size: 22px; color: #333; margin: 0 0 8px; }
.not-found p { color: #999; margin: 0 0 20px; }
.btn-back {
  padding: 10px 28px; background: #4361ee; color: white;
  border: none; border-radius: 10px; font-size: 14px;
  font-weight: 600; cursor: pointer;
}

/* ========= 响应式 ========= */
@media (max-width: 900px) {
  .detail-main { grid-template-columns: 1fr; }
  .detail-right { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .info-card, .stats-card { margin-bottom: 0; }
}
@media (max-width: 600px) {
  .detail-title { font-size: 22px; }
  .comment-section { padding: 20px; }
  .detail-right { grid-template-columns: 1fr; }
}
</style>
