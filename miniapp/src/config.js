// 全局环境配置（外置，禁止硬编码内网 IP / 明文口令）
// 取值优先级：构建变量 import.meta.env.VITE_* > process.env.* > 默认兜底（https 公网域名）
//
// uni-app + Vite 最佳实践：
//   1. 通过 .env 文件或构建命令注入，例如 .env.production：
//        VITE_BASE_URL=https://api.your-domain.com/api
//        VITE_ALGORITHM_BASE_URL=https://algo.your-domain.com
//   2. 不同平台（H5 / 小程序）也可配合条件编译区分，见 request.js / algorithm.js
//   3. 真实域名与内网地址差异一律走构建变量，禁止写死内网 IP（如 172.x / 10.x / 192.168.x）

const env = (typeof import.meta !== 'undefined' && import.meta.env) || {}
const processEnv = (typeof process !== 'undefined' && process.env) || {}

function readEnv(key, fallback) {
  if (env[key] !== undefined && env[key] !== '') return env[key]
  if (processEnv[key] !== undefined && processEnv[key] !== '') return processEnv[key]
  return fallback
}

// 主后端接口地址（默认 https 公网域名，请替换为真实业务域名；务必 https）
// 协议必须是 https，禁止 http / 内网 IP。
export const BASE_URL = readEnv(
  'VITE_BASE_URL',
  'https://api.example.com/api'
)

// 算法服务（人脸识别 / 行为检测）地址
// 注意：生产环境应由后端中转鉴权，前端不应直连算法服务（P1 整改项）
export const ALGORITHM_BASE_URL = readEnv(
  'VITE_ALGORITHM_BASE_URL',
  'https://algo.example.com'
)
