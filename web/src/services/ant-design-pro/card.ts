import { request } from '@umijs/max';

/** 顾客持卡（次卡）：itemName/customerName 为列表联出展示字段 */
export interface Card {
  id?: number;
  customerId?: number;
  /** 列表联出的展示字段 */
  customerName?: string;
  itemId?: number;
  /** 列表联出的展示字段 */
  itemName?: string;
  totalTimes?: number;
  remainingTimes?: number;
  /** 1 有效 / 0 停用 */
  status?: number;
  sourceOrderId?: number;
  createTime?: string;
}

/** 发卡：customerId + itemId + totalTimes（≥1）必填；余次=总次数、状态有效 */
export async function issueCard(
  body: { customerId: number; itemId: number; totalTimes: number; sourceOrderId?: number },
  options?: { [key: string]: any },
) {
  return request<Card>('/api/v1/card/', {
    method: 'POST',
    data: body,
    ...(options || {}),
  });
}

/** 按顾客查持卡（新卡在前） */
export async function listCardsByCustomer(
  customerId: number,
  options?: { [key: string]: any },
) {
  return request<Card[]>(`/api/v1/card/list?customerId=${customerId}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 停用 / 恢复：status 1 有效 / 0 停用 */
export async function setCardStatus(
  id: number,
  status: number,
  options?: { [key: string]: any },
) {
  return request<Card>(`/api/v1/card/${id}/status?status=${status}`, {
    method: 'PUT',
    ...(options || {}),
  });
}
