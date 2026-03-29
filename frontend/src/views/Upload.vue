<template>
  <div class="upload-page">
    <!-- 页头 -->
    <div class="upload-header">
      <div class="header-left">
        <h1>
          <span class="header-icon">✏️</span>
          创作新梗
        </h1>
        <p class="header-desc">填写梗百科信息，提交审核后将在平台展示</p>
      </div>
      <div class="header-actions">
        <button class="action-btn btn-discard" @click="handleDiscard">
          <span>↩</span> 放弃
        </button>
        <button class="action-btn btn-draft" @click="handleSaveDraft" :disabled="saving">
          <span v-if="saving" class="btn-spinner"></span>
          <span v-else>💾</span>
          保存草稿
        </button>
        <button class="action-btn btn-submit" @click="handleSubmit" :disabled="saving">
          <span v-if="saving" class="btn-spinner"></span>
          <span v-else>🚀</span>
          提交审核
        </button>
      </div>
    </div>

    <!-- 恢复提示 -->
    <Transition name="banner-slide">
      <div v-if="isRecovered" class="recovery-banner">
        <div class="banner-content">
          <span class="banner-icon">⚠️</span>
          <span>检测到上次未保存的编辑内容，已自动恢复</span>
        </div>
        <button class="banner-dismiss" @click="handleClearRecovery">忽略并清除</button>
      </div>
    </Transition>

    <!-- 表单内容 -->
    <div class="upload-grid">
      <!-- 左栏：基础信息 -->
      <div class="form-panel">
        <div class="panel-title">
          <span>📋</span> 基础信息
        </div>

        <!-- 梗名称 -->
        <div class="field-group">
          <label class="field-label">
            梗名称 <span class="required">*</span>
          </label>
          <input
            v-model="formData.title"
            type="text"
            class="field-input"
            placeholder="给您的梗起个响亮的名字"
            maxlength="50"
          />
          <div class="field-hint">{{ formData.title.length }} / 50</div>
        </div>

        <!-- 时间 -->
        <div class="field-row">
          <div class="field-group">
            <label class="field-label">
              起源时间 <span class="required">*</span>
            </label>
            <input
              v-model="formData.originTime"
              type="datetime-local"
              class="field-input"
            />
          </div>
          <div class="field-group">
            <label class="field-label">
              爆火时间 <span class="required">*</span>
            </label>
            <input
              v-model="formData.boomTime"
              type="datetime-local"
              class="field-input"
            />
          </div>
        </div>

        <!-- 媒体类型 -->
        <div class="field-group">
          <label class="field-label">媒体类型</label>
          <div class="media-type-group">
            <label
              class="media-type-item"
              :class="{ active: formData.mediaType === 1 }"
            >
              <input type="radio" v-model="formData.mediaType" :value="1" />
              <span class="type-icon">🖼️</span>
              <span class="type-text">图片</span>
            </label>
            <label
              class="media-type-item"
              :class="{ active: formData.mediaType === 2 }"
            >
              <input type="radio" v-model="formData.mediaType" :value="2" />
              <span class="type-icon">🎬</span>
              <span class="type-text">视频</span>
            </label>
            <label
              class="media-type-item"
              :class="{ active: formData.mediaType === 3 }"
            >
              <input type="radio" v-model="formData.mediaType" :value="3" />
              <span class="type-icon">🎵</span>
              <span class="type-text">音频</span>
            </label>
          </div>
        </div>

        <!-- 封面上传 -->
        <div class="field-group">
          <label class="field-label">封面媒体</label>
          <div class="media-upload" :class="{ 'has-media': formData.mediaUrl }">
            <template v-if="!formData.mediaUrl">
              <input
                ref="fileInputRef"
                type="file"
                class="hidden-input"
                :accept="acceptedMediaTypes"
                @change="handleMediaSelect"
              />
              <div
                class="media-placeholder"
                @click="triggerMediaUpload"
                @dragover.prevent
                @drop.prevent="handleDrop"
              >
                <div class="upload-icon-wrapper">
                  <div class="upload-icon-bg"></div>
                  <span class="upload-icon">📁</span>
                </div>
                <p class="upload-text">点击或拖拽上传</p>
                <p class="upload-hint">{{ acceptedMediaHint }}</p>
              </div>
            </template>
            <template v-else>
              <div class="media-preview">
                <img v-if="formData.mediaType === 1" :src="formData.mediaUrl" alt="预览" />
                <video v-else-if="formData.mediaType === 2" :src="formData.mediaUrl" controls />
                <audio v-else-if="formData.mediaType === 3" :src="formData.mediaUrl" controls />
                <button class="media-remove" @click="removeMedia">✕</button>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- 右栏：内容编辑 -->
      <div class="editor-panel">
        <div class="panel-title">
          <span>📝</span> 内容编辑 <span class="required">*</span>
        </div>
        <MarkdownEditor
          v-model="formData.content"
          @image-upload="handleEditorImageUpload"
        />
        <div class="editor-footer">
          <span class="char-info">{{ formData.content.length }} 字</span>
          <div class="editor-tips">
            支持 Markdown 语法，可插入图片、代码块等
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import { useEditCache } from '@/composables/useEditCache'
import { memeApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { Meme } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const fileInputRef = ref<HTMLInputElement>()
const saving = ref(false)

const initialForm: Meme = {
  title: '',
  originTime: '',
  boomTime: '',
  content: '',
  mediaType: 1,
  mediaUrl: '',
  status: 0
}

const { data: formData, hasUnsavedChanges, isRecovered, clearCache } = useEditCache<Meme>('meme_edit', initialForm)

const acceptedMediaTypes = computed(() => {
  switch (formData.value.mediaType) {
    case 1: return 'image/*'
    case 2: return 'video/*'
    case 3: return 'audio/*'
    default: return '*/*'
  }
})

const acceptedMediaHint = computed(() => {
  switch (formData.value.mediaType) {
    case 1: return '支持 JPG、PNG、GIF 等格式'
    case 2: return '支持 MP4、WebM 等格式'
    case 3: return '支持 MP3、WAV 等格式'
    default: return ''
  }
})

const triggerMediaUpload = () => fileInputRef.value?.click()

const handleMediaSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    const url = URL.createObjectURL(file)
    formData.value.mediaUrl = url
    ElMessage.success('媒体已添加')
  }
  if (target) target.value = ''
}

const handleDrop = (e: DragEvent) => {
  const file = e.dataTransfer?.files?.[0]
  if (file) {
    const url = URL.createObjectURL(file)
    formData.value.mediaUrl = url
    ElMessage.success('媒体已添加')
  }
}

const removeMedia = () => {
  formData.value.mediaUrl = ''
}

const handleEditorImageUpload = (file: File) => {
  const url = URL.createObjectURL(file)
  const markdown = `![${file.name}](${url})`
  formData.value.content += '\n' + markdown + '\n'
  ElMessage.success('图片已插入')
}

const validateForm = (): boolean => {
  if (!formData.value.title.trim()) {
    ElMessage.warning('请输入梗名称')
    return false
  }
  if (!formData.value.originTime) {
    ElMessage.warning('请选择起源时间')
    return false
  }
  if (!formData.value.boomTime) {
    ElMessage.warning('请选择爆火时间')
    return false
  }
  if (!formData.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return false
  }
  if (!formData.value.mediaUrl) {
    ElMessage.warning('请上传封面媒体')
    return false
  }
  return true
}

const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return ''
  return dateTimeStr.replace('T', ' ')
}

const handleSaveDraft = async () => {
  if (!validateForm()) return
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    return
  }
  saving.value = true
  try {
    const data = {
      ...formData.value,
      originTime: formatDateTime(formData.value.originTime),
      boomTime: formatDateTime(formData.value.boomTime),
      createUserId: userStore.user.id
    }
    const res = await memeApi.create(data)
    ElMessage.success('💾 草稿保存成功')
    clearCache()
    router.push('/')
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return
  if (!userStore.user) {
    ElMessage.warning('请先登录')
    return
  }
  saving.value = true
  try {
    const data = {
      ...formData.value,
      originTime: formatDateTime(formData.value.originTime),
      boomTime: formatDateTime(formData.value.boomTime),
      createUserId: userStore.user.id
    }
    const res = await memeApi.create(data)
    if (res.id) {
      await memeApi.submit(res.id)
      ElMessage.success('🚀 提交审核成功！')
      clearCache()
      router.push('/')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('提交失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleDiscard = async () => {
  if (hasUnsavedChanges.value) {
    try {
      await ElMessageBox.confirm('放弃后未保存的内容将丢失，确定要放弃吗？', '确认放弃', {
        confirmButtonText: '确定放弃',
        cancelButtonText: '继续编辑',
        type: 'warning'
      })
      clearCache()
      router.push('/')
    } catch { /* 取消 */ }
  } else {
    router.push('/')
  }
}

const handleClearRecovery = () => {
  clearCache()
  formData.value = { ...initialForm }
}
</script>

<style scoped>
/* ===== 基础 ===== */
.upload-page {
  padding: 24px 0 60px;
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 页头 ===== */
.upload-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left h1 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 28px;
}

.header-desc {
  margin: 0;
  font-size: 13px;
  color: #999;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

/* ===== 按钮 ===== */
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-discard {
  background: #f5f5f5;
  color: #666;
}

.btn-discard:hover:not(:disabled) {
  background: #eee;
  transform: translateY(-1px);
}

.btn-draft {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-draft:hover:not(:disabled) {
  background: #f0f2ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.25);
}

.btn-submit {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 22px rgba(102, 126, 234, 0.5);
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 恢复横幅 ===== */
.recovery-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #fff7e6, #fff1cc);
  border: 1px solid #ffe0a0;
  border-radius: 12px;
  flex-wrap: wrap;
  gap: 10px;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #d48806;
}

.banner-dismiss {
  background: none;
  border: none;
  color: #d48806;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 6px;
  transition: background 0.2s;
}

.banner-dismiss:hover {
  background: rgba(212, 136, 6, 0.1);
}

/* ===== 横幅过渡 ===== */
.banner-slide-enter-active {
  animation: bannerIn 0.4s ease;
}

.banner-slide-leave-active {
  animation: bannerOut 0.3s ease;
}

@keyframes bannerIn {
  from { opacity: 0; transform: translateY(-10px) scaleY(0.9); }
  to { opacity: 1; transform: translateY(0) scaleY(1); }
}

@keyframes bannerOut {
  from { opacity: 1; transform: scaleY(1); }
  to { opacity: 0; transform: scaleY(0.8); }
}

/* ===== 表单布局 ===== */
.upload-grid {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
  align-items: start;
}

/* ===== 面板 ===== */
.form-panel,
.editor-panel {
  background: white;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 2px solid #f0f2f8;
}

.required {
  color: #f5222d;
}

/* ===== 表单 ===== */
.field-group {
  margin-bottom: 20px;
}

.field-group:last-child {
  margin-bottom: 0;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #444;
}

.field-input {
  width: 100%;
  padding: 11px 14px;
  border: 2px solid #eef0f5;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
  background: #fafbfe;
}

.field-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.field-hint {
  text-align: right;
  font-size: 12px;
  color: #bbb;
  margin-top: 4px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

/* ===== 媒体类型 ===== */
.media-type-group {
  display: flex;
  gap: 10px;
}

.media-type-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border: 2px solid #eef0f5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s;
  background: #fafbfe;
}

.media-type-item input {
  display: none;
}

.media-type-item:hover {
  border-color: #c5c9e4;
  background: #f5f6ff;
}

.media-type-item.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f2ff, #e8ecff);
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.15);
}

.type-icon {
  font-size: 24px;
}

.type-text {
  font-size: 13px;
  font-weight: 600;
  color: #555;
}

/* ===== 媒体上传 ===== */
.media-upload {
  width: 100%;
  min-height: 180px;
  border: 2px dashed #dcdfe6;
  border-radius: 14px;
  overflow: hidden;
  transition: all 0.3s;
}

.media-upload:hover {
  border-color: #667eea;
  background: #fafbfe;
}

.media-upload.has-media {
  border-style: solid;
  border-color: #eef0f5;
}

.hidden-input {
  display: none;
}

.media-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px;
  cursor: pointer;
}

.upload-icon-wrapper {
  position: relative;
  margin-bottom: 12px;
}

.upload-icon-bg {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea15, #764ba215);
  animation: pulse 2.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}

.upload-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 28px;
}

.upload-text {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #555;
}

.upload-hint {
  margin: 0;
  font-size: 12px;
  color: #bbb;
}

.media-preview {
  position: relative;
}

.media-preview img {
  width: 100%;
  display: block;
  border-radius: 12px;
}

.media-preview video {
  width: 100%;
  display: block;
}

.media-preview audio {
  width: 100%;
  padding: 30px 20px;
  box-sizing: border-box;
}

.media-remove {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: rgba(0,0,0,0.6);
  color: white;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  backdrop-filter: blur(4px);
}

.media-remove:hover {
  background: #f5222d;
  transform: scale(1.1);
}

/* ===== 编辑器底部 ===== */
.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.char-info {
  font-size: 13px;
  color: #999;
  font-weight: 600;
}

.editor-tips {
  font-size: 12px;
  color: #ccc;
}

/* ===== 响应式 ===== */
@media (max-width: 960px) {
  .upload-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .upload-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
  }

  .action-btn {
    flex: 1;
    justify-content: center;
    padding: 10px 12px;
    font-size: 13px;
  }

  .field-row {
    grid-template-columns: 1fr;
  }

  .form-panel,
  .editor-panel {
    padding: 18px;
    border-radius: 14px;
  }
}
</style>
