import { request } from '@umijs/max';

/** 药材字典条目：处方计价的比价依据；price 为每克价格（分） */
export interface Herb {
  id?: number;
  /** 唯一，与处方药名精确比对 */
  name?: string;
  /** 每克价格（分），>0 */
  price?: number;
  createTime?: string;
}

export interface HerbPageResult {
  data?: Herb[];
  total?: number;
  pages?: number;
  success?: boolean;
}

/** 收录药材（name 唯一，price 每克分价 >0） */
export async function createHerb(
  body: { name: string; price: number },
  options?: { [key: string]: any },
) {
  return request<Herb>('/api/v1/herb/', {
    method: 'POST',
    data: body,
    ...(options || {}),
  });
}

/** 更新药材（改价/改名，名称查重不含自身） */
export async function updateHerb(
  body: { id: number; name: string; price: number },
  options?: { [key: string]: any },
) {
  return request<Herb>('/api/v1/herb/', {
    method: 'PUT',
    data: body,
    ...(options || {}),
  });
}

/** 删除药材（删后相关处方药味转「未比价」） */
export async function deleteHerb(id: number, options?: { [key: string]: any }) {
  return request<{ message?: string }>(`/api/v1/herb/${id}`, {
    method: 'DELETE',
    ...(options || {}),
  });
}

/** 字典分页；keyword 非空按名称包含过滤，名称升序 */
export async function queryHerbByPage(
  params: { current?: number; pageSize?: number; keyword?: string },
  options?: { [key: string]: any },
) {
  return request<HerbPageResult>('/api/v1/herb/page', {
    method: 'GET',
    params,
    ...(options || {}),
  });
}
