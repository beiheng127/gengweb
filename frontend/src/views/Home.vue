<template>
  <div class="home">
    <!-- Hero Banner -->
    <section class="hero">
      <div class="hero-content">
        <div class="hero-title-wrap">
          <span class="hero-emoji">🎭</span>
          <h1 class="hero-title">梗百科</h1>
        </div>
        <p class="hero-subtitle">收录互联网流行梗，记录起源、爆火时间与文化语境</p>
        <div class="hero-search-mini">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索你想了解的梗..."
            @keyup.enter="searchMemes"
          />
          <button @click="searchMemes">搜索</button>
        </div>
      </div>
      <div class="hero-stats">
        <div class="hero-stat" v-for="(stat, i) in heroStats" :key="i">
          <span class="stat-icon">{{ stat.icon }}</span>
          <strong class="stat-num">{{ stat.value }}</strong>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </section>

    <div class="home-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="sidebar-card">
          <div class="card-header">
            <span class="card-icon">🔥</span>
            <h3>近期热梗</h3>
          </div>
          <div class="hot-list">
            <div
              v-for="(item, index) in hotList"
              :key="item.id"
              class="hot-item"
              @click="goToDetail(item.id!)"
            >
              <span class="hot-rank" :class="'rank-' + Math.min(index + 1, 4)">{{ index + 1 }}</span>
              <span class="hot-title">{{ item.title }}</span>
              <span class="hot-score">{{ getHotScore(item) }}</span>
            </div>
            <div v-if="hotList.length === 0" class="empty-hot">暂无数据</div>
          </div>
        </div>

        <div class="sidebar-card">
          <div class="card-header">
            <span class="card-icon">🏷️</span>
            <h3>标签分类</h3>
          </div>
          <div class="tag-cloud">
            <button
              v-for="tag in tagList"
              :key="tag.id"
              class="tag-pill"
              :class="{ active: selectedTag === tag.id }"
              @click="selectTag(tag.id!)"
            >
              {{ tag.name }}
              <span class="tag-count">{{ tag.useCount || 0 }}</span>
            </button>
          </div>
        </div>
      </aside>

      <!-- 主内容 -->
      <main class="main-content">
        <!-- 搜索/筛选栏 -->
        <div class="filter-card">
          <div class="search-row">
            <div class="search-input-wrap">
              <span class="search-icon-inner">🔎</span>
              <input
                v-model="keyword"
                type="text"
                class="search-field"
                placeholder="搜索梗名称、描述..."
                @keyup.enter="searchMemes"
              />
              <button v-if="keyword" class="clear-btn" @click="clearKeyword">×</button>
            </div>
            <button class="search-submit" @click="searchMemes">搜索</button>
          </div>
          <div class="quick-tags">
            <span class="quick-label">热门：</span>
            <button
              v-for="tag in quickTags"
              :key="tag"
              class="quick-tag"
              :class="{ active: keyword === tag }"
              @click="quickSearch(tag)"
            >{{ tag }}</button>
            <button class="quick-tag reset-tag" @click="clearFilters" v-if="keyword || selectedTag">
              ✕ 清空筛选
            </button>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-area">
          <div class="loading-grid">
            <div v-for="i in 6" :key="i" class="skeleton-card">
              <div class="skeleton-cover shimmer"></div>
              <div class="skeleton-body">
                <div class="skeleton-line shimmer" style="width:70%"></div>
                <div class="skeleton-line shimmer" style="width:90%"></div>
                <div class="skeleton-line shimmer" style="width:50%"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="memeList.length === 0" class="empty-state">
          <div class="empty-emoji">🔍</div>
          <h3>没有找到相关梗</h3>
          <p>试试更换关键词，或者清空当前筛选条件</p>
          <button class="btn-reset" @click="clearFilters">重置筛选</button>
        </div>

        <!-- 梗卡片网格 -->
        <TransitionGroup v-else name="card-list" tag="div" class="meme-grid">
          <div
            v-for="meme in memeList"
            :key="meme.id"
            class="meme-card"
            @click="goToDetail(meme.id!)"
          >
            <div class="meme-cover">
              <LazyImage
                v-if="meme.mediaType === 1"
                :src="meme.mediaUrl"
                :alt="meme.title"
              />
              <div v-else-if="meme.mediaType === 2" class="media-placeholder video-bg">
                <span>▶ 视频</span>
              </div>
              <div v-else-if="meme.mediaType === 3" class="media-placeholder audio-bg">
                <span>🎵 音频</span>
              </div>
              <div v-else class="media-placeholder default-bg">
                <span>🎭</span>
              </div>
              <div class="cover-overlay">
                <span class="cover-view-icon">👁</span>
              </div>
            </div>
            <div class="meme-info">
              <h3 class="meme-title">{{ meme.title }}</h3>
              <p class="meme-desc">{{ truncateText(meme.content, 72) }}</p>
              <div class="meme-meta">
                <span class="meta-item"><span class="meta-icon">👁</span>{{ formatCount(meme.viewCount) }}</span>
                <span class="meta-item like"><span class="meta-icon">👍</span>{{ formatCount(meme.likeCount) }}</span>
                <span class="meta-item dislike"><span class="meta-icon">👎</span>{{ formatCount(meme.dislikeCount) }}</span>
              </div>
            </div>
          </div>
        </TransitionGroup>

        <!-- 分页 -->
        <div v-if="total > 0 && !loading" class="pagination">
          <button class="page-btn" :disabled="current === 1" @click="changePage(current - 1)">
            ← 上一页
          </button>
          <div class="page-numbers">
            <button
              v-for="p in visiblePages"
              :key="p"
              class="page-num"
              :class="{ active: p === current, ellipsis: p === '...' }"
              @click="typeof p === 'number' && changePage(p)"
            >{{ p }}</button>
          </div>
          <button class="page-btn" :disabled="current >= totalPages" @click="changePage(current + 1)">
            下一页 →
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { memeApi, tagApi } from "@/api";
import LazyImage from "@/components/LazyImage.vue";
import type { Meme, Tag } from "@/types";

const router = useRouter();

const keyword = ref("");
const selectedTag = ref<number | null>(null);
const current = ref(1);
const size = ref(12);
const total = ref(0);
const loading = ref(false);
const memeList = ref<Meme[]>([]);
const hotList = ref<Meme[]>([]);
const tagList = ref<Tag[]>([]);
const quickTags = ref(["搞笑", "热门", "经典", "网络热梗"]);

const totalPages = computed(() => Math.max(Math.ceil(total.value / size.value), 1));

const heroStats = computed(() => [
  { icon: "📚", value: total.value, label: "梗词条" },
  { icon: "👁", value: formatCount(memeList.value.reduce((s, m) => s + (m.viewCount || 0), 0)), label: "总浏览" },
  { icon: "👍", value: formatCount(memeList.value.reduce((s, m) => s + (m.likeCount || 0), 0)), label: "获赞数" },
]);

const visiblePages = computed(() => {
  const total2 = totalPages.value;
  const cur = current.value;
  if (total2 <= 7) return Array.from({ length: total2 }, (_, i) => i + 1);
  const pages: (number | string)[] = [1];
  if (cur > 3) pages.push("...");
  for (let i = Math.max(2, cur - 1); i <= Math.min(total2 - 1, cur + 1); i++) pages.push(i);
  if (cur < total2 - 2) pages.push("...");
  pages.push(total2);
  return pages;
});

const searchMemes = async () => {
  loading.value = true;
  try {
    const params: any = { current: current.value, size: size.value, status: 2 };
    if (keyword.value) params.keyword = keyword.value;
    if (selectedTag.value) params.tagId = selectedTag.value;
    const res = await memeApi.list(params);
    memeList.value = res.records;
    total.value = res.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const loadHotList = async () => {
  try {
    const res = await memeApi.list({ current: 1, size: 10, status: 2 });
    hotList.value = res.records
      .sort((a: Meme, b: Meme) => {
        const scoreA = (a.likeCount || 0) - (a.dislikeCount || 0) + (a.viewCount || 0) / 10;
        const scoreB = (b.likeCount || 0) - (b.dislikeCount || 0) + (b.viewCount || 0) / 10;
        return scoreB - scoreA;
      })
      .slice(0, 8);
  } catch (e) {}
};

const loadTags = async () => {
  try {
    const res = await tagApi.list();
    tagList.value = res;
  } catch (e) {}
};

const selectTag = (tagId: number) => {
  selectedTag.value = selectedTag.value === tagId ? null : tagId;
  current.value = 1;
  searchMemes();
};

const quickSearch = (tag: string) => {
  keyword.value = tag;
  current.value = 1;
  searchMemes();
};

const clearKeyword = () => {
  keyword.value = "";
};

const clearFilters = () => {
  keyword.value = "";
  selectedTag.value = null;
  current.value = 1;
  searchMemes();
};

const goToDetail = (id: number) => router.push(`/meme/${id}`);

const changePage = (page: number) => {
  current.value = page;
  searchMemes();
  window.scrollTo({ top: 0, behavior: "smooth" });
};

const truncateText = (text: string, max: number) => {
  if (!text) return "";
  const plain = text.replace(/[#*`\[\]!]/g, "").trim();
  return plain.length > max ? plain.substring(0, max) + "..." : plain;
};

const getHotScore = (meme: Meme) => {
  const score = (meme.likeCount || 0) - (meme.dislikeCount || 0);
  return score >= 0 ? `+${score}` : score.toString();
};

const formatCount = (n?: number) => {
  if (!n) return "0";
  if (n >= 10000) return (n / 10000).toFixed(1) + "w";
  return n.toString();
};

onMounted(async () => {
  await Promise.all([searchMemes(), loadHotList(), loadTags()]);
});
</script>

<style scoped>
.home { padding: 0; }

/* ========= Hero ========= */
.hero {
  background: linear-gradient(135deg, #0d3b66 0%, #1c6080 50%, #1a936f 100%);
  border-radius: 20px;
  padding: 36px 40px;
  margin-bottom: 28px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  position: relative;
  overflow: hidden;
}
.hero::before {
  content: '';
  position: absolute;
  top: -50%; right: -10%;
  width: 400px; height: 400px;
  background: rgba(255,255,255,0.04);
  border-radius: 50%;
}
.hero::after {
  content: '';
  position: absolute;
  bottom: -30%; left: 30%;
  width: 300px; height: 300px;
  background: rgba(255,255,255,0.03);
  border-radius: 50%;
}

.hero-content { z-index: 1; }

.hero-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.hero-emoji { font-size: 36px; }
.hero-title {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.hero-subtitle {
  margin: 0 0 20px;
  font-size: 15px;
  opacity: 0.85;
  max-width: 380px;
}

.hero-search-mini {
  display: flex;
  gap: 0;
  max-width: 400px;
  background: rgba(255,255,255,0.15);
  border-radius: 10px;
  overflow: hidden;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.2);
}
.hero-search-mini input {
  flex: 1;
  padding: 12px 16px;
  background: transparent;
  border: none;
  outline: none;
  color: white;
  font-size: 14px;
}
.hero-search-mini input::placeholder { color: rgba(255,255,255,0.6); }
.hero-search-mini button {
  padding: 12px 20px;
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.hero-search-mini button:hover { background: rgba(255,255,255,0.3); }

.hero-stats {
  display: flex;
  gap: 16px;
  z-index: 1;
  flex-shrink: 0;
}
.hero-stat {
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.18);
  border-radius: 14px;
  padding: 16px 20px;
  text-align: center;
  backdrop-filter: blur(8px);
  min-width: 90px;
}
.stat-icon { font-size: 20px; display: block; margin-bottom: 4px; }
.stat-num { display: block; font-size: 24px; font-weight: 800; line-height: 1.2; }
.stat-label { display: block; font-size: 11px; opacity: 0.75; margin-top: 2px; }

/* ========= 布局 ========= */
.home-layout { display: flex; gap: 24px; align-items: flex-start; }

.sidebar { width: 260px; flex-shrink: 0; position: sticky; top: 88px; }

.sidebar-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.card-icon { font-size: 18px; }
.card-header h3 { margin: 0; font-size: 15px; font-weight: 700; color: #1a1a2e; }

.hot-list { display: flex; flex-direction: column; gap: 10px; }
.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.hot-item:hover { background: #f5f7ff; }

.hot-rank {
  width: 22px; height: 22px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 6px;
  font-size: 12px; font-weight: 700;
  color: white; flex-shrink: 0;
  background: #ccc;
}
.rank-1 { background: linear-gradient(135deg, #ff6b6b, #ee5a24); }
.rank-2 { background: linear-gradient(135deg, #ffa502, #e67e22); }
.rank-3 { background: linear-gradient(135deg, #ffd32a, #d4a30b); color: #5a4000; }
.rank-4 { background: #ddd; color: #888; }

.hot-title { flex: 1; font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-score { font-size: 12px; color: #1a936f; font-weight: 700; flex-shrink: 0; }
.empty-hot { text-align: center; color: #bbb; font-size: 13px; padding: 16px; }

.tag-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-pill {
  padding: 5px 12px;
  border: none; border-radius: 20px;
  background: #f0f4ff;
  color: #4361ee;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex; align-items: center; gap: 4px;
}
.tag-pill:hover { background: #d8e0ff; }
.tag-pill.active { background: #4361ee; color: white; }
.tag-count { font-size: 11px; opacity: 0.7; }

/* ========= 主内容 ========= */
.main-content { flex: 1; min-width: 0; }

.filter-card {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.search-row { display: flex; gap: 12px; margin-bottom: 14px; }

.search-input-wrap {
  flex: 1; display: flex; align-items: center;
  border: 2px solid #e8ecf4;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.search-input-wrap:focus-within { border-color: #4361ee; }

.search-icon-inner { padding: 0 12px; font-size: 16px; color: #aaa; }
.search-field {
  flex: 1; height: 44px; border: none; outline: none;
  font-size: 14px; background: transparent;
}
.clear-btn {
  padding: 0 14px; background: none; border: none;
  color: #bbb; font-size: 18px; cursor: pointer;
}
.clear-btn:hover { color: #999; }

.search-submit {
  padding: 0 24px; height: 44px;
  background: linear-gradient(135deg, #4361ee, #7209b7);
  color: white; border: none; border-radius: 12px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.2s; white-space: nowrap;
  box-shadow: 0 2px 8px rgba(67, 97, 238, 0.35);
}
.search-submit:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(67, 97, 238, 0.45); }

.quick-tags { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.quick-label { font-size: 13px; color: #999; flex-shrink: 0; }
.quick-tag {
  padding: 5px 14px; border-radius: 20px;
  border: 1.5px solid #e0e6f7;
  background: white; color: #4361ee;
  font-size: 12px; font-weight: 500; cursor: pointer;
  transition: all 0.15s;
}
.quick-tag:hover, .quick-tag.active { background: #4361ee; color: white; border-color: #4361ee; }
.reset-tag { color: #f56c6c; border-color: #fde8e8; }
.reset-tag:hover { background: #f56c6c; color: white; border-color: #f56c6c; }

/* ========= 骨架屏 ========= */
.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.skeleton-card {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.skeleton-cover { height: 180px; background: #eee; }
.skeleton-body { padding: 16px; }
.skeleton-line { height: 13px; background: #eee; border-radius: 6px; margin-bottom: 10px; }
.shimmer {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* ========= 空状态 ========= */
.empty-state {
  background: white; border-radius: 16px; padding: 60px 24px;
  text-align: center; box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.empty-emoji { font-size: 56px; display: block; margin-bottom: 16px; }
.empty-state h3 { margin: 0 0 8px; color: #333; font-size: 18px; }
.empty-state p { margin: 0 0 20px; color: #999; font-size: 14px; }
.btn-reset {
  padding: 10px 28px; border-radius: 10px;
  background: #4361ee; color: white; border: none;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.btn-reset:hover { background: #3451d1; }

/* ========= 梗卡片 ========= */
.meme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.meme-card {
  background: white;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  position: relative;
}
.meme-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
}
.meme-card:hover .cover-overlay { opacity: 1; }

.meme-cover {
  width: 100%; height: 190px;
  overflow: hidden; position: relative;
  background: #f0f2f8;
}
.cover-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s;
}
.cover-view-icon { font-size: 28px; color: white; }

.media-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 600; color: white;
  gap: 6px;
}
.video-bg { background: linear-gradient(135deg, #1a1a2e, #16213e); }
.audio-bg { background: linear-gradient(135deg, #2c3e50, #3498db); }
.default-bg { background: linear-gradient(135deg, #667eea, #764ba2); font-size: 40px; }

.meme-info { padding: 16px; }
.meme-title {
  margin: 0 0 8px; font-size: 15px; font-weight: 700;
  color: #1a1a2e; line-height: 1.4;
  overflow: hidden; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.meme-desc {
  margin: 0 0 12px; font-size: 13px; color: #777;
  line-height: 1.6;
  overflow: hidden; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.meme-meta { display: flex; gap: 12px; }
.meta-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #aaa; }
.meta-item.like .meta-icon { color: #67c23a; }
.meta-item.dislike .meta-icon { color: #f56c6c; }
.meta-icon { font-size: 13px; }

/* ========= 卡片列表动画 ========= */
.card-list-enter-active {
  transition: all 0.35s ease;
}
.card-list-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.97);
}
.card-list-leave-active { transition: all 0.2s ease; }
.card-list-leave-to { opacity: 0; transform: scale(0.96); }

/* ========= 分页 ========= */
.pagination {
  display: flex; align-items: center; justify-content: center;
  gap: 12px; margin-top: 8px;
}
.page-btn {
  padding: 9px 20px; border-radius: 10px;
  border: 1.5px solid #e0e6f7;
  background: white; color: #4361ee;
  font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.page-btn:hover:not(:disabled) { background: #4361ee; color: white; border-color: #4361ee; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-numbers { display: flex; gap: 6px; }
.page-num {
  width: 36px; height: 36px; border-radius: 8px;
  border: 1.5px solid #e0e6f7; background: white;
  color: #555; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.page-num.active { background: #4361ee; color: white; border-color: #4361ee; }
.page-num.ellipsis { border: none; cursor: default; color: #bbb; }
.page-num:hover:not(.active):not(.ellipsis) { background: #f0f4ff; }

/* ========= 响应式 ========= */
@media (max-width: 1100px) {
  .sidebar { width: 220px; }
}
@media (max-width: 900px) {
  .hero { flex-direction: column; align-items: flex-start; }
  .hero-stats { flex-wrap: wrap; }
  .home-layout { flex-direction: column; }
  .sidebar { width: 100%; position: static; display: flex; gap: 16px; overflow-x: auto; }
  .sidebar-card { min-width: 240px; flex-shrink: 0; margin-bottom: 0; }
}
@media (max-width: 600px) {
  .hero { padding: 24px 20px; border-radius: 14px; }
  .hero-title { font-size: 28px; }
  .meme-grid { grid-template-columns: 1fr 1fr; gap: 12px; }
  .meme-cover { height: 140px; }
  .filter-card { padding: 16px; }
  .hero-stats { gap: 10px; }
  .hero-stat { padding: 12px 14px; min-width: 70px; }
}
@media (max-width: 420px) {
  .meme-grid { grid-template-columns: 1fr; }
  .page-numbers { display: none; }
}
</style>
