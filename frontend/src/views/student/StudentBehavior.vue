<template>
  <div class="student-behavior">
    <el-card>
      <template #header>
        <div class="filter-bar">
          <span>行为记录</span>
          <div>
            <el-select v-model="typeFilter" placeholder="行为类型" clearable style="width: 150px; margin-right: 8px;">
              <el-option label="全部" value="" />
              <el-option label="举手" value="raising_hand" />
              <el-option label="阅读" value="reading" />
              <el-option label="书写" value="writing" />
              <el-option label="玩手机" value="using_phone" />
              <el-option label="低头" value="bowing_head" />
              <el-option label="趴桌" value="leaning_over" />
            </el-select>
            <el-button type="primary" @click="loadData">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="behaviorList" v-loading="loading" stripe>
        <el-table-column prop="behaviorTime" label="时间" width="170" />
        <el-table-column prop="behaviorType" label="行为类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.behaviorType)" size="small">
              {{ typeLabel(row.behaviorType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="90">
          <template #default="{ row }">
            {{ row.confidence ? (row.confidence + '%') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="handled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.handled ? 'success' : 'warning'" size="small">
              {{ row.handled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleRemark" label="处理备注" min-width="200" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBehaviorList } from '@/api/student'

const loading = ref(false)
const behaviorList = ref([])
const total = ref(0)
const typeFilter = ref('')
const query = reactive({ pageNum: 1, pageSize: 10 })

function typeTag(t) {
  const map = {
    raising_hand: 'success', reading: 'primary', writing: '',
    using_phone: 'warning', bowing_head: 'danger', leaning_over: 'danger',
    sleeping: 'info', phone: 'warning', eating: '', talking: 'danger'
  }
  return map[t] || 'info'
}
function typeLabel(t) {
  const map = {
    raising_hand: '举手', reading: '阅读', writing: '书写',
    using_phone: '玩手机', bowing_head: '低头', leaning_over: '趴桌',
    sleeping: '睡觉', phone: '玩手机', eating: '吃东西', talking: '交头接耳'
  }
  return map[t] || t
}

async function loadData() {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (typeFilter.value) params.behaviorType = typeFilter.value
    if (userInfo.userId) params.studentId = userInfo.userId

    const body = await getBehaviorList(params)
    behaviorList.value = body.data?.records || []
    total.value = body.data?.total || 0
  } catch (e) {
    console.error('加载行为记录失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
