import { request } from '@umijs/max';

export async function createOrder(body: API.Staff,options?: { [key: string]: any }) {
  return request<API.Staff>('/api/v1/staff/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
