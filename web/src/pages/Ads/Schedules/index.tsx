import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import {
  ModalForm,
  ProFormRadio,
  ProFormSelect,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
import { Button, message, Popconfirm } from 'antd';
import { useEffect, useRef, useState } from 'react';
import {
  createSchedule,
  deleteSchedule,
  queryMaterialsByPage,
  querySchedulesByPage,
  queryScreensByPage,
  updateSchedule,
  type AdMaterial,
  type AdSchedule,
  type AdScreen,
} from '@/services/ant-design-pro/ads';

const WEEKDAY_LABELS = '一二三四五六日';

function weekdaysText(weekdays?: string) {
  if (!weekdays || !weekdays.trim()) return '每天';
  return weekdays
    .split(',')
    .map((d) => `周${WEEKDAY_LABELS[Number(d.trim()) - 1] ?? d.trim()}`)
    .join(' ');
}

function timeRange(start?: string, end?: string) {
  if (!start && !end) return '全天';
  return `${start || '00:00'} ~ ${end || '24:00'}`;
}

export default function Schedules() {
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AdSchedule | undefined>();
  const [screens, setScreens] = useState<AdScreen[]>([]);
  const [materials, setMaterials] = useState<AdMaterial[]>([]);

  useEffect(() => {
    // 下拉数据量小，一次拉全（演示规模）；量大再换搜索接口
    queryScreensByPage({ current: 1, pageSize: 100 }).then((res) => setScreens(res?.data || []));
    queryMaterialsByPage({ current: 1, pageSize: 100 }).then((res) => setMaterials(res?.data || []));
  }, [modalOpen]);

  const screenName = (id?: number) => screens.find((s) => s.id === id)?.name || id;
  const materialName = (id?: number) => materials.find((m) => m.id === id)?.name || id;

  const columns: ProColumns<AdSchedule>[] = [
    { dataIndex: 'index', valueType: 'indexBorder', width: 48 },
    { dataIndex: 'id', hideInSearch: true, hideInTable: true },
    {
      title: '屏',
      dataIndex: 'screenId',
      render: (_, entity) => screenName(entity.screenId),
      fieldProps: {
        options: screens.map((s) => ({ label: `${s.name}（${s.code}）`, value: s.id })),
      },
    },
    {
      title: '素材',
      dataIndex: 'materialId',
      render: (_, entity) => materialName(entity.materialId),
      fieldProps: {
        options: materials.map((m) => ({ label: m.name, value: m.id })),
      },
    },
    {
      title: '生效星期',
      dataIndex: 'weekdays',
      hideInSearch: true,
      width: 160,
      render: (_, entity) => weekdaysText(entity.weekdays),
    },
    {
      title: '时段',
      dataIndex: 'startTime',
      hideInSearch: true,
      width: 140,
      render: (_, entity) => timeRange(entity.startTime, entity.endTime),
    },
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
          title="确认删除该排期？"
          onConfirm={async () => {
            await deleteSchedule(entity.id!);
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
    <ProTable<AdSchedule>
      headerTitle="广告排期"
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
          新建排期
        </Button>,
      ]}
      request={async (params) => {
        const res = await querySchedulesByPage({
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
      <ModalForm<AdSchedule>
        title={editing ? '编辑排期' : '新建排期'}
        width={520}
        open={modalOpen}
        onOpenChange={setModalOpen}
        modalProps={{ destroyOnHidden: true }}
        initialValues={editing}
        onFinish={async (values) => {
          const res = editing ? await updateSchedule({ ...editing, ...values }) : await createSchedule(values);
          if (res?.id) {
            message.success(editing ? '已更新' : '已创建');
            setModalOpen(false);
            actionRef.current?.reload();
            return true;
          }
          return false;
        }}
      >
        <ProFormSelect
          name="screenId"
          label="屏"
          showSearch
          options={screens.map((s) => ({ label: `${s.name}（${s.code}）`, value: s.id }))}
          rules={[{ required: true, message: '请选择屏' }]}
        />
        <ProFormSelect
          name="materialId"
          label="素材"
          showSearch
          options={materials.map((m) => ({ label: `${m.name}（${m.type === 2 ? '视频' : '图片'}）`, value: m.id }))}
          rules={[{ required: true, message: '请选择素材' }]}
        />
        <ProFormText
          name="weekdays"
          label="生效星期"
          placeholder="如 1,2,3,4,5；留空 = 每天"
          tooltip="1=周一 … 7=周日，逗号分隔"
        />
        <div style={{ display: 'flex', gap: 16 }}>
          <ProFormText
            name="startTime"
            label="时段起"
            placeholder="09:00"
            tooltip="留空 = 00:00"
            fieldProps={{ style: { width: '100%' } }}
          />
          <ProFormText
            name="endTime"
            label="时段止"
            placeholder="18:00"
            tooltip="留空 = 24:00"
            fieldProps={{ style: { width: '100%' } }}
          />
        </div>
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
