import { request } from '@umijs/max';

export async function createCustomer(body: API.Customer,options?: { [key: string]: any }) {
  return request<API.Customer>('/api/v1/customer/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

export async function updateCustomer(body: API.Customer,options?: { [key: string]: any }) {
  return request<API.Customer>('/api/v1/customer/', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
export async function deleteCustomer( cid: number ,options?: { [key: string]: any }) {
  return request<API.Customer>(`/api/v1/customer/${cid}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}
export async function queryCustomerByPage(params: API.QueryCustomerParams,options?: { [key: string]: any }) {
  return request<API.QueryCustomerResult>('/api/v1/customer/page', {
    method: 'GET',
    params: params,
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}

export async function fetchCustomerById(cid:number|string,options?: { [key: string]: any }) {
  return request<API.Customer>(`/api/v1/customer/${cid}`, {
    method: 'GET',
    params: {
    },
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}
