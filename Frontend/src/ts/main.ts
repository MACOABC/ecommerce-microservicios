import {
  api,
  ApiError,
  type Producto,
  type Categoria,
  type Compra,
  type Orden,
  type Envio,
  type Seguimiento,
} from "./api";
import { art } from "./art";

const el = <T extends HTMLElement = HTMLElement>(id: string) =>
  document.getElementById(id) as T;
const escape = (value: unknown) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ]!,
  );
const money = (value: number) =>
  new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" }).format(
    value,
  );
const date = (value: string) => new Date(value).toLocaleString("es-PE");
const stateLabel = (value: string) => value.replaceAll("_", " ").toLowerCase();
function read<T>(key: string, fallback: T): T {
  try {
    return JSON.parse(localStorage.getItem(key) || "null") ?? fallback;
  } catch {
    return fallback;
  }
}
function save(key: string, value: unknown) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    notify(
      "El navegador no permite guardar tus datos. Mantén esta ventana abierta.",
      true,
    );
  }
}
type CartLine = { producto: Producto; cantidad: number };
const storedCart = read<unknown>("merca.cart", []);
let cart: CartLine[] = Array.isArray(storedCart)
  ? storedCart.filter(
      (x): x is CartLine =>
        x &&
        typeof x.producto?.id === "number" &&
        typeof x.producto?.nombre === "string" &&
        Number.isFinite(x.producto?.precio) &&
        Number.isInteger(x.cantidad) &&
        x.cantidad > 0 &&
        x.cantidad <= 999,
    )
  : [];
const storedIds = read<unknown>("merca.orders", []);
let orderIds: string[] = Array.isArray(storedIds)
  ? storedIds.filter((id): id is string => typeof id === "string")
  : [];
let pending = read<Compra | null>("merca.pending", null);
let products: Producto[] = [];
let categories: Categoria[] = [];
let selectedCategory = 0;
let query = "";
let busy = false;
let noticeTimer: ReturnType<typeof setTimeout>;
function notify(message: string, error = false) {
  el("notice").textContent = message;
  el("notice").className = error ? "error" : "";
  el("notice").hidden = false;
  clearTimeout(noticeTimer);
  noticeTimer = setTimeout(
    () => {
      el("notice").hidden = true;
    },
    error ? 10000 : 4000,
  );
}
const failure = (e: unknown) =>
  notify(e instanceof Error ? e.message : "Ocurrió un error.", true);
function remember(id: string) {
  orderIds = [id, ...orderIds.filter((x) => x !== id)].slice(0, 30);
  save("merca.orders", orderIds);
}
function persistCart() {
  save("merca.cart", cart);
  renderCart();
}
function renderCart() {
  el("cart-count").textContent = String(
    cart.reduce((sum, x) => sum + x.cantidad, 0),
  );
  el("cart-total").textContent = money(
    cart.reduce((sum, x) => sum + x.producto.precio * x.cantidad, 0),
  );
  el("cart-items").innerHTML = cart.length
    ? cart
        .map(
          (x) => `<article class="cart-row">
    <div><h3>${escape(x.producto.nombre)}</h3><span class="muted">${money(x.producto.precio)} por unidad</span>
    <div class="quantity"><button data-quantity="${x.producto.id}" data-delta="-1" aria-label="Restar una unidad de ${escape(x.producto.nombre)}">−</button>
    <span>${x.cantidad}</span><button data-quantity="${x.producto.id}" data-delta="1" aria-label="Sumar una unidad de ${escape(x.producto.nombre)}">+</button>
    <button class="remove" data-remove="${x.producto.id}" aria-label="Quitar ${escape(x.producto.nombre)}">×</button></div></div>
    <strong class="price">${money(x.producto.precio * x.cantidad)}</strong></article>`,
        )
        .join("")
    : '<div class="empty">Tu bolsa espera algo especial. Explora el catálogo y agrega tus favoritos.</div>';
  (el("checkout-button") as HTMLButtonElement).disabled =
    !cart.length || busy || pending !== null;
  el("checkout-button").textContent = busy
    ? "Procesando compra…"
    : pending
      ? "Resuelve tu compra pendiente en Mis pedidos"
      : "Confirmar compra ↗";
}
function renderCategories() {
  el("categories").innerHTML = [{ id: 0, nombre: "Todo" }, ...categories]
    .map(
      (c) =>
        `<button class="chip ${selectedCategory === c.id ? "active" : ""}" data-category="${c.id}" aria-pressed="${selectedCategory === c.id}">${escape(c.nombre)}</button>`,
    )
    .join("");
}
function renderProducts() {
  const shown = products.filter(
    (p) =>
      (!selectedCategory || p.categoriaId === selectedCategory) &&
      (p.nombre + " " + p.descripcion)
        .toLocaleLowerCase()
        .includes(query.toLocaleLowerCase()),
  );
  el("product-count").textContent = `${shown.length} productos`;
  el("product-grid").innerHTML = shown
    .map(
      (p) => `<article class="product-card">
    <div class="product-art"><span class="product-tag">${escape(categories.find((c) => c.id === p.categoriaId)?.nombre || "Selección")}</span>${art(p.id)}</div>
    <div class="product-content"><h3>${escape(p.nombre)}</h3><p>${escape(p.descripcion)}</p>
    <div class="product-bottom"><span class="price">${money(p.precio)}</span><button class="add-button" data-add="${p.id}" ${p.stock === 0 ? "disabled" : ""}>${p.stock === 0 ? "Agotado" : "Agregar a la bolsa ＋"}</button></div>
    <div class="stock">${p.stock > 0 ? `${p.stock} disponibles` : "Volverá pronto"}</div></div></article>`,
    )
    .join("");
  el("catalog-status").innerHTML = shown.length
    ? ""
    : '<div class="empty">No encontramos productos con esos filtros.</div>';
}
async function loadCatalog() {
  el("catalog-status").innerHTML =
    '<p class="loading">Preparando nuestra selección…</p>';
  try {
    [products, categories] = await Promise.all([
      api<Producto[]>("/productos"),
      api<Categoria[]>("/categorias"),
    ]);
    renderCategories();
    renderProducts();
  } catch (e) {
    el("catalog-status").innerHTML =
      `<div class="error-panel">${escape(e instanceof Error ? e.message : "Catálogo no disponible")} <button class="add-button" data-reload>Reintentar</button></div>`;
  }
}
function renderPending() {
  el("pending-purchase").innerHTML = pending
    ? `<div class="pending"><strong>Hay una compra por resolver.</strong><br>
    Conservamos el identificador ${escape(pending.id)}. Reintenta para consultar o completar la misma operación.
    <br><button class="button" data-resume ${busy ? "disabled" : ""}>${busy ? "Procesando…" : "Recuperar compra"}</button></div>`
    : "";
}
async function renderOrders() {
  renderPending();
  if (!orderIds.length) {
    el("orders-list").innerHTML =
      '<div class="empty">Tus próximas historias empiezan en el catálogo. Aquí verás las compras realizadas desde este navegador.</div>';
    return;
  }
  el("orders-list").innerHTML = '<p class="loading">Consultando pedidos…</p>';
  const results = await Promise.allSettled(
    orderIds.map((id) => api<Orden>("/ordenes/" + encodeURIComponent(id))),
  );
  el("orders-list").innerHTML = results
    .map((result, index) => {
      if (result.status === "rejected")
        return `<div class="error-panel">No se pudo consultar la orden ${escape(orderIds[index])}. Vuelve a abrir Mis pedidos para reintentar.</div>`;
      const o = result.value;
      return `<article class="order-card"><div class="order-top"><span class="order-id">${escape(o.id)}</span>
      <span class="badge ${o.estado === "RECHAZADA" ? "rejected" : ""}">${escape(o.estado)}</span></div>
      <p class="small muted">${escape(date(o.creada))} · ${escape(o.cliente)}</p>
      <ul>${o.items.map((i) => `<li>${i.cantidad} × ${escape(i.nombre || "Producto " + i.productoId)} ${i.precio === null ? "" : "· " + money(i.precio * i.cantidad)}</li>`).join("")}</ul>
      <strong>${money(o.total)}</strong><p class="small muted">${escape(o.direccion)}</p>
      <div class="order-actions">${o.estado === "PAGADA" ? `<button class="button" data-shipment="${escape(o.id)}">Generar o consultar guía ↗</button>` : o.estado === "CREADA" ? `<button class="button" data-retry-order="${escape(o.id)}">Reintentar orden</button>` : '<span class="small muted">Pago rechazado. El stock reservado fue liberado.</span>'}</div></article>`;
    })
    .join("");
}
async function finish(o: Orden) {
  remember(o.id);
  pending = null;
  save("merca.pending", null);
  if (o.estado === "PAGADA") {
    cart = [];
    persistCart();
    notify("¡Tu compra está lista! Estamos preparando tu guía.");
    try {
      const shipment = await api<Envio>("/ordenes/" + o.id + "/envio", "POST");
      (el("tracking-code") as HTMLInputElement).value = shipment.guia;
    } catch {
      notify(
        "Tu orden está pagada. Genera su guía desde Mis pedidos cuando logística esté disponible.",
        true,
      );
    }
  } else {
    notify("El pago simulado fue rechazado. Tu bolsa sigue disponible.", true);
  }
  renderCart();
  location.hash = "pedidos";
  await renderOrders();
  void loadCatalog();
}
async function submitPurchase(resume: boolean) {
  if (busy || !pending) return;
  busy = true;
  renderCart();
  renderPending();
  try {
    const o = resume
      ? await api<Orden>(
          "/ordenes/" + encodeURIComponent(pending.id) + "/reintentar",
          "POST",
        )
      : await api<Orden>("/ordenes", "POST", pending);
    await finish(o);
  } catch (e) {
    // Un 404 en recuperación puede significar que la primera solicitud nunca llegó.
    if (resume && e instanceof ApiError && e.status === 404) {
      try {
        await finish(await api<Orden>("/ordenes", "POST", pending));
      } catch (error) {
        handlePurchaseError(error);
      }
    } else handlePurchaseError(e);
  } finally {
    busy = false;
    renderCart();
    renderPending();
  }
}
function handlePurchaseError(e: unknown) {
  // 409 (por ejemplo stock insuficiente) revierte la reserva completa.
  // Las respuestas ambiguas y los fallos de conexión conservan el UUID para reintentar.
  if (e instanceof ApiError && [400, 404, 409].includes(e.status)) {
    pending = null;
    save("merca.pending", null);
  }
  failure(e);
  if (pending) {
    location.hash = "pedidos";
    renderPending();
  }
}
async function tracking(guia: string) {
  el("tracking-result").innerHTML = '<p class="loading">Buscando tu envío…</p>';
  try {
    const s = await api<Seguimiento>(
      "/envios/seguimiento/" + encodeURIComponent(guia.trim()),
    );
    el("tracking-result").innerHTML =
      `<article class="tracking-card"><span class="badge">${escape(stateLabel(s.envio.estado))}</span>
      <h2 style="margin-top:20px">${escape(s.envio.guia)}</h2>
      <p class="small muted">${escape(s.envio.destinatario)} · ${escape(s.envio.direccion)}</p>
      <ol class="timeline">${s.eventos.map((e) => `<li>${escape(stateLabel(e.estado))}<time datetime="${escape(e.fecha)}">${escape(date(e.fecha))}</time></li>`).join("")}</ol></article>`;
  } catch (e) {
    el("tracking-result").innerHTML =
      `<div class="error-panel">${escape(e instanceof Error ? e.message : "No se pudo consultar")}</div>`;
  }
}
function route() {
  const hash = location.hash.slice(1);
  const view = ["catalogo", "carrito", "pedidos", "seguimiento"].includes(hash)
    ? hash
    : "catalogo";
  document.querySelectorAll<HTMLElement>(".view").forEach((section) => {
    section.hidden = section.id !== view;
  });
  document
    .querySelectorAll<HTMLElement>("[data-tab]")
    .forEach((link) =>
      link.classList.toggle("active", link.dataset.tab === view),
    );
  if (view === "pedidos") void renderOrders();
  if (hash !== "productos") window.scrollTo({ top: 0 });
}
document.addEventListener("click", async (event) => {
  const button = (event.target as HTMLElement).closest<HTMLButtonElement>(
    "button",
  );
  if (!button || button.disabled) return;
  const data = button.dataset;
  if (pending && (data.add || data.quantity || data.remove)) {
    notify(
      "Recupera tu compra pendiente en Mis pedidos antes de modificar la bolsa.",
      true,
    );
    return;
  }
  if (data.add) {
    const p = products.find((p) => p.id === Number(data.add));
    if (!p) return;
    const line = cart.find((x) => x.producto.id === p.id);
    if ((line?.cantidad || 0) >= Math.min(p.stock, 999))
      return notify("Ya agregaste todas las unidades disponibles.", true);
    if (line) line.cantidad++;
    else cart.push({ producto: p, cantidad: 1 });
    persistCart();
    notify(p.nombre + " se agregó a tu bolsa.");
  } else if (data.quantity) {
    const line = cart.find((x) => x.producto.id === Number(data.quantity));
    if (!line) return;
    const max =
      products.find((p) => p.id === line.producto.id)?.stock ??
      line.producto.stock;
    const next = line.cantidad + Number(data.delta);
    if (next > Math.min(max, 999))
      return notify("No hay más unidades disponibles.", true);
    line.cantidad = next;
    cart = cart.filter((x) => x.cantidad > 0);
    persistCart();
  } else if (data.remove) {
    cart = cart.filter((x) => x.producto.id !== Number(data.remove));
    persistCart();
  } else if (data.category) {
    selectedCategory = Number(data.category);
    renderCategories();
    renderProducts();
  } else if ("reload" in data) void loadCatalog();
  else if ("resume" in data) await submitPurchase(true);
  else if (data.retryOrder) {
    button.disabled = true;
    try {
      const o = await api<Orden>(
        "/ordenes/" + encodeURIComponent(data.retryOrder) + "/reintentar",
        "POST",
      );
      notify("Estado de la orden: " + o.estado);
      await renderOrders();
      void loadCatalog();
    } catch (e) {
      failure(e);
    } finally {
      button.disabled = false;
    }
  } else if (data.shipment) {
    button.disabled = true;
    try {
      const envio = await api<Envio>(
        "/ordenes/" + encodeURIComponent(data.shipment) + "/envio",
        "POST",
      );
      (el("tracking-code") as HTMLInputElement).value = envio.guia;
      location.hash = "seguimiento";
      await tracking(envio.guia);
    } catch (e) {
      failure(e);
    } finally {
      button.disabled = false;
    }
  }
});
el("search").addEventListener("input", (event) => {
  query = (event.target as HTMLInputElement).value;
  renderProducts();
});
el("checkout-form").addEventListener("submit", (event) => {
  event.preventDefault();
  if (busy || pending || !cart.length) return;
  const data = new FormData(event.target as HTMLFormElement);
  pending = {
    id: crypto.randomUUID(),
    cliente: String(data.get("cliente")).trim(),
    email: String(data.get("email")).trim(),
    direccion: String(data.get("direccion")).trim(),
    aprobarPago: data.get("aprobarPago") === "true",
    items: cart.map((x) => ({
      productoId: x.producto.id,
      cantidad: x.cantidad,
    })),
  };
  save("merca.pending", pending);
  void submitPurchase(false);
});
el("order-search").addEventListener("submit", async (event) => {
  event.preventDefault();
  const id = String(
    new FormData(event.target as HTMLFormElement).get("id"),
  ).trim();
  try {
    const o = await api<Orden>("/ordenes/" + encodeURIComponent(id));
    remember(o.id);
    await renderOrders();
  } catch (e) {
    failure(e);
  }
});
el("tracking-form").addEventListener("submit", (event) => {
  event.preventDefault();
  void tracking((el("tracking-code") as HTMLInputElement).value);
});
window.addEventListener("hashchange", route);
renderCart();
route();
void loadCatalog();
