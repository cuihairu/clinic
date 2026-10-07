/**
 * 叫号提示音：Web Audio 双音（免音频文件，不用联网资源）。
 * 浏览器自动播放策略下没有用户手势时会被挂起——静默失败即可，别为它弹错。
 */
export function playChime() {
  try {
    const ctx = new AudioContext()
    if (ctx.state === 'suspended') void ctx.resume()
    const now = ctx.currentTime
    ;[
      { freq: 880, at: 0 },
      { freq: 1318.5, at: 0.18 },
    ].forEach(({ freq, at }) => {
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.type = 'sine'
      osc.frequency.value = freq
      gain.gain.setValueAtTime(0.0001, now + at)
      gain.gain.exponentialRampToValueAtTime(0.35, now + at + 0.02)
      gain.gain.exponentialRampToValueAtTime(0.0001, now + at + 0.5)
      osc.connect(gain).connect(ctx.destination)
      osc.start(now + at)
      osc.stop(now + at + 0.55)
    })
    window.setTimeout(() => void ctx.close().catch(() => {}), 1500)
  } catch {
    /* 音频不可用就静默 */
  }
}
