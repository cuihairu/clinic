import { request } from '@umijs/max';

export async function createStaff(body: API.Staff,options?: { [key: string]: any }) {
  return request<API.Staff>('/api/v1/staff/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

export async function updateStaff(body: API.Staff,options?: { [key: string]: any }) {
  return request<API.Staff>('/api/v1/staff/', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

export async function sign (body: API.Sign,options?: { [key: string]: any }) {
  return request<API.SignResult>('/api/v1/staff/sign', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

export async function deleteStaff( sid: number ,options?: { [key: string]: any }) {
  return request<API.Staff>(`/api/v1/staff/${sid}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}
export async function fetchStaffById(sid:number|string,options?: { [key: string]: any }) {
  return request<API.Staff>(`/api/v1/staff/${sid}`, {
    method: 'GET',
    params: {
    },
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}

export async function queryStaffTodayTimesheet(options?: { [key: string]: any }) {
  return request<API.DayTimesheet>('/api/v1/staff/timesheet/today', {
    method: 'GET',
    params: {},
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}

export async function fetchMouthTimesheet( month:number|string|undefined,options?: { [key: string]: any }) {
  return request<API.MonthTimesheet>(`/api/v1/staff/timesheet/month`, {
    method: 'GET',
    params: {
      month: month,
    },
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}
export async function queryStaffByPage(params: API.QueryCustomerParams,options?: { [key: string]: any }) {
  return request<API.QueryCustomerResult>('/api/v1/staff/page', {
    method: 'GET',
    params: params,
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}

export async function fetchTimesheet (body: API.Sign,options?: { [key: string]: any }) {
  return request<API.SignResult>('/api/v1/staff/sign', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

export async function createLeave(body: API.Staff,options?: { [key: string]: any }) {
  return request<API.Staff>('/api/v1/Leave/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
