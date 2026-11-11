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
const fnv = (s) => { let h = 0x811c9dc5; for (const ch of s) { h ^= ch.charCodeAt(0); h = Math.imul(h, 0x01000193) >>> 0; } return h.toString(16).padStart(8, '0'); };

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
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  bell: '<path d="M6 16v-5a6 6 0 0 1 12 0v5l2 2H4z"/><path d="M10 21h4"/>',
  game: '<rect x="2.5" y="7" width="19" height="11" rx="5"/><path d="M7 10.5v4M5 12.5h4"/><circle cx="15.5" cy="11.5" r="1"/><circle cx="18" cy="14" r="1"/>',
  qr: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><path d="M14 14h3v3h-3zM21 14v.01M14 21h.01M17 21h4v-4"/>',
  sync: '<path d="M20 11a8 8 0 0 0-14.5-4.5L4 8"/><path d="M4 4v4h4"/><path d="M4 13a8 8 0 0 0 14.5 4.5L20 16"/><path d="M20 20v-4h-4"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
  x: '<path d="M6 6l12 12M18 6L6 18"/>',
  mic: '<rect x="9" y="3" width="6" height="11" rx="3"/><path d="M5 11a7 7 0 0 0 14 0M12 18v3"/>',
  left: '<path d="M17 21v-8a3 3 0 0 0-3-3H5"/><path d="M9 6l-4 4 4 4"/>',
  right: '<path d="M7 21v-8a3 3 0 0 1 3-3h9"/><path d="M15 6l4 4-4 4"/>',
  sleft: '<path d="M15 21v-6L7 7"/><path d="M7 13V7h6"/>',
  sright: '<path d="M9 21v-6l8-8"/><path d="M11 7h6v6"/>',
  flag: '<path d="M5 21V4M5 4h12l-2.5 4L17 12H5"/>',
  pin: '<path d="M12 22s7-6.2 7-12a7 7 0 1 0-14 0c0 5.8 7 12 7 12z"/><circle cx="12" cy="10" r="2.6"/>',
  rest: '<path d="M8 3h8l5 5v8l-5 5H8l-5-5V8z"/><path d="M12 8v5M12 16v.01"/>',
  ladder: '<path d="M7 3v18M17 3v18M7 7h10M7 12h10M7 17h10"/>',
  dice: '<rect x="3.5" y="3.5" width="17" height="17" rx="4"/><circle cx="8.5" cy="8.5" r="1.2"/><circle cx="15.5" cy="15.5" r="1.2"/><circle cx="12" cy="12" r="1.2"/>',
  trophy: '<path d="M8 4h8v5a4 4 0 0 1-8 0z"/><path d="M8 6H5a3 3 0 0 0 3 4M16 6h3a3 3 0 0 1-3 4M12 13v4M8 21h8M9 17h6"/>',
  drag: '<circle cx="9" cy="6" r="1.6"/><circle cx="15" cy="6" r="1.6"/><circle cx="9" cy="12" r="1.6"/><circle cx="15" cy="12" r="1.6"/><circle cx="9" cy="18" r="1.6"/><circle cx="15" cy="18" r="1.6"/>',
  lock: '<rect x="5" y="11" width="14" height="10" rx="2.5"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/>',
  shield: '<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/><path d="M8.5 12l2.5 2.5 4.5-5"/>',
  clock: '<circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 2"/>',
  wifi: '<path d="M2.5 9a14 14 0 0 1 19 0M5.5 12.5a9.5 9.5 0 0 1 13 0M8.8 16a5 5 0 0 1 6.4 0"/><circle cx="12" cy="19.5" r="1"/>',
  wifioff: '<path d="M3 3l18 18M8.8 16a5 5 0 0 1 6.4 0M5.5 12.5a9.5 9.5 0 0 1 4-2.3M2.5 9a14 14 0 0 1 5-3M14.5 10.3a9.5 9.5 0 0 1 4 2.2M16.5 6a14 14 0 0 1 5 3"/><circle cx="12" cy="19.5" r="1"/>',
  grid: '<rect x="4" y="4" width="6" height="6" rx="1.5"/><rect x="14" y="4" width="6" height="6" rx="1.5"/><rect x="4" y="14" width="6" height="6" rx="1.5"/><rect x="14" y="14" width="6" height="6" rx="1.5"/>',
  nav: '<path d="M12 3l7 18-7-4-7 4z"/>',
  music: '<path d="M9 18V5l11-2v13"/><circle cx="6.5" cy="18" r="2.5"/><circle cx="17.5" cy="16" r="2.5"/>',
  fan: '<circle cx="12" cy="12" r="2"/><path d="M12 10c0-4 1-7 4-7 2 0 2 3 0 5M14 12c4 0 7 1 7 4 0 2-3 2-5 0M12 14c0 4-1 7-4 7-2 0-2-3 0-5M10 12c-4 0-7-1-7-4 0-2 3-2 5 0"/>',
  belt: '<circle cx="12" cy="5" r="2.2"/><path d="M8 21v-7a4 4 0 0 1 8 0v7M9 10l7 8"/>',
  light: '<path d="M14 6a6 6 0 0 1 0 12H11V6z"/><path d="M3 8h5M3 12h5M3 16h5"/>',
  tire: '<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="3"/>',
};
const icon = (name, cls = '') => `<svg class="i ${cls}" viewBox="0 0 24 24">${ICON_PATHS[name] || ''}</svg>`;
const hydrateIcons = (root = document) => $$('i[data-icon]', root).forEach((n) => n.replaceWith(el(icon(n.dataset.icon, n.className))));

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
const TRIP_ID = 'gn-2026-1111';
const SAMPLE_PLAN = `1. 안목해변 커피거리
2. 초당 순두부 (점심)
3. 경포대 산책
4. 숙소 https://naver.me/stjohns_gn`;

/* 지도 DB (내비 시뮬레이션이 poiId로 경로를 계산할 때 사용) */
const POI_DB = {
  anmok: { name: '안목해변 커피거리', addr: '강원 강릉시 창해로14번길', km: 228, min: 160, route: 0, lat: 37.7719, lon: 128.9476 },
  chodang1: { name: '초당할머니순두부', addr: '강원 강릉시 초당순두부길 77', km: 4.2, min: 12, route: 1, lat: 37.7912, lon: 128.9147 },
  chodang2: { name: '동화가든', addr: '강원 강릉시 초당순두부길77번길 15', km: 4.5, min: 13, route: 1, lat: 37.7905, lon: 128.9151 },
  chodang3: { name: '초당 공영주차장', addr: '강원 강릉시 초당동 309', km: 4.0, min: 11, route: 1, lat: 37.7899, lon: 128.9139 },
  gyeongpo: { name: '경포대', addr: '강원 강릉시 경포로 365', km: 3.1, min: 9, route: 2, lat: 37.7951, lon: 128.8966 },
  stjohns: { name: '세인트존스 호텔', addr: '강원 강릉시 창해로 307', km: 2.4, min: 7, route: 3, lat: 37.7929, lon: 128.9205 },
  rest1: { name: '평창휴게소', addr: '영동고속도로 강릉 방향', km: 12, min: 8, extra: 15 },
  rest2: { name: '대관령휴게소', addr: '영동고속도로 강릉 방향', km: 30, min: 20, extra: 15 },
  rest3: { name: '강릉 주유소', addr: '강원 강릉시 경강로', km: 2, min: 3, extra: 8 },
  cvs1: { name: 'GS25 초당점', addr: '강원 강릉시 초당동', km: 0.6, min: 2, extra: 6 },
  cvs2: { name: '스타벅스 경포점', addr: '강원 강릉시 경포로', km: 1.1, min: 3, extra: 10 },
  cvs3: { name: '초당 공영화장실', addr: '강원 강릉시 초당동', km: 0.4, min: 1, extra: 5 },
};
const KNOWN = [
  { keys: ['안목'], poi: 'anmok' },
  { keys: ['순두부'], ask: '비슷한 장소가 3곳 있어요', candidates: [['chodang1', '평점 4.6 · 대기 15분'], ['chodang2', '대기 40분'], ['chodang3', '주차 후 도보']] },
  { keys: ['경포'], poi: 'gyeongpo' },
  { keys: ['stjohns', '세인트존스'], link: true, poi: 'stjohns' },
];
const poiPlace = (id, note) => ({ poiId: id, name: POI_DB[id].name, addr: POI_DB[id].addr, km: POI_DB[id].km, min: POI_DB[id].min, note });

const CARS = [
  { id: 1, label: '1호차', driver: '민준', members: ['민준', '지은', '하은'], color: '#5b9dff', me: true, offset: 0 },
  { id: 2, label: '2호차', driver: '서연', members: ['서연', '도윤'], color: '#ff8a3d', offset: 0.035 },
  { id: 3, label: '3호차', driver: '지훈', members: ['지훈', '수아', '태오', '유나'], color: '#3ad38f', offset: -0.085 },
];
const anonCar = (id) => fnv(`${TRIP_ID}:car:${id}`);
const CAR_BY_ANON = Object.fromEntries(CARS.map((c) => [anonCar(c.id), c]));

const JOIN_SEQ = [['지은', 'QR'], ['하은', 'QR'], ['서연', '코드'], ['도윤', 'QR'], ['지훈', '링크'], ['수아', 'QR'], ['태오', 'QR'], ['유나', 'QR']];

const ROUTES = [
  { d: [[140, 1260], [420, 1260], [700, 980], [980, 980], [1260, 700], [1260, 420], [1400, 280]], roads: ['영동고속도로 강릉 방면', '대관령IC 방면', '강릉JC', '경강로', '창해로'] },
  { d: [[280, 1260], [280, 980], [560, 980], [560, 700], [980, 700], [980, 420], [1260, 420]], roads: ['창해로', '난설헌로', '초당순두부길', '초당원길'] },
  { d: [[140, 1120], [560, 1120], [560, 840], [840, 840], [1120, 560], [1120, 280]], roads: ['초당원길', '경포로', '난설헌로', '경포대 방면'] },
  { d: [[420, 1260], [420, 980], [700, 980], [700, 700], [1260, 700], [1260, 420]], roads: ['경포로', '해안로', '창해로', '호텔 진입로'] },
];

const QUICK = ['곧 출발해요', '휴게소 들러요', '먼저 가세요', '좋아요'];
const REPLIES = ['넵! 확인했어요', '좋아요', '알겠어요', '오케이~'];

/* =========================================================
   SYSTEM (시뮬레이션): 앱이 아닌 Pleos Connect 플랫폼 영역
   ========================================================= */
const UX = { BASELINE: 0, LIMIT_STRING_LENGTH: 4, NO_KEYBOARD: 8, NO_VIDEO: 16, LIMIT_CONTENT: 32, NO_SETUP: 64 };
const KR_DRIVING = UX.LIMIT_STRING_LENGTH | UX.NO_VIDEO | UX.LIMIT_CONTENT | UX.NO_SETUP;

const CarUxRestrictionsManager = {
  listeners: new Set(),
  current: { isRequiresDistractionOptimization: false, activeRestrictions: UX.BASELINE },
  registerListener(cb) { this.listeners.add(cb); cb(this.current); },
  unregisterListener(cb) { this.listeners.delete(cb); },
  getCurrentCarUxRestrictions() { return this.current; },
  _set(moving) {
    const next = { isRequiresDistractionOptimization: moving, activeRestrictions: moving ? KR_DRIVING : UX.BASELINE };
    if (next.isRequiresDistractionOptimization === this.current.isRequiresDistractionOptimization) return;
    this.current = next;
    this.listeners.forEach((cb) => cb(next));
  },
};

/* Navigation(시스템 앱) 시뮬레이터 */
const Nav = {
  route: null, geo: null, poi: null, progress: 0, running: false, waypoints: [null, null],
  speedMul: 1, parkLock: false, restTimer: null, listeners: new Set(), arrived: false,
  emit(name, arg) { this.listeners.forEach((l) => l[name] && l[name](arg)); },
  moving() { return this.running && !this.parkLock; },
  legSec() { return clamp(this.poi.min * 0.5, 26, 80); },
  start(routeInfo) {
    const poi = POI_DB[routeInfo.poiId];
    this.route = routeInfo; this.poi = poi; this.progress = 0; this.arrived = false;
    this.geo = buildGeo(ROUTES[poi.route ?? 0]);
    this.waypoints = [null, null];
    clearTimeout(this.restTimer);
    this.running = true;
    NavView.setup();
    this.emit('onRouteStarted', { name: poi.name, duration: poi.min * 60, distance: Math.round(poi.km * 1000) });
    this.emit('onRoutingCompleted');
    this.report();
  },
  cancel() { this.route = null; this.running = false; this.emit('onRouteCancelled'); NavView.setup(); },
  addWaypoint(req) {
    if (!this.route) return;
    const i = req.waypointIndex === 'SECOND' ? 1 : 0;
    const poi = POI_DB[req.poiId];
    const base = Math.min(this.progress + 0.12 + i * 0.1, 0.9);
    this.waypoints[i] = { index: req.waypointIndex, poiId: req.poiId, name: poi.name, extra: poi.extra || 8, at: base, passed: false };
    this.sortWaypoints();
    this.emit('onWaypointChanged', { cmd: 'ADD', index: req.waypointIndex });
    this.emit('onWayPointInfo', this.waypointInfo());
    NavView.pins();
  },
  removeWaypoint(index) {
    if (index === 'ALL') this.waypoints = [null, null];
    else this.waypoints[index === 'SECOND' ? 1 : 0] = null;
    this.emit('onWaypointChanged', { cmd: 'REMOVE', index });
    this.emit('onWayPointInfo', this.waypointInfo());
    NavView.pins();
  },
  sortWaypoints() {
    const list = this.waypoints.filter(Boolean).sort((a, b) => a.at - b.at);
    this.waypoints = [list[0] || null, list[1] || null];
    if (list[0]) list[0].index = 'FIRST';
    if (list[1]) list[1].index = 'SECOND';
  },
  waypointInfo() { return this.waypoints.filter(Boolean).map((w) => ({ index: w.index, poiId: w.poiId, name: w.name, passed: w.passed, extra: w.extra })); },
  remainMin() { return (1 - this.progress) * this.poi.min + this.waypoints.filter((w) => w && !w.passed).reduce((n, w) => n + w.extra, 0); },
  tick(dt) {
    if (!this.route || !this.moving()) return;
    this.progress = Math.min(1, this.progress + (dt * this.speedMul) / this.legSec());
    const wp = this.waypoints.find((w) => w && !w.passed && this.progress >= w.at);
    if (wp) {
      this.progress = wp.at; wp.passed = true; this.running = false;
      this.emit('onWayPointArrived', { index: wp.index, name: wp.name, duration: 0, distance: 0 });
      this.emit('onWayPointInfo', this.waypointInfo());
      NavView.pins();
      this.restTimer = setTimeout(() => { if (this.route && !this.arrived) this.running = true; }, 12000 / Math.sqrt(this.speedMul));
    }
    if (this.progress >= 1 && !this.arrived) {
      this.arrived = true; this.running = false;
      this.emit('onDestinationArrived', { name: this.poi.name, address: this.poi.addr, duration: 0, distance: 0 });
    }
  },
  report() {
    if (!this.route) return;
    const remainKm = (1 - this.progress) * this.poi.km;
    this.emit('onDrivingInfo', {
      destination: { name: this.poi.name, duration: Math.round(this.remainMin() * 60), distance: Math.round(remainKm * 1000) },
      waypoints: this.waypoints.filter((w) => w && !w.passed).map((w) => ({ name: w.name, duration: Math.round(((w.at - this.progress) * this.poi.min) * 60), distance: Math.round((w.at - this.progress) * this.poi.km * 1000) })),
    });
    const t = this.geo.turns.find((x) => x.at > this.progress + 0.001);
    this.emit('onTBTInfo', t ? [{ type: t.dir, distance: (t.at - this.progress) * this.poi.km * 1000, description: t.road }] : []);
  },
  speed() {
    if (!this.moving()) return 0;
    const hw = this.poi.km > 50;
    return Math.round((hw ? 98 : 42) + Math.sin(performance.now() / 900) * (hw ? 6 : 8));
  },
};

/* NaviHelper SDK 형태의 래퍼 (ai.pleos.playground.navi.helper.NaviHelper) */
const NaviHelper = {
  initialize(onConnected, onFailed) { setTimeout(() => (Nav ? onConnected() : onFailed(new Error('no navi'))), 200); },
  release() {},
  addListener(l) { Nav.listeners.add(l); },
  removeListener(l) { Nav.listeners.delete(l); },
  requestRoute(routeInfo) { Nav.start(routeInfo); },
  cancelRoute() { Nav.cancel(); },
  addWaypoint(req) { Nav.addWaypoint(req); },
  removeWaypoint(index) { Nav.removeWaypoint(index); },
  getWaypointInfo() { Nav.emit('onWayPointInfo', Nav.waypointInfo()); },
};

/* 시스템 Navigation 앱 화면 */
const NavView = {
  setup() {
    $('#map-bg').innerHTML = mapBackground();
    const g = Nav.geo;
    ['#route-casing', '#route-line', '#route-done'].forEach((s) => $(s).setAttribute('d', g ? g.d : ''));
    this.pins();
    $('#cars').innerHTML = g ? '<g id="mycar"><circle r="46" fill="#3d8bff" opacity=".22"/><circle r="32" fill="#fff"/><g class="heading"><path d="M0 -20 L15 16 L0 8 L-15 16 Z" fill="#3d8bff"/></g></g>' : '';
    if (!g) { $('#nav-tbt').innerHTML = ''; $('#nav-eta').innerHTML = ''; }
  },
  pins() {
    const g = Nav.geo;
    if (!g) { $('#pins').innerHTML = ''; return; }
    const a = g.pts[0], b = g.pts[g.pts.length - 1];
    let s = `<circle cx="${a.x}" cy="${a.y}" r="16" fill="#fff" stroke="#3d8bff" stroke-width="8"/>`;
    Nav.waypoints.filter((w) => w && !w.passed).forEach((w, i) => {
      const p = pointAt(g, w.at);
      s += `<g transform="translate(${p.x},${p.y})"><circle r="28" fill="#ffc24a" stroke="#0d1016" stroke-width="6"/><text y="11" text-anchor="middle" class="pin-label" style="fill:#1a1305;font-weight:800">${i + 1}</text></g>`;
    });
    s += `<g transform="translate(${b.x},${b.y})"><path d="M0 0 C -10 -22 -34 -36 -34 -62 A 34 34 0 1 1 34 -62 C 34 -36 10 -22 0 0 Z" fill="#ff7a3d" stroke="#0d1016" stroke-width="5"/><circle cy="-62" r="13" fill="#0d1016"/></g>`;
    $('#pins').innerHTML = s;
  },
  frame() {
    const g = Nav.geo;
    if (!g || WM.hidden('nav')) return;
    const me = pointAt(g, Nav.progress);
    $('#route-done').setAttribute('stroke-dasharray', `${Nav.progress * g.total} ${g.total * 2}`);
    const car = $('#mycar');
    if (car) { car.setAttribute('transform', `translate(${me.x.toFixed(1)},${me.y.toFixed(1)})`); car.querySelector('.heading').setAttribute('transform', `rotate(${me.ang + 90})`); }
    const box = $('#navapp').getBoundingClientRect();
    const aspect = box.width / Math.max(1, box.height);
    const h = 1180, w = h * aspect;
    const x = clamp(me.x - w / 2, Math.min(0, 1680 - w), Math.max(0, 1680 - w));
    const y = clamp(me.y - h * 0.6, 0, 1440 - h);
    $('#map').setAttribute('viewBox', `${x.toFixed(0)} ${y.toFixed(0)} ${w.toFixed(0)} ${h}`);
  },
  text() {
    if (!Nav.geo || WM.hidden('nav')) return;
    const t = Nav.geo.turns.find((x) => x.at > Nav.progress + 0.001);
    const remainKm = (1 - Nav.progress) * Nav.poi.km;
    const remain = Nav.remainMin();
    $('#nav-tbt').innerHTML = Nav.arrived ? `${icon('flag')}<div><b>도착</b><span>${Nav.poi.name}</span></div>`
      : t ? `${icon(t.dir)}<div><b>${fmtKm((t.at - Nav.progress) * Nav.poi.km)}</b><span>${t.road}</span></div>`
        : `${icon('flag')}<div><b>${fmtKm(remainKm)}</b><span>${Nav.poi.name}</span></div>`;
    $('#nav-eta').innerHTML = `<div><span>도착</span><b class="ok">${hm(remain)}</b></div><div><span>남은 시간</span><b>${fmtMin(remain)}</b></div><div><span>남은 거리</span><b>${fmtKm(remainKm)}</b></div>`;
  },
};

/* Driving view · Status bar */
const SysChrome = {
  update() {
    const moving = Nav.route && Nav.moving();
    $('#dv-speed').textContent = Nav.route ? Nav.speed() : 0;
    $('#dv-gear').textContent = moving ? 'D' : 'P';
    const t = Nav.geo && !Nav.arrived && Nav.route ? Nav.geo.turns.find((x) => x.at > Nav.progress + 0.001) : null;
    $('#dv-tbt').innerHTML = t ? `${icon(t.dir)}<div><b>${fmtKm((t.at - Nav.progress) * Nav.poi.km)}</b><span>${t.road}</span></div>` : '';
    const sd = $('#sb-drive');
    sd.textContent = moving ? '주행 중' : '정차 중';
    sd.classList.toggle('driving', !!moving);
    $$('[data-clock]').forEach((n) => (n.textContent = hm()));
  },
};

/* System Notification · Toast */
const SystemUI = {
  count: 0,
  notify({ type = 'app', ic = 'bell', color = '#5b9dff', name, title, desc = '', actions = null, align = 'horizontal', voice = false, ms = 4500, key }) {
    const img = type === 'profile'
      ? `<div class="noti-img profile" style="background:${color}">${name ? name[0] : ''}<small>${LOGO}</small></div>`
      : `<div class="noti-img" style="background:${color}26;color:${color}">${type === 'app' ? `<span class="logo sm" style="width:64px;height:64px">${LOGO}</span>` : icon(ic)}</div>`;
    const node = el(`<div class="noti">
      ${img}
      <div class="noti-head"><b>${title}</b><em>갈래말래 · 지금</em></div>
      <div class="noti-desc">${desc}</div>
      ${actions ? `<div class="noti-actions ${align}">${actions.map((a, i) => `<button class="btn ${a.primary ? 'primary' : ''}" data-n="${i}">${a.label}</button>`).join('')}</div>` : ''}
      ${voice ? `<div class="noti-voice">${icon('mic')}<span>“수락” 또는 “거절”이라고 말해 보세요</span></div>` : ''}
    </div>`);
    if (key) { const old = $(`#sys-notify [data-key="${key}"]`); old && old.remove(); node.dataset.key = key; }
    const close = () => { node.classList.remove('show'); setTimeout(() => node.remove(), 160); };
    if (actions) actions.forEach((a, i) => node.querySelector(`[data-n="${i}"]`).addEventListener('click', () => { close(); a.onClick(); }));
    $('#sys-notify').prepend(node);
    $$('#sys-notify .noti').slice(2).forEach((n) => n.remove());
    requestAnimationFrame(() => node.classList.add('show'));
    if (!actions) setTimeout(close, ms);
    this.count++; this.badge();
    return { close };
  },
  badge() { const b = $('#sb-noti-count'); b.textContent = this.count; b.classList.toggle('show', this.count > 0); },
  toastT: null,
  toast(text, ic = 'check') {
    const t = $('#sys-toast');
    t.innerHTML = `${icon(ic)}<span>${text}</span>`;
    t.classList.add('show');
    clearTimeout(this.toastT);
    this.toastT = setTimeout(() => t.classList.remove('show'), 2600);
  },
};

/* Gleo AI 음성 (시스템 음성 인식 결과를 앱 intent로 전달) */
const GleoAI = {
  handler: null,
  phrases: { yes: '응, 수락해', no: '아니, 거절해', next: '다음 일정 안내해줘', late: '늦는다고 보내줘' },
  say(intent) {
    const g = $('#sb-gleo');
    g.classList.add('on');
    setTimeout(() => g.classList.remove('on'), 1200);
    const v = $('#dlg-voice');
    if (v) { v.classList.add('heard'); v.querySelector('span').textContent = `“${this.phrases[intent]}”`; }
    setTimeout(() => this.handler && this.handler(intent), 500);
  },
};

/* 서버 (WebSocket 시뮬레이션) */
const Server = {
  online: true, handler: null, inbox: [], timer: null, active: false,
  connect(handler) { this.handler = handler; },
  start() { this.active = true; clearInterval(this.timer); this.timer = setInterval(() => this.pushCars(), 2000); this.pushCars(); },
  stop() { this.active = false; clearInterval(this.timer); },
  deliver(msg) { if (!this.online) { this.inbox.push(msg); return; } this.handler && this.handler(msg); },
  send(msg) {
    if (!this.online) return false;
    if (msg.type === 'route') {
      setTimeout(() => this.deliver({ type: 'routeAck', car: anonCar(2), leg: msg.leg }), 1200);
      setTimeout(() => this.deliver({ type: 'routeAck', car: anonCar(3), leg: msg.leg }), 2200);
    }
    return true;
  },
  setOnline(v) {
    this.online = v;
    if (v) { const q = this.inbox; this.inbox = []; q.forEach((m) => this.handler && this.handler(m)); this.pushCars(); }
  },
  pushCars() {
    if (!this.active || !this.online || !Nav.route) return;
    const cars = CARS.filter((c) => !c.me).map((c) => {
      const p = clamp(Nav.progress + c.offset, 0, 1);
      const extra = Nav.waypoints.filter((w) => w && !w.passed && p < w.at).reduce((n, w) => n + w.extra, 0);
      return { car: anonCar(c.id), progress: p, etaMin: (1 - p) * Nav.poi.min + extra };
    });
    this.deliver({ type: 'cars', cars, at: Date.now() });
  },
};

/* 창 관리 (1:1 / 2:0, 창 위치 변경, 포그라운드/백그라운드) */
const WM = {
  mode: 'split', swapped: false, navFront: false,
  apply() {
    const w = $('#windows');
    w.className = this.mode;
    if (this.swapped) w.classList.add('swapped');
    if (this.mode === 'full' && this.navFront) w.classList.add('nav-front');
    App.setVisible(!this.hidden('app'));
    NavView.setup(); NavView.frame(); NavView.text();
  },
  hidden(which) {
    if (this.mode === 'split') return false;
    return which === 'app' ? this.navFront : !this.navFront;
  },
};

/* =========================================================
   APP (갈래말래)
   ========================================================= */
const S = {
  screen: 'home', parsed: [], stops: [], joined: new Set(['민준']), leg: 0,
  remainMin: 0, remainKm: 0, myProgress: 0, arrived: false, awaiting: null,
  waypoints: [], cars: {}, carsAt: 0, feed: [], unread: 0, tab: 'cars', fired: new Set(),
  game: null, gameResults: [], stats: { msgs: 0, waypoints: 0, changes: 0, games: 0 },
  timers: [], driving: false, restrictions: 0, visible: true, dirty: true,
  share: true, queue: [], profile: '민준', pending: null, speedMul: 1, auto: true, routeAcks: {},
};
const later = (fn, ms) => { const t = setTimeout(fn, ms); S.timers.push(t); return t; };
const clearTimers = () => { S.timers.forEach(clearTimeout); S.timers = []; };
const curStop = () => S.stops[S.leg];
const limit = (s, n = 26) => (S.restrictions & UX.LIMIT_STRING_LENGTH && s && s.length > n ? `${s.slice(0, n - 1)}…` : s);
const hasFlag = (f) => (S.restrictions & f) !== 0;

/* 앱 생명주기: 화면에서 사라지면 렌더링을 멈추고 상태를 저장, 돌아오면 최신 상태로 다시 그림 */
const App = {
  setVisible(v) {
    if (S.visible === v) return;
    S.visible = v;
    if (v) this.onStart(); else this.onStop();
  },
  onStart() { renderTrip(); },
  onStop() { save(); },
};

function onUxRestrictionsChanged(r) {
  S.driving = r.isRequiresDistractionOptimization;
  S.restrictions = r.activeRestrictions;
  $('#app').classList.toggle('driving', S.driving);
  $('#app').classList.toggle('no-setup', hasFlag(UX.NO_SETUP));
  if (S.driving && S.game) S.abortGame = true;
  if (S.driving && $('#dialog-root .dlg[data-parked]')) closeDialog();
  updateParkedOnly();
  if (S.screen === 'trip') { renderPlanList(); renderGame(); renderFeed(); renderDest(); }
}

function updateParkedOnly() {
  $('#parked-only').classList.toggle('need', ['home', 'plan', 'invite', 'consent'].includes(S.screen));
}

/* ---------- 저장 / 복원 (프로세스 종료 · 재시작 대비) */
const STORE_KEY = 'gallae-state-v2';
let saveT;
function save() {
  clearTimeout(saveT);
  saveT = setTimeout(() => {
    if (S.screen !== 'trip') return;
    const feed = S.feed.slice(0, 30).map(({ actions, ...f }) => ({ ...f, state: f.state || (actions ? { ok: false, text: '만료됨' } : null) }));
    localStorage.setItem(STORE_KEY, JSON.stringify({ screen: S.screen, stops: S.stops, leg: S.leg, tab: S.tab, feed, stats: S.stats, gameResults: S.gameResults, share: S.share, at: Date.now() }));
  }, 300);
}
function loadSaved() { try { return JSON.parse(localStorage.getItem(STORE_KEY)); } catch { return null; } }
function clearSaved() { localStorage.removeItem(STORE_KEY); }

/* ---------- 화면 전환 */
function go(name) {
  S.screen = name;
  $$('#app .screen').forEach((s) => s.classList.toggle('active', s.id === `screen-${name}`));
  updateParkedOnly();
  if (name === 'home') renderHome();
}

/* ---------- Dialog (Pleos Dialog 형태, 앱 창 내부) */
let dialog = null;
function showDialog({ ic = 'route', iconColor, title, desc = '', body = '', primary, secondary, voice = false, parked = false }) {
  closeDialog();
  const node = el(`<div class="dlg-back"><div class="dlg" ${parked ? 'data-parked' : ''}>
    <div class="dlg-icon" ${iconColor ? `style="background:${iconColor}26;color:${iconColor}"` : ''}>${icon(ic)}</div>
    <div class="dlg-title">${title}</div>
    ${desc ? `<div class="dlg-desc">${desc}</div>` : ''}
    <div class="dlg-body">${body}</div>
    ${voice ? `<div class="dlg-voice" id="dlg-voice">${icon('mic')}<span>“응” 또는 “아니”라고 말해 보세요</span></div>` : ''}
    <div class="dlg-actions">
      ${secondary ? `<button class="btn ghost" data-m="s">${secondary.label}</button>` : ''}
      ${primary ? `<button class="btn primary" data-m="p">${primary.label}</button>` : ''}
    </div>
  </div></div>`);
  $('#dialog-root').appendChild(node);
  dialog = { primary, secondary };
  if (primary) node.querySelector('[data-m="p"]').addEventListener('click', () => { closeDialog(); primary.onClick && primary.onClick(); });
  if (secondary) node.querySelector('[data-m="s"]').addEventListener('click', () => { closeDialog(); secondary.onClick && secondary.onClick(); });
}
function closeDialog() { $('#dialog-root').innerHTML = ''; dialog = null; }

function onVoice(intent) {
  if (intent === 'yes' || intent === 'no') {
    if (dialog) {
      const a = intent === 'yes' ? dialog.primary : dialog.secondary;
      closeDialog();
      a && a.onClick && a.onClick();
      return;
    }
    if (S.pending) { const p = S.pending; intent === 'yes' ? p.accept() : p.decline(); return; }
    SystemUI.toast('응답할 요청이 없어요', 'mic');
    return;
  }
  if (S.screen !== 'trip') { SystemUI.toast('여정을 시작하면 쓸 수 있어요', 'mic'); return; }
  if (intent === 'next') nextLegRequest();
  if (intent === 'late') sendQuick('조금 늦어요');
}

/* =========================================================
   HOME
   ========================================================= */
function renderHome() {
  const saved = loadSaved();
  $('#profile-name').textContent = S.profile;
  $('#resume-slot').innerHTML = saved && saved.stops ? `
    <button class="home-card resume" data-action="resume">
      <span class="hc-icon">${icon('sync')}</span>
      <span class="hc-text"><b>진행 중인 여정 이어하기</b><span>강릉 1박 2일 · 일정 ${saved.leg + 1}/${saved.stops.length}</span></span>
      ${icon('next')}
    </button>` : '';
}

function profileDialog() {
  const list = ['민준', '지은', '게스트'];
  showDialog({
    ic: 'user', title: '프로필', desc: '공용 차량이면 내 프로필로 바꿔 주세요',
    body: `<div class="profiles">${list.map((p) => `<button class="btn ghost ${p === S.profile ? 'on' : ''}" data-profile="${p}">${icon('user')}${p}</button>`).join('')}</div>`,
    secondary: { label: '로그아웃', onClick: logout },
    primary: { label: '닫기' },
  });
}
function setProfile(p) {
  S.profile = p;
  $('#sb-profile').textContent = p;
  closeDialog();
  renderHome();
  SystemUI.toast(`${p} 프로필로 바꿨어요`, 'user');
}
function logout() {
  sessionStorage.removeItem('gallae-token');
  clearSaved();
  S.profile = '게스트';
  $('#sb-profile').textContent = '게스트';
  renderHome();
  SystemUI.toast('로그아웃했어요 · 저장된 여정을 지웠어요', 'user');
}

function joinTripDialog() {
  showDialog({
    ic: 'link', title: '초대 코드로 참여', desc: '받은 코드를 확인해 주세요', parked: true,
    body: `<div class="join-code">${'GLML'.split('').map((c) => `<span>${c}</span>`).join('')}<i>-</i>${'7K2Q'.split('').map((c) => `<span>${c}</span>`).join('')}</div>`,
    secondary: { label: '취소' },
    primary: { label: '참여하기', onClick: () => {
      S.stops = presetStops();
      JOIN_SEQ.forEach(([n]) => S.joined.add(n));
      openInvite(true);
      SystemUI.toast('강릉 1박 2일 여행에 참여했어요');
    } },
  });
}
const presetStops = () => [poiPlace('anmok'), poiPlace('chodang1'), poiPlace('gyeongpo'), poiPlace('stjohns')].map((p, i) => ({ id: `s${i}`, ...p }));

/* =========================================================
   PLAN
   ========================================================= */
function openPlan() { go('plan'); S.parsed = []; $('#plan-input').value = ''; renderParse(); }

function phoneQrDialog() {
  showDialog({
    ic: 'qr', title: '휴대폰으로 입력', desc: 'QR을 찍으면 입력 페이지가 열려요', parked: true,
    body: `<div class="m-qr"><div class="qr">${qrSVG(11)}</div><div class="desc">입력한 일정은<br>이 화면으로 바로 전송돼요</div></div>`,
    primary: { label: '닫기' },
  });
}

function pullFromPhone() {
  $('#plan-input').value = SAMPLE_PLAN;
  SystemUI.toast('휴대폰에서 일정을 받았어요', 'phone');
}

function parseLines(text) {
  return text.split('\n').map((l) => l.trim()).filter(Boolean).map((raw, i) => {
    const url = (raw.match(/https?:\/\/\S+/) || [])[0];
    const note = (raw.match(/\(([^)]+)\)/) || [])[1];
    const clean = raw.replace(/^\s*(\d+[.)]|[-•*])\s*/, '').replace(/https?:\/\/\S+/, '').replace(/\([^)]*\)/, '').trim();
    const hit = KNOWN.find((k) => k.keys.some((key) => raw.toLowerCase().includes(key)));
    const base = { id: `p${i}`, raw, note };
    if (hit && hit.candidates) return { ...base, status: 'check', ask: hit.ask, candidates: hit.candidates.map(([id, n]) => poiPlace(id, n)), name: clean };
    if (hit) return { ...base, status: hit.link || url ? 'link' : 'ok', ...poiPlace(hit.poi) };
    const id = `u${fnv(clean || raw)}`;
    POI_DB[id] = { name: clean || '지도 링크 장소', addr: '강원 강릉시', km: 5.2, min: 14, route: i % 4 };
    return { ...base, status: 'check', name: clean || raw, ask: '이 장소가 맞나요?', candidates: [poiPlace(id, '가장 가까움')] };
  });
}

async function runParse() {
  const text = $('#plan-input').value.trim();
  if (!text) { SystemUI.toast('먼저 일정을 입력해 주세요', 'list'); return; }
  $('#parse-list').innerHTML = `<div class="loading">${icon('spark')}AI가 일정을 정리하고 있어요</div>`;
  $('#btn-to-invite').disabled = true;
  $('#parse-count').textContent = '';
  await wait(1200);
  S.parsed = parseLines(text);
  renderParse();
}

function renderParse() {
  const list = $('#parse-list');
  if (!S.parsed.length) {
    list.innerHTML = '<div class="empty">일정을 입력하면 목적지를 순서대로 정리해요</div>';
    $('#btn-to-invite').disabled = true; $('#parse-count').textContent = '';
    return;
  }
  const pending = S.parsed.filter((p) => p.status === 'check').length;
  $('#parse-count').textContent = pending ? `확인 필요 ${pending}곳` : `${S.parsed.length}곳`;
  list.innerHTML = S.parsed.map((p, i) => {
    const tag = p.status === 'ok' ? '<span class="tag ok">확인됨</span>' : p.status === 'link' ? '<span class="tag link">링크 인식</span>' : '<span class="tag check">확인 필요</span>';
    const body = p.status === 'check'
      ? `<div class="p-ask">${p.ask}</div><div class="cands">${p.candidates.map((c, ci) => `<button class="cand" data-pick="${i}:${ci}">${icon('pin')}<b>${c.name}</b><span>${c.note || ''}</span></button>`).join('')}</div>`
      : `<div class="p-addr">${p.addr}</div>`;
    return `<div class="p-item ${p.status}"><div class="p-num">${i + 1}</div><div class="p-main"><div class="p-name">${p.name}${tag}</div>${body}</div></div>`;
  }).join('');
  $('#btn-to-invite').disabled = pending > 0;
}

function pickCandidate(i, ci) {
  const p = S.parsed[i];
  const c = p.candidates[ci];
  S.parsed[i] = { id: p.id, raw: p.raw, note: p.note, status: 'ok', ...c };
  renderParse();
  SystemUI.toast(`${c.name}(으)로 정했어요`);
}

/* =========================================================
   INVITE
   ========================================================= */
function openInvite(guest = false) {
  if (!guest) S.stops = S.parsed.map((p, i) => ({ id: `s${i}`, poiId: p.poiId, name: p.name, addr: p.addr, km: p.km, min: p.min }));
  go('invite');
  if (!guest) {
    S.joined = new Set(['민준']);
    JOIN_SEQ.forEach(([name], i) => later(() => { S.joined.add(name); renderMembers(); }, 900 + i * 650));
  }
  renderMembers();
}

function renderMembers() {
  const total = CARS.reduce((n, c) => n + c.members.length, 0);
  const joinedCars = CARS.filter((c) => S.joined.has(c.driver)).length;
  $('#join-count').textContent = `차량 ${joinedCars}/3 · ${S.joined.size}/${total}명`;
  $('#member-groups').innerHTML = CARS.map((c) => {
    const on = S.joined.has(c.driver);
    return `<div class="mg ${on ? '' : 'waiting'}">
      <div class="mg-head"><span class="car-chip" style="background:${c.color}">${c.id}</span>${c.label} · ${c.driver}${c.me ? ' (나)' : ''}<em>${on ? '참여함' : '대기 중'}</em></div>
      <div class="mg-members">${c.members.filter((m) => S.joined.has(m)).map((m) => `<span class="mchip">${m}</span>`).join('')}</div>
    </div>`;
  }).join('');
}

/* =========================================================
   TRIP
   ========================================================= */
const routeInfoOf = (s) => {
  const p = POI_DB[s.poiId];
  return { latitude: p.lat || 37.77, longitude: p.lon || 128.9, displayLatitude: null, displayLongitude: null, poiName: s.name, poiId: s.poiId, poiSubId: '0', address: s.addr };
};

function openTrip(restore) {
  clearTimers();
  go('trip');
  S.cars = {}; S.carsAt = 0; S.waypoints = []; S.arrived = false; S.awaiting = null; S.pending = null;
  S.fired = new Set(); S.game = null; S.routeAcks = {};
  if (restore) {
    Object.assign(S, { stops: restore.stops, leg: restore.leg, tab: restore.tab || 'cars', feed: restore.feed || [], stats: restore.stats, gameResults: restore.gameResults || [], share: restore.share !== false });
    S.unread = 0;
    feedId = S.feed.reduce((m, f) => Math.max(m, f.id), 0);
  } else {
    S.leg = 0; S.feed = []; S.unread = 0; S.gameResults = [];
    S.stats = { msgs: 0, waypoints: 0, changes: 0, games: 0 };
    pushFeed({ ic: 'users', color: CARS[2].color, title: '일행 3대 · 9명 참여', text: '같은 일정을 함께 안내해요' });
    S.unread = 0;
  }
  Server.start();
  renderQuick();
  setTab(S.tab);
  renderTrip();
  if (restore) {
    later(() => { startLeg(false, true); }, 400);
  } else {
    later(() => askStart(true), 500);
  }
}

function askStart(first) {
  const s = curStop();
  S.awaiting = { first };
  renderTrip();
  const prev = S.stops[S.leg - 1];
  showDialog({
    ic: 'route',
    title: first ? '1번 목적지로 안내할까요?' : '다음 목적지로 안내할까요?',
    desc: '일행 차량에도 같은 목적지를 보내요',
    body: `<div class="m-dest"><span class="p-num">${S.leg + 1}</span><div><b>${s.name}</b><span>${limit(s.addr)}</span></div><em>${fmtMin(s.min)}</em></div>
      ${first || !prev ? '' : `<div class="m-cars">${CARS.map((c) => {
        const info = c.me ? { progress: 1 } : S.cars[anonCar(c.id)] || { progress: 1 };
        const done = info.progress >= 1;
        return `<div><span class="car-chip" style="background:${c.color}">${c.id}</span>${c.driver}<em class="${done ? 'ok' : ''}">${done ? '도착' : fmtMin(info.etaMin)}</em></div>`;
      }).join('')}</div>`}`,
    voice: true,
    secondary: { label: '나중에', onClick: () => SystemUI.toast('준비되면 ‘안내 시작’을 눌러 주세요', 'route') },
    primary: { label: '안내 시작', onClick: () => startLeg(first) },
  });
}

function startLeg(first, restored = false) {
  S.awaiting = null; S.arrived = false; S.waypoints = [];
  const s = curStop();
  NaviHelper.requestRoute(routeInfoOf(s));
  const ok = Server.send({ type: 'route', leg: S.leg, poiId: s.poiId });
  const item = pushFeed({ ic: 'route', color: '#5b9dff', title: `${S.leg + 1}번 ${s.name}`, text: ok ? '일행 차량 확인 중…' : '연결되면 일행 차량에 보내요' });
  S.routeAcks = { leg: S.leg, feedId: item.id, cars: new Set() };
  if (!ok) S.queue.push({ type: 'route', leg: S.leg, poiId: s.poiId });
  SystemUI.toast(restored ? '현재 목적지를 다시 안내해요' : first ? '내비 안내를 시작했어요' : `${s.name}(으)로 안내해요`, 'route');
  renderTrip();
  save();
}

function nextLegRequest() {
  if (S.awaiting) { askStart(S.awaiting.first); return; }
  const isLast = S.leg >= S.stops.length - 1;
  if (S.arrived) {
    if (isLast) return finishTrip();
    S.leg++; renderTrip(); askStart(false); return;
  }
  if (isLast) { SystemUI.toast('마지막 목적지로 가고 있어요', 'flag'); return; }
  const next = S.stops[S.leg + 1];
  showDialog({
    ic: 'next', title: '다음 일정으로 넘어갈까요?',
    desc: `아직 도착 전이에요. 일행 모두 ‘${next.name}’(으)로 바뀌어요`,
    voice: true,
    secondary: { label: '취소' },
    primary: { label: '넘어가기', onClick: () => {
      pushFeed({ ic: 'sync', color: '#ff8a3d', title: `${curStop().name} 건너뜀`, text: '호스트가 다음 일정으로 넘겼어요' });
      S.leg++;
      startLeg(false);
    } },
  });
}

function finishTrip() {
  closeDialog();
  NaviHelper.cancelRoute();
  Server.stop();
  clearTimers();
  clearSaved();
  const totalKm = S.stops.reduce((n, s) => n + s.km, 0);
  const stats = [[S.stops.length, '방문 장소'], [Math.round(totalKm), '총 이동 km'], [3, '함께한 차량'], [S.stats.waypoints, '정차 경유'], [S.stats.msgs, '간편 메세지'], [S.stats.changes + S.stats.games, '일정 변경 · 게임']];
  $('#summary-stats').innerHTML = stats.map(([v, l]) => `<div class="ss"><b>${v}</b><span>${l}</span></div>`).join('');
  $('#summary-timeline').innerHTML = ['출발', ...S.stops.map((s) => s.name)].map((n) => `<span>${n}</span>`).join('<em>→</em>');
  go('summary');
  SystemUI.toast('여정이 끝나 위치 공유를 껐어요', 'shield');
}

/* ---------- NaviHelperEventListener */
const naviListener = {
  onRouteStarted() {},
  onDrivingInfo(info) {
    const s = curStop();
    if (!s || S.screen !== 'trip') return;
    S.remainMin = info.destination.duration / 60;
    S.remainKm = info.destination.distance / 1000;
    S.myProgress = clamp(1 - S.remainKm / s.km, 0, 1);
    autoScenario();
    renderLive();
  },
  onWayPointInfo(list) { S.waypoints = list; renderDest(); renderLive(); },
  onWayPointArrived(info) {
    pushFeed({ ic: 'rest', color: '#ffc24a', title: `${info.name} 도착`, text: '일행이 모이면 다시 출발해요' });
    SystemUI.notify({ type: 'icon', ic: 'rest', color: '#ffc24a', title: `${info.name}에 도착했어요`, desc: '정차 중에는 게임을 할 수 있어요', ms: 3500 });
  },
  onDestinationArrived() {
    if (S.screen !== 'trip') return;
    S.arrived = true;
    const s = curStop();
    const isLast = S.leg >= S.stops.length - 1;
    const next = S.stops[S.leg + 1];
    pushFeed({ ic: 'flag', color: CARS[2].color, title: `${s.name} 도착`, text: isLast ? '마지막 목적지예요' : `다음 · ${next.name}` });
    SystemUI.notify({ type: 'app', title: `${s.name}에 도착했어요`, desc: isLast ? '마지막 목적지예요' : `다음 · ${next.name}`, ms: 3500 });
    renderTrip();
    const legAt = S.leg;
    later(() => {
      if (S.leg !== legAt || !S.arrived || S.screen !== 'trip') return;
      if (isLast) {
        showDialog({ ic: 'flag', iconColor: '#3ad38f', title: '모든 일정을 마쳤어요', desc: '일행 모두 수고하셨어요', voice: true, secondary: { label: '닫기' }, primary: { label: '여정 마치기', onClick: finishTrip } });
      } else { S.leg++; renderTrip(); askStart(false); }
    }, 1600);
  },
  onRouteCancelled() {},
  onError(code) { SystemUI.toast(`내비 오류 · ${code}`, 'x'); },
};

/* ---------- 서버 메세지 */
function onServer(msg) {
  if (msg.type === 'cars') {
    msg.cars.forEach((c) => (S.cars[c.car] = { progress: c.progress, etaMin: c.etaMin, at: msg.at }));
    S.carsAt = msg.at;
    renderLive();
    return;
  }
  if (S.screen !== 'trip') return;
  const car = CAR_BY_ANON[msg.car];
  switch (msg.type) {
    case 'routeAck': {
      if (!S.routeAcks || S.routeAcks.leg !== msg.leg) return;
      S.routeAcks.cars.add(car.label);
      const f = S.feed.find((x) => x.id === S.routeAcks.feedId);
      if (f) { f.text = `${[...S.routeAcks.cars].join(' · ')} 내비 적용 완료`; renderFeed(); }
      break;
    }
    case 'msg':
      pushFeed({ ic: 'msg', color: car.color, title: `${car.label} ${msg.name}`, text: `“${msg.text}”` });
      SystemUI.notify({ type: 'profile', color: car.color, name: msg.name, title: `${car.label} ${msg.name}`, desc: limit(`“${msg.text}”`, 30), ms: 4000 });
      break;
    case 'stopRequest': incomingStop(car, msg); break;
    case 'gameResult': finishGame(msg.kind, msg.text, true); break;
    case 'planChange': {
      const a = S.leg + 1, b = S.leg + 2;
      if (b >= S.stops.length) return;
      reorderStop(b, a, '호스트 모바일 웹');
      SystemUI.notify({ type: 'app', title: '일정 순서가 바뀌었어요', desc: `다음 · ${S.stops[a].name}`, ms: 4000 });
      break;
    }
    default:
  }
}

function autoScenario() {
  if (!S.auto) return;
  const once = (key, cond, fn) => { if (cond && !S.fired.has(key)) { S.fired.add(key); fn(); } };
  once('msg0', S.leg === 0 && S.myProgress > 0.1, () => Server.deliver({ type: 'msg', car: anonCar(3), name: '지훈', text: '저희 기름 넣고 따라갈게요!' }));
  once('stop0', S.leg === 0 && S.myProgress > 0.24, () => demoStop());
  once('msg1', S.leg === 1 && S.myProgress > 0.35, () => Server.deliver({ type: 'msg', car: anonCar(2), name: '서연', text: '순두부집 웨이팅 걸어둘게요' }));
  once('game2', S.leg === 2 && S.myProgress > 0.3, () => demoLadderWeb());
}

/* ---------- 정차 요청 (경유지 최대 2곳) */
let stopSeq = 0;
function demoStop() {
  const highway = curStop() && curStop().km > 50;
  const pool = highway ? ['rest1', 'rest2', 'rest3'] : ['cvs1', 'cvs2', 'cvs3'];
  const who = stopSeq % 2 === 0 ? { car: 2, name: '서연' } : { car: 3, name: '수아' };
  const poiId = pool[stopSeq % 3];
  stopSeq++;
  Server.deliver({ type: 'stopRequest', car: anonCar(who.car), name: who.name, reason: '화장실', poiId });
}

function incomingStop(car, msg) {
  if (!Nav.route || S.arrived) return;
  const poi = POI_DB[msg.poiId];
  if (S.myProgress > 0.85) { SystemUI.toast('목적지가 가까워 정차 요청을 보류했어요', 'rest'); return; }
  const active = S.waypoints.filter((w) => !w.passed);
  const full = active.length >= 2;
  let item, noti;
  const finish = (state) => {
    item.state = state; S.pending = null; renderFeed(); noti && noti.close();
  };
  const accept = () => {
    if (item.state) return;
    if (full) NaviHelper.removeWaypoint(active[0].index);
    const freeIdx = full ? active[0].index : (active.some((w) => w.index === 'FIRST') ? 'SECOND' : 'FIRST');
    NaviHelper.addWaypoint({ latitude: 37.7, longitude: 128.9, poiName: poi.name, waypointIndex: freeIdx, poiId: msg.poiId, poiSubId: '0', address: poi.addr });
    S.stats.waypoints++;
    finish({ ok: true, text: full ? `${active[0].name} 대신 ${poi.name} 경유` : `수락 · ${poi.name} 경유` });
    if (!Server.send({ type: 'waypoint', poiId: msg.poiId })) S.queue.push({ type: 'waypoint', poiId: msg.poiId });
    SystemUI.toast(`경유지 추가 · ${poi.name}`, 'sync');
    renderQueue();
    later(() => Server.deliver({ type: 'msg', car: msg.car, name: msg.name, text: '감사해요!!' }), 2500);
  };
  const decline = () => {
    if (item.state) return;
    finish({ ok: false, text: '거절 · “다음 휴게소에서 쉬어요” 전송' });
    Server.send({ type: 'msg', text: '다음 휴게소에서 쉬어요' });
    SystemUI.toast(`${msg.name}님께 거절 메세지를 보냈어요`, 'msg');
  };
  const desc = full ? `경유지가 가득 찼어요 (최대 2곳) · ${poi.name}` : `${msg.reason} · ${poi.name} · +${poi.extra}분`;
  item = pushFeed({
    ic: 'rest', color: car.color, title: `${car.label} ${msg.name} · 정차 요청`, text: desc,
    actions: [{ label: '거절', onClick: decline }, { label: full ? '첫 경유지와 바꾸기' : '수락', primary: true, onClick: accept }],
  });
  S.pending = { accept, decline };
  noti = SystemUI.notify({
    type: 'profile', color: car.color, name: msg.name, key: 'stop',
    title: `${car.label} ${msg.name} · 정차 요청`, desc: limit(desc, 34), voice: true,
    actions: [{ label: '거절', onClick: decline }, { label: full ? '바꾸기' : '수락', primary: true, onClick: accept }],
  });
}

/* ---------- 메세지 (오프라인 시 대기열) */
function sendQuick(text) {
  S.stats.msgs++;
  const ok = Server.send({ type: 'msg', text });
  pushFeed({ ic: 'msg', color: '#5b9dff', title: '나 → 일행 전체', text: `“${text}”`, state: ok ? null : { ok: false, text: '전송 대기 중' } });
  if (S.tab !== 'feed') { S.unread = Math.max(0, S.unread - 1); updateBadge(); }
  if (!ok) { S.queue.push({ type: 'msg', text, feedId: S.feed[0].id }); renderQueue(); SystemUI.toast('연결되면 보낼게요', 'wifioff'); return; }
  SystemUI.toast(`“${text}” 전송`, 'msg');
  const c = CARS[1 + Math.floor(Math.random() * 2)];
  later(() => Server.deliver({ type: 'msg', car: anonCar(c.id), name: c.driver, text: REPLIES[Math.floor(Math.random() * REPLIES.length)] }), 2600);
}
function flushQueue() {
  const q = S.queue; S.queue = [];
  q.forEach((m) => {
    Server.send(m);
    if (m.feedId) { const f = S.feed.find((x) => x.id === m.feedId); if (f) f.state = { ok: true, text: '전송됨' }; }
  });
  if (q.length) SystemUI.toast(`대기 중이던 ${q.length}건을 보냈어요`, 'sync');
  renderQueue(); renderFeed();
}
function renderQueue() { $('#queue-note').textContent = S.queue.length ? `전송 대기 ${S.queue.length}건 · 연결되면 보내요` : ''; }

/* ---------- 일정 변경 */
function reorderStop(from, to, by) {
  const [m] = S.stops.splice(from, 1);
  S.stops.splice(to, 0, m);
  S.stats.changes++;
  renderPlanList(); renderDest();
  pushFeed({ ic: 'sync', color: CARS[2].color, title: '일정 순서 변경', text: `${by} · ${m.name} → ${to + 1}번째` });
  if (by.startsWith('나')) SystemUI.toast('일정을 바꿨어요 · 일행 차량에 동기화', 'sync');
  save();
}

/* ---------- 게임 (정차 중에만) */
function runLadder(source) {
  if (S.game || S.driving) return;
  S.game = 'ladder'; S.abortGame = false;
  const names = ['민준', '서연', '지훈', '하은'];
  const colors = ['#5b9dff', '#ff8a3d', '#3ad38f', '#c77dff'];
  const prizes = shuffle(['커피 쏘기', '통과', '간식 담당', '통과']);
  const n = 4, levels = 7, W = 760, top = 60, bottom = 330;
  const xs = names.map((_, i) => 95 + i * ((W - 190) / (n - 1)));
  const ly = (l) => top + 20 + (l + 0.5) * ((bottom - top - 40) / levels);
  const rungs = [];
  for (let l = 0; l < levels; l++) { const row = new Array(n - 1).fill(false); for (let i = 0; i < n - 1; i++) if (Math.random() < 0.5 && !(i > 0 && row[i - 1])) row[i] = true; rungs.push(row); }
  const traces = names.map((_, start) => {
    let c = start;
    const pts = [[xs[c], top]];
    for (let l = 0; l < levels; l++) {
      const y = ly(l);
      if (c > 0 && rungs[l][c - 1]) { pts.push([xs[c], y], [xs[c - 1], y]); c--; } else if (c < n - 1 && rungs[l][c]) { pts.push([xs[c], y], [xs[c + 1], y]); c++; }
    }
    pts.push([xs[c], bottom]);
    let len = 0;
    for (let i = 1; i < pts.length; i++) len += Math.hypot(pts[i][0] - pts[i - 1][0], pts[i][1] - pts[i - 1][1]);
    return { pts, end: c, len };
  });
  let svg = `<svg viewBox="0 0 ${W} 400">`;
  xs.forEach((x, i) => {
    svg += `<line x1="${x}" y1="${top}" x2="${x}" y2="${bottom}" stroke="var(--surface-tertiary)" stroke-width="6"/>`;
    svg += `<text x="${x}" y="40" text-anchor="middle" fill="${colors[i]}" font-size="30">${names[i]}</text>`;
    svg += `<text x="${x}" y="378" text-anchor="middle" fill="var(--text-secondary)" font-size="28" id="prize-${i}">${prizes[i]}</text>`;
  });
  rungs.forEach((row, l) => row.forEach((on, i) => { if (on) svg += `<line x1="${xs[i]}" y1="${ly(l)}" x2="${xs[i + 1]}" y2="${ly(l)}" stroke="var(--surface-tertiary)" stroke-width="6"/>`; }));
  traces.forEach((t, i) => { svg += `<polyline class="trace" id="trace-${i}" points="${t.pts.map((p) => p.join(',')).join(' ')}" stroke="${colors[i]}" stroke-dasharray="${t.len}" stroke-dashoffset="${t.len}"/>`; });
  svg += '</svg>';
  $('#pane-game').innerHTML = `<div class="game-stage"><div class="game-stage-head">사다리타기 · 커피 내기<span>${source}</span></div><div class="ladder">${svg}</div></div>`;
  const reveal = (i) => { const pr = $(`#prize-${traces[i].end}`); if (pr) { pr.setAttribute('fill', colors[i]); pr.textContent = `${prizes[traces[i].end]} · ${names[i]}`; } };
  (async () => {
    for (let i = 0; i < n; i++) {
      const p = $(`#trace-${i}`);
      if (!p || S.abortGame) break;
      p.getBoundingClientRect();
      p.style.transition = 'stroke-dashoffset .9s ease-in-out';
      p.style.strokeDashoffset = 0;
      await wait(950);
      reveal(i);
    }
    const loser = traces.findIndex((t) => prizes[t.end] === '커피 쏘기');
    const snack = traces.findIndex((t) => prizes[t.end] === '간식 담당');
    finishGame('사다리타기', `${names[loser]} 커피 · ${names[snack]} 간식`);
  })();
}

async function runDraw() {
  if (S.game || S.driving) return;
  S.game = 'draw'; S.abortGame = false;
  const names = ['민준', '지은', '하은', '서연', '도윤', '지훈', '수아', '태오', '유나'];
  const pick = Math.floor(Math.random() * names.length);
  $('#pane-game').innerHTML = `<div class="game-stage"><div class="game-stage-head">뽑기 · 다음 구간 DJ<span>9명 중 1명</span></div>
    <div class="draw-box" id="draw-box">?</div><div class="draw-names">${names.map((n) => `<span data-n="${n}">${n}</span>`).join('')}</div></div>`;
  for (let k = 0; k < 8 && !S.abortGame && $('#draw-box'); k++) {
    const idx = (pick + k + 1) % names.length;
    $('#draw-box').textContent = names[idx];
    $$('.draw-names span').forEach((s) => s.classList.toggle('hit', s.dataset.n === names[idx]));
    await wait(160);
  }
  const box = $('#draw-box');
  if (box) { box.textContent = names[pick]; box.classList.add('final'); }
  $$('.draw-names span').forEach((s) => s.classList.toggle('hit', s.dataset.n === names[pick]));
  finishGame('뽑기', `다음 구간 DJ는 ${names[pick]}`);
}

function demoLadderWeb() {
  const names = ['민준', '서연', '지훈', '하은'];
  const p = shuffle(names);
  Server.deliver({ type: 'gameResult', kind: '사다리타기', text: `${p[0]} 커피 · ${p[1]} 간식` });
}

function finishGame(kind, text, remote = false) {
  S.stats.games++;
  S.gameResults.unshift({ text: `${kind} · ${text}`, time: hm() });
  pushFeed({ ic: 'trophy', color: '#ffc24a', title: `${kind} 결과${remote ? ' · 동승자 웹' : ''}`, text });
  if (remote) { renderGame(); return; }
  later(() => { S.game = null; renderGame(); }, 3000);
}

/* ---------- 탭 */
function setTab(tab) {
  S.tab = tab;
  $$('.tabs button').forEach((b) => b.classList.toggle('on', b.dataset.tab === tab));
  $$('.tab-pane').forEach((p) => p.classList.toggle('on', p.dataset.pane === tab));
  if (tab === 'feed') { S.unread = 0; updateBadge(); }
  save();
}

function showInvite() {
  if (hasFlag(UX.NO_SETUP)) {
    showDialog({ ic: 'link', title: '초대 코드', desc: 'QR은 정차 중에 보여드려요', body: '<div class="code-box" style="text-align:center">GLML-7K2Q</div>', primary: { label: '닫기' } });
    return;
  }
  showDialog({
    ic: 'qr', title: '일행 추가 초대', desc: '동승자는 QR, 다른 차량은 코드로 참여해요', parked: true,
    body: `<div class="m-qr"><div class="qr">${qrSVG(23)}</div><div class="code-box">GLML-7K2Q</div></div>`,
    primary: { label: '닫기' },
  });
}

/* =========================================================
   TRIP — render (이벤트가 올 때만 갱신)
   ========================================================= */
let feedId = 0;
function pushFeed(item) {
  const f = { id: ++feedId, time: hm(), ...item };
  S.feed.unshift(f);
  if (S.tab !== 'feed') S.unread++;
  updateBadge(); renderFeed(); save();
  return f;
}
function updateBadge() { const b = $('#feed-badge'); b.textContent = S.unread; b.classList.toggle('show', S.unread > 0); }

function renderTrip() {
  if (S.screen !== 'trip' || !S.visible) return;
  renderDest(); renderCars(); renderFeed(); renderPlanList(); renderGame(); renderNet(); renderQueue();
  $$('#app [data-clock]').forEach((n) => (n.textContent = hm()));
}

function renderLive() {
  if (S.screen !== 'trip' || !S.visible) return;
  renderEta(); renderCars();
}

function renderNet() {
  const chip = $('#net-chip');
  if (!Server.online) { chip.className = 'chip-state off'; chip.innerHTML = `${icon('wifioff')}연결 끊김`; }
  else if (!S.share) { chip.className = 'chip-state private'; chip.innerHTML = `${icon('shield')}위치 비공개`; }
  else { chip.className = 'chip-state'; chip.innerHTML = ''; }
  $('#sb-net').classList.toggle('off', !Server.online);
  $('#sb-net').innerHTML = `${icon(Server.online ? 'wifi' : 'wifioff')}${Server.online ? 'LTE' : '오프라인'}`;
}

function renderDest() {
  if (S.screen !== 'trip') return;
  const s = curStop();
  if (!s) return;
  const isLast = S.leg >= S.stops.length - 1;
  $('#dest-step').textContent = `일정 ${S.leg + 1}/${S.stops.length}`;
  $('#dest-kind').textContent = S.arrived ? '도착했어요' : S.awaiting ? '안내 대기' : '현재 목적지';
  $('#dest-name').textContent = s.name;
  $('#dest-card').classList.toggle('arrived', S.arrived);
  $('#dest-wps').innerHTML = S.waypoints.filter((w) => !w.passed).map((w, i) => `<div class="wp-row"><span class="wp-idx">경유 ${i + 1}</span>${limit(w.name)}<em>+${w.extra}분</em></div>`).join('');
  const label = S.awaiting ? '안내 시작' : S.arrived ? (isLast ? '여정 마치기' : '다음 일정 안내') : isLast ? '마지막 목적지' : '다음 일정으로';
  $('#btn-next').innerHTML = `${label}${icon('next')}`;
  $('#btn-next').disabled = !S.awaiting && !S.arrived && isLast;
  renderEta();
}

function renderEta() {
  const s = curStop();
  if (!s) return;
  const remain = S.awaiting ? s.min : S.remainMin;
  const km = S.awaiting ? s.km : S.remainKm;
  $('#dest-eta').textContent = S.arrived ? '도착' : hm(remain);
  $('#dest-remain').textContent = S.arrived ? '-' : fmtMin(remain);
  $('#dest-dist').textContent = S.arrived ? '-' : fmtKm(km);
}

function carInfo(c) {
  if (S.awaiting) return { progress: 0, etaMin: curStop().min, at: Date.now() };
  if (c.me) return { progress: S.awaiting ? 0 : S.myProgress, etaMin: S.awaiting ? curStop().min : S.remainMin, at: Date.now() };
  return S.cars[anonCar(c.id)] || { progress: 0, etaMin: curStop() ? curStop().min : 0, at: 0 };
}

function renderCars() {
  const s = curStop();
  if (!s) return;
  const stale = !Server.online && S.carsAt;
  const staleText = stale ? ` · ${hm((S.carsAt - Date.now()) / 60000)} 기준` : '';
  const myP = carInfo(CARS[0]).progress;
  $('#pane-cars').innerHTML = `
    <div class="convoy">
      <div class="convoy-head"><span>일행 진행 상황${staleText}</span><span>${limit(s.name, 14)}</span></div>
      <div class="convoy-track">
        <div class="cv-fill" style="width:${myP * 100}%"></div>
        ${CARS.map((c) => `<div class="cv-dot" style="background:${c.color};left:calc(${carInfo(c).progress * 100}% * .94 + 22px)">${c.id}</div>`).join('')}
        <div class="cv-flag">${icon('flag')}</div>
      </div>
    </div>
    ${CARS.map((c) => {
      const info = carInfo(c);
      const arrived = info.progress >= 1;
      const late = !c.me && info.progress < myP - 0.05;
      const gap = (info.progress - myP) * s.km;
      const sub = c.me
        ? `${c.members.join(' · ')}${S.share ? '' : ' · 위치 비공개'}`
        : `${c.members.join(' · ')} · ${Math.abs(gap) < 0.05 ? '내 차와 함께' : `${fmtKm(Math.abs(gap))} ${gap > 0 ? '앞' : '뒤'}`}`;
      return `<div class="car-row">
        <span class="car-chip" style="background:${c.color}">${c.id}</span>
        <div class="cr-main"><div class="cr-name">${c.label} · ${c.driver}${c.me ? '<span class="me">내 차</span>' : ''}</div><div class="cr-sub">${sub}</div></div>
        <div class="cr-right ${arrived ? 'arrived' : late ? 'late' : ''}"><b>${arrived ? '도착' : hm(info.etaMin)}</b><span>${arrived ? '목적지' : `${fmtMin(info.etaMin)} 남음`}</span></div>
      </div>`;
    }).join('')}`;
}

function renderFeed() {
  if (S.screen !== 'trip') return;
  const lim = hasFlag(UX.LIMIT_CONTENT);
  const list = lim ? S.feed.slice(0, 3) : S.feed;
  $('#pane-feed').innerHTML = list.map((f) => `
    <div class="feed-item ${f.actions && !f.state ? 'new' : ''}">
      <div class="f-icon" style="background:${f.color}26;color:${f.color}">${icon(f.ic)}</div>
      <div class="f-main">
        <div class="f-top"><div class="f-title">${f.title}</div><div class="f-time">${f.time}</div></div>
        ${f.text ? `<div class="f-text">${limit(f.text, 40)}</div>` : ''}
        ${f.actions && !f.state ? `<div class="f-actions">${f.actions.map((a, i) => `<button class="btn ${a.primary ? 'primary' : 'ghost'}" data-feed="${f.id}:${i}">${a.label}</button>`).join('')}</div>` : ''}
        ${f.state ? `<div class="f-state ${f.state.ok ? 'ok' : ''}">${f.state.text}</div>` : ''}
      </div>
    </div>`).join('') + (lim && S.feed.length > 3 ? `<div class="limit-note">${icon('lock')}주행 중에는 최근 알림 3개만 보여요</div>` : '');
}

function renderPlanList() {
  if (S.screen !== 'trip') return;
  const locked = hasFlag(UX.NO_SETUP);
  $('#pane-plan').innerHTML = `
    <div class="plan-note ${locked ? 'lock' : ''}">${icon(locked ? 'lock' : 'sync')}${locked ? '주행 중에는 순서를 바꿀 수 없어요' : '손잡이를 끌어 순서를 바꿔요 · 일행에 동기화'}</div>
    <div class="stop-list" id="stop-list" style="display:flex;flex-direction:column;gap:14px">
      ${S.stops.map((s, i) => {
        const st = i < S.leg ? 'done' : i === S.leg ? 'current' : 'next';
        const right = st === 'next' && !locked ? `<span class="s-handle" data-handle="${i}">${icon('drag')}순서</span>` : `<span class="s-state">${st === 'done' ? '완료' : st === 'current' ? '이동 중' : ''}</span>`;
        return `<div class="stop ${st}" data-idx="${i}"><span class="s-num">${i < S.leg ? '✓' : i + 1}</span><div class="s-main"><div class="s-name">${s.name}</div><div class="s-meta">${limit(s.addr)} · ${fmtKm(s.km)}</div></div>${right}</div>`;
      }).join('')}
    </div>`;
}

function renderGame() {
  if (S.screen !== 'trip' || (S.game && !S.driving)) return;
  if (S.driving) S.game = null;
  const results = S.gameResults.length ? S.gameResults.map((r) => `<div class="gr">${icon('trophy')}<span>${limit(r.text, 30)}</span><em>${r.time}</em></div>`).join('') : '<div class="gr" style="color:var(--text-tertiary)">아직 게임 결과가 없어요</div>';
  $('#pane-game').innerHTML = S.driving
    ? `<div class="locked">${icon('lock')}<b>게임은 정차 중에만 할 수 있어요</b><span>동승자는 모바일 웹에서 함께해요</span></div>${results}`
    : `<div class="game-cards">
        <button class="game-card" data-game="ladder"><span class="gc-icon" style="background:#ffc24a26;color:#ffc24a">${icon('ladder')}</span><b>사다리타기</b><span>커피 내기</span></button>
        <button class="game-card" data-game="draw"><span class="gc-icon" style="background:#5b9dff26;color:#5b9dff">${icon('dice')}</span><b>뽑기</b><span>다음 구간 DJ</span></button>
      </div>${results}`;
}

function renderQuick() { $('#quick-chips').innerHTML = QUICK.map((q) => `<button class="qchip">${q}</button>`).join(''); }

/* ---------- 일정 드래그 (정차 중) */
function startDrag(e, handle) {
  if (hasFlag(UX.NO_SETUP)) return;
  e.preventDefault();
  const scale = S.scale;
  const list = $('#stop-list');
  const item = handle.closest('.stop');
  const from = +item.dataset.idx;
  const items = $$('.stop', list);
  const rects = items.map((n) => n.getBoundingClientRect());
  const startY = e.clientY;
  const h = rects[from].height + 14 * scale;
  let to = from;
  item.classList.add('dragging');
  items.forEach((n, i) => i !== from && n.classList.add('shifting'));
  try { handle.setPointerCapture(e.pointerId); } catch { /* 합성 이벤트 */ }
  const move = (ev) => {
    const dy = ev.clientY - startY;
    item.style.transform = `translateY(${dy / scale}px)`;
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
      n.style.transform = shift ? `translateY(${shift / scale}px)` : '';
    });
  };
  const up = () => {
    handle.removeEventListener('pointermove', move);
    handle.removeEventListener('pointerup', up);
    handle.removeEventListener('pointercancel', up);
    if (to !== from) reorderStop(from, to, '나(호스트)'); else renderPlanList();
  };
  handle.addEventListener('pointermove', move);
  handle.addEventListener('pointerup', up);
  handle.addEventListener('pointercancel', up);
}

/* =========================================================
   nav map geometry / background
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
  let s = '<rect width="1680" height="1440" style="fill:var(--map-bg)"/>';
  for (let y = 0; y < 11; y++) for (let x = 0; x < 12; x++) {
    const v = r();
    const fill = v < 0.08 ? '--map-park' : v < 0.5 ? '--map-block2' : '--map-block';
    s += `<rect x="${x * 140 + 13}" y="${y * 140 + 13}" width="114" height="114" rx="12" style="fill:var(${fill})"/>`;
  }
  s += '<path d="M0 520 C 260 480 420 620 700 580 S 1120 460 1520 520" fill="none" style="stroke:var(--map-water)" stroke-width="54" stroke-linecap="round"/>';
  s += '<path d="M1500 0 C 1460 300 1560 600 1520 900 S 1600 1300 1570 1440 L 1680 1440 L 1680 0 Z" style="fill:var(--map-sea)"/>';
  s += '<ellipse cx="1180" cy="200" rx="150" ry="80" style="fill:var(--map-water)"/>';
  ROUTES.forEach((rt) => { s += `<path d="M${rt.d.map((p) => p.join(' ')).join(' L ')}" fill="none" style="stroke:var(--map-road)" stroke-width="20" stroke-linecap="round" stroke-linejoin="round"/>`; });
  [[180, 470, '남대천'], [1100, 205, '경포호'], [760, 1080, '강릉역'], [640, 820, '초당동'], [1310, 120, '안목항'], [180, 1380, '강릉IC']].forEach(([x, y, t]) => { s += `<text class="map-label" x="${x}" y="${y}">${t}</text>`; });
  s += '<text class="map-label sea" x="1596" y="980">동</text><text class="map-label sea" x="1596" y="1036">해</text>';
  return s;
}

/* =========================================================
   wiring
   ========================================================= */
function fit() {
  const s = Math.min(innerWidth / 2600, innerHeight / 1480);
  $('#stage').style.setProperty('--s', s);
  S.scale = s;
  NavView.frame();
}
addEventListener('resize', fit);

function resetAll() {
  clearTimers(); closeDialog(); NaviHelper.cancelRoute(); Server.stop(); clearSaved();
  S.awaiting = null; S.pending = null; S.queue = [];
  $('#sys-notify').innerHTML = '';
  go('home');
}

const ACTIONS = {
  'new-trip': openPlan,
  'join-trip': joinTripDialog,
  home: () => { if (S.screen === 'summary') resetAll(); else { clearTimers(); go('home'); } },
  profile: profileDialog,
  resume: () => { const saved = loadSaved(); if (saved) openTrip(saved); },
  'phone-qr': phoneQrDialog,
  'pull-phone': pullFromPhone,
  parse: runParse,
  'to-invite': () => openInvite(false),
  'back-plan': () => { clearTimers(); go('plan'); },
  'back-invite': () => go('invite'),
  'copy-link': () => SystemUI.toast('초대 링크를 복사했어요', 'link'),
  'send-msg': () => SystemUI.toast('메세지로 초대 링크를 보냈어요', 'msg'),
  'start-trip': () => { clearTimers(); CARS.forEach((c) => c.members.forEach((m) => S.joined.add(m))); go('consent'); },
  'consent-yes': () => { S.share = true; sessionStorage.setItem('gallae-token', fnv(`${S.profile}:${Date.now()}`)); openTrip(null); },
  'consent-no': () => { S.share = false; openTrip(null); SystemUI.toast('위치 공유 없이 시작해요', 'shield'); },
  'next-leg': nextLegRequest,
  'show-invite': showInvite,
};

document.addEventListener('click', (e) => {
  const sys = e.target.closest('[data-sys]');
  if (sys) { WM.swapped = !WM.swapped; WM.apply(); return; }
  const a = e.target.closest('[data-action]');
  if (a && ACTIONS[a.dataset.action]) { ACTIONS[a.dataset.action](); return; }
  const pf = e.target.closest('[data-profile]');
  if (pf) { setProfile(pf.dataset.profile); return; }
  const pick = e.target.closest('[data-pick]');
  if (pick) { const [i, ci] = pick.dataset.pick.split(':').map(Number); pickCandidate(i, ci); return; }
  const tab = e.target.closest('[data-tab]');
  if (tab) { setTab(tab.dataset.tab); return; }
  const q = e.target.closest('.qchip');
  if (q) { sendQuick(q.textContent); return; }
  const fb = e.target.closest('[data-feed]');
  if (fb) {
    const [id, i] = fb.dataset.feed.split(':').map(Number);
    const f = S.feed.find((x) => x.id === id);
    if (f && !f.state) f.actions[i].onClick();
    return;
  }
  const g = e.target.closest('[data-game]');
  if (g) { g.dataset.game === 'ladder' ? runLadder('차량 화면') : runDraw(); }
});

document.addEventListener('pointerdown', (e) => {
  const h = e.target.closest('[data-handle]');
  if (h) { startDrag(e, h); return; }
  if (e.target.closest('#stage button') && navigator.vibrate) navigator.vibrate(8);
});

/* 스크롤 끝 바운스 */
document.addEventListener('wheel', (e) => {
  const sc = e.target.closest('#stage .scroll');
  if (!sc) return;
  const atTop = sc.scrollTop <= 0 && e.deltaY < 0;
  const atBottom = sc.scrollTop + sc.clientHeight >= sc.scrollHeight - 1 && e.deltaY > 0;
  if (!atTop && !atBottom) return;
  const cls = atTop ? 'bounce-top' : 'bounce-bottom';
  if (sc.classList.contains(cls)) return;
  sc.classList.add(cls);
  setTimeout(() => sc.classList.remove(cls), 180);
}, { passive: true });

document.addEventListener('visibilitychange', () => App.setVisible(!document.hidden && !WM.hidden('app')));

/* demo panel */
$('#demo-toggle').addEventListener('click', () => $('#demo').classList.toggle('open'));
const seg = (sel, attr, fn) => $(sel).addEventListener('click', (e) => {
  const b = e.target.closest(`[${attr}]`);
  if (!b) return;
  $$(`${sel} button`).forEach((x) => x.classList.toggle('on', x === b));
  fn(b.getAttribute(attr));
});
seg('#demo-speed', 'data-speed', (v) => { Nav.speedMul = +v; S.speedMul = +v; });
seg('#demo-window', 'data-win', (v) => { WM.mode = v; WM.navFront = false; WM.apply(); });
seg('#demo-theme', 'data-theme', (v) => { $('#stage').dataset.theme = v; NavView.setup(); NavView.frame(); });
$('#demo-auto').addEventListener('change', (e) => (S.auto = e.target.checked));
$('#demo-park').addEventListener('change', (e) => { Nav.parkLock = e.target.checked; });
$('#demo-online').addEventListener('change', (e) => {
  Server.setOnline(e.target.checked);
  if (e.target.checked) flushQueue();
  renderNet(); renderCars();
  SystemUI.toast(e.target.checked ? '다시 연결됐어요' : '연결이 끊겼어요 · 최근 정보로 보여드려요', e.target.checked ? 'wifi' : 'wifioff');
});
$('#demo').addEventListener('click', (e) => {
  const sd = e.target.closest('[data-sysdemo]');
  if (sd) {
    if (sd.dataset.sysdemo === 'swap') { WM.swapped = !WM.swapped; }
    if (sd.dataset.sysdemo === 'bg') {
      if (WM.mode === 'split') { WM.mode = 'full'; WM.navFront = true; } else WM.navFront = !WM.navFront;
      $$('#demo-window button').forEach((x) => x.classList.toggle('on', x.dataset.win === WM.mode));
      sd.textContent = WM.navFront ? '앱 다시 보기' : '앱 숨기기';
    }
    WM.apply();
    return;
  }
  const v = e.target.closest('[data-voice]');
  if (v) { GleoAI.say(v.dataset.voice); return; }
  const b = e.target.closest('[data-demo]');
  if (!b) return;
  const need = () => { if (S.screen !== 'trip') { SystemUI.toast('여정을 시작한 뒤 사용할 수 있어요', 'route'); return false; } return true; };
  switch (b.dataset.demo) {
    case 'stop': if (need()) demoStop(); break;
    case 'msg': if (need()) Server.deliver({ type: 'msg', car: anonCar(3), name: '지훈', text: '저희 조금 늦어요, 먼저 가세요!' }); break;
    case 'ladder': if (need()) demoLadderWeb(); break;
    case 'change': if (need()) Server.deliver({ type: 'planChange' }); break;
    case 'arrive': if (need() && Nav.route && !Nav.arrived) { Nav.waypoints.forEach((w) => w && (w.passed = true)); Nav.running = true; Nav.progress = 1; Nav.tick(0.0001); Nav.report(); } break;
    case 'reset': resetAll(); break;
    default:
  }
});

document.addEventListener('keydown', (e) => {
  if (e.target.tagName === 'TEXTAREA') return;
  if (e.key === 'v' || e.key === 'V') GleoAI.say('yes');
  if (e.key === 'd' || e.key === 'D') $('#demo').style.display = $('#demo').style.display === 'none' ? '' : 'none';
});

/* =========================================================
   init
   ========================================================= */
$$('[data-logo]').forEach((n) => (n.innerHTML = LOGO));
$$('[data-qr]').forEach((n) => (n.innerHTML = qrSVG(+n.dataset.qr)));
hydrateIcons();
fit();

CarUxRestrictionsManager.registerListener(onUxRestrictionsChanged);
Server.connect(onServer);
GleoAI.handler = onVoice;
NaviHelper.initialize(() => NaviHelper.addListener(naviListener), () => SystemUI.toast('내비 연결에 실패했어요', 'x'));

/* 시스템 루프: 내비 시뮬레이션 · 주행 상태 */
let lastTs = performance.now();
setInterval(() => {
  const now = performance.now();
  Nav.tick(Math.min(0.2, (now - lastTs) / 1000));
  lastTs = now;
  CarUxRestrictionsManager._set(!!(Nav.route && Nav.moving()));
}, 100);
setInterval(() => { Nav.report(); NavView.text(); SysChrome.update(); }, 1000);
(function navLoop() { NavView.frame(); requestAnimationFrame(navLoop); })();

SysChrome.update();
WM.apply();
App.onStart();
go('home');
renderParse();
