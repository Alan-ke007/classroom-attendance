<template>
  <view class="page">
    <view class="filter-bar">
      <picker mode="selector" :range="typeOpts" @change="onTypeChange">
        <view class="filter-chip">{{ typeOpts[typeIdx] }} ▼</view>
      </picker>
    </view>

    <view v-if="list.length === 0 && !loading" class="empty">
      <text class="empty-icon">📊</text>
      <text class="empty-text">暂无行为记录</text>
    </view>

    <view v-for="item in list" :key="item.id" class="b-item">
      <view class="bi-head">
        <text class="bi-type" :class="'bt-'+item.behaviorType">{{ typeMap[item.behaviorType] || item.behaviorType }}</text>
        <text class="bi-time">{{ fmtTime(item.behaviorTime) }}</text>
      </view>
      <view class="bi-row">
        <text class="bir-label">置信度</text>
        <text class="bir-value">{{ item.confidence ? (item.confidence * 100).toFixed(1) + '%' : '--' }}</text>
      </view>
      <view class="bi-row">
        <text class="bir-label">状态</text>
        <text class="bir-value" :class="item.handled === 1 ? 'c-green' : 'c-orange'">
          {{ item.handled === 1 ? '已处理' : '未处理' }}
        </text>
      </view>
      <text v-if="item.handleRemark" class="bi-remark">备注：{{ item.handleRemark }}</text>
    </view>

    <view v-if="loading" class="load-tip">加载中...</view>
    <view v-if="!hasMore && list.length > 0" class="no-more">— 没有更多了 —</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { getBehaviorList } from '@/api/behavior'

const typeOpts = ['全部', '举手', '阅读', '书写', '玩手机', '低头', '趴桌', '睡觉', '吃东西', '讲话']
const typeValues = ['', 'raising_hand', 'reading', 'writing', 'using_phone', 'bowing_head', 'leaning_over', 'sleeping', 'eating', 'talking']
const typeMap = {
  raising_hand: '举手', reading: '阅读', writing: '书写',
  using_phone: '玩手机', bowing_head: '低头', leaning_over: '趴桌',
  sleeping: '睡觉', phone: '玩手机', eating: '吃东西', talking: '讲话'
}

const typeIdx = ref(0)
const list = ref([])
const pageNum = ref(1)
const hasMore = ref(true)
const loading = ref(false)

function fmtTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '' }

function onTypeChange(e) { typeIdx.value = e.detail.value; loadData(true) }

async function loadData(reset = false) {
  if (loading.value) return
  if (reset) { pageNum.value = 1; list.value = []; hasMore.value = true }
  if (!hasMore.value) return
  loading.value = true
  try {
    const params = {}
    const t = typeValues[typeIdx.value]
    if (t) params.behaviorType = t
    const res = await getBehaviorList({ pageNum: pageNum.value, pageSize: 20, ...params })
    const records = res.records || []
    if (pageNum.value === 1) list.value = records
    else list.value = [...list.value, ...records]
    hasMore.value = records.length >= 20
    pageNum.value++
  } catch (e) { console.error('加载行为记录失败', e) }
  finally { loading.value = false }
}

onMounted(() => loadData(true))
onReachBottom(() => loadData())
onPullDownRefresh(() => { loadData(true).finally(() => uni.stopPullDownRefresh()) })
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.filter-bar { display: flex; padding: 16rpx 20rpx; background: #fff; }
.filter-chip {
  background: #f5f5f5; padding: 10rpx 24rpx; border-radius: 30rpx;
  font-size: 26rpx; color: #606266;
}

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 64rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #c0c4cc; }

.b-item {
  background: #fff; margin: 12rpx 20rpx; border-radius: 16rpx; padding: 24rpx;
}
.bi-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.bi-type { font-size: 26rpx; font-weight: 600; padding: 6rpx 16rpx; border-radius: 12rpx; }
.bt-raising_hand, .bt-reading, .bt-writing { background: #ecf5ff; color: #409EFF; }
.bt-using_phone, .bt-phone { background: #fdf6ec; color: #E6A23C; }
.bt-bowing_head, .bt-leaning_over, .bt-sleeping { background: #fef0f0; color: #F56C6C; }
.bt-eating, .bt-talking { background: #f4f4f5; color: #909399; }
.bi-time { font-size: 24rpx; color: #909399; }

.bi-row { display: flex; justify-content: space-between; padding: 6rpx 0; }
.bir-label { font-size: 26rpx; color: #909399; }
.bir-value { font-size: 26rpx; color: #606266; font-weight: 500; }
.bi-remark { font-size: 24rpx; color: #909399; margin-top: 8rpx; display: block; }
.c-green { color: #67C23A; }
.c-orange { color: #E6A23C; }

.load-tip { text-align: center; padding: 24rpx; color: #909399; font-size: 26rpx; }
.no-more { text-align: center; padding: 24rpx; color: #c0c4cc; font-size: 24rpx; }
</style>
