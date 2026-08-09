<template>
  <div class="student-credit">
    <ParticleBackground mode="floating" color="#00E5FF" />
    <div class="page-header">
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
      <h2>我的学风分</h2>
    </div>

    <div class="credit-hero" :class="gradeClass">
      <div class="hero-score">{{ creditData.creditScore }}</div>
      <div class="hero-label">学风分</div>
      <div class="hero-grade">等级 {{ grade }}</div>
      <div class="hero-bar-bg">
        <div class="hero-bar-fill" :style="{ width: (creditData.creditScore / 200 * 100) + '%' }"></div>
      </div>
      <div class="hero-range">0 · 80 · 120 · 150 · 180 · 200</div>
    </div>

    <div class="detail-grid">
      <div class="detail-card earned">
        <div class="d-num">+{{ creditData.creditEarned || 0 }}</div>
        <div class="d-label">累计加分</div>
        <div class="d-desc">出勤、积极课堂行为</div>
      </div>
      <div class="detail-card deducted">
        <div class="d-num">-{{ creditData.creditDeducted || 0 }}</div>
        <div class="d-label">累计扣分</div>
        <div class="d-desc">迟到、缺勤、违规行为</div>
      </div>
    </div>

    <el-card shadow="never" class="rules-card">
      <template #header><span style="font-weight:600">评分规则</span></template>
      <div class="rule-section">
        <div class="rule-title">出勤签到</div>
        <div class="rule-item"><span>准时签到</span><span class="rule-plus">+2分</span></div>
        <div class="rule-item"><span>迟到签到</span><span class="rule-minus">-1分</span></div>
        <div class="rule-item"><span>缺勤</span><span class="rule-minus">-3分</span></div>
      </div>
      <div class="rule-section">
        <div class="rule-title">课堂行为</div>
        <div class="rule-item"><span>举手发言/阅读/写作</span><span class="rule-plus">+1分</span></div>
        <div class="rule-item"><span>使用手机</span><span class="rule-minus">-2分</span></div>
        <div class="rule-item"><span>低头/侧身/睡觉</span><span class="rule-minus">-1分</span></div>
      </div>
      <div class="rule-section">
        <div class="rule-title">等级划分</div>
        <div class="rule-item"><span>S 卓越</span><span>180-200分</span></div>
        <div class="rule-item"><span>A 优秀</span><span>150-179分</span></div>
        <div class="rule-item"><span>B 良好</span><span>120-149分</span></div>
        <div class="rule-item"><span>C 一般</span><span>80-119分</span></div>
        <div class="rule-item"><span>D 需改进</span><span>0-79分</span></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCreditScore } from '@/api/student'
import { ArrowLeft } from '@element-plus/icons-vue'
import ParticleBackground from '@/components/sci-fi/ParticleBackground.vue'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const creditData = ref({ creditScore: 100, creditEarned: 0, creditDeducted: 0 })

const grade = computed(() => {
  const s = creditData.value.creditScore
  if (s >= 180) return 'S'
  if (s >= 150) return 'A'
  if (s >= 120) return 'B'
  if (s >= 80) return 'C'
  return 'D'
})

const gradeClass = computed(() => 'grade-' + grade.value.toLowerCase())

onMounted(async () => {
  try {
    const res = await getCreditScore(userInfo.studentId)
    if (res.data) creditData.value = res.data
  } catch (e) {
    console.error('加载学风分失败', e)
  }
})
</script>

<style scoped>
.student-credit { max-width: 600px; margin: 0 auto; padding: 16px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; color: var(--c-text); }

.credit-hero {
  text-align: center; padding: 32px 20px; border-radius: 16px;
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 2px solid var(--c-glass-border);
  box-shadow: var(--c-glass-shadow);
  margin-bottom: 16px;
}
.credit-hero.grade-s { border-color: #FFD700; background: linear-gradient(135deg, rgba(255,215,0,0.1), var(--c-glass-bg)); }
.credit-hero.grade-a { border-color: #67C23A; }
.credit-hero.grade-b { border-color: #409EFF; }
.credit-hero.grade-c { border-color: #E6A23C; }
.credit-hero.grade-d { border-color: #F56C6C; }
.hero-score { font-size: 64px; font-weight: 800; color: var(--c-text); line-height: 1; }
.hero-label { font-size: 14px; color: var(--c-text-tertiary); margin-top: 4px; }
.hero-grade { font-size: 20px; font-weight: 700; color: var(--c-primary); margin-top: 8px; }
.hero-bar-bg {
  height: 6px; background: var(--c-border-light); border-radius: 3px;
  margin: 16px auto 4px; max-width: 300px; overflow: hidden;
}
.hero-bar-fill {
  height: 100%; background: linear-gradient(90deg, #F56C6C, #E6A23C, #409EFF, #67C23A, #FFD700);
  border-radius: 3px; transition: width 0.6s ease;
}
.hero-range { font-size: 11px; color: var(--c-text-tertiary); max-width: 300px; margin: 0 auto; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 16px; }
.detail-card {
  text-align: center; padding: 20px; border-radius: 12px;
  background: var(--c-glass-bg);
  backdrop-filter: blur(8px) saturate(180%);
  -webkit-backdrop-filter: blur(8px) saturate(180%);
  border: 1px solid var(--c-glass-border);
}
.detail-card.earned { border-left: 4px solid var(--c-success); }
.detail-card.deducted { border-left: 4px solid var(--c-danger); }
.d-num { font-size: 28px; font-weight: 700; }
.earned .d-num { color: var(--c-success); }
.deducted .d-num { color: var(--c-danger); }
.d-label { font-size: 13px; color: var(--c-text-secondary); margin-top: 2px; }
.d-desc { font-size: 11px; color: var(--c-text-tertiary); margin-top: 4px; }

.rules-card { margin-bottom: 16px; }
.rule-section { margin-bottom: 16px; }
.rule-section:last-child { margin-bottom: 0; }
.rule-title { font-size: 14px; font-weight: 600; color: var(--c-text); margin-bottom: 8px; }
.rule-item { display: flex; justify-content: space-between; font-size: 13px; color: var(--c-text-secondary); padding: 4px 0; }
.rule-plus { color: var(--c-success); font-weight: 600; }
.rule-minus { color: var(--c-danger); font-weight: 600; }

:deep(.el-card) {
  background: var(--c-glass-bg);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border: 1px solid var(--c-glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--c-glass-shadow);
}
:deep(.el-card__header) { border-bottom: 1px solid var(--c-border-light); }

@media (max-width: 768px) {
  .student-credit { padding: 12px; }
  .hero-score { font-size: 48px; }
  .credit-hero { padding: 24px 16px; }
}
</style>
