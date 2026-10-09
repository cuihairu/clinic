import { request } from '@umijs/max';

/** 订单状态：0 已下单、1 已确认（接待中）、2 已完成、9 已取消 */
export const ORDER_STATUS = {
  CREATED: 0,
  CONFIRMED: 1,
  DONE: 2,
  CANCELLED: 9,
} as const;

export interface Order {
  id?: number;
  customerId?: number;
  /** 列表联出的展示字段 */
  customerName?: string;
  customerPhone?: string;
  itemId?: number;
  itemName?: string;
  /** 接待员工 id，Kiosk 单为空 */
  staffId?: number | null;
  status?: number;
  /** 成交价（元，下单时刻卡项价格快照） */
  price?: number;
  /** 支付方式（结算联出）：1 储值 / 2 微信 / 3 支付宝 / 4 现金，未结算为 null */
  payType?: number;
  createTime?: string;
  updateTime?: string;
}

export interface OrderPageResult {
  data?: Order[];
  total?: number;
  pages?: number;
  success?: boolean;
}

/** 顾客消费汇总（顾客档案「累计消费」统计卡）：只统计已完成订单 */
export interface OrderSummary {
  orders?: number;
  /** 累计消费金额（元，已完成订单价格合计） */
  amount?: number;
}

export async function queryOrderPage(
  params: { current?: number; pageSize?: number; status?: number; pending?: boolean },
  options?: { [key: string]: any },
) {
  return request<OrderPageResult>('/api/v1/order/page', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

export async function fetchOrder(id: number, options?: { [key: string]: any }) {
  return request<Order>(`/api/v1/order/${id}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 按顾客汇总消费：累计消费=已完成订单价格合计 */
export async function fetchOrderSummary(
  customerId: number,
  options?: { [key: string]: any },
) {
  return request<OrderSummary>('/api/v1/order/summary', {
    method: 'GET',
    params: { customerId },
    ...(options || {}),
  });
}

/** 状态流转：0→1 接单、1→2 完成、0/1→9 取消（服务端校验非法流转） */
export async function updateOrderStatus(
  body: { id: number; status: number },
  options?: { [key: string]: any },
) {
  return request<Order>('/api/v1/order/status', {
    method: 'PUT',
    data: body,
    ...(options || {}),
  });
}

export async function deleteOrder(id: number, options?: { [key: string]: any }) {
  return request<{ message?: string }>(`/api/v1/order/${id}`, {
    method: 'DELETE',
    ...(options || {}),
  });
}
