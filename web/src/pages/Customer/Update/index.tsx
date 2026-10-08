import React, { useCallback, useEffect, useRef, useState } from 'react';
import { updateCustomer, fetchCustomerById } from '@/services/ant-design-pro/customer';
import { queryTreatByPage } from '@/services/ant-design-pro/treat';
import {
  PageContainer,
  ProForm,
  ProFormText,
  ProFormTextArea,
  ProFormDatePicker,
  ProFormSelect,
  ProFormInstance,
} from '@ant-design/pro-components';
import { Button, Drawer, message } from 'antd';
import { history, useSearchParams } from '@umijs/max';
import moment from 'moment';
import './index.less';

/** 会员等级展示口径：1 普通 / 2 银卡 / 3 金卡（演示种子数据即此三档） */
const LEVEL_TEXT: Record<number, string> = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员' };
const levelText = (level?: number) => LEVEL_TEXT[level ?? 0] ?? (level ? `会员 L${level}` : '普通会员');
const maskPhone = (p?: string) => (p && p.length === 11 ? `${p.slice(0, 3)}****${p.slice(7)}` : p || '—');

type TreatRow = { id?: number; createTime?: string; diagnose?: string; desc?: string; plan?: string };

const Update: React.FC = () => {
  const [searchParams] = useSearchParams();
  const customerId = searchParams.get('customerId');
  const formRef = useRef<ProFormInstance>();
  const [customer, setCustomer] = useState<API.Customer | undefined>();
  const [treats, setTreats] = useState<TreatRow[]>([]);
  const [treatTotal, setTreatTotal] = useState(0);
  const [editOpen, setEditOpen] = useState(false);

  const load = useCallback(async () => {
    if (!customerId) return;
    const v = await fetchCustomerById(customerId);
    setCustomer(v);
    const res = await queryTreatByPage({ customerId: Number(customerId), pageSize: 4, current: 1 });
    setTreats(res?.data || []);
    setTreatTotal(res?.total || 0);
  }, [customerId]);

  useEffect(() => {
    if (customerId === null || customerId === undefined || customerId === '') {
      message.error('没有传入顾客id');
      history.push('/customer/query');
      return;
    }
    load();
  }, [customerId, load]);

  const openEdit = () => {
    formRef.current?.setFieldsValue(customer);
    setEditOpen(true);
  };

  const lastVisit = treats
    .map((t) => t.createTime)
    .filter(Boolean)
    .sort()
    .pop();
  const lastVisitText = lastVisit
    ? moment().diff(moment(lastVisit), 'days') === 0
      ? '今天到店'
      : `最近到店 ${moment().diff(moment(lastVisit), 'days')} 天前`
    : '暂无到店记录';

  return (
    <PageContainer title={customer ? `${customer.name || ''} · 顾客档案` : '顾客档案'}>
      <div className="cu-head">
        <div className="avatar">{(customer?.name || '客').slice(0, 1)}</div>
        <div className="who">
          <h2>
            {customer?.name || '—'}
            <span className="tier">{levelText(customer?.level)}</span>
          </h2>
          <p>
            <span>{maskPhone(customer?.phone)}</span>
            <span>
              {customer?.gender === 1 ? '男' : '女'} · {customer?.age ?? '—'} 岁
            </span>
            {customer?.address ? <span>{customer.address}</span> : null}
            <span>{lastVisitText}</span>
          </p>
        </div>
        <div className="head-actions">
          <Button onClick={() => window.print()}>打印档案</Button>
          <Button onClick={openEdit}>编辑资料</Button>
          <Button
            type="primary"
            onClick={() => history.push(`/treat/create?customerId=${customerId}`)}
          >
            发起接诊
          </Button>
        </div>
      </div>

      <div className="cu-stats">
        <div className="stat">
          <div className="k">诊疗记录</div>
          <div className="v">
            {treatTotal}
            <small>次</small>
          </div>
        </div>
        <div className="stat">
          <div className="k">累计消费</div>
          <div className="v hold">规划功能 · 无订单聚合接口</div>
        </div>
        <div className="stat">
          <div className="k">持卡卡项</div>
          <div className="v hold">规划功能 · 无卡项持有实体</div>
        </div>
        <div className="stat">
          <div className="k">下次回访</div>
          <div className="v hold">规划功能 · 无按顾客回访查询</div>
        </div>
      </div>

      <div className="cu-cols">
        <section className="cu-panel">
          <h3>
            诊疗记录
            {treatTotal > 4 ? (
              <a onClick={() => history.push(`/treat/history?customerId=${customerId}`)}>
                查看全部 {treatTotal} 次
              </a>
            ) : null}
          </h3>
          {treats.length === 0 ? (
            <div className="cu-empty">暂无接诊记录，可从「发起接诊」建立第一张接诊单</div>
          ) : (
            treats.map((t) => (
              <div className="visit" key={t.id}>
                <div className="v-date">
                  <div className="d">{moment(t.createTime).date()}</div>
                  <div className="m">{moment(t.createTime).format('M 月')}</div>
                </div>
                <div className="v-body">
                  <div className="t">{t.diagnose || '接诊记录'}</div>
                  <div className="s">{t.desc || t.plan || '—'}</div>
                </div>
              </div>
            ))
          )}
        </section>
        <aside className="cu-panel">
          <h3>
            持卡卡项
            <span className="plan-pill">规划功能</span>
          </h3>
          <div className="plan-note">
            卡项持有、余次与有效期管理，以及按顾客的回访提醒，属规划域（暂无卡项持有实体与按顾客回访查询接口），界面按原型
            docs/design/mockups/admin-customer 预留，接入后在此渲染。
          </div>
        </aside>
      </div>

      <Drawer title="编辑顾客资料" width={560} open={editOpen} onClose={() => setEditOpen(false)}>
        <ProForm<API.Customer>
          formRef={formRef}
          grid
          layout={'vertical'}
          onFinish={async (values) => {
            values.id = Number(customerId);
            const result = await updateCustomer(values);
            if (result?.id) {
              message.success('更新成功');
              setEditOpen(false);
              load();
            }
            return true;
          }}
        >
          <ProFormText
            colProps={{ md: 12, xl: 12 }}
            name="name"
            label="姓名"
            tooltip="长度范围2-20字符,选填项"
            placeholder="输入顾客姓名"
            required={true}
          />
          <ProFormText colProps={{ md: 12, xl: 6 }} name="age" label="年龄" placeholder="20" />
          <ProFormSelect
            colProps={{ md: 12, xl: 6 }}
            name="gender"
            label="性别"
            options={[
              { value: 0, label: '女' },
              { value: 1, label: '男' },
            ]}
            placeholder={'男'}
          />
          <ProFormText
            colProps={{ md: 12, xl: 12 }}
            name="phone"
            label="手机号码"
            tooltip="方便按手机查找"
            placeholder="输入手机号码"
          />
          <ProFormText
            colProps={{ md: 12, xl: 12 }}
            name="level"
            label="会员等级"
            tooltip="1 普通会员 / 2 银卡会员 / 3 金卡会员"
            placeholder="1"
          />
          <ProFormDatePicker colProps={{ md: 12, xl: 12 }} label="生日" name="birthday" />
          <ProFormTextArea colProps={{ span: 24 }} name="address" label="详细的工作地址或家庭住址" />
        </ProForm>
      </Drawer>
    </PageContainer>
  );
};

export default Update;
