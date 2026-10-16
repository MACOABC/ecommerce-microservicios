export interface Categoria {
  id: number;
  nombre: string;
}
export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  stock: number;
  categoriaId: number;
  activo: boolean;
}
export interface Item {
  productoId: number;
  cantidad: number;
}
export interface Compra {
  id: string;
  cliente: string;
  email: string;
  direccion: string;
  aprobarPago: boolean;
  items: Item[];
}
export interface Orden {
  id: string;
  cliente: string;
  email: string;
  direccion: string;
  estado: "CREADA" | "PAGADA" | "RECHAZADA";
  total: number;
  creada: string;
  items: (Item & { nombre: string | null; precio: number | null })[];
}
export interface Envio {
  id: string;
  ordenId: string;
  guia: string;
  destinatario: string;
  direccion: string;
  estado: string;
  creado: string;
  actualizado: string;
}
export interface Seguimiento {
  envio: Envio;
  eventos: { estado: string; fecha: string }[];
}
export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}
export async function api<T>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 25000);
  try {
    const response = await fetch("/api" + path, {
      method,
      signal: controller.signal,
      headers: body === undefined ? {} : { "Content-Type": "application/json" },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    const data = await response.json().catch(() => null);
    if (!response.ok)
      throw new ApiError(
        response.status,
        data?.message || "No se pudo completar la solicitud.",
      );
    return data as T;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(
      0,
      "No se pudo conectar. Comprueba que los servicios estén iniciados y vuelve a intentarlo.",
    );
  } finally {
    clearTimeout(timeout);
  }
}
