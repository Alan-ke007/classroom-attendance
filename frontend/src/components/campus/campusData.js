// 3D 校园建筑数据 — Brutalist Terminal palette
export const buildings = [
  {
    id: 1,
    name: '教学楼A',
    position: [-7, 0, -5],
    size: [3.5, 3.0, 3.0],
    color: '#00E5FF',
    attendanceRate: 0.95
  },
  {
    id: 2,
    name: '教学楼B',
    position: [0, 0, -5],
    size: [3.5, 3.5, 3.0],
    color: '#E040FB',
    attendanceRate: 0.88
  },
  {
    id: 3,
    name: '教学楼C',
    position: [7, 0, -5],
    size: [3.5, 2.5, 3.0],
    color: '#FFD600',
    attendanceRate: 0.72
  },
  {
    id: 4,
    name: '图书馆',
    position: [-5, 0, 4],
    size: [4.5, 4.5, 3.5],
    color: '#00E676',
    attendanceRate: 0.91
  },
  {
    id: 5,
    name: '实验楼',
    position: [5, 0, 4],
    size: [3.5, 3.2, 3.5],
    color: '#FF1744',
    attendanceRate: 0.65
  },
  {
    id: 6,
    name: '行政楼',
    position: [0, 0, 0],
    size: [2.5, 2.0, 2.5],
    color: '#B0BEC5',
    attendanceRate: 0.85
  }
]

// 粒子流路径: from/to 指向 buildings 数组索引
export const particlePaths = [
  { from: 0, to: 3, count: 20 },
  { from: 1, to: 3, count: 15 },
  { from: 2, to: 4, count: 25 },
  { from: 0, to: 5, count: 10 },
  { from: 1, to: 5, count: 12 },
  { from: 3, to: 5, count: 8 }
]

// 出勤率 → 颜色映射
export function getRateColor(rate) {
  if (rate >= 0.9) return '#00E676'
  if (rate >= 0.8) return '#00E5FF'
  if (rate >= 0.7) return '#FFD600'
  return '#FF1744'
}
