import { PageContainer, ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import type { ActionType } from '@ant-design/pro-components';
import { message, Popconfirm } from 'antd';
import { useCallback, useEffect, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import {
  deleteOrder,
  queryOrderPage,
  updateOrderStatus,
  ORDER_STATUS,
  type Order,
} from '@/services/ant-design-pro/order';
import './index.less';

/** 状态文案对齐原型与自助机口径：0 已下单 → 「待接待」（不做在线支付，到店结算） */
const STATUS_TEXT: Record<number, string> = {
  [ORDER_STATUS.CREATED]: '待接待',
  [ORDER_STATUS.CONFIRMED]: '接待中',
  [ORDER_STATUS.DONE]: '已完成',
  [ORDER_STATUS.CANCELLED]: '已取消',
};
const STATUS_CLASS: Record<number, string> = {
  [ORDER_STATUS.CREATED]: 'wait',
  [ORDER_STATUS.CONFIRMED]: 'serving',
  [ORDER_STATUS.DONE]: 'done',
  [ORDER_STATUS.CANCELLED]: 'cancel',
};
const TABS = [
  { key: undefined, label: '全部' },
  { key: ORDER_STATUS.CREATED, label: '待接待' },
  { key: ORDER_STATUS.CONFIRMED, label: '接待中' },
  { key: ORDER_STATUS.DONE, label: '已完成' },
  { key: ORDER_STATUS.CANCELLED, label: '已取消' },
];

const maskPhone = (p?: string) => (p && p.length === 11 ? `${p.slice(0, 3)}****${p.slice(7)}` : p || '');

const statusPill = (status?: number) => {
  if (status == null || STATUS_TEXT[status] === undefined) return '-';
  return <span className={`st ${STATUS_CLASS[status]}`}>{STATUS_TEXT[status]}</span>;
};

export default function OrderQuery() {
  const actionRef = useRef<ActionType>();
  const statusRef = useRef<number | undefined>(undefined);
  const [activeTab, setActiveTab] = useState<number | undefined>(undefined);
  const [counts, setCounts] = useState<Record<string, number>>({});

  // 原型 tab 带计数：按状态各查一次 total（pageSize=1 只取 total，数据全真实）
  const loadCounts = useCallback(async () => {
    const [a, b, c, d] = await Promise.all(
      [ORDER_STATUS.CREATED, ORDER_STATUS.CONFIRMED, ORDER_STATUS.DONE, ORDER_STATUS.CANCELLED].map((s) =>
        queryOrderPage({ current: 1, pageSize: 1, status: s }),
      ),
    );
    setCounts({
      all: (a?.total || 0) + (b?.total || 0) + (c?.total || 0) + (d?.total || 0),
      0: a?.total || 0,
      1: b?.total || 0,
      2: c?.total || 0,
      9: d?.total || 0,
    });
  }, []);

  useEffect(() => {
    loadCounts();
  }, [loadCounts]);

  const switchTab = (key: number | undefined) => {
    statusRef.current = key;
    setActiveTab(key);
    actionRef.current?.reloadAndRest?.();
  };

  const flow = async (id: number, status: number, tip: string) => {
    const res = await updateOrderStatus({ id, status });
    if (res?.id) {
      message.success(tip);
      loadCounts();
      actionRef.current?.reload();
    }
  };

  const columns: ProColumns<Order>[] = [
    { title: 'id', dataIndex: 'id', width: 60, hideInTable: true },
    {
      title: '单号',
      dataIndex: 'id',
      width: 150,
      hideInSearch: true,
      render: (_, entity) => (
        <>
          <span className="oid">#{entity.id}</span>
          {/* 接待员工为空即自助机下单（Kiosk 单），否则前台 */}
          <span className={`src${entity.staffId == null ? ' kiosk' : ''}`}>
            {entity.staffId == null ? '自助机' : '前台'}
          </span>
        </>
      ),
    },
    {
      title: '顾客',
      dataIndex: 'customerName',
      width: 130,
      render: (_, entity) =>
        entity.customerName ? (
          <div className="who">
            <div className="nm">{entity.customerName}</div>
            <div className="tel">{maskPhone(entity.customerPhone)}</div>
          </div>
        ) : (
          '-'
        ),
    },
    {
      title: '项目',
      dataIndex: 'itemName',
      width: 160,
      render: (_, entity) => entity.itemName || '-',
    },
    {
      title: '合计',
      dataIndex: 'price',
      width: 100,
      hideInSearch: true,
      render: (_, entity) => (entity.price == null ? '-' : <span className="money">¥{entity.price}</span>),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 96,
      render: (_, entity) => statusPill(entity.status),
    },
    {
      title: '下单时间',
      dataIndex: 'createTime',
      width: 160,
      hideInSearch: true,
      render: (_, entity) =>
        entity.createTime ? new Date(entity.createTime).toLocaleString('zh-CN', { hour12: false }) : '-',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      width: 170,
      fixed: 'right',
      render: (_, entity) => {
        if (!entity.id) return [];
        const actions: ReactNode[] = [];
        if (entity.status === ORDER_STATUS.CREATED) {
          actions.push(
            <a key="confirm" className="op-go" onClick={() => flow(entity.id!, ORDER_STATUS.CONFIRMED, '已接单')}>
              接单
            </a>,
          );
        }
        if (entity.status === ORDER_STATUS.CONFIRMED) {
          actions.push(
            <a key="done" className="op-go amber" onClick={() => flow(entity.id!, ORDER_STATUS.DONE, '已完成')}>
              完成
            </a>,
          );
        }
        if (entity.status === ORDER_STATUS.CREATED || entity.status === ORDER_STATUS.CONFIRMED) {
          actions.push(
            <Popconfirm
              key="cancel"
              title="取消这笔订单？"
              onConfirm={() => flow(entity.id!, ORDER_STATUS.CANCELLED, '已取消')}
            >
              <a className="op-stop">取消</a>
            </Popconfirm>,
          );
        }
        if (entity.status === ORDER_STATUS.CANCELLED) {
          actions.push(
            <Popconfirm
              key="delete"
              title="删除这条已取消的订单？"
              onConfirm={async () => {
                const res = await deleteOrder(entity.id!);
                if (res && (res.message || res.message === undefined)) {
                  message.success('删除成功');
                  loadCounts();
                  actionRef.current?.reload();
                }
              }}
            >
              <a className="op-stop">删除</a>
            </Popconfirm>,
          );
        }
        if (actions.length === 0) actions.push(<span key="none">-</span>);
        return actions;
      },
    },
  ];

  return (
    <PageContainer
      title="订单管理"
      content="自助机下单后自动进入「待接待」，前台接单后顾客到店；不做在线支付，到店结算。"
    >
      <div className="od-tabs">
        {TABS.map((t) => (
          <button
            key={String(t.key)}
            type="button"
            className={`tab${activeTab === t.key ? ' on' : ''}`}
            onClick={() => switchTab(t.key)}
          >
            {t.label} <b>{t.key === undefined ? counts.all : counts[String(t.key)]}</b>
          </button>
        ))}
      </div>
      <ProTable<Order>
        rowKey="id"
        columns={columns}
        actionRef={actionRef}
        search={false}
        options={false}
        rowClassName={(entity) => (entity.status === ORDER_STATUS.CANCELLED ? 'od-cancel' : '')}
        request={async (params) => {
          const res = await queryOrderPage({
            current: params.current,
            pageSize: params.pageSize,
            status: statusRef.current,
          });
          return { data: res?.data || [], success: true, total: res?.total || 0 };
        }}
        pagination={{ pageSize: 20, showTotal: (total) => `共 ${total} 条 · 每页 20 条` }}
        dateFormatter="string"
      />
    </PageContainer>
  );
}
