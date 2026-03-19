/**
 * FlowHR API Client
 * Fetch utility dengan JWT auth header untuk semua request ke backend.
 */

const API_BASE = import.meta.env.PUBLIC_API_URL || 'http://localhost:8080/api/v1';

function getToken(): string | null {
    if (typeof localStorage !== 'undefined') {
        return localStorage.getItem('flowhr_token');
    }
    return null;
}

export function setToken(token: string): void {
    localStorage.setItem('flowhr_token', token);
}

export function clearToken(): void {
    localStorage.removeItem('flowhr_token');
    localStorage.removeItem('flowhr_user');
}

async function request<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {
    const token = getToken();
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string> || {}),
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

    if (res.status === 401) {
        clearToken();
        window.location.href = '/login';
        throw new Error('Sesi habis, silakan login kembali');
    }

    const json = await res.json();

    if (!json.success) {
        throw new Error(json.message || 'Terjadi kesalahan');
    }

    return json.data as T;
}

export const api = {
    get: <T>(path: string) => request<T>(path),
    post: <T>(path: string, body: unknown) =>
        request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
    put: <T>(path: string, body?: unknown) =>
        request<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
    patch: <T>(path: string, body?: unknown) =>
        request<T>(path, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined }),
    delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
};

// Auth helpers
export const auth = {
    login: async (username: string, password: string) => {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password }),
        });
        const json = await res.json();
        if (!json.success) throw new Error(json.message);
        setToken(json.data.token);
        localStorage.setItem('flowhr_user', JSON.stringify(json.data));
        return json.data;
    },
    logout: () => {
        clearToken();
        window.location.href = '/login';
    },
    getUser: () => {
        if (typeof localStorage === 'undefined') return null;
        const u = localStorage.getItem('flowhr_user');
        return u ? JSON.parse(u) : null;
    },
    isAuthenticated: () => !!getToken(),
};
