import { PageContainer } from '@ant-design/pro-components';
import { ProFormDigit, ProFormSelect, ProFormText, ProForm } from '@ant-design/pro-components';
import { Button, Card, Col, message, Row, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import {
  createCall,
  queryRecentCalls,
  queryScreensByPage,
  type AdScreen,
  type QueueCall,
} from '@/services/ant-design-pro/ads';

export default function Calls() {
  const [formRef] = ProForm.useForm();
  const [screens, setScreens] = useState<AdScreen[]>([]);
  const [recent, setRecent] = useState<QueueCall[]>([]);
  const [calling, setCalling] = useState(false);

  const loadRecent = () => {
    queryRecentCalls().then((res) => setRecent(res || []));
  };

  useEffect(() => {
    queryScreensByPage({ current: 1, pageSize: 100 }).then((res) => setScreens(res?.data || []));
    loadRecent();
  }, []);

  const onCall = async (values: QueueCall) => {
    setCalling(true);
    try {
      const res = await createCall({ ...values, screenId: values.screenId ?? null });
      if (res?.id) {
        message.success(`已叫号：${res.room} ${res.number} 号`);
        formRef.resetFields(['number', 'patientMasked']);
        loadRecent();
      }
    } finally {
      setCalling(false);
    }
  };

  return (
    <PageContainer title="叫号操作" content="前台叫号后，定向屏（或全部屏）在 3 秒内全屏提示号码与诊室">
      <Row gutter={16}>
        <Col xs={24} lg={9}>
          <Card title="发起叫号">
            <ProForm<QueueCall>
              form={formRef}
              layout="horizontal"
              submitter={{
                render: (_, dom) => dom,
                submitButtonProps: { loading: calling, block: true },
              }}
              onFinish={onCall}
            >
              <ProFormDigit
                name="number"
                label="号码"
                min={1}
                fieldProps={{ precision: 0 }}
                tooltip="叫号号码，如 8"
                rules={[{ required: true, message: '请输入号码' }]}
              />
              <ProFormText
                name="room"
                label="诊室"
                placeholder="如 第二诊室"
                rules={[{ required: true, message: '请填写诊室' }]}
              />
              <ProFormText
                name="patientMasked"
                label="脱敏姓名"
                placeholder="如 张*（可不填）"
                tooltip="只在屏上显示脱敏姓名，隐私默认"
              />
              <ProFormSelect
                name="screenId"
                label="目标屏"
                placeholder="不选 = 全部屏"
                allowClear
                options={screens
                  .filter((s) => s.enabled === 1)
                  .map((s) => ({ label: `${s.name}（${s.code}）`, value: s.id }))}
              />
            </ProForm>
          </Card>
        </Col>
        <Col xs={24} lg={15}>
          <Card title="最近叫号" extra={<Button size="small" onClick={loadRecent}>刷新</Button>}>
            <Table<QueueCall>
              rowKey="id"
              size="small"
              pagination={{ pageSize: 8 }}
              dataSource={recent}
              columns={[
                { title: 'id', dataIndex: 'id', width: 60 },
                {
                  title: '屏',
                  dataIndex: 'screenId',
                  width: 140,
                  render: (_, entity) =>
                    entity.screenId ? (
                      screens.find((s) => s.id === entity.screenId)?.name || entity.screenId
                    ) : (
                      <Tag>全部屏</Tag>
                    ),
                },
                { title: '号码', dataIndex: 'number', width: 80 },
                { title: '诊室', dataIndex: 'room', width: 120 },
                { title: '脱敏姓名', dataIndex: 'patientMasked', width: 100 },
                {
                  title: '叫号时间',
                  dataIndex: 'calledAt',
                  render: (_, entity) =>
                    entity.calledAt
                      ? new Date(entity.calledAt).toLocaleString('zh-CN', { hour12: false })
                      : '-',
                },
              ]}
            />
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
}
