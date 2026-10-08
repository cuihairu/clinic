import { PageContainer } from '@ant-design/pro-components';
import React, { useCallback, useEffect, useState } from 'react';
import {
  fetchReviewByDay,
  fetchReviewCustomerBulk,
  fetchReviewStaffBulk,
} from '@/services/ant-design-pro/review';
import { queryStaffByPage } from '@/services/ant-design-pro/staff';
import { queryCustomerByPage } from '@/services/ant-design-pro/customer';
import { fuzzyQueryTreatByPage } from '@/services/ant-design-pro/treat';
import { Button, message } from 'antd';
import { history } from '@umijs/max';
import moment from 'moment';
import './index.less';

type StaffReviewRow = {
  id?: number | string;
  name?: string;
  cost?: number;
  done?: string;
  advice?: string;
};
type CustomerReviewRow = {
  id?: number | string;
  name?: string;
  customerId?: number;
  last?: string;
  advice?: string;
};
type DayRow = { day: string; count: number };

/** 分号/换行拆成条目（今日总结的两栏文本） */
const splitItems = (text?: string) =>
  (text || '')
    .split(/[\n;；]+/)
    .map((s) => s.trim())
    .filter(Boolean);

const DayDashboard: React.FC = () => {
  const [day, setDay] = useState<moment.Moment>(moment());
  const [staffReviews, setStaffReviews] = useState<StaffReviewRow[]>([]);
  const [allStaff, setAllStaff] = useState<string[]>([]);
  const [customerReviews, setCustomerReviews] = useState<CustomerReviewRow[]>([]);
  const [review, setReview] = useState<{ good?: string; improvement?: string }>({});
  const [treatTotal, setTreatTotal] = useState<number>();
  const [newCustomers, setNewCustomers] = useState<number>();
  const [bars, setBars] = useState<DayRow[]>([]);

  const isToday = day.isSame(moment(), 'day');

  const load = useCallback(async (d: moment.Moment) => {
    const dayStr = d.format('YYYY-MM-DD');
    const nextDayStr = d.clone().add(1, 'day').format('YYYY-MM-DD');

    // 员工总结（当日已提交）+ 全员名册 → 未填写 = 名册 − 已提交
    const staffBulk = (await fetchReviewStaffBulk(d.toDate())) || [];
    setStaffReviews(staffBulk);
    const staffPage = await queryStaffByPage({ current: 1, pageSize: 100 });
    const names = (staffPage?.data || []).map((s) => s.name || '').filter(Boolean);
    setAllStaff(names);

    // 今日回访 + 今日总结
    setCustomerReviews((await fetchReviewCustomerBulk(d.toDate())) || []);
    setReview((await fetchReviewByDay(d.toDate())) || {});

    // 顶部统计：员工在岗无按日在岗人数聚合接口（timesheet 接口为个人工时口径），如实占位；
    // 接诊 / 新客 / 回访到期均真实查询

    const treatRes = await fuzzyQueryTreatByPage({
      current: 1,
      pageSize: 1,
      startTime: dayStr,
      endTime: nextDayStr,
    });
    setTreatTotal(treatRes?.total ?? 0);
    const custRes = await queryCustomerByPage({
      current: 1,
      pageSize: 1,
      startTime: dayStr,
      endTime: nextDayStr,
    });
    setNewCustomers(custRes?.total ?? 0);

    // 近 7 日接诊单量（逐日实查，接口按创建时间过滤）
    const days: DayRow[] = [];
    for (let i = 6; i >= 0; i -= 1) {
      const cur = d.clone().subtract(i, 'day');
      days.push({ day: cur.format('M-DD'), count: 0 });
    }
    const counts = await Promise.all(
      days.map((row, idx) => {
        const start = d.clone().subtract(6 - idx, 'day');
        return fuzzyQueryTreatByPage({
          current: 1,
          pageSize: 1,
          startTime: start.format('YYYY-MM-DD'),
          endTime: start.clone().add(1, 'day').format('YYYY-MM-DD'),
        }).then((res) => res?.total ?? 0);
      }),
    );
    setBars(days.map((row, idx) => ({ ...row, count: counts[idx] })));
  }, []);

  useEffect(() => {
    load(day);
  }, [day, load]);

  const maxCount = Math.max(1, ...bars.map((b) => b.count));
  const submittedNames = new Set(staffReviews.map((r) => r.name));
  const missing = allStaff.filter((n) => !submittedNames.has(n));
  const goodItems = splitItems(review.good);
  const fixItems = splitItems(review.improvement);

  return (
    <PageContainer
      title={`今日报表 · ${day.format('M 月 D 日')}`}
      className="dd-page"
      extra={[
        <Button key="prev" onClick={() => setDay(day.clone().subtract(1, 'day'))}>
          ‹ 前一天
        </Button>,
        <Button key="next" disabled={isToday} onClick={() => setDay(day.clone().add(1, 'day'))}>
          后一天 ›
        </Button>,
      ]}
    >
      <div className="dd-stats">
        <div className="stat">
          <div className="k">员工在岗</div>
          <div className="v hold">规划功能 · 无在岗人数聚合接口</div>
        </div>
        <div className="stat">
          <div className="k">今日接诊</div>
          <div className="v">
            {treatTotal ?? '—'}
            <small>单</small>
          </div>
        </div>
        <div className="stat">
          <div className="k">新客建档</div>
          <div className="v">
            {newCustomers ?? '—'}
            <small>位</small>
          </div>
        </div>
        <div className="stat">
          <div className="k">回访到期</div>
          <div className="v amber">
            {customerReviews.length}
            <small>位</small>
          </div>
        </div>
      </div>

      <div className="dd-cols">
        <section className="dd-panel">
          <h3>
            员工总结
            <span className="hint">各员工当天工作日复盘，未填可提醒（提醒推送属规划域，暂无通知接口）</span>
          </h3>
          <table className="dd-table">
            <thead>
              <tr>
                <th style={{ width: 110 }}>员工</th>
                <th style={{ width: 70 }}>耗卡</th>
                <th>卡项已有/已做</th>
                <th>成单/建议</th>
                <th style={{ width: 84 }}>状态</th>
              </tr>
            </thead>
            <tbody>
              {staffReviews.map((r) => (
                <tr key={String(r.id ?? r.name)}>
                  <td>
                    <div className="who">{r.name || '—'}</div>
                  </td>
                  <td>{r.cost ?? '—'}</td>
                  <td>{r.done || '—'}</td>
                  <td>{r.advice || '—'}</td>
                  <td>
                    <span className="pill ok">已提交</span>
                  </td>
                </tr>
              ))}
              {missing.map((n) => (
                <tr key={n} className="dim">
                  <td>
                    <div className="who">{n}</div>
                  </td>
                  <td>—</td>
                  <td>今日尚未填写总结</td>
                  <td>—</td>
                  <td>
                    <span className="pill todo">未填写</span>
                  </td>
                </tr>
              ))}
              {staffReviews.length === 0 && missing.length === 0 ? (
                <tr>
                  <td colSpan={5} className="dd-empty">
                    当日无员工复盘数据
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </section>

        <div className="dd-stack">
          <section className="dd-panel">
            <h3>
              今日回访
              <span className="hint">到期待办 · 去回访进入顾客档案</span>
            </h3>
            {customerReviews.length === 0 ? (
              <div className="dd-empty">当日无到期回访</div>
            ) : (
              customerReviews.map((r) => (
                <div className="rv" key={String(r.id ?? r.name)}>
                  <div className="av">{(r.name || '客').slice(0, 1)}</div>
                  <div>
                    <div className="nm">
                      {r.name || '—'}
                      {r.last ? <span>最近到店 {moment(r.last).format('M-DD')}</span> : null}
                    </div>
                    <div className="why">{r.advice || '—'}</div>
                  </div>
                  {r.customerId ? (
                    <a
                      className="act"
                      onClick={() => history.push(`/customer/update?customerId=${r.customerId}`)}
                    >
                      去回访
                    </a>
                  ) : null}
                </div>
              ))
            )}
          </section>

          <section className="dd-panel">
            <h3>
              今日总结
              <span className="hint">按天沉淀，只保留一份</span>
            </h3>
            {goodItems.length === 0 && fixItems.length === 0 ? (
              <div className="dd-empty">当日未填写总结</div>
            ) : (
              <div className="sum">
                {goodItems.length ? <div className="sg">做得好</div> : null}
                {goodItems.map((t) => (
                  <div className="li good" key={t}>
                    {t}
                  </div>
                ))}
                {fixItems.length ? <div className="sg">待改进</div> : null}
                {fixItems.map((t) => (
                  <div className="li fix" key={t}>
                    {t}
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="dd-panel">
            <h3>
              近 7 日接诊
              <span className="hint">每日接诊单量</span>
            </h3>
            <div className="dd-bars">
              {bars.map((b, idx) => (
                <div className={`bar${idx === bars.length - 1 ? ' on' : ''}`} key={b.day}>
                  <i style={{ height: `${Math.max(6, (b.count / maxCount) * 100)}%` }} />
                  <span>
                    {b.day}
                    <br />
                    <b>{b.count}</b>
                  </span>
                </div>
              ))}
            </div>
          </section>
        </div>
      </div>
    </PageContainer>
  );
};

export default DayDashboard;
