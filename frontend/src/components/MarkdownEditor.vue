<template>
  <div class="markdown-editor">
    <div class="editor-toolbar">
      <button type="button" class="toolbar-btn" @click="insertText('**', '**')" title="加粗">
        <span class="icon">B</span>
      </button>
      <button type="button" class="toolbar-btn" @click="insertText('*', '*')" title="斜体">
        <span class="icon">I</span>
      </button>
      <button type="button" class="toolbar-btn" @click="insertText('# ')" title="标题">
        <span class="icon">H</span>
      </button>
      <div class="toolbar-divider"></div>
      <button type="button" class="toolbar-btn" @click="insertText('[', '](url)')" title="链接">
        <span class="icon">🔗</span>
      </button>
      <button type="button" class="toolbar-btn" @click="insertText('![', '](url)')" title="图片">
        <span class="icon">🖼️</span>
      </button>
      <button type="button" class="toolbar-btn" @click="triggerImageUpload" title="上传图片">
        <span class="icon">📤</span>
      </button>
      <div class="toolbar-divider"></div>
      <button type="button" class="toolbar-btn" @click="insertText('`', '`')" title="行内代码">
        <span class="icon">{'<>'}</span>
      </button>
      <button type="button" class="toolbar-btn" @click="insertText('```\n', '\n```')" title="代码块">
        <span class="icon">📝</span>
      </button>
      <button type="button" class="toolbar-btn" @click="insertText('- ')" title="无序列表">
        <span class="icon">•</span>
      </button>
      <button type="button" class="toolbar-btn" @click="insertText('1. ')" title="有序列表">
        <span class="icon">1.</span>
      </button>
      <div class="toolbar-spacer"></div>
      <button type="button" class="toolbar-btn" :class="{ active: previewMode }" @click="togglePreview" title="预览">
        <span class="icon">👁️</span>
      </button>
    </div>

    <div class="editor-content">
      <textarea
        v-if="!previewMode"
        ref="textareaRef"
        v-model="localValue"
        class="editor-textarea"
        placeholder="开始编辑您的内容..."
        @input="handleInput"
        @drop="handleDrop"
      />
      <div v-else class="editor-preview" v-html="renderedMarkdown"></div>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      class="hidden-file-input"
      @change="handleFileSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

marked.setOptions({
  highlight: function (code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  },
  breaks: true,
  gfm: true
})

interface Props {
  modelValue: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'image-upload', file: File): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const localValue = ref(props.modelValue)
const previewMode = ref(false)
const textareaRef = ref<HTMLTextAreaElement>()
const fileInputRef = ref<HTMLInputElement>()

const renderedMarkdown = computed(() => {
  return marked.parse(localValue.value) as string
})

watch(() => props.modelValue, (newVal) => {
  if (newVal !== localValue.value) {
    localValue.value = newVal
  }
})

watch(localValue, (newVal) => {
  emit('update:modelValue', newVal)
})

const handleInput = () => {
}

const insertText = (before: string, after: string = '') => {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = localValue.value
  const selectedText = text.substring(start, end)

  localValue.value = text.substring(0, start) + before + selectedText + after + text.substring(end)

  nextTick(() => {
    textarea.focus()
    if (selectedText) {
      textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
    } else {
      textarea.setSelectionRange(start + before.length, start + before.length)
    }
  })
}

const triggerImageUpload = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    emit('image-upload', file)
  }
  if (target) {
    target.value = ''
  }
}

const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  const file = e.dataTransfer?.files?.[0]
  if (file && file.type.startsWith('image/')) {
    emit('image-upload', file)
  }
}

const togglePreview = () => {
  previewMode.value = !previewMode.value
}

defineExpose({
  insertText
})
</script>

<style scoped>
.markdown-editor {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  flex-wrap: wrap;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: bold;
}

.toolbar-btn:hover {
  background: #e9ecef;
}

.toolbar-btn.active {
  background: #409eff;
  color: white;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #e0e0e0;
  margin: 0 4px;
}

.toolbar-spacer {
  flex: 1;
}

.editor-content {
  display: flex;
  flex-direction: column;
}

.editor-textarea {
  width: 100%;
  min-height: 400px;
  padding: 16px;
  border: none;
  outline: none;
  resize: vertical;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 14px;
  line-height: 1.6;
  background: #fff;
}

.editor-preview {
  min-height: 400px;
  padding: 16px;
  line-height: 1.8;
  overflow-y: auto;
}

.editor-preview :deep(img) {
  max-width: 100%;
  border-radius: 4px;
}

.editor-preview :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.9em;
}

.editor-preview :deep(pre) {
  background: #282c34;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
}

.editor-preview :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #abb2bf;
}

.editor-preview :deep(h1),
.editor-preview :deep(h2),
.editor-preview :deep(h3) {
  margin-top: 24px;
  margin-bottom: 12px;
  font-weight: 600;
}

.editor-preview :deep(p) {
  margin: 12px 0;
}

.editor-preview :deep(ul),
.editor-preview :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.editor-preview :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 16px 0;
  color: #666;
}

.editor-preview :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.editor-preview :deep(a:hover) {
  text-decoration: underline;
}

.hidden-file-input {
  display: none;
}
</style>
