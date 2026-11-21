"use strict";
// Every value from the server is inserted with textContent; never build HTML from data.

const $ = (sel) => document.querySelector(sel);
const view = $("#view");
const tooltip = $("#tooltip");

function el(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (v == null || v === false) continue;
    if (k === "class") node.className = v;
    else if (k === "text") node.textContent = v;
    else if (k.startsWith("on")) node.addEventListener(k.slice(2), v);
    else node.setAttribute(k, v === true ? "" : v);
  }
  for (const c of children.flat(Infinity)) {
    if (c == null || c === false) continue;
    node.append(c instanceof Node ? c : document.createTextNode(String(c)));
  }
  return node;
}

/* ---------- formatting ---------- */
const nf = new Intl.NumberFormat("ko-KR");
const money = (n) => el("span", { class: "money", text: "$" + nf.format(Number(n) || 0) });
function playtime(ticks) {
  const min = Math.floor((Number(ticks) || 0) / 20 / 60);
  const h = Math.floor(min / 60);
  return h > 0 ? `${h}시간 ${min % 60}분` : `${min}분`;
}
function date(ms) {
  if (!ms) return "";
  const d = new Date(ms);
  const p = (x) => String(x).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}
const yesNo = (v) => (v === true || v === 1 ? "✓" : "✕");
// Strips in-game colour codes (&a, §l, &#ffffff) that players type into chat and inquiries.
const plain = (t) => (t == null ? "" : String(t).replace(/[&§](#[0-9a-f]{6}|[0-9a-fk-or])/gi, ""));
const short = (uuid) => (uuid || "").slice(0, 8);

/* ---------- api ---------- */
class Unauthorized extends Error {}
async function api(path, opts = {}) {
  const res = await fetch(path, { credentials: "same-origin", ...opts });
  if (res.status === 401) throw new Unauthorized();
  if (res.status === 204) return null;
  if (!res.ok) throw new Error(`${path} → ${res.status}`);
  return res.json();
}

/* ---------- login ---------- */
const loginForm = $("#login-form");
loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const err = $("#login-error");
  err.textContent = "";
  const res = await fetch("/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ key: $("#key").value }),
  });
  if (res.ok) {
    $("#key").value = "";
    showApp();
    return;
  }
  err.textContent = res.status === 429
    ? "시도가 너무 많습니다. 1분 뒤 다시 입력하세요."
    : "키가 올바르지 않습니다. 다시 입력하세요.";
  loginForm.classList.remove("shake");
  void loginForm.offsetWidth;
  loginForm.classList.add("shake");
});

$("#logout").addEventListener("click", async () => {
  try { await api("/api/logout", { method: "POST", headers: { "Content-Type": "application/json" }, body: "{}" }); }
  catch { /* already logged out */ }
  showLogin();
});

function showLogin() {
  $("#app").hidden = true;
  $("#login").hidden = false;
  $("#key").focus();
}

function showApp() {
  $("#login").hidden = true;
  $("#app").hidden = false;
  go(state.tab || "overview");
}

/* ---------- tabs ---------- */
const state = { tab: null, uuid: null };
const tabs = { overview, users, inquiries, bags, mailbox, products, advancements };

document.querySelectorAll(".tab").forEach((b) => b.addEventListener("click", () => go(b.dataset.tab)));

async function go(tab, uuid = null) {
  state.tab = tab;
  state.uuid = uuid;
  document.querySelectorAll(".tab").forEach((b) => b.setAttribute("aria-selected", String(b.dataset.tab === tab)));
  hideTooltip();
  view.replaceChildren(el("p", { class: "muted", text: "불러오는 중…" }));
  try {
    view.replaceChildren(...[await tabs[tab](uuid)].flat(Infinity).filter(Boolean));
  } catch (e) {
    if (e instanceof Unauthorized) return showLogin();
    view.replaceChildren(el("p", { class: "empty", text: "데이터를 불러오지 못했습니다. 서버 로그를 확인하세요. (" + e.message + ")" }));
  }
}

/* ---------- reusable table ---------- */
function table(columns, rows, { onRow, selectedKey } = {}) {
  let sortKey = null, dir = 1;
  const tbody = el("tbody");
  const ths = columns.map((c) => el("th", {
    scope: "col", text: c.label, tabindex: "0",
    onclick: () => sortBy(c), onkeydown: (e) => e.key === "Enter" && sortBy(c),
  }));
  function sortBy(c) {
    dir = sortKey === c.key ? -dir : 1;
    sortKey = c.key;
    ths.forEach((th, i) => th.setAttribute("aria-sort", columns[i] === c ? (dir > 0 ? "ascending" : "descending") : "none"));
    render();
  }
  function render() {
    const list = [...rows];
    if (sortKey) list.sort((a, b) => {
      const x = a[sortKey], y = b[sortKey];
      return (typeof x === "number" && typeof y === "number" ? x - y : String(x ?? "").localeCompare(String(y ?? ""), "ko")) * dir;
    });
    tbody.replaceChildren(...list.map((r) => {
      const tr = el("tr", { tabindex: onRow ? "0" : null, class: selectedKey && selectedKey(r) ? "selected" : null });
      if (onRow) {
        tr.addEventListener("click", () => onRow(r, tr));
        tr.addEventListener("keydown", (e) => e.key === "Enter" && onRow(r, tr));
      }
      for (const c of columns) {
        const v = c.render ? c.render(r) : r[c.key];
        tr.append(el("td", { "data-label": c.label, class: c.num ? "num" : null }, v));
      }
      return tr;
    }));
  }
  render();
  return el("table", { class: "cards" }, el("thead", {}, el("tr", {}, ths)), tbody);
}

/* ---------- 개요 ---------- */
async function overview() {
  const o = await api("/api/overview");
  return [
    el("h2", { text: "서버 현황" }),
    el("div", { class: "stats" },
      el("div", { class: "stat" }, el("b", { text: nf.format(o.users) }), "등록된 유저"),
      el("div", { class: "stat" }, el("b", {}, money(o.totalDollars)), "유저 보유 달러 합계"),
      el("div", { class: "stat" }, el("b", { text: nf.format(o.pendingInquiries) }), "답변 대기 문의"),
    ),
    el("h2", { text: "최근 문의" }),
    o.recentInquiries.length ? o.recentInquiries.map(inquiryCard) : el("p", { class: "empty", text: "아직 들어온 문의가 없습니다." }),
  ];
}

/* ---------- 유저 ---------- */
async function users(focusUuid) {
  const list = await api("/api/users");
  const search = el("input", { type: "search", placeholder: "이름 또는 UUID로 찾기", "aria-label": "유저 검색" });
  const detail = el("aside", { class: "detail" }, el("p", { class: "muted", text: "유저를 누르면 자세한 정보가 여기에 나옵니다." }));
  const holder = el("div");
  let selected = focusUuid;

  const columns = [
    { key: "name", label: "이름" },
    { key: "role", label: "직업" },
    { key: "level", label: "레벨", num: true },
    { key: "dollars", label: "달러", num: true, render: (r) => money(r.dollars) },
    { key: "playTime", label: "플레이타임", num: true, render: (r) => playtime(r.playTime) },
    { key: "tier", label: "티어" },
    { key: "mastery", label: "숙련도" },
  ];
  function draw() {
    const q = search.value.trim().toLowerCase();
    const rows = list.filter((u) => !q || (u.name || "").toLowerCase().includes(q) || (u.uuid || "").includes(q));
    holder.replaceChildren(rows.length
      ? table(columns, rows, { onRow: (r) => select(r.uuid), selectedKey: (r) => r.uuid === selected })
      : el("p", { class: "empty", text: "검색어와 맞는 유저가 없습니다." }));
  }
  async function select(uuid) {
    selected = uuid;
    draw();
    const u = await api("/api/users/" + encodeURIComponent(uuid));
    detail.replaceChildren(userDetail(u));
  }
  search.addEventListener("input", draw);
  draw();
  if (focusUuid) select(focusUuid).catch(() => {});
  return [
    el("h2", { text: `유저 ${nf.format(list.length)}명` }),
    el("div", { class: "toolbar" }, search),
    el("div", { class: "split" }, holder, detail),
  ];
}

function userDetail(u) {
  const rows = [
    ["직업", u.role], ["레벨", u.level], ["경험치", nf.format(u.Exp ?? 0)], ["직업 경험치", nf.format(u.roleExp ?? 0)],
    ["달러", money(u.dollars)], ["플레이타임", playtime(u.playTime)], ["티어", u.tier], ["숙련도", u.mastery],
    ["죄악 포인트", u.sinPoints], ["한기 상태", u.icingState], ["문의 횟수", u.inquireCount],
    ["모바일", yesNo(u.isMobile)], ["신규 유저", yesNo(u.isNewUser)], ["한기 면역", yesNo(u.isImmuneToIcing)],
    ["스코어보드 표시", yesNo(u.showBoard)], ["쿠키 수신", yesNo(u.wantsCookie)],
  ];
  return el("div", {},
    el("h3", { text: u.name || short(u.uuid) }),
    el("div", { class: "uuid", text: u.uuid }),
    el("dl", {}, rows.flatMap(([k, v]) => [el("dt", { text: k }), el("dd", {}, v ?? "-")])),
    el("div", { class: "muted", text: "보유 칭호" }),
    el("div", { class: "badges" }, (u.availablePrefix || []).map((p) =>
      el("span", { class: "badge" + (p === u.equipPrefix ? " on" : ""), text: p === u.equipPrefix ? p + " (장착)" : p }))),
    el("div", { class: "actions" },
      el("button", { class: "mc-btn small", text: "가방 보기", onclick: () => go("bags", u.uuid) }),
      el("button", { class: "mc-btn small", text: "문의 보기", onclick: () => go("inquiries", u.uuid) }),
      el("button", { class: "mc-btn small", text: "후원 보기", onclick: () => go("products", u.uuid) }),
    ),
  );
}

/* ---------- 문의 ---------- */
function inquiryCard(q) {
  const answered = q.answer && q.answer.trim();
  return el("article", { class: "inq" },
    el("div", { class: "inq-head" },
      el("strong", { text: q.player_name }),
      el("span", { class: "muted", text: date(q.created_at) }),
      el("span", { class: "badge " + (q.status === "RESOLVED" ? "ok" : "wait"), text: q.status === "RESOLVED" ? "답변 완료" : "답변 대기" }),
      el("span", { class: "muted", text: "#" + q.id }),
    ),
    el("div", { class: "bubble q", text: plain(q.message) }),
    answered ? el("div", { class: "bubble a", text: plain(q.answer) }) : el("div", { class: "bubble a none", text: "아직 답변하지 않았습니다." }),
  );
}

async function inquiries(uuid) {
  let status = "";
  const list = el("div");
  const chips = [["", "전체"], ["PENDING", "답변 대기"], ["RESOLVED", "답변 완료"]].map(([v, label]) =>
    el("button", { class: "chip", "aria-pressed": String(v === status), text: label, onclick: () => { status = v; load(); } }));
  async function load() {
    chips.forEach((c, i) => c.setAttribute("aria-pressed", String(["", "PENDING", "RESOLVED"][i] === status)));
    let rows = await api("/api/inquiries" + (status ? "?status=" + status : ""));
    if (uuid) rows = rows.filter((r) => r.uuid === uuid);
    list.replaceChildren(...(rows.length ? rows.map(inquiryCard) : [el("p", { class: "empty", text: "조건에 맞는 문의가 없습니다." })]));
  }
  await load();
  return [
    el("h2", { text: "문의 내역" }),
    el("div", { class: "toolbar" }, chips,
      uuid && el("button", { class: "chip", "aria-pressed": "true", text: short(uuid) + " 유저의 문의만 보는 중 (누르면 해제)", onclick: () => go("inquiries") })),
    list,
  ];
}

/* ---------- 가방 / 우편함 ---------- */
function itemColor(id) {
  let h = 0;
  for (const ch of id) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
  return `hsl(${h % 360} 45% 42%)`;
}

function grid(items, slots) {
  const bySlot = new Map(items.filter((i) => i.slot >= 0).map((i) => [i.slot, i]));
  const unreadable = items.filter((i) => i.slot < 0);
  const total = Math.max(slots, Math.ceil((Math.max(-1, ...bySlot.keys()) + 1) / 9) * 9);
  const cells = [];
  for (let s = 0; s < total; s++) {
    const it = bySlot.get(s);
    if (!it) { cells.push(el("div", { class: "slot" })); continue; }
    const face = el("span", { class: "item", text: it.name.slice(0, 2) });
    face.style.background = itemColor(it.model || it.id);
    cells.push(el("button", {
      class: "slot filled", type: "button", "aria-label": `${it.name} ${it.count}개`,
      onmouseenter: (e) => showTooltip(it, e.currentTarget), onfocus: (e) => showTooltip(it, e.currentTarget),
      onmouseleave: hideTooltip, onblur: hideTooltip,
    }, face, it.count > 1 ? el("span", { class: "count", text: it.count }) : null));
  }
  return [
    el("div", { class: "grid" }, cells),
    unreadable.map((u) => el("p", { class: "muted", text: "해석하지 못한 데이터: " + (u.lore[0] || "") })),
  ];
}

function showTooltip(it, anchor) {
  const name = el("div", { class: "t-name", text: it.name });
  name.style.color = mcColor(it.nameColor) || "#fff";
  tooltip.replaceChildren(
    name,
    ...it.enchants.map((e) => el("div", { class: "t-ench", text: e })),
    ...it.lore.map((l) => el("div", { class: "t-lore", text: l })),
    el("div", { class: "t-id", text: it.model ? `${it.id} (${it.model})` : it.id }),
  );
  tooltip.hidden = false;
  const r = anchor.getBoundingClientRect();
  const w = tooltip.offsetWidth, h = tooltip.offsetHeight;
  let x = r.right + 8, y = r.top - 4;
  if (x + w > innerWidth - 8) x = Math.max(8, r.left - w - 8);
  if (y + h > innerHeight - 8) y = innerHeight - h - 8;
  tooltip.style.left = x + "px";
  tooltip.style.top = Math.max(8, y) + "px";
}
function hideTooltip() { tooltip.hidden = true; }

const MC_COLORS = {
  black: "#000", dark_blue: "#00a", dark_green: "#0a0", dark_aqua: "#0aa", dark_red: "#a00", dark_purple: "#a0a",
  gold: "#fa0", gray: "#aaa", dark_gray: "#555", blue: "#55f", green: "#5f5", aqua: "#5ff", red: "#f55",
  light_purple: "#f5f", yellow: "#ff5", white: "#fff",
};
const mcColor = (c) => (c && (MC_COLORS[c] || (/^#[0-9a-f]{6}$/i.test(c) ? c : null)));

async function bags(uuid) {
  const owners = await api("/api/bags");
  if (!owners.length) return [el("h2", { text: "가방" }), el("p", { class: "empty", text: "저장된 가방이 없습니다." })];
  const panel = el("div");
  const buttons = owners.map((o) => el("button", {
    class: "mc-btn", onclick: () => open(o.uuid),
  }, el("span", { text: o.name }), el("span", { class: "muted", text: o.itemCount })));
  async function open(id) {
    owners.forEach((o, i) => buttons[i].setAttribute("aria-selected", String(o.uuid === id)));
    const bag = await api("/api/bags/" + encodeURIComponent(id));
    if (!bag) return panel.replaceChildren(el("p", { class: "empty", text: "이 유저는 가방을 연 적이 없습니다." }));
    panel.replaceChildren(el("div", { class: "container" },
      el("h3", { text: `${bag.name}의 가방 (${bag.items.length}칸 사용)` }), grid(bag.items, 54)));
  }
  const first = owners.find((o) => o.uuid === uuid) || owners[0];
  if (uuid && !owners.some((o) => o.uuid === uuid)) panel.append(el("p", { class: "empty", text: "이 유저는 가방을 연 적이 없습니다." }));
  else await open(first.uuid);
  return [el("h2", { text: "가방" }), el("div", { class: "bag-layout" }, el("div", { class: "owner-list" }, buttons), panel)];
}

async function mailbox() {
  const rows = await api("/api/mailbox");
  return [
    el("h2", { text: "가방 우편함" }),
    el("p", { class: "muted", text: "접속하지 않은 유저에게 보낸 아이템입니다. 유저가 서버에 들어오면 가방으로 옮겨지고 여기서 사라집니다." }),
    rows.length
      ? rows.map((m) => el("div", { class: "container mail" },
        el("h3", { text: `${m.name}에게 보낸 묶음 #${m.id}` }), grid(m.items, 9)))
      : el("p", { class: "empty", text: "받지 않은 우편이 없습니다." }),
  ];
}

/* ---------- 후원 ---------- */
async function products(uuid) {
  let rows = await api("/api/products");
  if (uuid) rows = rows.filter((r) => r.receiver === uuid);
  const columns = [
    { key: "id", label: "번호", num: true },
    { key: "name", label: "받는 사람" },
    { key: "product", label: "상품" },
    { key: "granted", label: "지급", render: (r) => el("span", { class: "badge " + (r.granted ? "ok" : "wait"), text: r.granted ? "지급 완료" : "지급 대기" }) },
  ];
  return [
    el("h2", { text: "후원 지급 기록" }),
    uuid && el("div", { class: "toolbar" }, el("button", { class: "chip", "aria-pressed": "true", text: short(uuid) + " 유저의 기록만 보는 중 (누르면 해제)", onclick: () => go("products") })),
    rows.length ? table(columns, rows) : el("p", { class: "empty", text: "후원 지급 기록이 없습니다." }),
  ];
}

/* ---------- 업적 ---------- */
async function advancements() {
  const players = await api("/api/advancements");
  return [
    el("h2", { text: "업적" }),
    players.length ? players.map((p) => el("details", { class: "adv" },
      el("summary", {}, el("strong", { text: p.name }), el("span", { class: "muted", text: `${p.done.length}개 달성` })),
      p.done.length
        ? el("ul", {}, p.done.map((a) => el("li", { text: a.key, title: `${a.namespace}:${a.key} (진행 ${a.progression})` })))
        : el("p", { class: "muted", text: "아직 달성한 업적이 없습니다." }),
    )) : el("p", { class: "empty", text: "업적 데이터가 없습니다." }),
  ];
}

/* ---------- snow ---------- */
(function snow() {
  const canvas = $("#snow");
  if (matchMedia("(prefers-reduced-motion: reduce)").matches) { canvas.remove(); return; }
  const ctx = canvas.getContext("2d");
  let flakes = [];
  function resize() {
    canvas.width = innerWidth;
    canvas.height = innerHeight;
    flakes = Array.from({ length: Math.round(innerWidth / 14) }, () => ({
      x: Math.random() * innerWidth, y: Math.random() * innerHeight,
      s: Math.random() < 0.2 ? 3 : 2, v: 0.2 + Math.random() * 0.5, d: Math.random() * Math.PI * 2,
    }));
  }
  function frame() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = "rgba(255,255,255,.55)";
    for (const f of flakes) {
      f.y += f.v;
      f.d += 0.01;
      f.x += Math.sin(f.d) * 0.3;
      if (f.y > canvas.height) { f.y = -4; f.x = Math.random() * canvas.width; }
      ctx.fillRect(Math.round(f.x), Math.round(f.y), f.s, f.s);
    }
    requestAnimationFrame(frame);
  }
  addEventListener("resize", resize);
  resize();
  frame();
})();

/* ---------- boot: an existing session cookie skips the login screen ---------- */
api("/api/overview").then(() => showApp()).catch(() => showLogin());
