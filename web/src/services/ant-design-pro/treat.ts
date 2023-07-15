import { request } from '@umijs/max';

export async function createTreat(body: API.Treat,options?: { [key: string]: any }) {
  return request<API.Treat>('/api/v1/treat/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

export async function fetchTreatById(id:number|string,options?: { [key: string]: any }) {
  return request<API.Treat>(`/api/v1/treat/${id}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}
export async function deleteTreat(id: number,options?: { [key: string]: any }) {
  return request<API.MessageResp>(`/api/v1/treat/${id}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}
export async function queryTreatByPage(params: API.QueryTreatParams,options?: { [key: string]: any }) {
  return request<API.QueryTreatResult>('/api/v1/treat/history', {
    method: 'GET',
    params: params,
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}

export async function fuzzyQueryTreatByPage(params: API.QueryTreatParams,options?: { [key: string]: any }) {
  return request<API.QueryTreatResult>('/api/v1/treat/fuzzy', {
    method: 'GET',
    params: params,
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}
