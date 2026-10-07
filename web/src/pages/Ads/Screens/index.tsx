import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ModalForm, ProFormRadio, ProFormText, ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Tag } from 'antd';
import { useRef, useState } from 'react';
import {
  createScreen,
  deleteScreen,
  queryScreensByPage,
  updateScreen,
  type AdScreen,
} from '@/services/ant-design-pro/ads';

const OFFLINE_AFTER_MS = 2 * 60 * 1000; // 心跳超过 2 分钟视为离线（平板轮询 30s）

function heartbeatTag(lastSeenAt?: string) {
  if (!lastSeenAt) return <Tag>从未上线</Tag>;
  const elapsed = Date.now() - new Date(lastSeenAt).getTime();
  return elapsed < OFFLINE_AFTER_MS ? <Tag color="green">在线</Tag> : <Tag color="red">离线</Tag>;
}

export default function Screens() {
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AdScreen | undefined>();

  const columns: ProColumns<AdScreen>[] = [
    { dataIndex: 'index', valueType: 'indexBorder', width: 48 },
    { dataIndex: 'id', hideInSearch: true, hideInTable: true },
    { title: '屏标识', dataIndex: 'code', ellipsis: true, tooltip: '平板以 /tablet/?screen=屏标识 打开' },
    { title: '名称', dataIndex: 'name', ellipsis: true },
    { title: '位置', dataIndex: 'location', hideInSearch: true, ellipsis: true },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      valueEnum: {
        1: { text: '启用', status: 'Success' },
        0: { text: '停用', status: 'Default' },
      },
    },
    {
      title: '心跳',
      dataIndex: 'lastSeenAt',
      hideInSearch: true,
      width: 200,
      render: (_, entity) => (
        <>
          {heartbeatTag(entity.lastSeenAt)}
          {entity.lastSeenAt && (
            <span style={{ marginLeft: 8, color: '#999' }}>
              {new Date(entity.lastSeenAt).toLocaleString('zh-CN', { hour12: false })}
            </span>
          )}
        </>
      ),
    },
    { title: '更新时间', dataIndex: 'updateTime', hideInSearch: true, width: 160 },
    {
      title: '操作',
      valueType: 'option',
      width: 140,
      render: (_, entity) => [
        <a
          key="edit"
          onClick={() => {
            setEditing(entity);
            setModalOpen(true);
          }}
        >
          编辑
        </a>,
        <Popconfirm
          key="delete"
          title="确认删除该屏？该屏的排期不会自动删除"
          onConfirm={async () => {
            await deleteScreen(entity.id!);
            message.success('已删除');
            actionRef.current?.reload();
          }}
        >
          <a style={{ color: '#ff4d4f' }}>删除</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <ProTable<AdScreen>
      headerTitle="广告屏"
      actionRef={actionRef}
      rowKey="id"
      search={false}
      toolBarRender={() => [
        <Button
          key="create"
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setEditing(undefined);
            setModalOpen(true);
          }}
        >
          注册屏
        </Button>,
      ]}
      request={async (params) => {
        const res = await queryScreensByPage({
          current: params.current,
          pageSize: params.pageSize,
        });
        return {
          data: res?.data || [],
          total: res?.total || 0,
          success: res?.success !== false,
        };
      }}
      columns={columns}
      pagination={{ pageSize: 10 }}
    >
      <ModalForm<AdScreen>
        title={editing ? '编辑屏' : '注册屏'}
        width={480}
        open={modalOpen}
        onOpenChange={setModalOpen}
        modalProps={{ destroyOnHidden: true }}
        initialValues={editing}
        onFinish={async (values) => {
          const res = editing ? await updateScreen({ ...editing, ...values }) : await createScreen(values);
          if (res?.id) {
            message.success(editing ? '已更新' : '已注册');
            setModalOpen(false);
            actionRef.current?.reload();
            return true;
          }
          return false;
        }}
      >
        <ProFormText
          name="code"
          label="屏标识"
          tooltip="唯一；同一标识只能注册一块屏"
          rules={[{ required: true, message: '请填写屏标识' }]}
        />
        <ProFormText name="name" label="名称" rules={[{ required: true, message: '请填写名称' }]} />
        <ProFormText name="location" label="位置" placeholder="如：一楼候诊区" />
        <ProFormRadio.Group
          name="enabled"
          label="状态"
          initialValue={1}
          options={[
            { label: '启用', value: 1 },
            { label: '停用', value: 0 },
          ]}
          rules={[{ required: true }]}
        />
      </ModalForm>
    </ProTable>
  );
}
