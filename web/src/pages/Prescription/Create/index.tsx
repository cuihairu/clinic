import React, { useEffect, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Button, Input, InputNumber, Select, message } from 'antd';
import { CheckCircleFilled, MinusCircleOutlined, PlusOutlined, WarningFilled } from '@ant-design/icons';
import {
  checkCompatibility,
  createPrescription,
  type CompatibilityResult,
  type PrescriptionHerb,
} from '@/services/ant-design-pro/prescription';
import { fetchCustomerByPhone } from '@/services/ant-design-pro/customer';
import { queryStaffByPage } from '@/services/ant-design-pro/staff';
import './index.less';

/** 会员等级展示口径（同顾客档案）：1 普通 / 2 银卡 / 3 金卡 */
const LEVEL_TEXT: Record<number, string> = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员' };

type CustomerBrief = { id?: number; name?: string; phone?: string; level?: number };

const Create: React.FC = () => {
  const [customer, setCustomer] = useState<CustomerBrief | undefined>();
  const [phone, setPhone] = useState('');
  const [finding, setFinding] = useState(false);
  const [staffOptions, setStaffOptions] = useState<{ label: string; value: number }[]>([]);
  const [staffId, setStaffId] = useState<number | undefined>();
  const [herbs, setHerbs] = useState<PrescriptionHerb[]>([{ herb: '', weight: undefined }]);
  const [doses, setDoses] = useState<number>(7);
  const [usage, setUsage] = useState('');
  const [remark, setRemark] = useState('');
  const [busy, setBusy] = useState(false);
  const [compat, setCompat] = useState<CompatibilityResult | undefined>();
  const [checking, setChecking] = useState(false);
  const compatSeq = useRef(0);

  // 配伍审方：药名停顿 500ms 自动比对十八反/十九畏；少于两味不查
  const namedHerbs = herbs.map((r) => (r.herb || '').trim()).filter(Boolean);
  useEffect(() => {
    if (namedHerbs.length < 2) {
      setCompat(undefined);
      setChecking(false);
      return;
    }
    const seq = ++compatSeq.current;
    setChecking(true);
    const timer = setTimeout(async () => {
      try {
        const result = await checkCompatibility(namedHerbs);
        if (seq === compatSeq.current) {
          setCompat(result);
        }
      } catch {
        if (seq === compatSeq.current) {
          setCompat(undefined);
        }
      } finally {
        if (seq === compatSeq.current) {
          setChecking(false);
        }
      }
    }, 500);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [namedHerbs.join('|')]);

  const loadStaff = async () => {
    if (staffOptions.length > 0) return;
    const staffs = await queryStaffByPage({ current: 1, pageSize: 100 });
    setStaffOptions((staffs?.data || []).map((s) => ({ label: s.name || `员工${s.id}`, value: s.id! })));
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

  const setHerb = (i: number, patch: Partial<PrescriptionHerb>) => {
    setHerbs((rows) => rows.map((r, k) => (k === i ? { ...r, ...patch } : r)));
  };

  const addHerb = () => setHerbs((rows) => [...rows, { herb: '', weight: undefined }]);
  const removeHerb = (i: number) => setHerbs((rows) => rows.filter((_, k) => k !== i));

  const submit = async () => {
    if (!customer?.id) {
      message.warning('请先按手机号定位顾客');
      return;
    }
    const rows = herbs.filter((r) => (r.herb || '').trim() !== '' || r.weight != null);
    if (rows.length === 0) {
      message.warning('处方至少要有 1 味药');
      return;
    }
    for (let i = 0; i < rows.length; i++) {
      if (!(rows[i].herb || '').trim()) {
        message.warning(`第 ${i + 1} 味药名不能为空`);
        return;
      }
      if (!rows[i].weight || rows[i].weight! <= 0) {
        message.warning(`药材「${rows[i].herb!.trim()}」剂量必须大于 0`);
        return;
      }
    }
    setBusy(true);
    try {
      const created = await createPrescription({
        customerId: customer.id,
        staffId,
        doses,
        usage: usage.trim() || undefined,
        remark: remark.trim() || undefined,
        herbs: rows.map((r) => ({ herb: (r.herb || '').trim(), weight: r.weight, special: r.special || undefined })),
      });
      if (created?.id) {
        message.success(`处方 #${created.id} 已开`);
        history.push('/prescription/query');
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageContainer
      title="中药处方"
      content="按手机号定位顾客 → 写药味与剂数；配伍审方（十八反/十九畏）随写随查、提示不拦截。药材字典与计价仍为规划功能。"
    >
      <div className="pr-create">
        <div className="pr-customer">
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
            <div className="none">未定位顾客，先查找再写方</div>
          )}
        </div>

        <div className="pr-sheet">
          <h3>2 · 处方笺</h3>
          <div className="rows">
            {herbs.map((row, i) => (
              <div className="row" key={i}>
                <span className="idx">{i + 1}</span>
                <Input
                  className="herb"
                  placeholder="药名，如 柴胡"
                  value={row.herb}
                  onChange={(e) => setHerb(i, { herb: e.target.value })}
                />
                <InputNumber
                  className="wt"
                  placeholder="克"
                  min={0.1}
                  value={row.weight}
                  onChange={(v) => setHerb(i, { weight: v ?? undefined })}
                />
                <Input
                  className="special"
                  placeholder="特殊煎法，如 先煎/后下（选填）"
                  value={row.special}
                  onChange={(e) => setHerb(i, { special: e.target.value })}
                />
                <button
                  type="button"
                  className="del"
                  title="去掉这味"
                  disabled={herbs.length <= 1}
                  onClick={() => removeHerb(i)}
                >
                  <MinusCircleOutlined />
                </button>
              </div>
            ))}
          </div>
          <Button type="dashed" className="add" icon={<PlusOutlined />} onClick={addHerb}>
            添加一味
          </Button>

          <div className={`compat ${compat && (compat.findings?.length ?? 0) > 0 ? 'bad' : 'ok'}`}>
            <h3>3 · 配伍审方</h3>
            {namedHerbs.length < 2 ? (
              <div className="tip">写够两味药后自动比对十八反 / 十九畏</div>
            ) : checking ? (
              <div className="tip">审方中…</div>
            ) : compat && (compat.findings?.length ?? 0) > 0 ? (
              <div className="hits">
                <div className="head">
                  <WarningFilled /> 发现 {compat!.findings!.length} 组配伍提示
                </div>
                {compat!.findings!.map((f, i) => (
                  <div className="hit" key={i}>
                    <span className={`lv ${f.level === '禁忌' ? 'fan' : 'wei'}`}>{f.level}</span>
                    <span className="pair">
                      {f.a} × {f.b}
                    </span>
                    <span className="rule">
                      {f.rule} · {f.note}
                    </span>
                  </div>
                ))}
                <div className="note">提示不拦截，是否照用由医师判断</div>
              </div>
            ) : (
              <div className="pass">
                <CheckCircleFilled /> 未发现配伍禁忌（十八反 / 十九畏）
              </div>
            )}
          </div>

          <div className="meta">
            <label>
              剂数
              <InputNumber min={1} precision={0} value={doses} onChange={(v) => setDoses(v ?? 7)} /> 付
            </label>
            <label>
              医师
              <Select
                allowClear
                placeholder="选填"
                style={{ minWidth: 160 }}
                options={staffOptions}
                value={staffId}
                onDropdownVisibleChange={loadStaff}
                onChange={(v) => setStaffId(v)}
              />
            </label>
          </div>
          <Input.TextArea
            className="usage"
            placeholder="用法：煎服法/频次/代煎说明，如「水煎服，日一剂，早晚温服；代煎 7 袋」"
            rows={2}
            value={usage}
            onChange={(e) => setUsage(e.target.value)}
          />
          <Input
            className="remark"
            placeholder="备注（选填）：复诊提醒、禁忌等"
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
          />
          <div className="foot">
            <span className="hint">演示口径：药材无字典与价格，计价/库存为规划功能；审方提示不拦截</span>
            <Button type="primary" size="large" loading={busy} onClick={submit}>
              开方
            </Button>
          </div>
        </div>
      </div>
    </PageContainer>
  );
};

export default Create;
