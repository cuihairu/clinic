import React, { useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Button, Select, message } from 'antd';
import { createOrder } from '@/services/ant-design-pro/order';
import { fetchCustomerByPhone } from '@/services/ant-design-pro/customer';
import { queryStaffByPage } from '@/services/ant-design-pro/staff';
import { queryItemByPage } from '@/services/ant-design-pro/item';
import './index.less';

/** 会员等级展示口径（同顾客档案）：1 普通 / 2 银卡 / 3 金卡 */
const LEVEL_TEXT: Record<number, string> = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员' };

type CustomerBrief = { id?: number; name?: string; phone?: string; level?: number };
type ItemBrief = { id?: number; name?: string; price?: number };

const Create: React.FC = () => {
  const [customer, setCustomer] = useState<CustomerBrief | undefined>();
  const [phone, setPhone] = useState('');
  const [finding, setFinding] = useState(false);
  const [staffOptions, setStaffOptions] = useState<{ label: string; value: number }[]>([]);
  const [staffId, setStaffId] = useState<number | undefined>();
  const [itemOptions, setItemOptions] = useState<{ label: string; value: number }[]>([]);
  const [itemsById, setItemsById] = useState<Record<number, ItemBrief>>({});
  const [itemId, setItemId] = useState<number | undefined>();
  const [busy, setBusy] = useState(false);

  const loadOptions = async () => {
    if (staffOptions.length > 0 && itemOptions.length > 0) return;
    const [staffs, items] = await Promise.all([
      queryStaffByPage({ current: 1, pageSize: 100 }),
      queryItemByPage({ current: 1, pageSize: 100 }),
    ]);
    setStaffOptions((staffs?.data || []).map((s) => ({ label: s.name || `员工${s.id}`, value: s.id! })));
    const opts = (items?.data || []).map((i) => ({
      label: `${i.name || `卡项${i.id}`}${i.price != null ? ` · ¥${i.price}` : ''}`,
      value: i.id!,
    }));
    setItemOptions(opts);
    const map: Record<number, ItemBrief> = {};
    (items?.data || []).forEach((i) => {
      if (i.id != null) map[i.id] = { id: i.id, name: i.name, price: i.price };
    });
    setItemsById(map);
  };

  const findCustomer = async () => {
    const p = phone.trim();
    if (!/^1\d{10}$/.test(p)) {
      message.warning('请输入 1 开头的 11 位手机号');
      return;
    }
    setFinding(true);
    try {
      const found = await fetchCustomerByPhone(p);
      if (found?.id) {
        setCustomer(found);
      } else {
        setCustomer(undefined);
        message.warning('未找到该手机号顾客，请先在「顾客 → 新建顾客」建档');
      }
    } finally {
      setFinding(false);
    }
  };

  const submit = async () => {
    if (!customer?.id) {
      message.warning('请先按手机号定位顾客');
      return;
    }
    if (!itemId) {
      message.warning('请选择卡项');
      return;
    }
    setBusy(true);
    try {
      const created = await createOrder({ customerId: customer.id, itemId, staffId });
      if (created?.id) {
        message.success(`已下单 #${created.id}，可到结算台收款`);
        history.push('/order/query');
      }
    } finally {
      setBusy(false);
    }
  };

  const picked = itemId != null ? itemsById[itemId] : undefined;

  return (
    <PageContainer
      title="前台下单"
      content="按手机号定位顾客 → 选卡项下单（价格取卡项现价快照，落待接待）；之后照常接单/完成/取消，收款到结算台。"
    >
      <div className="od-create">
        <div className="od-customer">
          <h3>1 · 顾客</h3>
          <div className="find">
            <input
              className="phone"
              placeholder="顾客手机号"
              value={phone}
              maxLength={11}
              onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
              onKeyDown={(e) => e.key === 'Enter' && findCustomer()}
            />
            <Button type="primary" loading={finding} onClick={findCustomer}>
              查找
            </Button>
          </div>
          {customer ? (
            <div className="found">
              <span className="nm">{customer.name}</span>
              <span className="tier">{LEVEL_TEXT[customer.level ?? 0] ?? '普通会员'}</span>
              <span className="tel">{customer.phone}</span>
            </div>
          ) : (
            <div className="none">未定位顾客，先查找再选卡项</div>
          )}
        </div>

        <div className="od-form">
          <h3>2 · 卡项</h3>
          <div className="row">
            <label>卡项</label>
            <Select
              className="item"
              placeholder="选择卡项"
              options={itemOptions}
              value={itemId}
              onDropdownVisibleChange={loadOptions}
              onChange={(v) => setItemId(v)}
            />
          </div>
          <div className="row">
            <label>建单人</label>
            <Select
              className="staff"
              allowClear
              placeholder="选填，默认不记录"
              options={staffOptions}
              value={staffId}
              onDropdownVisibleChange={loadOptions}
              onChange={(v) => setStaffId(v)}
            />
          </div>
          <div className="foot">
            <span className="price">
              {picked?.price != null ? (
                <>
                  应收 <b>¥{picked.price}</b>
                </>
              ) : (
                '价格取卡项现价快照'
              )}
            </span>
            <Button type="primary" size="large" loading={busy} onClick={submit}>
              下单
            </Button>
          </div>
        </div>
      </div>
    </PageContainer>
  );
};

export default Create;
