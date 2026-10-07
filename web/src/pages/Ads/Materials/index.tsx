import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ModalForm, ProFormDigit, ProFormRadio, ProFormText, ProTable } from '@ant-design/pro-components';
import { Button, message, Popconfirm, Upload } from 'antd';
import { useRef, useState } from 'react';
import {
  createMaterial,
  deleteMaterial,
  MATERIAL_UPLOAD_ACTION,
  queryMaterialsByPage,
  updateMaterial,
  uploadAuthHeaders,
  type AdMaterial,
} from '@/services/ant-design-pro/ads';

const TYPE_IMAGE = 1;
const TYPE_VIDEO = 2;

export default function Materials() {
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AdMaterial | undefined>();
  // 上传成功后的媒体地址（ModalForm 字段之外单独持有，提交时合入）
  const [mediaUrl, setMediaUrl] = useState('');
  const [mediaType, setMediaType] = useState<1 | 2>(TYPE_IMAGE);
  const [uploading, setUploading] = useState(false);

  const openCreate = () => {
    setEditing(undefined);
    setMediaUrl('');
    setMediaType(TYPE_IMAGE);
    setModalOpen(true);
  };

  const openEdit = (entity: AdMaterial) => {
    setEditing(entity);
    setMediaUrl(entity.url || '');
    setMediaType((entity.type as 1 | 2) || TYPE_IMAGE);
    setModalOpen(true);
  };

  const columns: ProColumns<AdMaterial>[] = [
    { dataIndex: 'index', valueType: 'indexBorder', width: 48 },
    { dataIndex: 'id', hideInSearch: true, hideInTable: true },
    { title: '素材名', dataIndex: 'name', ellipsis: true },
    {
      title: '类型',
      dataIndex: 'type',
      width: 80,
      valueEnum: {
        1: { text: '图片' },
        2: { text: '视频' },
      },
    },
    {
      title: '媒体',
      dataIndex: 'url',
      hideInSearch: true,
      ellipsis: true,
      copyable: true,
      render: (_, entity) => (
        <a href={entity.url} target="_blank" rel="noreferrer">
          {entity.url}
        </a>
      ),
    },
    {
      title: '停留',
      dataIndex: 'durationMs',
      hideInSearch: true,
      width: 90,
      render: (_, entity) => `${Math.round((entity.durationMs || 0) / 100) / 10}s`,
    },
    { title: '顺序', dataIndex: 'sort', hideInSearch: true, width: 70 },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      valueEnum: {
        1: { text: '启用', status: 'Success' },
        0: { text: '停用', status: 'Default' },
      },
    },
    { title: '更新时间', dataIndex: 'updateTime', hideInSearch: true, width: 160 },
    {
      title: '操作',
      valueType: 'option',
      width: 180,
      render: (_, entity) => [
        <a key="edit" onClick={() => openEdit(entity)}>
          编辑
        </a>,
        <a
          key="toggle"
          onClick={async () => {
            const next = entity.enabled === 1 ? 0 : 1;
            const res = await updateMaterial({ ...entity, enabled: next });
            if (res?.id) {
              message.success(next === 1 ? '已启用' : '已停用');
              actionRef.current?.reload();
            }
          }}
        >
          {entity.enabled === 1 ? '停用' : '启用'}
        </a>,
        <Popconfirm
          key="delete"
          title="确认删除该素材？"
          onConfirm={async () => {
            await deleteMaterial(entity.id!);
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
    <ProTable<AdMaterial>
      headerTitle="广告素材"
      actionRef={actionRef}
      rowKey="id"
      search={{ labelWidth: 'auto' }}
      toolBarRender={() => [
        <Button key="create" type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          新建素材
        </Button>,
      ]}
      request={async (params) => {
        const res = await queryMaterialsByPage({
          current: params.current,
          pageSize: params.pageSize,
          name: params.name,
          enabled: params.enabled as number | undefined,
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
      <ModalForm<AdMaterial>
        title={editing ? '编辑素材' : '新建素材'}
        width={520}
        open={modalOpen}
        onOpenChange={setModalOpen}
        modalProps={{ destroyOnHidden: true }}
        initialValues={editing}
        onFinish={async (values) => {
          if (!mediaUrl) {
            message.error('请先上传媒体文件');
            return false;
          }
          const payload: AdMaterial = { ...values, url: mediaUrl };
          const res = editing ? await updateMaterial({ ...editing, ...payload }) : await createMaterial(payload);
          if (res?.id) {
            message.success(editing ? '已更新' : '已创建');
            setModalOpen(false);
            actionRef.current?.reload();
            return true;
          }
          return false;
        }}
      >
        <ProFormText name="name" label="素材名" rules={[{ required: true, message: '请填写素材名' }]} />
        <ProFormRadio.Group
          name="type"
          label="类型"
          initialValue={mediaType}
          fieldProps={{ onChange: (e) => setMediaType(e.target.value) }}
          options={[
            { label: '图片', value: TYPE_IMAGE },
            { label: '视频', value: TYPE_VIDEO },
          ]}
          rules={[{ required: true }]}
        />
        <Upload
          name="file"
          action={MATERIAL_UPLOAD_ACTION}
          headers={uploadAuthHeaders()}
          maxCount={1}
          accept={mediaType === TYPE_VIDEO ? 'video/*' : 'image/*'}
          showUploadList={false}
          disabled={uploading}
          onChange={(info) => {
            if (info.file.status === 'uploading') {
              setUploading(true);
              return;
            }
            setUploading(false);
            if (info.file.status === 'done') {
              const url = info.file.response?.url;
              if (url) {
                setMediaUrl(url);
                message.success('上传成功');
              } else {
                message.error(info.file.response?.errorMessage || '上传失败');
              }
            } else if (info.file.status === 'error') {
              message.error('上传失败');
            }
          }}
        >
          <Button loading={uploading}>
            {mediaType === TYPE_VIDEO ? '上传视频 (mp4/webm/mov)' : '上传图片 (png/jpg/webp/gif)'}
          </Button>
        </Upload>
        <div style={{ margin: '8px 0 16px', color: mediaUrl ? '#1677ff' : '#999', wordBreak: 'break-all' }}>
          {mediaUrl ? `已选媒体：${mediaUrl}` : '尚未上传媒体'}
        </div>
        <ProFormDigit
          name="durationMs"
          label="停留时长（毫秒）"
          min={1500}
          step={500}
          fieldProps={{ precision: 0 }}
          tooltip="图片轮播停留时长；视频按实际播完为准，此值作兜底"
          rules={[{ required: true, message: '请填写停留时长' }]}
        />
        <ProFormDigit
          name="sort"
          label="轮播顺序"
          min={0}
          fieldProps={{ precision: 0 }}
          tooltip="数字小的先播"
          rules={[{ required: true, message: '请填写顺序' }]}
        />
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
