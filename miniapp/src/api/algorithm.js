// 算法服务直连（已废弃，P1）
//
// 小程序不再直连算法服务。原 recognizeFace / detectBehavior / checkAlgorithmHealth
// 均直连算法服务，已按 NF1 全部移除（算法服务直连地址与密钥仅保留在后端）。
// 现有人脸能力统一经后端中转（建档/核验见 src/api/face.js，行为检测/健康检查待后端代理补齐）。
//
// NF1：小程序不持有算法服务密钥，不直连算法服务。
export {}
