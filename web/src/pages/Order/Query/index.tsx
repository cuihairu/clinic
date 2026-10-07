import { PageContainer } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import type { ActionType } from '@ant-design/pro-components';
import { message, Popconfirm, Tag } from 'antd';
import { useRef } from 'react';
import type { ReactNode } from 'react';
import {
  deleteOrder,
  queryOrderPage,
  updateOrderStatus,
  ORDER_STATUS,
  type Order,
} from '@/services/ant-design-pro/order';

const statusTag = (status?: number) => {
  switch (status) {
    case ORDER_STATUS.CREATED:
      return <Tag color="blue">已下单</Tag>;
    case ORDER_STATUS.CONFIRMED:
      return <Tag color="orange">接待中</Tag>;
    case ORDER_STATUS.DONE:
      return <Tag color="green">已完成</Tag>;
    case ORDER_STATUS.CANCELLED:
      return <Tag>已取消</Tag>;
    default:
      return '-';
  }
};

export default function OrderQuery() {
  const actionRef = useRef<ActionType>();

  const flow = async (id: number, status: number, tip: string) => {
    const res = await updateOrderStatus({ id, status });
    if (res?.id) {
      message.success(tip);
      actionRef.current?.reload();
    }
  };

  const columns: ProColumns<Order>[] = [
    { title: 'id', dataIndex: 'id', width: 60, hideInSearch: true },
    {
      title: '顾客',
      dataIndex: 'customerName',
      width: 110,
      render: (_, entity) =>
        entity.customerName ? `${entity.customerName}${entity.customerPhone ? `（${entity.customerPhone}）` : ''}` : '-',
    },
    { title: '卡项', dataIndex: 'itemName', width: 150, render: (_, entity) => entity.itemName || '-' },
    { title: '金额', dataIndex: 'price', width: 90, hideInSearch: true, render: (_, entity) => (entity.price == null ? '-' : `¥${entity.price}`) },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      valueType: 'select',
      valueEnum: {
        0: { text: '已下单' },
        1: { text: '接待中' },
        2: { text: '已完成' },
        9: { text: '已取消' },
      },
      render: (_, entity) => statusTag(entity.status),
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
      width: 200,
      render: (_, entity) => {
        if (!entity.id) return [];
        const actions: ReactNode[] = [];
        if (entity.status === ORDER_STATUS.CREATED) {
          actions.push(
            <a key="confirm" onClick={() => flow(entity.id!, ORDER_STATUS.CONFIRMED, '已接单')}>
              接单
            </a>,
          );
        }
        if (entity.status === ORDER_STATUS.CONFIRMED) {
          actions.push(
            <a key="done" onClick={() => flow(entity.id!, ORDER_STATUS.DONE, '已完成')}>
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
              <a>取消</a>
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
                  actionRef.current?.reload();
                }
              }}
            >
              <a>删除</a>
            </Popconfirm>,
          );
        }
        if (actions.length === 0) actions.push(<span key="none">-</span>);
        return actions;
      },
    },
  ];

  return (
    <PageContainer title="订单管理" content="顾客 Kiosk 下的单在这里接待：接单 → 完成，未接待可取消；删除仅限已取消的单">
      <ProTable<Order>
        rowKey="id"
        columns={columns}
        actionRef={actionRef}
        cardBordered
        request={async (params) => {
          const res = await queryOrderPage({
            current: params.current,
            pageSize: params.pageSize,
            status: params.status ? Number(params.status) : undefined,
          });
          return { data: res?.data || [], success: true, total: res?.total || 0 };
        }}
        pagination={{ pageSize: 10 }}
        dateFormatter="string"
        search={{ labelWidth: 'auto' }}
      />
    </PageContainer>
  );
}
