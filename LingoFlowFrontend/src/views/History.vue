<template>
  <div class="dashboard-layout">
    <MobileHeader />
    <Sidebar />

    <div class="main-content">
      <div class="page-header">
        <div class="title-area">
          <h1 class="page-title">阅读历史</h1>
          <p class="page-subtitle">温故而知新。你一共生成了 <strong>{{ total }}</strong> 篇专属文章。</p>
        </div>
      </div>

      <div v-if="isLoading" class="status-state">
        <div class="spinner"></div>
        <p>正在翻阅历史档案...</p>
      </div>

      <div v-else-if="articleList.length === 0" class="status-state empty-state">
        <div class="empty-icon">📭</div>
        <h2>暂无阅读记录</h2>
        <button class="primary-btn" style="width: auto; padding: 10px 24px; margin-top: 15px;" @click="goToDashboard">去工作台</button>
      </div>

      <div v-else style="display: flex; flex-direction: column; flex: 1;">
        <div class="history-list">
          <div v-for="article in articleList" :key="article.id" class="history-card">
            
            <div class="card-header">
              <div class="badge-group">
                <div class="header-info-wrapper">
                   <span class="time-text">{{ formatDate(article.createTime || article.createdAt) }}</span>
                   <span class="difficulty-badge">{{ article.difficultyLevel || '标准难度' }}</span>
                </div>
              </div>
              <div class="card-top-actions">
                <button class="resume-btn desktop-only" @click="resumeLearning(article)">继续学习</button>
                <button class="delete-icon-btn" title="删除记录" @click="removeArticle(article.id)">🗑️</button>
              </div>
            </div>

            <div class="text-comparison">
              <div class="text-box">
                <div class="box-label">原文</div>
                <p class="text-content">{{ article.originalText }}</p>
              </div>
              <div class="text-box">
                <div class="box-label">i+1 改写版</div>
                <p class="text-content">{{ article.adaptedText }}</p>
              </div>
            </div>

            <div class="card-footer">
              <button class="resume-btn small" @click="resumeLearning(article)">继续学习</button>
              <button class="correction-btn-small" @click="openCorrectionModal(article)">内容纠错</button>
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
          <h2>文章改写纠错</h2>
          <button class="close-modal" @click="closeCorrectionModal">✕</button>
        </div>
        <div class="modal-body">
          <p class="correction-tip">您对这篇改写文章的哪些地方不满意？请直接修改：</p>
          <textarea v-model="correctionSuggestion" class="correction-textarea" placeholder="请输入修改后的文章内容..."></textarea>
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
import { getArticleListApi, deleteArticleApi } from '../api/article'
import { submitCorrectionApi } from '../api/correction'

const router = useRouter()
const isLoading = ref(true)
const articleList = ref([])

// 分页状态
const currentPage = ref(1)
const pageSize = ref(5) // 历史文章比较长，一页显示 5 篇即可
const total = ref(0)
const totalPages = ref(1)

const fetchArticles = async () => {
  isLoading.value = true
  try {
    const res = await getArticleListApi(currentPage.value, pageSize.value)
    if (res.records) {
      articleList.value = res.records
      total.value = res.total
      totalPages.value = Math.ceil(res.total / pageSize.value)
    } else {
      articleList.value = res || []
      total.value = articleList.value.length
    }
  } catch (error) {
    console.error('获取失败', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => { fetchArticles() })


const changePage = (page) => {
  currentPage.value = page
  fetchArticles()
}

const goToDashboard = () => router.push('/dashboard')

const resumeLearning = (article) => {
  localStorage.setItem('lingoflow_resume_article', JSON.stringify(article))
  router.push('/dashboard')
}

const removeArticle = async (id) => {
  if (!confirm('确定永久删除这条记录吗？')) return
  try {
    await deleteArticleApi(id)
    fetchArticles() // 刷新当前页数据
  } catch (error) {
    alert('删除失败！')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间'
  return dateStr.replace('T', ' ').substring(0, 16)
}

// 纠错弹窗逻辑
const showCorrectionModal = ref(false)
const correctionOriginal = ref('')
const correctionSuggestion = ref('')
const currentCorrectionId = ref(null)
const isSubmittingCorrection = ref(false)

const openCorrectionModal = (article) => {
  currentCorrectionId.value = article.id
  correctionOriginal.value = article.adaptedText
  correctionSuggestion.value = article.adaptedText
  showCorrectionModal.value = true
}

const closeCorrectionModal = () => {
  showCorrectionModal.value = false
  currentCorrectionId.value = null
}

const submitCorrection = async () => {
  if (!correctionSuggestion.value.trim()) return alert('建议内容不能为空！')
  if (correctionSuggestion.value === correctionOriginal.value) return alert('请先修改内容再提交！')
  isSubmittingCorrection.value = true
  try {
    await submitCorrectionApi({
      type: 'ARTICLE',
      targetId: currentCorrectionId.value,
      originalContent: correctionOriginal.value,
      userSuggestion: correctionSuggestion.value
    })
    alert('文章纠错提交成功！审核通过后将通过站内信通知您。')
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

.outline-btn:hover { background: #f9fafb !important; color: #111827 !important; }

/* Correction Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.4); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: white; width: 90%; max-width: 600px; border-radius: 20px; padding: 24px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.modal-header h2 { font-size: 20px; font-weight: 800; margin: 0; color: #111827; }
.close-modal { background: none; border: none; font-size: 20px; cursor: pointer; color: #9ca3af; }
.correction-tip { font-size: 14px; color: #4b5563; margin-bottom: 12px; }
.correction-textarea { width: 100%; height: 200px; padding: 12px; border-radius: 12px; border: 1px solid #e5e7eb; outline: none; resize: vertical; font-size: 14px; line-height: 1.6; box-sizing: border-box; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.btn-cancel { padding: 8px 20px; background: white; border: 1px solid #e5e7eb; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-submit { padding: 8px 20px; background: #111827; color: white; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; }


.main-content { flex: 1; display: flex; flex-direction: column; padding: 40px 60px; overflow-y: auto; }
.page-header { margin-bottom: 40px; }
.page-title { font-size: 32px; font-weight: 800; color: #111827; margin: 0 0 12px 0; }
.page-subtitle { font-size: 16px; color: #6b7280; margin: 0; }

.status-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 50vh; color: #6b7280; }
.empty-icon { font-size: 60px; margin-bottom: 20px; }
.spinner { width: 40px; height: 40px; border: 4px solid #e5e7eb; border-top: 4px solid #111827; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 20px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.history-list { display: flex; flex-direction: column; gap: 24px; margin-bottom: 30px; flex: 1; }
.history-card { background: #ffffff; border-radius: 16px; border: 1px solid #e5e7eb; padding: 24px; display: flex; flex-direction: column; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; border-bottom: 1px solid #f3f4f6; padding-bottom: 20px;}
.badge-group { display: flex; align-items: center; gap: 15px; }
.header-info-wrapper { display: flex; align-items: center; gap: 15px; }
.difficulty-badge { background: #111827; color: #ffffff; padding: 6px 12px; border-radius: 8px; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px;}
.time-text { font-size: 13px; color: #9ca3af; font-weight: 500; }

.card-top-actions { display: flex; align-items: center; gap: 12px; }
.resume-btn { background: #111827; color: white; border: none; padding: 8px 16px; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.resume-btn:hover { background: #374151; transform: translateY(-1px); }

.delete-icon-btn { background: none; border: none; cursor: pointer; font-size: 18px; opacity: 0.4; transition: 0.2s; padding: 5px;}
.delete-icon-btn:hover { opacity: 1; transform: scale(1.2) rotate(5deg);}

.text-comparison { display: flex; gap: 20px; }
.text-box { flex: 1; display: flex; flex-direction: column; width: 50%;}
.highlight-box { background: #fafafa; border-radius: 8px; padding: 15px; border: 1px solid #f3f4f6;}
.box-label { font-size: 13px; font-weight: 700; color: #9ca3af; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
.text-content { font-size: 16px; color: #1f2937; line-height: 1.7; margin: 0; white-space: pre-wrap; font-family: 'Inter', -apple-system, sans-serif;}

.card-footer { display: flex; justify-content: flex-end; margin-top: 20px; padding-top: 15px; border-top: 1px dashed #e5e7eb; }
.resume-btn.small { display: none; }
.correction-btn-small { background: transparent; color: #9ca3af; border: 1px solid #e5e7eb; padding: 6px 12px; border-radius: 8px; font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.correction-btn-small:hover { color: #111827; border-color: #111827; background: #f9fafb; }

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
  .text-comparison { flex-direction: column; gap: 20px; }
  .text-box { width: 100%; }

  .desktop-only { display: none !important; }
  
  .header-info-wrapper {
    display: flex;
    flex-direction: column;
    gap: 6px;
    align-items: flex-start;
  }

  .card-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
  }

  .difficulty-badge {
    padding: 4px 8px;
    font-size: 11px;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .resume-btn.small {
    display: block !important;
    padding: 6px 12px;
    font-size: 12px;
  }

  .correction-btn-small {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
