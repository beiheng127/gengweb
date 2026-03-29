<template>
  <div class="lazy-image" ref="containerRef">
    <img
      v-if="isLoaded"
      :src="src"
      :alt="alt"
      class="lazy-image__img"
      @load="onLoad"
      @error="onError"
    />
    <div v-else class="lazy-image__placeholder">
      <div class="lazy-image__spinner"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Props {
  src: string
  alt?: string
}

const props = defineProps<Props>()

const isLoaded = ref(false)
const isInView = ref(false)
const containerRef = ref<HTMLElement>()
let observer: IntersectionObserver | null = null

const loadImage = () => {
  if (isLoaded.value) return
  const img = new Image()
  img.onload = () => {
    isLoaded.value = true
  }
  img.onerror = () => {
    isLoaded.value = true
  }
  img.src = props.src
}

const onLoad = () => {
}

const onError = () => {
}

onMounted(() => {
  if ('IntersectionObserver' in window) {
    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          isInView.value = true
          loadImage()
          observer?.unobserve(entry.target)
        }
      })
    }, {
      rootMargin: '100px'
    })

    if (containerRef.value) {
      observer.observe(containerRef.value)
    }
  } else {
    isInView.value = true
    loadImage()
  }
})

onUnmounted(() => {
  if (observer && containerRef.value) {
    observer.unobserve(containerRef.value)
  }
})
</script>

<style scoped>
.lazy-image {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.lazy-image__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  animation: fadeIn 0.3s ease;
}

.lazy-image__placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.lazy-image__spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e0e0e0;
  border-top-color: #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
