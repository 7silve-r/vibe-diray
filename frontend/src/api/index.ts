declare const __UPLOAD_PREFIX__: string;

export class ApiError extends Error {
  constructor(
    message: string,
    public status = 0,
  ) {
    super(message);
  }
}
const base = import.meta.env.VITE_API_BASE || '/backend';
export function token() {
  return sessionStorage.getItem('vibe-token') || '';
}
export function media(url?: string) {
  if (!url) return '';
  if (url.startsWith(typeof __UPLOAD_PREFIX__ === 'string' ? __UPLOAD_PREFIX__ : '/uploads/'))
    return base + url;
  return /^(https?:|blob:)/.test(url) ? url : '';
}
export async function api<T = any>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  if (token()) headers.Authorization = `Bearer ${token()}`;
  if (body && !(body instanceof FormData)) headers['Content-Type'] = 'application/json';
  let response: Response;
  try {
    response = await fetch(base + path, {
      method,
      headers,
      body: body instanceof FormData ? body : body === undefined ? undefined : JSON.stringify(body),
      signal: AbortSignal.timeout(20000),
    });
  } catch {
    throw new ApiError('暂时连接不上服务，请稍后重试');
  }
  const result = await response.json().catch(() => null);
  if (!response.ok || result?.code !== 200) {
    if (response.status === 401) window.dispatchEvent(new Event('vibe:unauthorized'));
    throw new ApiError(result?.message || `请求未完成（${response.status}）`, response.status);
  }
  return result.data as T;
}
export function query(values: Record<string, unknown>) {
  return new URLSearchParams(
    Object.entries(values)
      .filter(([, v]) => v !== '' && v !== undefined && v !== null)
      .map(([k, v]) => [k, String(v)]),
  ).toString();
}
export async function upload(
  path: string,
  file: File,
  method = 'PUT',
  values: Record<string, string> = {},
) {
  const data = new FormData();
  data.append('file', file);
  Object.entries(values).forEach(([k, v]) => data.append(k, v));
  return api(path, method, data);
}
