<template>
  <view class="page">
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="搜索姓名或学号" @confirm="doSearch" />
      <button class="search-btn" @tap="doSearch">搜索</button>
    </view>

    <view class="class-filter">
      <scroll-view scroll-x class="class-scroll">
        <text class="cls-tag" :class="{ active: selectedClassId === '' }" @tap="selectedClassId = ''">全部</text>
        <text
          v-for="cls in classes" :key="cls.id"
          class="cls-tag" :class="{ active: selectedClassId === cls.id }"
          @tap="selectedClassId = cls.id"
        >{{ cls.className }}</text>
      </scroll-view>
    </view>

    <view v-if="filteredList.length === 0" class="empty">暂无学生数据</view>

    <view v-for="item in filteredList" :key="item.id" class="s-item">
      <view class="si-avatar">{{ (item.name || '?')[0] }}</view>
      <view class="si-body">
        <text class="si-name">{{ item.name }}</text>
        <text class="si-no">{{ item.studentNo }}</text>
      </view>
      <view class="si-meta">
        <text class="si-class">{{ item.className || '未分班' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStudentList } from '@/api/student'
import { getAllClasses } from '@/api/class'

const list = ref([])
const classes = ref([])
const keyword = ref('')
const selectedClassId = ref('')

const filteredList = computed(() => {
  let result = list.value
  if (selectedClassId.value) result = result.filter(s => s.classId === selectedClassId.value)
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(s => s.name?.toLowerCase().includes(kw) || s.studentNo?.includes(kw))
  }
  return result
})

function doSearch() {}

async function loadData() {
  try {
    const res = await getStudentList({ pageNum: 1, pageSize: 200 })
    list.value = res.records || []
  } catch (e) { console.error('加载学生列表失败', e) }
}

async function loadClasses() {
  try {
    const data = await getAllClasses()
    classes.value = data || []
  } catch (e) { console.error('加载班级列表失败', e) }
}

onMounted(() => { loadClasses(); loadData() })
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; padding-bottom: 20rpx; }

.search-bar { display: flex; padding: 16rpx 20rpx; background: #fff; border-bottom: 1rpx solid #f5f5f5; }
.search-input {
  flex: 1; height: 72rpx; border: 1rpx solid #ebeef5; border-radius: 36rpx;
  padding: 0 24rpx; font-size: 28rpx; background: #f8f9fb;
}
.search-btn {
  margin-left: 12rpx; background: #4A90D9; color: #fff;
  border: none; border-radius: 36rpx; font-size: 26rpx; padding: 0 28rpx; line-height: 72rpx;
}

.class-filter { background: #fff; padding: 12rpx 0; }
.class-scroll { white-space: nowrap; padding: 0 16rpx; }
.cls-tag {
  display: inline-block; padding: 8rpx 22rpx; margin: 0 6rpx;
  font-size: 26rpx; border-radius: 20rpx; color: #606266; background: #f5f5f5;
}
.cls-tag.active { background: #4A90D9; color: #fff; }

.empty { text-align: center; padding: 120rpx 0; color: #c0c4cc; font-size: 28rpx; }

.s-item {
  display: flex; align-items: center; background: #fff;
  margin: 8rpx 20rpx; padding: 24rpx; border-radius: 14rpx;
}
.si-avatar {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: linear-gradient(135deg, #4A90D9, #357ABD);
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 32rpx; font-weight: 700; margin-right: 20rpx; flex-shrink: 0;
}
.si-body { flex: 1; }
.si-name { font-size: 30rpx; font-weight: 500; color: #303133; display: block; }
.si-no { font-size: 24rpx; color: #909399; margin-top: 4rpx; display: block; }
.si-meta { margin-left: 16rpx; }
.si-class { font-size: 24rpx; color: #606266; background: #f5f5f5; padding: 6rpx 16rpx; border-radius: 10rpx; }
</style>
