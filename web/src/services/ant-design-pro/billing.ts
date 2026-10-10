import { request } from '@umijs/max';

/** 支付方式：1 储值 / 2 微信 / 3 支付宝 / 4 现金 / 5 次卡抵扣（微信/支付宝为演示口径：仅记录方式） */
export const PAY_TYPE = {
  STORED_VALUE: 1,
  WECHAT: 2,
  ALIPAY: 3,
  CASH: 4,
  CARD: 5,
} as const;

export const PAY_TYPE_TEXT: Record<number, string> = {
  1: '储值卡',
  2: '微信',
  3: '支付宝',
  4: '现金',
  5: '次卡',
};

export interface Recharge {
  id?: number;
  customerId?: number;
  /** 列表联出的展示字段 */
  customerName?: string;
  /** 金额（元，充值为正、储值支付为负） */
  money?: number;
  createTime?: string;
}

export interface RechargeBalance {
  customerId?: number;
  /** 储值余额（元，流水合计，可为 0） */
  balance?: number;
}

export interface Settlement {
  id?: number;
  orderId?: number;
  customerId?: number;
  /** 列表联出的展示字段 */
  customerName?: string;
  payType?: number;
  /** 实收金额（元） */
  money?: number;
  createTime?: string;
}

export interface BillingPageResult<T> {
  data?: T[];
  total?: number;
  pages?: number;
  success?: boolean;
}

/** 储值充值：落一条正数流水 */
export async function createRecharge(
  body: { customerId: number; money: number },
  options?: { [key: string]: any },
) {
  return request<Recharge>('/api/v1/recharge/', {
    method: 'POST',
    data: body,
    ...(options || {}),
  });
}

/** 储值余额 = recharges 流水合计 */
export async function fetchRechargeBalance(
  customerId: number,
  options?: { [key: string]: any },
) {
  return request<RechargeBalance>('/api/v1/recharge/balance', {
    method: 'GET',
    params: { customerId },
    ...(options || {}),
  });
}

/** 储值流水分页；金额为负的行是储值支付扣减 */
export async function queryRechargePage(
  params: { current?: number; pageSize?: number; customerId?: number },
  options?: { [key: string]: any },
) {
  return request<BillingPageResult<Recharge>>('/api/v1/recharge/page', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

/** 收款结算：订单 0/1 → 2 已完成，一单一结算；储值支付余额不足报 400 */
export async function settleOrder(
  body: { orderId: number; payType: number },
  options?: { [key: string]: any },
) {
  return request<Settlement>('/api/v1/settlement/', {
    method: 'POST',
    data: body,
    ...(options || {}),
  });
}

/** 结算单分页（时间倒序） */
export async function querySettlementPage(
  params: { current?: number; pageSize?: number },
  options?: { [key: string]: any },
) {
  return request<BillingPageResult<Settlement>>('/api/v1/settlement/page', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}

/** 结算小票打印地址：80mm HTML（服务端按结算单填充，浏览器打印即可） */
export const receiptPrintUrl = (settlementId: number) => `/api/v1/print/receipt/${settlementId}`;
