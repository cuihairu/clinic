import React, { useEffect, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Button, InputNumber, message } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import moment from 'moment';
import { Order, ORDER_STATUS, queryOrderPage } from '@/services/ant-design-pro/order';
import {
  PAY_TYPE,
  PAY_TYPE_TEXT,
  Recharge,
  Settlement,
  createRecharge,
  fetchRechargeBalance,
  querySettlementPage,
  settleOrder,
} from '@/services/ant-design-pro/billing';
import { listCardsByCustomer, type Card } from '@/services/ant-design-pro/card';
import './index.less';

const maskPhone = (p?: string) => (p ? p.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2') : '');

const Settle: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [settlements, setSettlements] = useState<Settlement[]>([]);
  const [selected, setSelected] = useState<Order | undefined>();
  const [balance, setBalance] = useState<number | undefined>();
  const [payType, setPayType] = useState<number>(PAY_TYPE.STORED_VALUE);
  const [rechargeAmount, setRechargeAmount] = useState<number>();
  const [busy, setBusy] = useState(false);
  /** 选中订单的顾客对该订单卡项的有效余次（无卡/停用为 0） */
  const [cardRest, setCardRest] = useState<number | undefined>();

  const loadQueue = async () => {
    const [queue, recent] = await Promise.all([
      queryOrderPage({ current: 1, pageSize: 50, pending: true }),
      querySettlementPage({ current: 1, pageSize: 8 }),
    ]);
    setOrders(queue?.data || []);
    setSettlements(recent?.data || []);
  };

  useEffect(() => {
    loadQueue();
  }, []);

  const choose = async (order: Order) => {
    setSelected(order);
    setRechargeAmount(undefined);
    setCardRest(undefined);
    if (order.customerId) {
      const b = await fetchRechargeBalance(order.customerId);
      setBalance(b?.balance ?? 0);
      const cards: Card[] = (await listCardsByCustomer(order.customerId)) || [];
      const hit = cards.find(
        (c) => c.itemId === order.itemId && c.status === 1 && (c.remainingTimes ?? 0) > 0,
      );
      setCardRest(hit?.remainingTimes ?? 0);
    } else {
      setCardRest(0);
    }
  };

  const price = selected?.price ?? 0;
  const storedShort = payType === PAY_TYPE.STORED_VALUE && balance !== undefined && balance < price;
  const need = Math.max(price - (balance ?? 0), 0);

  const pay = async () => {
    if (!selected?.id || !selected.customerId) return;
    setBusy(true);
    try {
      if (storedShort) {
        const amount = rechargeAmount ?? need;
        if (!amount || amount <= 0) {
          message.warning('请填写充值金额');
          return;
        }
        const recharged: Recharge | undefined = await createRecharge({
          customerId: selected.customerId,
          money: amount,
        });
        if (!recharged?.id) return;
        const refreshed = await fetchRechargeBalance(selected.customerId);
        setBalance(refreshed?.balance ?? 0);
        if ((refreshed?.balance ?? 0) < price) {
          message.warning('充值后余额仍不足，请补足或改用其他支付');
          return;
        }
      }
      const done = await settleOrder({ orderId: selected.id, payType });
      if (done?.id) {
        message.success(`已收款 ¥${done.money}（${PAY_TYPE_TEXT[payType]}）`);
        setSelected(undefined);
        setBalance(undefined);
        setRechargeAmount(undefined);
        await loadQueue();
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <PageContainer
      title="结算台"
      content="左侧为待结算队列（自助机/前台单据），选单后选支付方式收款；储值支付自动扣减余额；次卡抵扣需顾客持本单卡项的有效余次卡。"
    >
      <div className="bl-grid">
        <div className="bl-left">
          <section className="bl-panel">
            <h3>
              待结算队列<span className="hint">{orders.length} 单待收</span>
              <Button type="text" size="small" className="refresh" onClick={loadQueue} icon={<ReloadOutlined />} />
            </h3>
            {orders.length === 0 && <div className="bl-empty">队列已清空——自助机 / 前台新单会出现在这里</div>}
            {orders.map((o) => (
              <div
                key={o.id}
                className={`q-row ${selected?.id === o.id ? 'on' : ''}`}
                onClick={() => choose(o)}
              >
                <span className="oid">#{o.id}</span>
                <span className="src">{o.staffId == null ? '自助机' : '前台'}</span>
                <span className="who">
                  <b>{o.customerName}</b>
                  <i>{o.itemName}</i>
                </span>
                <span className="amt">¥{o.price}</span>
                <span className={`st ${o.status === ORDER_STATUS.CONFIRMED ? 'serving' : 'wait'}`}>
                  {o.status === ORDER_STATUS.CONFIRMED ? '接待中' : '待接待'}
                </span>
              </div>
            ))}
          </section>

          <section className="bl-panel">
            <h3>最近结算</h3>
            {settlements.length === 0 && <div className="bl-empty">暂无结算记录</div>}
            {settlements.map((s) => (
              <div key={s.id} className="s-row">
                <span className="oid">S-{s.id}</span>
                <span className="who">
                  <b>{s.customerName}</b>
                </span>
                <span className="pay">{PAY_TYPE_TEXT[s.payType ?? 0]}</span>
                <span className="amt">¥{s.money}</span>
                <span className="tm">{s.createTime ? moment(s.createTime).format('MM-DD HH:mm') : ''}</span>
              </div>
            ))}
          </section>
        </div>

        <div className="bl-right">
          <section className="bl-due">
            {!selected ? (
              <div className="bl-empty tall">从左侧队列选一单开始收款</div>
            ) : (
              <>
                <div className="head">
                  <div className="avatar">{(selected.customerName || '?').slice(0, 1)}</div>
                  <div className="who">
                    <div className="nm">{selected.customerName}</div>
                    <div className="mt">
                      {maskPhone(selected.customerPhone)} · 单 #{selected.id} ·{' '}
                      {selected.createTime ? moment(selected.createTime).format('HH:mm') : ''} 下单
                    </div>
                  </div>
                  <div className="bal">
                    <div className="k">储值卡余额</div>
                    <div className="v">¥{balance ?? '—'}</div>
                  </div>
                </div>

                <div className="line">
                  <div className="nm">{selected.itemName}</div>
                  <div className="num">¥{selected.price}</div>
                </div>

                <div className="k">应收合计</div>
                <div className="total">¥{price}</div>

                <div className="pays">
                  {[PAY_TYPE.STORED_VALUE, PAY_TYPE.WECHAT, PAY_TYPE.ALIPAY, PAY_TYPE.CASH, PAY_TYPE.CARD].map((t) => (
                    <div
                      key={t}
                      className={`pay ${payType === t ? 'on' : ''} ${t === PAY_TYPE.STORED_VALUE && storedShort ? 'short' : ''}`}
                      onClick={() => setPayType(t)}
                    >
                      <span className="pi">{PAY_TYPE_TEXT[t].slice(0, 1)}</span>
                      <div>
                        <div className="pn">{PAY_TYPE_TEXT[t]}</div>
                        <div className="ps">
                          {t === PAY_TYPE.STORED_VALUE
                            ? balance !== undefined
                              ? `余额 ¥${balance}`
                              : '查询中'
                            : t === PAY_TYPE.CASH
                              ? '收银找零'
                              : t === PAY_TYPE.CARD
                                ? cardRest === undefined
                                  ? '查询中'
                                  : cardRest > 0
                                    ? `扣 1 次 · 余 ${cardRest} 次`
                                    : '无有效次卡'
                                : '记录支付方式'}
                        </div>
                      </div>
                      <span className="ck" />
                    </div>
                  ))}
                </div>

                {storedShort && (
                  <div className="recharge">
                    <div className="tip">
                      储值余额不足（差 <b>¥{need}</b>），先充值再一并收款：
                    </div>
                    <div className="row">
                      <InputNumber
                        min={1}
                        max={100000}
                        precision={0}
                        value={rechargeAmount ?? need}
                        onChange={(v) => setRechargeAmount(v ?? undefined)}
                        addonBefore="充值 ¥"
                      />
                      {[need || 500, 500, 1000]
                        .filter((v, i, arr) => v > 0 && arr.indexOf(v) === i)
                        .map((v) => (
                          <Button key={v} size="small" onClick={() => setRechargeAmount(v)}>
                            {v}
                          </Button>
                        ))}
                    </div>
                  </div>
                )}

                <Button type="primary" className="btn-pay" loading={busy} onClick={pay}>
                  {storedShort ? `充值 ¥${rechargeAmount ?? need} 并收款 ¥${price}` : `收款 ¥${price}`}
                </Button>
                <div className="note">演示口径：微信/支付宝仅记录支付方式；次卡抵扣扣 1 次、实收记 0（需顾客持本单卡项的有效余次卡）；小票打印为规划功能。</div>
              </>
            )}
          </section>
        </div>
      </div>
    </PageContainer>
  );
};

export default Settle;
