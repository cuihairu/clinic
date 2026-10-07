import { useEffect } from 'react'
import type { QueueCallItem } from '../types'
import { playChime } from './chime'
import { speakCall } from './speech'
import './call.css'

/**
 * 全屏叫号提示：盖在轮播之上，停留 holdMs（默认 15 秒）后回调父级换下一条/回轮播。
 */
export function CallOverlay({
  call,
  holdMs,
  onDone,
}: {
  call: QueueCallItem
  holdMs: number
  onDone: () => void
}) {
  useEffect(() => {
    playChime()
    speakCall(call)
    const timer = window.setTimeout(onDone, holdMs)
    return () => window.clearTimeout(timer)
  }, [call.id])

  return (
    <div className="call-overlay">
      <div className="call-card">
        <div className="call-room">{call.room}</div>
        <div className="call-number">{call.number}</div>
        {call.patientMasked && <div className="call-patient">{call.patientMasked}</div>}
        <div className="call-hint">请前往 {call.room} 就诊</div>
      </div>
    </div>
  )
}
