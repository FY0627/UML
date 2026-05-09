<template>
  <div class="left-panel">
    <div class="brand-header">
      <div class="logo-box">LF</div>
      <span class="brand-name">LingoFlow</span>
    </div>

    <div class="animated-i-section">
      <div class="brand-symbol-group">
        <div class="i-character" :class="{ 'looking-away': isPasswordFocused }">
          <div class="i-dot-head">
            <div class="eyes-container">
              <div class="eye left-eye">
                <div class="pupil" ref="leftPupil"><div class="highlight"></div></div>
              </div>
              <div class="eye right-eye">
                <div class="pupil" ref="rightPupil"><div class="highlight"></div></div>
              </div>
            </div>
          </div>
          <div class="i-body"></div>
        </div>
        <div class="plus-one-static">+1</div>
      </div>
    </div>

    <div class="hero-content">
      <span class="hero-title">沉浸式外语学习体验</span>
      <span class="hero-subtitle">智能生成，让学习顺其自然。</span>
    </div>

    <div class="footer-links">
      <span class="link">隐私政策</span>
      <span class="link">服务条款</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

// 接收来自父组件（Login.vue）的属性，告诉动画现在是不是在输密码
const props = defineProps({
  isPasswordFocused: {
    type: Boolean,
    default: false
  }
})

const leftPupil = ref(null)
const rightPupil = ref(null)

const handleMouseMove = (event) => {
  if (props.isPasswordFocused) return 

  const pupils = [leftPupil.value, rightPupil.value]
  const mouseX = event.clientX
  const mouseY = event.clientY

  pupils.forEach(pupil => {
    if (!pupil) return
    const rect = pupil.getBoundingClientRect()
    const pupilCenterX = rect.left + rect.width / 2
    const pupilCenterY = rect.top + rect.height / 2
    const angleRad = Math.atan2(mouseY - pupilCenterY, mouseX - pupilCenterX)
    const maxOffset = 6 
    const offsetX = Math.cos(angleRad) * maxOffset
    const offsetY = Math.sin(angleRad) * maxOffset
    pupil.style.transform = `translate(${offsetX}px, ${offsetY}px)`
  })
}

// 当密码框失去焦点时，清除内联样式，恢复鼠标跟随
watch(() => props.isPasswordFocused, (newVal) => {
  if (!newVal) {
    if (leftPupil.value) leftPupil.value.style.transform = ''
    if (rightPupil.value) rightPupil.value.style.transform = ''
  }
})

onMounted(() => { window.addEventListener('mousemove', handleMouseMove) })
onUnmounted(() => { window.removeEventListener('mousemove', handleMouseMove) })
</script>

<style scoped>
/* 把原本 App.vue 里和 .left-panel、.animated-i-section 相关的 CSS 剪切到这里，保持代码整洁 */
.left-panel { display: none; flex: 1; background-color: #f3f4f6; padding: 3rem; color: #111827; flex-direction: column; justify-content: space-between; overflow: hidden; position: relative; border-right: 1px solid #e5e7eb; }
@media (min-width: 1024px) { .left-panel { display: flex; } }
.brand-header { display: flex; align-items: center; gap: 10px; z-index: 10; }
.logo-box { width: 32px; height: 32px; background: #111827; border-radius: 8px; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 14px; }
.brand-name { font-size: 1.125rem; font-weight: 600; }
.animated-i-section { flex: 1; display: flex; align-items: center; justify-content: center; z-index: 5; }
.brand-symbol-group { display: flex; align-items: flex-end; gap: 15px; }
.i-character { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.i-dot-head { width: 90px; height: 90px; background-color: #111827; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.eyes-container { display: flex; gap: 8px; }
.eye { width: 28px; height: 28px; background-color: #ffffff; border-radius: 50%; display: flex; align-items: center; justify-content: center; overflow: hidden; box-shadow: inset 0 2px 4px rgba(0,0,0,0.2); }
.pupil { width: 14px; height: 14px; background-color: #111827; border-radius: 50%; position: relative; transition: transform 0.05s linear; }
.pupil .highlight { position: absolute; top: 2px; left: 2px; width: 4px; height: 4px; background-color: #ffffff; border-radius: 50%; opacity: 0.9; }
.i-body { width: 90px; height: 220px; background-color: #111827; border-radius: 45px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.plus-one-static { font-size: 140px; font-weight: 900; color: #111827; line-height: 0.75; margin-bottom: 15px; letter-spacing: -5px; user-select: none; }
.i-character.looking-away .pupil { transform: translate(-6px, -4px) !important; transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.hero-content { z-index: 10; margin-bottom: 20px; text-align: center; }
.hero-title { display: block; font-size: 2rem; font-weight: bold; margin-bottom: 10px; color: #111827; }
.hero-subtitle { font-size: 1rem; color: #6b7280; }
.footer-links { display: flex; justify-content: center; gap: 20px; z-index: 10; font-size: 0.875rem; color: #9ca3af; }
.link { color: #9ca3af; cursor: pointer; text-decoration: none; }
.link:hover { color: #111827; }
</style>