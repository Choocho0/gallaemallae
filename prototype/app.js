'use strict';

/* =========================================================
   utils
   ========================================================= */
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const el = (html) => { const t = document.createElement('template'); t.innerHTML = html.trim(); return t.content.firstElementChild; };
const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
const wait = (ms) => new Promise((r) => setTimeout(r, ms));
const pad = (n) => String(n).padStart(2, '0');
const hm = (offsetMin = 0) => { const d = new Date(Date.now() + offsetMin * 60000); return `${pad(d.getHours())}:${pad(d.getMinutes())}`; };
const fmtMin = (m) => {
  m = Math.max(0, Math.round(m));
  if (m < 60) return `${m}분`;
  return m % 60 ? `${Math.floor(m / 60)}시간 ${m % 60}분` : `${m / 60}시간`;
};
const fmtKm = (km) => (km < 1 ? `${Math.max(10, Math.round(km * 100) * 10)}m` : `${km.toFixed(km < 10 ? 1 : 0)}km`);
const rng = (seed) => () => { seed = (seed * 16807) % 2147483647; return (seed - 1) / 2147483646; };
const shuffle = (a) => { const b = [...a]; for (let i = b.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [b[i], b[j]] = [b[j], b[i]]; } return b; };

const ICON_PATHS = {
  route: '<circle cx="6" cy="19" r="2"/><circle cx="18" cy="5" r="2"/><path d="M8 19h7a3.5 3.5 0 0 0 0-7H9a3.5 3.5 0 0 1 0-7h7"/>',
  car: '<path d="M4 17v-5l2.2-5.2A2 2 0 0 1 8 5.5h8a2 2 0 0 1 1.8 1.3L20 12v5"/><path d="M3 12h18v5H3z"/><path d="M6 17v2M18 17v2"/>',
  msg: '<path d="M4 5h16v11H9l-5 4z"/><path d="M8 9.5h8M8 12.5h5"/>',
  link: '<path d="M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1"/><path d="M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1"/>',
  next: '<path d="M5 12h13M13 6l6 6-6 6"/>',
  back: '<path d="M19 12H6M11 6l-6 6 6 6"/>',
  phone: '<rect x="7" y="2.5" width="10" height="19" rx="2.5"/><path d="M11 18.5h2"/>',
  list: '<path d="M9 6h11M9 12h11M9 18h11"/><circle cx="4.5" cy="6" r="1"/><circle cx="4.5" cy="12" r="1"/><circle cx="4.5" cy="18" r="1"/>',
  spark: '<path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z"/><path d="M19 16l.7 2 2 .7-2 .7-.7 2-.7-2-2-.7 2-.7z"/>',
  users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0"/><path d="M16 4.5a3.5 3.5 0 0 1 0 7M18 14a6.5 6.5 0 0 1 3.5 6"/>',
  bell: '<path d="M6 16v-5a6 6 0 0 1 12 0v5l2 2H4z"/><path d="M10 21h4"/>',
  game: '<rect x="2.5" y="7" width="19" height="11" rx="5"/><path d="M7 10.5v4M5 12.5h4"/><circle cx="15.5" cy="11.5" r="1"/><circle cx="18" cy="14" r="1"/>',
  qr: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><path d="M14 14h3v3h-3zM21 14v.01M14 21h.01M17 21h4v-4"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  minus: '<path d="M5 12h14"/>',
  target: '<circle cx="12" cy="12" r="7"/><circle cx="12" cy="12" r="2"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3"/>',
  sync: '<path d="M20 11a8 8 0 0 0-14.5-4.5L4 8"/><path d="M4 4v4h4"/><path d="M4 13a8 8 0 0 0 14.5 4.5L20 16"/><path d="M20 20v-4h-4"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
  x: '<path d="M6 6l12 12M18 6L6 18"/>',
  mic: '<rect x="9" y="3" width="6" height="11" rx="3"/><path d="M5 11a7 7 0 0 0 14 0M12 18v3"/>',
  left: '<path d="M17 21v-8a3 3 0 0 0-3-3H5"/><path d="M9 6l-4 4 4 4"/>',
  right: '<path d="M7 21v-8a3 3 0 0 1 3-3h9"/><path d="M15 6l4 4-4 4"/>',
  sleft: '<path d="M15 21v-6L7 7"/><path d="M7 13V7h6"/>',
  sright: '<path d="M9 21v-6l8-8"/><path d="M11 7h6v6"/>',
  straight: '<path d="M12 21V4"/><path d="M6 10l6-6 6 6"/>',
  flag: '<path d="M5 21V4M5 4h12l-2.5 4L17 12H5"/>',
  pin: '<path d="M12 22s7-6.2 7-12a7 7 0 1 0-14 0c0 5.8 7 12 7 12z"/><circle cx="12" cy="10" r="2.6"/>',
  rest: '<path d="M8 3h8l5 5v8l-5 5H8l-5-5V8z"/><path d="M12 8v5M12 16v.01"/>',
  coffee: '<path d="M4 9h13v5a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5z"/><path d="M17 10h1.5a2.5 2.5 0 0 1 0 5H17M8 3v3M12 3v3"/>',
  ladder: '<path d="M7 3v18M17 3v18M7 7h10M7 12h10M7 17h10"/>',
  dice: '<rect x="3.5" y="3.5" width="17" height="17" rx="4"/><circle cx="8.5" cy="8.5" r="1.2"/><circle cx="15.5" cy="15.5" r="1.2"/><circle cx="12" cy="12" r="1.2"/>',
  trophy: '<path d="M8 4h8v5a4 4 0 0 1-8 0z"/><path d="M8 6H5a3 3 0 0 0 3 4M16 6h3a3 3 0 0 1-3 4M12 13v4M8 21h8M9 17h6"/>',
  drag: '<circle cx="9" cy="6" r="1.6"/><circle cx="15" cy="6" r="1.6"/><circle cx="9" cy="12" r="1.6"/><circle cx="15" cy="12" r="1.6"/><circle cx="9" cy="18" r="1.6"/><circle cx="15" cy="18" r="1.6"/>',
};
const icon = (name, cls = '') => `<svg class="i ${cls}" viewBox="0 0 24 24">${ICON_PATHS[name] || ''}</svg>`;
const hydrateIcons = (root = document) => $$('i[data-icon]', root).forEach((n) => {
  const s = el(icon(n.dataset.icon, n.className)); n.replaceWith(s);
});

const LOGO = `<svg viewBox="0 0 120 120">
  <rect width="120" height="120" rx="32" fill="#1d222c"/>
  <path d="M60 104V66C60 50 44 46 36 30" fill="none" stroke="#ff8a3d" stroke-width="12" stroke-linecap="round"/>
  <path d="M60 66C60 50 76 46 84 30" fill="none" stroke="#3d8bff" stroke-width="12" stroke-linecap="round"/>
  <circle cx="36" cy="26" r="9" fill="#ff9a40"/><circle cx="84" cy="26" r="9" fill="#3d8bff"/>
</svg>`;

function qrSVG(seed) {
  const n = 25, r = rng(seed * 7919 + 13);
  const inFinder = (x, y) => (x < 8 && y < 8) || (x > 16 && y < 8) || (x < 8 && y > 16);
  let cells = '';
  for (let y = 0; y < n; y++) for (let x = 0; x < n; x++) if (!inFinder(x, y) && r() < 0.47) cells += `<rect x="${x}" y="${y}" width="1" height="1"/>`;
  const f = (x, y) => `<rect x="${x}" y="${y}" width="7" height="7" fill="#111"/><rect x="${x + 1}" y="${y + 1}" width="5" height="5" fill="#fff"/><rect x="${x + 2}" y="${y + 2}" width="3" height="3" fill="#111"/>`;
  return `<svg viewBox="0 0 25 25"><g fill="#111">${cells}</g>${f(0, 0)}${f(18, 0)}${f(0, 18)}</svg>`;
}

/* =========================================================
   data
   ========================================================= */
const SAMPLE_PLAN = `1. 안목해변 커피거리
2. 초당 순두부 (점심)
3. 경포대 산책
4. 숙소 https://naver.me/stjohns_gn`;

const KNOWN = [
  { keys: ['안목'], place: { name: '안목해변 커피거리', addr: '강원 강릉시 창해로14번길 일대', km: 228, min: 160 } },
  { keys: ['순두부'], ask: '비슷한 장소가 3곳 있어요. 어디로 갈까요?', candidates: [
    { name: '초당할머니순두부', addr: '강원 강릉시 초당순두부길 77', km: 4.2, min: 12, note: '평점 4.6 · 대기 약 15분' },
    { name: '동화가든', addr: '강원 강릉시 초당순두부길77번길 15', km: 4.5, min: 13, note: '짬뽕순두부 · 대기 약 40분' },
    { name: '초당순두부마을 공영주차장', addr: '강원 강릉시 초당동 309', km: 4.0, min: 11, note: '주차 후 도보 이동' },
  ] },
  { keys: ['경포'], place: { name: '경포대', addr: '강원 강릉시 경포로 365', km: 3.1, min: 9 } },
  { keys: ['stjohns', '세인트존스'], link: true, place: { name: '세인트존스 호텔', addr: '강원 강릉시 창해로 307', km: 2.4, min: 7 } },
];

const CARS = [
  { id: 1, label: '1호차', driver: '민준', members: ['민준', '지은', '하은'], color: '#3d8bff', me: true, offset: 0 },
  { id: 2, label: '2호차', driver: '서연', members: ['서연', '도윤'], color: '#ff8a3d', offset: 0.035 },
  { id: 3, label: '3호차', driver: '지훈', members: ['지훈', '수아', '태오', '유나'], color: '#2fd08a', offset: -0.085 },
];

const JOIN_SEQ = [
  [1, '지은', 'QR'], [1, '하은', 'QR'], [2, '서연', '코드'], [2, '도윤', 'QR'],
  [3, '지훈', '링크'], [3, '수아', 'QR'], [3, '태오', 'QR'], [3, '유나', 'QR'],
];

/* 지도 위 경로 (1680×1440 viewBox, 140px 격자) */
const ROUTES = [
  { d: [[140, 1260], [420, 1260], [700, 980], [980, 980], [1260, 700], [1260, 420], [1400, 280]], roads: ['영동고속도로 강릉 방면', '대관령IC 방면', '강릉JC 동해고속도로', '경강로', '창해로'] },
  { d: [[280, 1260], [280, 980], [560, 980], [560, 700], [980, 700], [980, 420], [1260, 420]], roads: ['창해로', '난설헌로', '초당순두부길', '초당원길'] },
  { d: [[140, 1120], [560, 1120], [560, 840], [840, 840], [1120, 560], [1120, 280]], roads: ['초당원길', '경포로', '난설헌로', '경포대 방면'] },
  { d: [[420, 1260], [420, 980], [700, 980], [700, 700], [1260, 700], [1260, 420]], roads: ['경포로', '해안로', '창해로', '호텔 진입로'] },
];

const QUICK = ['곧 출발해요', '휴게소 들러요', '먼저 가세요', '좋아요'];
const REPLIES = ['넵! 확인했어요', '좋아요', '알겠어요, 따라갈게요', '오케이~'];

/* =========================================================
   state
   ========================================================= */
const S = {
  parsed: [],
  stops: [],
  joined: new Set(['민준']),
  leg: 0,
  progress: 0,
  running: false,
  pausedAt: null,
  speedMul: 1,
  auto: true,
  zoom: 1,
  geo: null,
  waypoint: null,
  feed: [],
  unread: 0,
  tab: 'cars',
  fired: new Set(),
  game: null,
  gameResults: [],
  stats: { msgs: 0, waypoints: 0, changes: 0, games: 0 },
  timers: [],
};
const later = (fn, ms) => { const t = setTimeout(fn, ms); S.timers.push(t); return t; };
const clearTimers = () => { S.timers.forEach(clearTimeout); S.timers = []; };

/* =========================================================
   stage scaling / screens
   ========================================================= */
function fit() {
  const s = Math.min(innerWidth / 2600, innerHeight / 1480);
  $('#stage').style.setProperty('--s', s);
  S.scale = s;
}
addEventListener('resize', fit);

function go(name) {
  $$('.screen').forEach((s) => s.classList.toggle('active', s.id === `screen-${name}`));
  S.screen = name;
}

function tickClock() { $$('[data-clock]').forEach((n) => (n.textContent = hm())); }

/* =========================================================
   toast / banner / modal
   ========================================================= */
let toastT;
function toast(text, ic = 'check') {
  const t = $('#toast');
  t.innerHTML = `${icon(ic)}<span>${text}</span>`;
  t.classList.add('show');
  clearTimeout(toastT);
  toastT = setTimeout(() => t.classList.remove('show'), 2600);
}

let bannerT;
function banner({ ic = 'bell', color = '#3d8bff', from = '', title, text = '', actions = null, urgent = false, ms = 4500 }) {
  const b = $('#banner');
  b.className = `banner${urgent ? ' urgent' : ''}`;
  b.innerHTML = `
    <div class="b-icon" style="background:${color}22;color:${color}">${icon(ic)}</div>
    <div class="b-main">
      ${from ? `<div class="b-from">${from}</div>` : ''}
      <div class="b-title">${title}</div>
      ${text ? `<div class="b-text">${text}</div>` : ''}
      ${actions ? `<div class="b-actions">${actions.map((a, i) => `<button class="btn ${a.cls || ''}" data-b="${i}">${a.icon ? icon(a.icon) : ''}${a.label}</button>`).join('')}</div>` : ''}
    </div>`;
  if (actions) actions.forEach((a, i) => b.querySelector(`[data-b="${i}"]`).addEventListener('click', () => { hideBanner(); a.onClick(); }));
  requestAnimationFrame(() => b.classList.add('show'));
  clearTimeout(bannerT);
  if (!actions) bannerT = setTimeout(hideBanner, ms);
}
function hideBanner() { $('#banner').classList.remove('show'); }

let modalOpen = null;
function showModal({ ic = 'route', iconColor, title, sub = '', body = '', primary, secondary, voice = false }) {
  closeModal();
  const root = $('#modal-root');
  const node = el(`
    <div class="modal-back"><div class="modal">
      <div class="m-icon" ${iconColor ? `style="background:${iconColor}22;color:${iconColor}"` : ''}>${icon(ic)}</div>
      <div class="m-title">${title}</div>
      ${sub ? `<div class="m-sub">${sub}</div>` : ''}
      ${body ? `<div class="m-body">${body}</div>` : ''}
      ${voice ? `<div class="m-voice" id="m-voice">
          <button class="mic" id="m-mic">${icon('mic')}</button>
          <div class="wave"><i></i><i></i><i></i><i></i><i></i></div>
          <div class="m-voice-text" id="m-voice-text">화면을 누르거나 <b>“응”</b> 이라고 말해 주세요</div>
        </div>` : ''}
      <div class="m-actions">
        ${secondary ? `<button class="btn" data-m="s">${secondary.label}</button>` : ''}
        ${primary ? `<button class="btn primary" data-m="p">${primary.label}</button>` : ''}
      </div>
    </div></div>`);
  root.appendChild(node);
  modalOpen = { primary, secondary, voice };
  if (primary) node.querySelector('[data-m="p"]').addEventListener('click', () => { closeModal(); primary.onClick(); });
  if (secondary) node.querySelector('[data-m="s"]').addEventListener('click', () => { closeModal(); secondary.onClick && secondary.onClick(); });
  if (voice) {
    node.querySelector('#m-mic').addEventListener('click', simulateVoice);
    later(() => $('#m-voice') && $('#m-voice').classList.add('listening'), 500);
  }
}
function closeModal() { $('#modal-root').innerHTML = ''; modalOpen = null; }

async function simulateVoice() {
  if (!modalOpen || !modalOpen.voice) return;
  const m = modalOpen;
  const v = $('#m-voice'), t = $('#m-voice-text');
  v.classList.add('listening');
  t.innerHTML = '듣고 있어요…';
  await wait(1100);
  if (modalOpen !== m) return;
  t.innerHTML = '<b>“응, 진행해”</b> 인식됨';
  v.classList.remove('listening');
  await wait(700);
  if (modalOpen !== m) return;
  closeModal();
  m.primary.onClick();
}

/* =========================================================
   HOME
   ========================================================= */
function joinTripModal() {
  showModal({
    ic: 'link',
    title: '초대 코드로 참여',
    sub: '호스트에게 받은 코드를 입력하면 같은 여정이 이 차량 내비에 등록돼요.',
    body: `<div class="join-code">${'GLML'.split('').map((c) => `<span>${c}</span>`).join('')}<i>-</i>${'7K2Q'.split('').map((c) => `<span>${c}</span>`).join('')}</div>`,
    secondary: { label: '취소' },
    primary: { label: '참여하기', onClick: () => {
      S.stops = presetStops();
      JOIN_SEQ.forEach(([, n]) => S.joined.add(n));
      openInvite(true);
      toast('강릉 1박 2일 여행에 참여했어요');
    } },
  });
}

function presetStops() {
  return KNOWN.map((k, i) => ({ id: `s${i}`, ...(k.place || k.candidates[0]) }));
}

/* =========================================================
   PLAN (일정 등록 + AI 인식)
   ========================================================= */
function openPlan() {
  go('plan');
  S.parsed = [];
  $('#plan-input').value = '';
  renderParse();
}

async function pullFromPhone() {
  const ta = $('#plan-input');
  ta.value = '';
  ta.classList.add('typing');
  toast('휴대폰에서 일정을 받았어요', 'phone');
  for (let i = 0; i <= SAMPLE_PLAN.length; i++) {
    ta.value = SAMPLE_PLAN.slice(0, i);
    await wait(14);
  }
  ta.classList.remove('typing');
}

function parseLines(text) {
  return text.split('\n').map((l) => l.trim()).filter(Boolean).map((raw, i) => {
    const url = (raw.match(/https?:\/\/\S+/) || [])[0];
    const note = (raw.match(/\(([^)]+)\)/) || [])[1];
    const clean = raw.replace(/^\s*(\d+[.)]|[-•*])\s*/, '').replace(/https?:\/\/\S+/, '').replace(/\([^)]*\)/, '').trim();
    const hit = KNOWN.find((k) => k.keys.some((key) => raw.toLowerCase().includes(key)));
    const base = { id: `p${i}`, raw, note };
    if (hit && hit.candidates) return { ...base, status: 'check', ask: hit.ask, candidates: hit.candidates, name: clean };
    if (hit) return { ...base, status: hit.link || url ? 'link' : 'ok', ...hit.place };
    if (url) return { ...base, status: 'link', name: '지도 링크 장소', addr: url.replace(/^https?:\/\//, ''), km: 5, min: 14 };
    return {
      ...base, status: 'check', name: clean || raw, ask: '정확한 위치를 찾지 못했어요. 이 장소가 맞나요?',
      candidates: [
        { name: clean, addr: '강원 강릉시 · 검색 결과 1', km: 5.2, min: 14, note: '가장 가까움' },
        { name: `${clean} 2호점`, addr: '강원 강릉시 · 검색 결과 2', km: 7.8, min: 19, note: '' },
      ],
    };
  });
}

async function runParse() {
  const text = $('#plan-input').value.trim();
  if (!text) { toast('먼저 일정을 입력하거나 휴대폰에서 받아오세요', 'phone'); return; }
  const list = $('#parse-list');
  list.innerHTML = `<div class="parse-loading">${icon('spark')}<div>AI가 일정을 읽고 있어요…</div></div>`;
  $('#btn-to-invite').disabled = true;
  $('#parse-count').textContent = '';
  await wait(1500);
  S.parsed = parseLines(text);
  renderParse(true);
}

function renderParse(animate = false) {
  const list = $('#parse-list');
  if (!S.parsed.length) {
    list.innerHTML = `<div class="empty">${icon('route')}입력한 일정을 AI가 읽고<br>목적지를 순서대로 정리해요</div>`;
    $('#btn-to-invite').disabled = true;
    $('#parse-count').textContent = '';
    return;
  }
  const pending = S.parsed.filter((p) => p.status === 'check').length;
  $('#parse-count').textContent = pending ? `확인 필요 ${pending}곳` : `${S.parsed.length}곳`;
  list.innerHTML = S.parsed.map((p, i) => {
    const tag = p.status === 'ok' ? '<span class="tag ok">확인됨</span>'
      : p.status === 'link' ? '<span class="tag link">지도 링크 인식</span>'
      : '<span class="tag check">확인 필요</span>';
    const note = p.note ? `<span class="tag note">${p.note}</span>` : '';
    const body = p.status === 'check'
      ? `<div class="p-raw">입력: ${p.raw}</div><div class="p-ask">${p.ask}</div>
         <div class="cands">${p.candidates.map((c, ci) => `<button class="cand" data-pick="${i}:${ci}">${icon('pin')}<b>${c.name}</b><span>${c.note || ''}<br>${c.addr}</span></button>`).join('')}</div>`
      : `<div class="p-addr">${p.addr}</div>`;
    return `<div class="p-item ${p.status}" style="animation-delay:${animate ? i * 0.18 : 0}s">
      <div class="p-num">${i + 1}</div>
      <div class="p-main"><div class="p-name">${p.name}${tag}${note}</div>${body}</div>
    </div>`;
  }).join('');
  $('#btn-to-invite').disabled = pending > 0;
}

function pickCandidate(i, ci) {
  const p = S.parsed[i];
  const c = p.candidates[ci];
  S.parsed[i] = { id: p.id, raw: p.raw, note: p.note, status: 'ok', ...c };
  renderParse();
  toast(`${c.name}(으)로 확정했어요`);
}

/* =========================================================
   INVITE
   ========================================================= */
function openInvite(guest = false) {
  if (!guest) S.stops = S.parsed.map((p, i) => ({ id: `s${i}`, name: p.name, addr: p.addr, km: p.km, min: p.min }));
  go('invite');
  $('#trip-preview').innerHTML = S.stops.map((s, i) => `<div class="tp-row"><span>${i + 1}</span>${s.name}<em>${fmtKm(s.km)}</em></div>`).join('');
  renderMembers();
  if (!guest) {
    S.joined = new Set(['민준']);
    renderMembers();
    JOIN_SEQ.forEach(([, name], i) => later(() => { S.joined.add(name); renderMembers(); }, 900 + i * 650));
  }
}

function renderMembers() {
  const total = CARS.reduce((n, c) => n + c.members.length, 0);
  const joinedCars = CARS.filter((c) => S.joined.has(c.driver)).length;
  $('#join-count').textContent = `차량 ${joinedCars}/3 · ${S.joined.size}/${total}명`;
  $('#member-groups').innerHTML = CARS.map((c) => {
    const on = S.joined.has(c.driver);
    const via = (n) => (JOIN_SEQ.find((j) => j[1] === n) || [, , '호스트'])[2];
    return `<div class="mg ${on ? '' : 'waiting'}">
      <div class="mg-head"><span class="car-chip" style="background:${c.color}">${c.id}</span>${c.label} · ${c.driver}${c.me ? ' (나)' : ''}
        <em>${on ? '내비 목적지 등록됨' : '참여 대기 중'}</em></div>
      <div class="mg-members">${c.members.filter((m) => S.joined.has(m)).map((m) => `<span class="mchip">${m}<small>${via(m)}</small></span>`).join('')}</div>
    </div>`;
  }).join('');
}

/* =========================================================
   DRIVE — geometry
   ========================================================= */
function buildGeo(route) {
  const pts = route.d.map(([x, y]) => ({ x, y }));
  const cum = [0];
  for (let i = 1; i < pts.length; i++) cum.push(cum[i - 1] + Math.hypot(pts[i].x - pts[i - 1].x, pts[i].y - pts[i - 1].y));
  const total = cum[cum.length - 1];
  const turns = [];
  for (let i = 1; i < pts.length - 1; i++) {
    const a = { x: pts[i].x - pts[i - 1].x, y: pts[i].y - pts[i - 1].y };
    const b = { x: pts[i + 1].x - pts[i].x, y: pts[i + 1].y - pts[i].y };
    const cross = a.x * b.y - a.y * b.x;
    const ang = Math.abs(Math.atan2(cross, a.x * b.x + a.y * b.y)) * 180 / Math.PI;
    const dir = cross > 0 ? (ang < 60 ? 'sright' : 'right') : (ang < 60 ? 'sleft' : 'left');
    turns.push({ at: cum[i] / total, dir, road: route.roads[(i - 1) % route.roads.length] });
  }
  return { pts, cum, total, turns, d: 'M' + pts.map((p) => `${p.x} ${p.y}`).join(' L ') };
}

function pointAt(geo, f) {
  const L = clamp(f, 0, 1) * geo.total;
  let i = 1;
  while (i < geo.cum.length - 1 && geo.cum[i] < L) i++;
  const a = geo.pts[i - 1], b = geo.pts[i];
  const seg = geo.cum[i] - geo.cum[i - 1] || 1;
  const t = (L - geo.cum[i - 1]) / seg;
  return { x: a.x + (b.x - a.x) * t, y: a.y + (b.y - a.y) * t, ang: Math.atan2(b.y - a.y, b.x - a.x) * 180 / Math.PI };
}

function mapBackground() {
  const r = rng(42);
  let s = '<rect width="1680" height="1440" fill="#232a35"/>';
  for (let y = 0; y < 11; y++) for (let x = 0; x < 12; x++) {
    const v = r();
    const fill = v < 0.08 ? '#18271f' : v < 0.5 ? '#171c24' : '#191f28';
    s += `<rect x="${x * 140 + 13}" y="${y * 140 + 13}" width="114" height="114" rx="12" fill="${fill}"/>`;
  }
  s += '<path d="M0 520 C 260 480 420 620 700 580 S 1120 460 1520 520" fill="none" stroke="#16283a" stroke-width="54" stroke-linecap="round"/>';
  s += '<path d="M1500 0 C 1460 300 1560 600 1520 900 S 1600 1300 1570 1440 L 1680 1440 L 1680 0 Z" fill="#132233"/>';
  s += '<ellipse cx="1180" cy="200" rx="150" ry="80" fill="#16283a"/>';
  ROUTES.forEach((rt) => { s += `<path d="M${rt.d.map((p) => p.join(' ')).join(' L ')}" fill="none" stroke="#2d3543" stroke-width="20" stroke-linecap="round" stroke-linejoin="round"/>`; });
  const labels = [[180, 470, '남대천'], [1100, 205, '경포호'], [760, 1080, '강릉역'], [640, 820, '초당동'], [1310, 120, '안목항'], [180, 1380, '강릉IC']];
  labels.forEach(([x, y, t]) => { s += `<text class="map-label" x="${x}" y="${y}">${t}</text>`; });
  s += '<text class="map-label sea" x="1596" y="980">동</text><text class="map-label sea" x="1596" y="1036">해</text>';
  return s;
}

/* =========================================================
   DRIVE — leg lifecycle
   ========================================================= */
const curStop = () => S.stops[S.leg];
const legDuration = (stop) => clamp(stop.min * 0.5, 26, 80);

function openDrive() {
  clearTimers();
  go('drive');
  $('#map-bg').innerHTML = mapBackground();
  S.leg = 0; S.progress = 0; S.running = false; S.waypoint = null; S.zoom = 1;
  S.feed = []; S.unread = 0; S.fired = new Set(); S.gameResults = []; S.game = null;
  S.stats = { msgs: 0, waypoints: 0, changes: 0, games: 0 };
  setupLeg();
  renderQuick();
  setTab('cars');
  pushFeed({ ic: 'users', color: '#2fd08a', title: '일행 3대 · 9명이 모두 참여했어요', text: '모든 차량 내비에 같은 일정이 등록됐어요.' });
  S.unread = 0; updateBadge();
  renderGame();
  later(() => askStart(true), 600);
}

function setupLeg() {
  const route = ROUTES[S.leg % ROUTES.length];
  S.geo = buildGeo(route);
  $('#route-casing').setAttribute('d', S.geo.d);
  $('#route-line').setAttribute('d', S.geo.d);
  $('#route-done').setAttribute('d', S.geo.d);
  renderPins();
  renderCarMarkers();
  renderDest();
  renderCars();
  renderPlanList();
  $('#btn-next').classList.remove('pulse');
  frame(true);
}

function askStart(first) {
  const s = curStop();
  showModal({
    ic: 'route',
    title: first ? '일정을 진행하시겠습니까?' : '다음 일정을 진행하시겠습니까?',
    sub: first ? '수락하면 1번 목적지가 일행 모든 차량 내비에 동시에 안내돼요.' : '수락하면 일행 모든 차량의 내비 목적지가 함께 바뀌어요.',
    body: `<div class="m-dest"><span class="p-num">${S.leg + 1}</span><div><b>${s.name}</b><span>${s.addr}</span></div><em>약<br><b>${fmtMin(s.min)}</b></em></div>
      ${first ? '' : `<div class="m-cars">${CARS.map((c) => {
        const pc = c.me ? 1 : clamp(1 + c.offset, 0, 1);
        return `<div><span class="car-chip" style="background:${c.color}">${c.id}</span>${c.driver}<em class="${pc >= 1 ? 'ok' : ''}">${pc >= 1 ? '도착' : `${fmtMin((1 - pc) * S.stops[S.leg - 1].min)} 남음`}</em></div>`;
      }).join('')}</div>`}`,
    voice: true,
    secondary: { label: '나중에', onClick: () => {
      S.awaiting = { first };
      $('#btn-next').classList.add('pulse');
      toast('준비되면 ‘다음 일정’ 버튼을 눌러주세요', 'route');
    } },
    primary: { label: first ? '응, 출발할게요' : '응, 다음 일정으로', onClick: () => { S.awaiting = null; $('#btn-next').classList.remove('pulse'); startLeg(first); } },
  });
}

function startLeg(first) {
  S.running = true;
  const s = curStop();
  pushFeed({ ic: 'route', color: '#3d8bff', title: `${S.leg + 1}번 목적지 안내 시작`, text: `${s.name} · 3대 차량 내비에 동시에 등록됐어요.` });
  toast(first ? '일행 3대 내비에 목적지가 등록됐어요' : `전 차량 목적지가 ‘${s.name}’(으)로 바뀌었어요`, 'sync');
}

function nextLegRequest() {
  if (S.awaiting) { const { first } = S.awaiting; S.awaiting = null; askStart(first); return; }
  const isLast = S.leg >= S.stops.length - 1;
  if (S.progress >= 1) {
    if (isLast) return finishTrip();
    S.leg++; S.progress = 0; S.waypoint = null;
    setupLeg();
    askStart(false);
    return;
  }
  if (isLast) { toast('마지막 목적지로 이동 중이에요', 'flag'); return; }
  const next = S.stops[S.leg + 1];
  showModal({
    ic: 'next',
    title: '다음 일정으로 바로 넘어갈까요?',
    sub: `아직 ‘${curStop().name}’에 도착하지 않았어요.<br>넘어가면 일행 모든 차량이 ‘${next.name}’(으)로 안내돼요.`,
    voice: true,
    secondary: { label: '취소' },
    primary: { label: '응, 넘어갈게요', onClick: () => {
      pushFeed({ ic: 'sync', color: '#ff8a3d', title: `${curStop().name} 건너뜀`, text: '호스트가 다음 일정으로 넘겼어요.' });
      S.leg++; S.progress = 0; S.waypoint = null;
      setupLeg();
      startLeg(false);
    } },
  });
}

function arrive() {
  S.running = false;
  const s = curStop();
  const isLast = S.leg >= S.stops.length - 1;
  pushFeed({ ic: 'flag', color: '#2fd08a', title: `${s.name} 도착`, text: CARS.filter((c) => !c.me).map((c) => `${c.label} ${1 + c.offset >= 1 ? '도착' : `${fmtMin((-c.offset) * s.min)} 뒤 도착`}`).join(' · ') });
  frame(true);
  if (isLast) {
    showModal({
      ic: 'flag', iconColor: '#2fd08a',
      title: `${s.name}에 도착했어요`,
      sub: '마지막 목적지예요. 일행 모두 수고하셨어요!',
      primary: { label: '여정 마치기', onClick: finishTrip },
      secondary: { label: '닫기' },
    });
    return;
  }
  const upcoming = S.stops[S.leg + 1];
  const legAt = S.leg;
  later(() => {
    if (S.leg !== legAt || S.progress < 1) return;
    S.leg++; S.progress = 0; S.waypoint = null;
    setupLeg();
    askStart(false);
  }, 1400);
  banner({ ic: 'flag', color: '#2fd08a', title: `${s.name}에 도착했어요`, text: `다음 일정 · ${upcoming.name}` });
}

function finishTrip() {
  S.running = false;
  closeModal();
  hideBanner();
  const totalKm = S.stops.reduce((n, s) => n + s.km, 0);
  const stats = [
    [S.stops.length, '방문 장소'], [`${Math.round(totalKm)}`, '총 이동 km'], [3, '함께한 차량'],
    [S.stats.waypoints, '정차 경유'], [S.stats.msgs, '간편 메세지'], [S.stats.changes + S.stats.games, '일정 변경 · 게임'],
  ];
  $('#summary-stats').innerHTML = stats.map(([v, l]) => `<div class="ss"><b>${v}</b><span>${l}</span></div>`).join('');
  $('#summary-timeline').innerHTML = ['출발', ...S.stops.map((s) => s.name)].map((n) => `<span>${n}</span>`).join('<em>→</em>');
  go('summary');
}

/* =========================================================
   DRIVE — render
   ========================================================= */
function renderPins() {
  const g = S.geo;
  const a = g.pts[0], b = g.pts[g.pts.length - 1];
  const name = curStop().name;
  const w = name.length * 27 + 44;
  let s = `<circle cx="${a.x}" cy="${a.y}" r="16" fill="#fff" stroke="#3d8bff" stroke-width="8"/>`;
  if (S.waypoint && !S.waypoint.passed) {
    const p = pointAt(g, S.waypoint.at);
    const lw = S.waypoint.short.length * 25 + 40;
    s += `<g transform="translate(${p.x},${p.y})">
      <circle r="26" fill="#ffc24a" stroke="#0d1016" stroke-width="6"/>
      <path d="M-7 -8 h14 M0 -8 v16" stroke="#0d1016" stroke-width="5" stroke-linecap="round"/>
      <g transform="translate(36,-62)"><rect width="${lw}" height="50" rx="14" fill="#ffc24a"/><text x="20" y="34" class="pin-label" style="fill:#1a1305">${S.waypoint.short}</text></g>
    </g>`;
  }
  s += `<g transform="translate(${b.x},${b.y})">
    <path d="M0 0 C -10 -22 -34 -36 -34 -62 A 34 34 0 1 1 34 -62 C 34 -36 10 -22 0 0 Z" fill="#ff7a3d" stroke="#0d1016" stroke-width="5"/>
    <circle cy="-62" r="13" fill="#0d1016"/>
    <g transform="translate(${b.x + w + 60 > 1680 ? -w - 50 : 50},-130)"><rect width="${w}" height="56" rx="16" fill="#0d1016" opacity=".9"/><text x="22" y="38" class="pin-label">${name}</text></g>
  </g>`;
  $('#pins').innerHTML = s;
}

function renderCarMarkers() {
  $('#cars').innerHTML = CARS.slice().reverse().map((c) => c.me
    ? `<g id="car-${c.id}"><circle r="46" fill="#3d8bff" opacity=".22"/><circle r="32" fill="#fff"/><g class="heading"><path d="M0 -20 L15 16 L0 8 L-15 16 Z" fill="#3d8bff"/></g></g>`
    : `<g id="car-${c.id}"><circle r="26" fill="${c.color}" stroke="#0d1016" stroke-width="6"/><text y="10" text-anchor="middle" class="car-label" style="fill:#0d1016;font-size:28px;font-weight:800">${c.id}</text>
        <g class="lbl-g" transform="translate(36,-24)"><rect class="lbl-bg" width="210" height="48" rx="14" fill="#0d1016" opacity=".88"/><text class="car-label lbl" x="18" y="33"></text></g></g>`).join('');
}

function carProgress(c) { return clamp(S.progress + c.offset, 0, 1); }
function wpExtra() { return S.waypoint && !S.waypoint.passed ? S.waypoint.extraMin : 0; }

function renderDest() {
  const s = curStop();
  $('#dest-step').textContent = `일정 ${S.leg + 1} / ${S.stops.length}`;
  $('#dest-name').textContent = s.name;
  $('#dest-addr').textContent = s.addr;
  const n = S.stops[S.leg + 1];
  $('#dest-next').textContent = n ? n.name : '마지막 목적지';
  $('#btn-next').innerHTML = `${S.leg >= S.stops.length - 1 ? '여정 마치기' : '다음 일정'}${icon('next')}`;
}

function renderCars() {
  $('#pane-cars').innerHTML = `
    <div class="convoy">
      <div class="convoy-head"><span>일행 진행 상황</span><span id="convoy-dest">${curStop().name}</span></div>
      <div class="convoy-track"><div class="convoy-fill" id="cv-fill"></div>
        <div id="cv-wp"></div>
        ${CARS.map((c) => `<div class="cv-dot" id="cv-${c.id}" style="background:${c.color}">${c.id}</div>`).join('')}
        <div class="cv-flag">${icon('flag')}</div>
      </div>
    </div>
    ${CARS.map((c) => `
      <div class="car-row">
        <span class="car-chip" style="background:${c.color}">${c.id}</span>
        <div class="cr-main">
          <div class="cr-name">${c.label} · ${c.driver}${c.me ? '<span class="me">내 차</span>' : ''}</div>
          <div class="cr-sub" id="cr-sub-${c.id}"></div>
        </div>
        <div class="cr-right" id="cr-r-${c.id}"><b></b><span></span></div>
      </div>`).join('')}`;
}

let lastText = 0;
function frame(force = false) {
  if (S.screen !== 'drive' || !S.geo) return;
  const g = S.geo, s = curStop();
  const me = pointAt(g, S.progress);
  $('#route-done').setAttribute('stroke-dasharray', `${S.progress * g.total} ${g.total * 2}`);

  CARS.forEach((c) => {
    const p = pointAt(g, carProgress(c));
    const n = $(`#car-${c.id}`);
    if (!n) return;
    n.setAttribute('transform', `translate(${p.x.toFixed(1)},${p.y.toFixed(1)})`);
    if (c.me) n.querySelector('.heading').setAttribute('transform', `rotate(${p.ang + 90})`);
  });

  const svg = $('#map');
  if (S.zoom > 1) {
    const w = 1680 / S.zoom, h = 1440 / S.zoom;
    const x = clamp(me.x - w * 0.42, 0, 1680 - w), y = clamp(me.y - h * 0.55, 0, 1440 - h);
    svg.setAttribute('viewBox', `${x.toFixed(1)} ${y.toFixed(1)} ${w} ${h}`);
  } else svg.setAttribute('viewBox', '0 0 1680 1440');

  const now = performance.now();
  if (!force && now - lastText < 200) return;
  lastText = now;

  const remainMin = (1 - S.progress) * s.min + wpExtra();
  const remainKm = (1 - S.progress) * s.km;
  $('#eta-time').textContent = hm(remainMin);
  $('#eta-remain').textContent = fmtMin(remainMin);
  $('#eta-dist').textContent = fmtKm(remainKm);
  $('#dest-eta').textContent = hm(remainMin);
  $('#dest-remain').textContent = fmtMin(remainMin);
  $('#dest-dist').textContent = fmtKm(remainKm);
  const highway = s.km > 50;
  $('#speed').textContent = S.running ? Math.round((highway ? 98 : 42) + Math.sin(now / 900) * (highway ? 6 : 8)) : 0;

  const t = g.turns.find((x) => x.at > S.progress + 0.001);
  if (S.progress >= 1) {
    $('#turn-arrow').innerHTML = icon('flag');
    $('#turn-dist').textContent = '도착';
    $('#turn-road').textContent = s.name;
  } else if (t) {
    $('#turn-arrow').innerHTML = icon(t.dir);
    $('#turn-dist').textContent = fmtKm((t.at - S.progress) * s.km);
    $('#turn-road').textContent = t.road;
  } else {
    $('#turn-arrow').innerHTML = icon('flag');
    $('#turn-dist').textContent = fmtKm(remainKm);
    $('#turn-road').textContent = `${s.name} 도착`;
  }

  $('#cv-fill') && ($('#cv-fill').style.width = `${S.progress * 100}%`);
  const wpEl = $('#cv-wp');
  if (wpEl) wpEl.innerHTML = S.waypoint && !S.waypoint.passed ? `<div class="cv-wp" style="left:${S.waypoint.at * 100}%"></div>` : '';
  CARS.forEach((c) => {
    const pc = carProgress(c);
    const dot = $(`#cv-${c.id}`);
    if (dot) dot.style.left = `calc(${pc * 100}% * .94)`;
    const rem = (1 - pc) * s.min + (S.waypoint && !S.waypoint.passed && pc < S.waypoint.at ? S.waypoint.extraMin : 0);
    const sub = $(`#cr-sub-${c.id}`), right = $(`#cr-r-${c.id}`);
    if (sub) {
      const gap = (pc - S.progress) * s.km;
      sub.textContent = c.me
        ? `${c.members.join(' · ')} · ${S.running ? '주행 중' : S.progress >= 1 ? '도착' : '대기'}`
        : `${c.members.join(' · ')} · ${Math.abs(gap) < 0.05 ? '내 차와 함께' : `내 차보다 ${fmtKm(Math.abs(gap))} ${gap > 0 ? '앞' : '뒤'}`}`;
      right.className = `cr-right ${pc >= 1 ? 'arrived' : !c.me && pc < S.progress - 0.05 ? 'late' : ''}`;
      right.querySelector('b').textContent = pc >= 1 ? '도착' : fmtMin(rem);
      right.querySelector('span').textContent = pc >= 1 ? '목적지 도착' : `${hm(rem)} 도착 예정`;
    }
    if (!c.me) {
      const lbl = $(`#car-${c.id} .lbl`);
      if (lbl) {
        const txt = `${c.driver} · ${pc >= 1 ? '도착' : fmtMin(rem)}`;
        const lw = txt.length * 22 + 36;
        lbl.textContent = txt;
        $(`#car-${c.id} .lbl-bg`).setAttribute('width', lw);
        if (c.offset < 0) $(`#car-${c.id} .lbl-g`).setAttribute('transform', `translate(${-lw - 36},-24)`);
      }
    }
  });
}

/* =========================================================
   main loop
   ========================================================= */
let lastTs = 0;
function loop(ts) {
  const dt = Math.min(0.1, (ts - lastTs) / 1000 || 0);
  lastTs = ts;
  if (S.screen === 'drive' && S.running && !modalOpen) {
    const s = curStop();
    S.progress = Math.min(1, S.progress + (dt * S.speedMul) / legDuration(s));
    const wp = S.waypoint;
    if (wp && !wp.passed && S.progress >= wp.at) {
      S.progress = wp.at;
      wp.passed = true;
      S.running = false;
      reachWaypoint(wp);
    }
    autoScenario();
    if (S.progress >= 1) arrive();
  }
  frame();
  requestAnimationFrame(loop);
}

function autoScenario() {
  if (!S.auto) return;
  const once = (key, cond, fn) => { if (cond && !S.fired.has(key)) { S.fired.add(key); fn(); } };
  once('msg0', S.leg === 0 && S.progress > 0.1, () => incomingMsg(3, '지훈', '저희 기름 넣고 따라갈게요!'));
  once('stop0', S.leg === 0 && S.progress > 0.24, () => incomingStop());
  once('msg1', S.leg === 1 && S.progress > 0.35, () => incomingMsg(2, '서연', '순두부집 웨이팅 미리 걸어둘게요'));
  once('game2', S.leg === 2 && S.progress > 0.3, () => runLadder('동승자 웹'));
}

/* =========================================================
   feed
   ========================================================= */
let feedId = 0;
function pushFeed(item) {
  const f = { id: ++feedId, time: hm(), ...item };
  S.feed.unshift(f);
  if (S.tab !== 'feed') S.unread++;
  updateBadge();
  renderFeed();
  return f;
}
function updateBadge() {
  const b = $('#feed-badge');
  b.textContent = S.unread;
  b.classList.toggle('show', S.unread > 0);
}
function renderFeed() {
  $('#pane-feed').innerHTML = S.feed.map((f) => `
    <div class="feed-item ${f.actions && !f.state ? 'new' : ''}">
      <div class="f-icon" style="background:${f.color}22;color:${f.color}">${icon(f.ic)}</div>
      <div class="f-main">
        <div class="f-top"><div class="f-title">${f.title}</div><div class="f-time">${f.time}</div></div>
        ${f.text ? `<div class="f-text">${f.text}</div>` : ''}
        ${f.actions && !f.state ? `<div class="f-actions">${f.actions.map((a, i) => `<button class="btn ${a.cls || ''}" data-feed="${f.id}:${i}">${a.label}</button>`).join('')}</div>` : ''}
        ${f.state ? `<div class="f-state ${f.state.ok ? 'ok' : 'no'}">${f.state.text}</div>` : ''}
      </div>
    </div>`).join('');
}

/* =========================================================
   events: 정차 요청 / 메세지 / 일정 변경
   ========================================================= */
function incomingStop() {
  if (S.screen !== 'drive') return;
  if (S.waypoint && !S.waypoint.passed) { toast('이미 경유지가 추가되어 있어요', 'rest'); return; }
  const s = curStop();
  const highway = s.km > 50;
  const wp = highway
    ? { name: '평창휴게소 (강릉 방향)', short: '평창휴게소', dist: '12km', min: 8, extraMin: 15 }
    : { name: 'GS25 강릉초당점', short: 'GS25 초당점', dist: '600m', min: 2, extraMin: 6 };
  if (S.progress > 0.85) { toast('목적지가 가까워 정차 요청을 보류했어요', 'rest'); return; }
  let item;
  const accept = () => {
    if (S.progress > 0.88) { item.state = { ok: false, text: '목적지가 가까워 경유지를 추가하지 않았어요' }; renderFeed(); return; }
    S.waypoint = { ...wp, at: Math.min(S.progress + 0.14, 0.92), passed: false };
    S.stats.waypoints++;
    item.state = { ok: true, text: `수락함 · ${wp.short} 경유지 추가` };
    renderFeed(); renderPins(); frame(true);
    toast(`전 차량 경로에 ‘${wp.short}’ 경유지 추가`, 'sync');
    later(() => incomingMsg(2, '서연', '감사해요!!'), 2500);
  };
  const decline = () => {
    item.state = { ok: false, text: '거절함 · “다음 휴게소에서 쉬어요” 전송' };
    renderFeed();
    toast('서연님께 “다음 휴게소에서 쉬어요” 전송', 'msg');
  };
  item = pushFeed({
    ic: 'rest', color: '#ff8a3d', title: '2호차 서연 · 정차 요청',
    text: `화장실이 급해요. 가까운 ${wp.name} (${wp.dist}, 약 ${wp.min}분)`,
    actions: [{ label: '거절', cls: 'ghost', onClick: decline }, { label: '수락 · 경유지 추가', cls: 'primary', onClick: accept }],
  });
  banner({
    ic: 'rest', color: '#ff8a3d', urgent: true,
    from: '2호차 · 동승자 서연 (모바일 웹)', title: '휴게소 정차 요청',
    text: `“화장실이 급해요” · ${wp.name}<br>${wp.dist} 앞 · 약 ${wp.min}분 · 수락 시 일행 모든 차량 경로에 추가`,
    actions: [{ label: '거절', cls: 'ghost', icon: 'x', onClick: decline }, { label: '수락 · 경유지 추가', cls: 'primary', icon: 'check', onClick: accept }],
  });
}

function reachWaypoint(wp) {
  pushFeed({ ic: 'rest', color: '#ffc24a', title: `${wp.short} 도착`, text: '일행이 모이면 다시 출발해요.' });
  banner({ ic: 'rest', color: '#ffc24a', title: `${wp.short}에 도착했어요`, text: '잠시 쉬었다가 자동으로 다시 안내해요.', ms: 3500 });
  renderPins();
  later(() => { S.running = true; toast(`${curStop().name}(으)로 다시 안내해요`, 'route'); }, 4000 / Math.sqrt(S.speedMul));
}

function incomingMsg(carId, name, text) {
  if (S.screen !== 'drive') return;
  const c = CARS.find((x) => x.id === carId);
  pushFeed({ ic: 'msg', color: c.color, title: `${c.label} ${name}`, text: `“${text}”` });
  banner({ ic: 'msg', color: c.color, from: `${c.label} · ${name}`, title: `“${text}”`, ms: 4000 });
}

function sendQuick(text, btn) {
  S.stats.msgs++;
  pushFeed({ ic: 'msg', color: '#3d8bff', title: '나 → 일행 전체', text: `“${text}”` });
  if (S.tab !== 'feed') { S.unread = Math.max(0, S.unread - 1); updateBadge(); }
  toast(`일행 2대에 “${text}” 전송`, 'msg');
  btn.classList.add('sent');
  setTimeout(() => btn.classList.remove('sent'), 1200);
  const c = CARS[1 + Math.floor(Math.random() * 2)];
  later(() => incomingMsg(c.id, c.driver, REPLIES[Math.floor(Math.random() * REPLIES.length)]), 2600);
}

function renderQuick() {
  $('#quick-chips').innerHTML = QUICK.map((q) => `<button class="qchip">${q}</button>`).join('');
}

/* ---------- 일정 (드래그 재정렬) */
function renderPlanList() {
  $('#pane-plan').innerHTML = `
    <div class="plan-hint">${icon('sync')}다음 일정은 드래그해서 순서를 바꿀 수 있어요. 바꾸면 일행 모든 차량에 동기화돼요.</div>
    <div class="stop-list" id="stop-list">
      ${S.stops.map((s, i) => {
        const st = i < S.leg ? 'done' : i === S.leg ? 'current' : 'next';
        return `<div class="stop ${st}" data-idx="${i}">
          <span class="s-num">${i < S.leg ? '✓' : i + 1}</span>
          <div class="s-main"><div class="s-name">${s.name}</div><div class="s-meta">${s.addr} · ${fmtKm(s.km)}</div></div>
          ${st === 'next' ? `<span class="s-handle" data-handle="${i}">${icon('drag')}</span>` : `<span class="s-state">${st === 'done' ? '완료' : '이동 중'}</span>`}
        </div>`;
      }).join('')}
    </div>`;
}

function startDrag(e, handle) {
  e.preventDefault();
  const list = $('#stop-list');
  const item = handle.closest('.stop');
  const from = +item.dataset.idx;
  const items = $$('.stop', list);
  const rects = items.map((n) => n.getBoundingClientRect());
  const startY = e.clientY;
  const h = rects[from].height + 14 * S.scale;
  let to = from;
  item.classList.add('dragging');
  try { handle.setPointerCapture(e.pointerId); } catch { /* 합성 이벤트 */ }

  const move = (ev) => {
    const dy = ev.clientY - startY;
    item.style.transform = `translateY(${dy / S.scale}px)`;
    const centerY = rects[from].top + rects[from].height / 2 + dy;
    to = from;
    for (let i = S.leg + 1; i < items.length; i++) {
      const mid = rects[i].top + rects[i].height / 2;
      if (i < from && centerY < mid) { to = i; break; }
      if (i > from && centerY > mid) to = i;
    }
    items.forEach((n, i) => {
      if (i === from) return;
      let shift = 0;
      if (from < to && i > from && i <= to) shift = -h;
      if (from > to && i >= to && i < from) shift = h;
      n.style.transform = shift ? `translateY(${shift / S.scale}px)` : '';
    });
  };
  const up = () => {
    handle.removeEventListener('pointermove', move);
    handle.removeEventListener('pointerup', up);
    handle.removeEventListener('pointercancel', up);
    if (to !== from) reorderStop(from, to, '나(호스트)');
    else renderPlanList();
  };
  handle.addEventListener('pointermove', move);
  handle.addEventListener('pointerup', up);
  handle.addEventListener('pointercancel', up);
}

function reorderStop(from, to, by) {
  const [m] = S.stops.splice(from, 1);
  S.stops.splice(to, 0, m);
  S.stats.changes++;
  renderPlanList();
  renderDest();
  pushFeed({ ic: 'sync', color: '#2fd08a', title: '일정 순서 변경 · 전 차량 동기화', text: `${by}: ‘${m.name}’을(를) ${to + 1}번째로 옮겼어요.` });
  toast('일정이 바뀌었어요 · 2호차, 3호차 동기화 완료', 'sync');
}

function demoChange() {
  if (S.screen !== 'drive') return;
  const a = S.leg + 1, b = S.leg + 2;
  if (b >= S.stops.length) { toast('바꿀 수 있는 다음 일정이 부족해요', 'list'); return; }
  reorderStop(b, a, '호스트 모바일 웹');
  banner({ ic: 'sync', color: '#2fd08a', from: '호스트 모바일 웹', title: '일정 순서가 바뀌었어요', text: `다음 일정 · ${S.stops[a].name}` });
}

/* ---------- 게임 */
function renderGame() {
  if (S.game) return;
  $('#pane-game').innerHTML = `
    <div class="game-cards">
      <button class="game-card" data-game="ladder"><span class="gc-icon" style="background:#ffc24a22;color:#ffc24a">${icon('ladder')}</span><b>사다리타기</b><span>누가 커피 쏠까?</span></button>
      <button class="game-card" data-game="draw"><span class="gc-icon" style="background:#3d8bff22;color:#3d8bff">${icon('dice')}</span><b>뽑기</b><span>다음 구간 DJ 뽑기</span></button>
    </div>
    <div class="game-note">운전자는 결과만 확인하면 돼요. 동승자는 모바일 웹에서 함께 참여할 수 있어요.</div>
    <div class="game-results">
      ${S.gameResults.length ? S.gameResults.map((r) => `<div class="gr">${icon('trophy')}<span>${r.text}</span><em>${r.time}</em></div>`).join('') : '<div class="gr" style="color:var(--mute)">아직 게임 결과가 없어요</div>'}
    </div>`;
}

function ladderLayout(n, levels) {
  const rungs = [];
  for (let l = 0; l < levels; l++) {
    const row = new Array(n - 1).fill(false);
    for (let i = 0; i < n - 1; i++) if (Math.random() < 0.5 && !(i > 0 && row[i - 1])) row[i] = true;
    rungs.push(row);
  }
  return rungs;
}

async function runLadder(source = '차량 화면') {
  if (S.game) return;
  S.game = 'ladder';
  const names = ['민준', '서연', '지훈', '하은'];
  const colors = ['#3d8bff', '#ff8a3d', '#2fd08a', '#c77dff'];
  const prizes = shuffle(['커피 쏘기', '통과', '간식 담당', '통과']);
  const n = 4, levels = 7, W = 760, top = 60, bottom = 330;
  const xs = names.map((_, i) => 95 + i * ((W - 190) / (n - 1)));
  const ly = (l) => top + 20 + (l + 0.5) * ((bottom - top - 40) / levels);
  const rungs = ladderLayout(n, levels);
  const traces = names.map((_, start) => {
    let c = start;
    const pts = [[xs[c], top]];
    for (let l = 0; l < levels; l++) {
      const y = ly(l);
      if (c > 0 && rungs[l][c - 1]) { pts.push([xs[c], y], [xs[c - 1], y]); c--; }
      else if (c < n - 1 && rungs[l][c]) { pts.push([xs[c], y], [xs[c + 1], y]); c++; }
    }
    pts.push([xs[c], bottom]);
    let len = 0;
    for (let i = 1; i < pts.length; i++) len += Math.hypot(pts[i][0] - pts[i - 1][0], pts[i][1] - pts[i - 1][1]);
    return { pts, end: c, len };
  });
  let svg = `<svg viewBox="0 0 ${W} 400">`;
  xs.forEach((x, i) => {
    svg += `<line x1="${x}" y1="${top}" x2="${x}" y2="${bottom}" stroke="#3a4252" stroke-width="6"/>`;
    svg += `<text x="${x}" y="40" text-anchor="middle" fill="${colors[i]}" font-size="30">${names[i]}</text>`;
    svg += `<text x="${x}" y="378" text-anchor="middle" fill="#9aa3b3" font-size="26" id="prize-${i}">${prizes[i]}</text>`;
  });
  rungs.forEach((row, l) => row.forEach((on, i) => { if (on) svg += `<line x1="${xs[i]}" y1="${ly(l)}" x2="${xs[i + 1]}" y2="${ly(l)}" stroke="#3a4252" stroke-width="6"/>`; }));
  traces.forEach((t, i) => { svg += `<polyline class="trace" id="trace-${i}" points="${t.pts.map((p) => p.join(',')).join(' ')}" stroke="${colors[i]}" stroke-dasharray="${t.len}" stroke-dashoffset="${t.len}"/>`; });
  svg += '</svg>';
  $('#pane-game').innerHTML = `
    <div class="game-stage">
      <div class="game-stage-head">사다리타기 · 누가 커피 쏠까?<span>${source}에서 시작</span></div>
      <div class="ladder">${svg}</div>
    </div>
    <div class="game-results" id="ladder-res"></div>`;
  for (let i = 0; i < n; i++) {
    const p = $(`#trace-${i}`);
    if (!p) break;
    p.style.strokeDashoffset = traces[i].len;
    p.getBoundingClientRect();
    p.style.transition = 'stroke-dashoffset 1.1s ease-in-out';
    p.style.strokeDashoffset = 0;
    await wait(1150);
    const pr = $(`#prize-${traces[i].end}`);
    if (pr) { pr.setAttribute('fill', colors[i]); pr.textContent = `${prizes[traces[i].end]} · ${names[i]}`; }
  }
  const loser = traces.findIndex((t) => prizes[t.end] === '커피 쏘기');
  const snack = traces.findIndex((t) => prizes[t.end] === '간식 담당');
  const text = `${names[loser]} 커피 쏘기 · ${names[snack]} 간식 담당`;
  finishGame('사다리타기', text);
}

async function runDraw() {
  if (S.game) return;
  S.game = 'draw';
  const names = ['민준', '지은', '하은', '서연', '도윤', '지훈', '수아', '태오', '유나'];
  $('#pane-game').innerHTML = `
    <div class="game-stage">
      <div class="game-stage-head">뽑기 · 다음 구간 DJ<span>9명 중 1명</span></div>
      <div class="draw-box" id="draw-box">?</div>
      <div class="draw-names">${names.map((n) => `<span data-n="${n}">${n}</span>`).join('')}</div>
    </div>`;
  let delay = 60, idx = 0;
  const pick = Math.floor(Math.random() * names.length);
  const steps = 22 + pick;
  for (let k = 0; k <= steps; k++) {
    idx = k % names.length;
    $('#draw-box').textContent = names[idx];
    $$('.draw-names span').forEach((s) => s.classList.toggle('hit', s.dataset.n === names[idx]));
    await wait(delay);
    delay *= 1.09;
  }
  $('#draw-box').classList.add('final');
  finishGame('뽑기', `다음 구간 DJ는 ${names[idx]}`);
}

function finishGame(kind, text) {
  S.stats.games++;
  S.gameResults.unshift({ text: `${kind} · ${text}`, time: hm() });
  pushFeed({ ic: 'trophy', color: '#ffc24a', title: `${kind} 결과`, text });
  banner({ ic: 'trophy', color: '#ffc24a', from: `게임 결과 · ${kind}`, title: text, ms: 4500 });
  setTimeout(() => { S.game = null; renderGame(); }, 3500);
}

/* ---------- 탭 */
function setTab(tab) {
  S.tab = tab;
  $$('.tabs button').forEach((b) => b.classList.toggle('on', b.dataset.tab === tab));
  $$('.tab-pane').forEach((p) => p.classList.toggle('on', p.dataset.pane === tab));
  if (tab === 'feed') { S.unread = 0; updateBadge(); }
}

function showInviteQR() {
  showModal({
    ic: 'qr', title: '일행 추가 초대',
    sub: '동승자는 QR, 다른 차량은 코드로 참여해요.',
    body: `<div class="m-qr"><div class="qr">${qrSVG(23)}</div><div><div class="code-box">GLML-7K2Q</div><div class="link-box">${icon('link')}gallae.link/j/7K2Q</div></div></div>`,
    primary: { label: '닫기', onClick: () => {} },
  });
}

/* =========================================================
   wiring
   ========================================================= */
function resetAll() {
  clearTimers(); closeModal(); hideBanner();
  S.running = false; S.awaiting = null;
  go('home');
}

const ACTIONS = {
  'new-trip': openPlan,
  'join-trip': joinTripModal,
  home: resetAll,
  'pull-phone': pullFromPhone,
  parse: runParse,
  'to-invite': () => openInvite(false),
  'back-plan': () => { clearTimers(); go('plan'); },
  'copy-link': () => toastFlow('초대 링크를 복사했어요'),
  'send-kakao': () => toastFlow('메세지로 초대 링크를 보냈어요'),
  'start-trip': () => { clearTimers(); CARS.forEach((c) => c.members.forEach((m) => S.joined.add(m))); openDrive(); },
  'next-leg': nextLegRequest,
  'show-qr': showInviteQR,
  'zoom-in': () => { S.zoom = 1.7; $$('.map-btn')[2].classList.add('on'); frame(true); },
  'zoom-out': () => { S.zoom = 1; $$('.map-btn')[2].classList.remove('on'); frame(true); },
  recenter: () => { S.zoom = S.zoom > 1 ? 1 : 1.7; $$('.map-btn')[2].classList.toggle('on', S.zoom > 1); frame(true); },
};

function toastFlow(text) {
  // 주행 화면 밖에서도 보이도록 임시 토스트
  const t = el(`<div class="toast show" style="bottom:120px;left:50%">${icon('check')}<span>${text}</span></div>`);
  $('#stage').appendChild(t);
  setTimeout(() => t.remove(), 2200);
}

document.addEventListener('click', (e) => {
  const a = e.target.closest('[data-action]');
  if (a && ACTIONS[a.dataset.action]) { ACTIONS[a.dataset.action](); return; }
  const pick = e.target.closest('[data-pick]');
  if (pick) { const [i, ci] = pick.dataset.pick.split(':').map(Number); pickCandidate(i, ci); return; }
  const tab = e.target.closest('[data-tab]');
  if (tab) { setTab(tab.dataset.tab); return; }
  const q = e.target.closest('.qchip');
  if (q) { sendQuick(q.textContent, q); return; }
  const fb = e.target.closest('[data-feed]');
  if (fb) {
    const [id, i] = fb.dataset.feed.split(':').map(Number);
    const f = S.feed.find((x) => x.id === id);
    if (f && !f.state) { hideBanner(); f.actions[i].onClick(); }
    return;
  }
  const g = e.target.closest('[data-game]');
  if (g) { g.dataset.game === 'ladder' ? runLadder('차량 화면') : runDraw(); return; }
});

document.addEventListener('pointerdown', (e) => {
  const h = e.target.closest('[data-handle]');
  if (h) startDrag(e, h);
});

/* demo panel */
$('#demo-toggle').addEventListener('click', () => $('#demo').classList.toggle('open'));
$('#demo-speed').addEventListener('click', (e) => {
  const b = e.target.closest('[data-speed]');
  if (!b) return;
  S.speedMul = +b.dataset.speed;
  $$('#demo-speed button').forEach((x) => x.classList.toggle('on', x === b));
});
$('#demo-auto').addEventListener('change', (e) => (S.auto = e.target.checked));
$('#demo').addEventListener('click', (e) => {
  const b = e.target.closest('[data-demo]');
  if (!b) return;
  const need = () => { if (S.screen !== 'drive') { alert('주행 화면에서 사용할 수 있어요. 여정을 시작해 주세요.'); return false; } return true; };
  switch (b.dataset.demo) {
    case 'stop': if (need()) incomingStop(); break;
    case 'msg': if (need()) incomingMsg(3, '지훈', '저희 조금 늦어요, 먼저 가세요!'); break;
    case 'ladder': if (need()) runLadder('동승자 웹'); break;
    case 'change': if (need()) demoChange(); break;
    case 'arrive': if (need() && S.progress < 1) { closeModal(); S.awaiting = null; if (S.waypoint) S.waypoint.passed = true; S.progress = 1; arrive(); } break;
    case 'reset': resetAll(); break;
  }
});

document.addEventListener('keydown', (e) => {
  if (e.target.tagName === 'TEXTAREA') return;
  if (e.key === 'Enter' && modalOpen && modalOpen.primary) { const m = modalOpen; closeModal(); m.primary.onClick(); }
  if ((e.key === 'v' || e.key === 'V') && modalOpen) simulateVoice();
  if (e.key === 'd' || e.key === 'D') $('#demo').style.display = $('#demo').style.display === 'none' ? '' : 'none';
});

/* init */
$$('[data-logo]').forEach((n) => (n.innerHTML = LOGO));
$$('[data-qr]').forEach((n) => (n.innerHTML = qrSVG(+n.dataset.qr)));
hydrateIcons();
fit();
tickClock();
setInterval(tickClock, 10000);
renderParse();
requestAnimationFrame(loop);
