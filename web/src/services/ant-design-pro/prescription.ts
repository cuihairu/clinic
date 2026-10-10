import { request } from '@umijs/max';

/** 药味：药材名为自由文本（MVP 无药材字典/库存），weight 为单剂克数 */
export interface PrescriptionHerb {
  id?: number;
  herb?: string;
  weight?: number;
  /** 特殊煎法：先煎/后下/包煎等，可空 */
  special?: string;
  sort?: number;
}

export interface Prescription {
  id?: number;
  treatId?: number;
  customerId?: number;
  /** 列表联出的展示字段 */
  customerName?: string;
  staffId?: number;
  /** 列表联出的展示字段 */
  staffName?: string;
  /** 剂数（几付） */
  doses?: number;
  /** 煎服法/频次/代煎说明等 */
  usage?: string;
  remark?: string;
  /** 详情/列表联出的药味（按 sort 排序） */
  herbs?: PrescriptionHerb[];
  createTime?: string;
}

export interface PrescriptionPageResult {
  data?: Prescription[];
  total?: number;
  pages?: number;
  success?: boolean;
}

/** 开方：customerId + herbs（至少 1 味）必填；doses 默认 7；服务端校验顾客与剂量 */
export async function createPrescription(
  body: {
    customerId: number;
    staffId?: number;
    treatId?: number;
    doses?: number;
    usage?: string;
    remark?: string;
    herbs: PrescriptionHerb[];
  },
  options?: { [key: string]: any },
) {
  return request<Prescription>('/api/v1/prescription/', {
    method: 'POST',
    data: body,
    ...(options || {}),
  });
}

/** 处方分页（id 倒序，联出顾客/医师/药味）；customerId 可选过滤 */
export async function queryPrescriptionPage(
  params: { current?: number; pageSize?: number; customerId?: number },
  options?: { [key: string]: any },
) {
  return request<PrescriptionPageResult>('/api/v1/prescription/page', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

/** 处方详情（含药味） */
export async function fetchPrescription(id: number, options?: { [key: string]: any }) {
  return request<Prescription>(`/api/v1/prescription/${id}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 删除处方（连同药味；演示环境口径，无留痕） */
export async function deletePrescription(id: number, options?: { [key: string]: any }) {
  return request<{ message?: string }>(`/api/v1/prescription/${id}`, {
    method: 'DELETE',
    ...(options || {}),
  });
}

/** 配伍审方命中条目：level 禁忌（十八反）/ 慎用（十九畏） */
export interface CompatibilityFinding {
  level?: string;
  rule?: string;
  a?: string;
  b?: string;
  note?: string;
}

export interface CompatibilityResult {
  /** 参与比对的药材数 */
  checked?: number;
  /** 空即未发现配伍禁忌 */
  findings?: CompatibilityFinding[];
}

/** 配伍审方：经典十八反/十九畏静态规则；自由文本药名按别名包含匹配；提示不拦截 */
export async function checkCompatibility(
  herbs: string[],
  options?: { [key: string]: any },
) {
  return request<CompatibilityResult>('/api/v1/prescription/compatibility', {
    method: 'POST',
    data: { herbs },
    ...(options || {}),
  });
}
