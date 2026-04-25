<template>
  <div class="dashboard-layout">
    <MobileHeader />
    <Sidebar />

    <div class="main-content">
      <div class="page-header">
        <div class="title-area">
          <h1 class="page-title">核心生词库</h1>
          <p class="page-subtitle">结合原句语境，构建你的专属记忆宫殿。共收录 <strong>{{ total }}</strong> 个单词。</p>
        </div>
      </div>

      <div v-if="isLoading" class="status-state">
        <div class="spinner"></div>
        <p>正在从记忆宫殿中提取生词...</p>
      </div>

      <div v-else-if="vocabList.length === 0" class="status-state empty-state">
        <div class="empty-icon">📭</div>
        <h2>生词本空空如也</h2>
        <button class="primary-btn" style="width: auto; padding: 10px 24px; margin-top: 15px;" @click="goToDashboard">去阅读文章</button>
      </div>

      <div v-else style="display: flex; flex-direction: column; flex: 1;">
        <div class="vocab-grid">
          <div v-for="vocab in vocabList" :key="vocab.id" class="vocab-card" :class="{ 'mastered': vocab.mastered === 1 }">
            <div class="card-top">
              <h2 class="word">{{ vocab.word }}</h2>
            </div>
            <div class="card-translation">{{ vocab.translation }}</div>
            <div class="card-context">
              <span class="context-label">语境回忆：</span>
              <p class="context-sentence">{{ vocab.contextSentence }}</p>
            </div>
            <div class="card-actions">
              <button class="action-icon-btn outline-btn" @click="openCorrectionModal(vocab)">报错</button>
              <button v-if="vocab.mastered !== 1" class="action-icon-btn check-btn" @click="markMastered(vocab)">掌握</button>
              <button class="action-icon-btn delete-btn" @click="removeVocab(vocab.id)">移除</button>
            </div>
          </div>
        </div>

        <div class="pagination-bar" v-if="totalPages > 1">
          <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">上一页</button>
          <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">下一页</button>
        </div>
      </div>

    </div>

    <!-- 纠错报错弹窗 -->
    <div class="modal-overlay" v-if="showCorrectionModal" @click.self="closeCorrectionModal">
      <div class="modal-card">
        <div class="modal-header">
          <h2>词典翻译纠错</h2>
          <button class="close-modal" @click="closeCorrectionModal">✕</button>
        </div>
        <div class="modal-body">
          <p class="correction-tip">对于单词 <strong>{{ correctionWord }}</strong>，请修改你不满意的翻译：</p>
          <textarea v-model="correctionSuggestion" class="correction-textarea" placeholder="请输入更好的翻译或词义解析..."></textarea>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeCorrectionModal" :disabled="isSubmittingCorrection">取消</button>
          <button class="btn-submit" @click="submitCorrection" :disabled="isSubmittingCorrection">
            {{ isSubmittingCorrection ? '提交中...' : '提交建议' }}
          </button>
        </div>
      </div>
    </div>
    <Tabbar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import MobileHeader from '../components/MobileHeader.vue'
import Tabbar from '../components/Tabbar.vue'
import { getVocabularyListApi, updateVocabularyStatusApi, deleteVocabularyApi } from '../api/vocabulary'
import { submitCorrectionApi } from '../api/correction'

const router = useRouter()
const isLoading = ref(true)
const vocabList = ref([])

// 分页状态
const currentPage = ref(1)
const pageSize = ref(12) // 一页显示 12 个比较好看
const total = ref(0)
const totalPages = ref(1)

const fetchVocabularies = async () => {
  isLoading.value = true
  try {
    const res = await getVocabularyListApi(currentPage.value, pageSize.value)
    if (res.records) {
      vocabList.value = res.records
      total.value = res.total
      totalPages.value = Math.ceil(res.total / pageSize.value)
    } else {
      vocabList.value = res || []
      total.value = vocabList.value.length
    }
  } catch (error) {
    console.error('获取失败', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => { fetchVocabularies() })


const changePage = (page) => {
  currentPage.value = page
  fetchVocabularies()
}

const goToDashboard = () => router.push('/dashboard')

const removeVocab = async (id) => {
  if (!confirm('确定移出专属词库吗？')) return
  try {
    await deleteVocabularyApi(id)
    // 删除了要刷新当前页
    fetchVocabularies() 
  } catch (error) {
    alert('移除失败！')
  }
}

const markMastered = async (vocab) => {
  try {
    await updateVocabularyStatusApi(vocab.id, 1)
    vocab.mastered = 1
  } catch (error) {
    alert('状态更新失败！')
  }
}

const showCorrectionModal = ref(false)
const correctionWord = ref('')
const correctionOriginal = ref('')
const correctionSuggestion = ref('')
const currentCorrectionId = ref(null)
const isSubmittingCorrection = ref(false)

const openCorrectionModal = (vocab) => {
  currentCorrectionId.value = vocab.id
  correctionWord.value = vocab.word
  correctionOriginal.value = vocab.translation
  correctionSuggestion.value = vocab.translation
  showCorrectionModal.value = true
}

const closeCorrectionModal = () => {
  showCorrectionModal.value = false
  currentCorrectionId.value = null
}

const submitCorrection = async () => {
  if (!correctionSuggestion.value.trim()) return alert('建议内容不能为空！')
  isSubmittingCorrection.value = true
  try {
    await submitCorrectionApi({
      type: 'VOCABULARY',
      targetId: currentCorrectionId.value,
      originalContent: correctionOriginal.value,
      userSuggestion: correctionSuggestion.value
    })
    alert('纠错提交成功！审核通过后将通过站内信通知您。')
    closeCorrectionModal()
  } catch (error) {
    alert('提交失败，请稍后重试。')
  } finally {
    isSubmittingCorrection.value = false
  }
}
</script>

<style scoped>
.dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #f3f4f6; font-family: -apple-system, sans-serif; overflow: hidden;}

.outline-btn { background: #ffffff; color: #4b5563; border: 1px solid #d1d5db; }

.outline-btn:hover { background: #f9fafb; color: #111827; }

/* Correction Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.4); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: white; width: 90%; max-width: 500px; border-radius: 20px; padding: 24px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.modal-header h2 { font-size: 20px; font-weight: 800; margin: 0; color: #111827; }
.close-modal { background: none; border: none; font-size: 20px; cursor: pointer; color: #9ca3af; }
.correction-tip { font-size: 14px; color: #4b5563; margin-bottom: 12px; }
.correction-textarea { width: 100%; height: 120px; padding: 12px; border-radius: 12px; border: 1px solid #e5e7eb; outline: none; resize: vertical; font-size: 14px; line-height: 1.6; box-sizing: border-box; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.btn-cancel { padding: 8px 20px; background: white; border: 1px solid #e5e7eb; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-submit { padding: 8px 20px; background: #111827; color: white; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; }


.main-content { flex: 1; display: flex; flex-direction: column; padding: 30px 40px; overflow-y: auto; }
.page-header { margin-bottom: 30px; }
.page-title { font-size: 28px; font-weight: 800; color: #111827; margin: 0 0 10px 0; }
.page-subtitle { font-size: 15px; color: #6b7280; margin: 0; }

.status-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 50vh; color: #6b7280; }
.empty-icon { font-size: 60px; margin-bottom: 20px; }
.spinner { width: 40px; height: 40px; border: 4px solid #e5e7eb; border-top: 4px solid #111827; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 20px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.vocab-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; margin-bottom: 30px; flex: 1; align-content: start;}
.vocab-card { background: #ffffff; border-radius: 16px; border: 1px solid #e5e7eb; padding: 20px; display: flex; flex-direction: column; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); transition: transform 0.2s; }
.vocab-card.mastered { opacity: 0.7; background: #f9fafb; border-color: #d1d5db; }
.card-top { margin-bottom: 10px; }
.word { font-size: 24px; font-weight: 800; color: #111827; margin: 0; }
.card-translation { font-size: 15px; color: #4b5563; line-height: 1.5; white-space: pre-wrap; margin-bottom: 15px; flex: 1;}
.card-context { background: #f8fafc; padding: 12px; border-radius: 8px; border-left: 3px solid #cbd5e1; margin-bottom: 15px; }
.context-label { font-size: 12px; color: #64748b; font-weight: bold; margin-bottom: 5px; display: block;}
.context-sentence { font-size: 13.5px; color: #475569; font-style: italic; margin: 0; line-height: 1.5; }
.card-actions { display: flex; gap: 10px; border-top: 1px solid #f3f4f6; padding-top: 15px; }
.action-icon-btn { flex: 1; padding: 8px; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; border: none; }
.check-btn { background: #f0fdf4; color: #16a34a; }
.delete-btn { background: #fef2f2; color: #dc2626; }
.primary-btn { padding: 12px; background: #111827; color: white; border-radius: 8px; border: none; font-weight: 600; cursor: pointer; }

/* 分页器样式 */
.pagination-bar { display: flex; justify-content: center; align-items: center; gap: 20px; padding: 20px 0; border-top: 1px solid #e5e7eb; margin-top: auto;}
.page-btn { padding: 8px 16px; background: #ffffff; border: 1px solid #d1d5db; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s;}
.page-btn:hover:not([disabled]) { background: #f3f4f6; border-color: #111827;}
.page-btn[disabled] { opacity: 0.5; cursor: not-allowed; }
.page-info { font-size: 14px; color: #4b5563; font-weight: 500;}

@media (max-width: 768px) {
  .dashboard-layout {
    flex-direction: column;
    overflow-y: auto;
  }
  .main-content { padding: 20px 15px; padding-bottom: 90px; }
  .vocab-grid { grid-template-columns: 1fr; gap: 15px; }
}
</style>
