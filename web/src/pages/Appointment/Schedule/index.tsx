import { PageContainer } from '@ant-design/pro-components';
import { Button, message } from 'antd';
import { Fragment, useCallback, useEffect, useMemo, useState } from 'react';
import { LeftOutlined, RightOutlined, ReloadOutlined } from '@ant-design/icons';
import moment from 'moment';
import {
  queryAppointmentPage,
  updateAppointmentStatus,
  APPOINTMENT_STATUS,
  type Appointment,
} from '@/services/ant-design-pro/appointment';
import './index.less';

/** 排班时段：9:00–18:00 整点（对齐设计原型 admin-booking.html 的时段网格） */
const HOURS = Array.from({ length: 10 }, (_, i) => 9 + i);
const UNASSIGNED = '未指派';

const STATUS_TEXT: Record<number, string> = {
  [APPOINTMENT_STATUS.BOOKED]: '待到店',
  [APPOINTMENT_STATUS.RECEIVED]: '已接待',
  [APPOINTMENT_STATUS.CANCELLED]: '已取消',
};
/** 网格里的短标签：已接待 = 已到店 */
const SLOT_TEXT: Record<number, string> = {
  [APPOINTMENT_STATUS.BOOKED]: '待到店',
  [APPOINTMENT_STATUS.RECEIVED]: '已到店',
  [APPOINTMENT_STATUS.CANCELLED]: '已取消',
};
const SLOT_CLASS: Record<number, string> = {
  [APPOINTMENT_STATUS.BOOKED]: 'wait',
  [APPOINTMENT_STATUS.RECEIVED]: 'in',
  [APPOINTMENT_STATUS.CANCELLED]: 'pass',
};

export default function AppointmentSchedule() {
  const [day, setDay] = useState(moment());
  const [list, setList] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(false);

  const dayStr = day.format('YYYY-MM-DD');

  const load = useCallback(async (target: string) => {
    setLoading(true);
    try {
      // 排班页看整日全部预约（含已取消），不做状态过滤
      const res = await queryAppointmentPage({ current: 1, pageSize: 200, date: target });
      setList(res?.data || []);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(dayStr);
  }, [dayStr, load]);

  const move = (n: number) => setDay((d) => d.clone().add(n, 'day'));

  /** 列（医师）：本日出现的医师 + 未指派兜底；按姓名稳定排序 */
  const columns = useMemo(() => {
    const names = new Set<string>();
    list.forEach((a) => names.add(a.staffName || UNASSIGNED));
    return Array.from(names).sort((a, b) => (a === UNASSIGNED ? 1 : b === UNASSIGNED ? -1 : a.localeCompare(b)));
  }, [list]);

  /** 格位索引：`${hh}|${staffName}` → 预约 */
  const cell = useMemo(() => {
    const map: Record<string, Appointment> = {};
    list.forEach((a) => {
      if (!a.startTime) return;
      const hh = moment(a.startTime).hour();
      const key = `${hh}|${a.staffName || UNASSIGNED}`;
      // 同格多条（时段冲突，后端不拦截）取第一条，其余在详情里看
      if (!map[key]) map[key] = a;
    });
    return map;
  }, [list]);

  const totalInCell = useMemo(() => {
    const map: Record<string, number> = {};
    list.forEach((a) => {
      if (!a.startTime) return;
      const key = `${moment(a.startTime).hour()}|${a.staffName || UNASSIGNED}`;
      map[key] = (map[key] || 0) + 1;
    });
    return map;
  }, [list]);

  const counts = useMemo(() => {
    const c = { total: list.length, in: 0, wait: 0, pass: 0 };
    list.forEach((a) => {
      if (a.status === APPOINTMENT_STATUS.RECEIVED) c.in += 1;
      else if (a.status === APPOINTMENT_STATUS.BOOKED) c.wait += 1;
      else if (a.status === APPOINTMENT_STATUS.CANCELLED) c.pass += 1;
    });
    return c;
  }, [list]);

  const receive = async (a: Appointment) => {
    if (!a.id) return;
    const res = await updateAppointmentStatus({ id: a.id, status: APPOINTMENT_STATUS.RECEIVED });
    if (res?.id) {
      message.success('已接待，可发起接诊');
      load(dayStr);
    }
  };

  return (
    <PageContainer
      title="排班视图"
      content="按日看医师×时段占用：建约留空档，到店在此一键接待。同一时段不冲突校验（演示边界），一格多条取第一条。"
    >
      <div className="sch-toolbar">
        <div className="sch-nav">
          <Button type="text" icon={<LeftOutlined />} onClick={() => move(-1)} title="前一天" />
          <span className="d">{day.format('YYYY-MM-DD dddd')}</span>
          <Button type="text" icon={<RightOutlined />} onClick={() => move(1)} title="后一天" />
          <Button size="small" onClick={() => setDay(moment())} disabled={day.isSame(moment(), 'day')}>
            今天
          </Button>
          <Button
            type="text"
            icon={<ReloadOutlined />}
            loading={loading}
            onClick={() => load(dayStr)}
            title="刷新"
          />
        </div>
        <div className="sch-stats">
          <span className="s">
            预约总数 <b>{counts.total}</b>
          </span>
          <span className="s">
            已到店 <b className="in">{counts.in}</b>
          </span>
          <span className="s">
            待到店 <b className="gd">{counts.wait}</b>
          </span>
          <span className="s">
            已取消 <b>{counts.pass}</b>
          </span>
        </div>
      </div>

      <div className="sch-grid-wrap">
        <div className="sch-grid" style={{ gridTemplateColumns: `64px repeat(${Math.max(columns.length, 1)}, 1fr)` }}>
          <span className="hd tm-hd" />
          {columns.length === 0 ? (
            <span className="hd">——</span>
          ) : (
            columns.map((name) => (
              <span className={`hd${name === UNASSIGNED ? ' unassigned' : ''}`} key={name}>
                {name}
              </span>
            ))
          )}

          {HOURS.map((h) => (
            <Fragment key={h}>
              <span className="tm">{String(h).padStart(2, '0')}:00</span>
              {(columns.length === 0 ? [UNASSIGNED] : columns).map((name) => {
                const a = cell[`${h}|${name}`];
                if (!a) {
                  return (
                    <div className="slot empty" key={`${h}-${name}`}>
                      可预约
                    </div>
                  );
                }
                const more = (totalInCell[`${h}|${name}`] || 1) - 1;
                return (
                  <div className={`slot${a.status === APPOINTMENT_STATUS.CANCELLED ? ' off' : ''}`} key={`${h}-${name}`}>
                    <div className="bk">
                      <div className="n">
                        {a.customerName || `顾客${a.customerId}`}
                        <span className={`st ${SLOT_CLASS[a.status ?? 0]}`}>{SLOT_TEXT[a.status ?? 0]}</span>
                        {more > 0 ? <span className="more">+{more}</span> : null}
                      </div>
                      <div className="s">{a.itemName || '到店再定'}</div>
                    </div>
                    {a.status === APPOINTMENT_STATUS.BOOKED ? (
                      <button type="button" className="go" onClick={() => receive(a)}>
                        接待
                      </button>
                    ) : null}
                  </div>
                );
              })}
            </Fragment>
          ))}
        </div>
      </div>
      <div className="sch-legend">
        {Object.entries(SLOT_TEXT).map(([k, text]) => (
          <span key={k} className={`lg ${SLOT_CLASS[Number(k)]}`}>
            {text}
          </span>
        ))}
        <span className="lg-empty">虚线 = 可预约空档</span>
      </div>
    </PageContainer>
  );
}
