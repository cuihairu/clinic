/**
 * 语音播报（可选增强）：Web Speech API 播「请 X 到 Y 诊室就诊」。
 * 各平板浏览器支持度不一——不支持或没有中文语音就静默跳过，只留提示音。
 */
export function speakCall(call: { number: string; room: string; patientMasked: string | null }) {
  try {
    if (typeof speechSynthesis === 'undefined') return
    const text = `请${call.number}号，${call.patientMasked ? call.patientMasked + '，' : ''}到${call.room}就诊`
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'zh-CN'
    utterance.rate = 0.9
    // 连续叫号时掐掉上一条，避免叠音
    speechSynthesis.cancel()
    speechSynthesis.speak(utterance)
  } catch {
    /* 播报不可用就静默 */
  }
}
