import request from '@/utils/request'
import type { User, Meme, Tag, Comment, PaginationParams, PaginationResult } from '@/types'

export const userApi = {
  login: (data: { username: string; password: string }) =>
    request.post<{ token: string; user: User }>('/user/login', data),
  register: (data: { username: string; password: string; email?: string }) =>
    request.post<User>('/user/register', data),
  list: (keyword?: string) =>
    request.get<User[]>('/user/list', { params: { keyword } }),
  updateRole: (id: number, role: number) =>
    request.put<void>(`/user/${id}/role`, { role }),
  updateStatus: (id: number, status: number) =>
    request.put<void>(`/user/${id}/status`, { status })
}

export const memeApi = {
  list: (params: PaginationParams) =>
    request.get<PaginationResult<Meme>>('/meme/list', { params }),
  detail: (id: number) =>
    request.get<Meme>(`/meme/${id}`),
  create: (data: Meme) =>
    request.post<Meme>('/meme', data),
  update: (id: number, data: Partial<Meme>) =>
    request.put<Meme>(`/meme/${id}`, data),
  submit: (id: number) =>
    request.post<void>(`/meme/${id}/submit`),
  audit: (id: number, data: { status: number; rejectReason?: string; auditorId?: number }) =>
    request.post<void>(`/meme/${id}/audit`, data),
  delete: (id: number) =>
    request.delete<void>(`/meme/${id}`),
  like: (id: number) =>
    request.post<void>(`/meme/${id}/like`),
  unlike: (id: number) =>
    request.post<void>(`/meme/${id}/unlike`),
  dislike: (id: number) =>
    request.post<void>(`/meme/${id}/dislike`),
  undislike: (id: number) =>
    request.post<void>(`/meme/${id}/undislike`)
}

export const tagApi = {
  list: (params?: { type?: number }) =>
    request.get<Tag[]>('/tag/list', { params }),
  create: (data: Tag) =>
    request.post<Tag>('/tag', data),
  delete: (id: number) =>
    request.delete<void>(`/tag/${id}`)
}

export const commentApi = {
  create: (data: { memeId: number; content: string }) =>
    request.post<Comment>('/comment', data),
  list: (memeId: number) =>
    request.get<Comment[]>(`/comment/list/${memeId}`),
  like: (id: number) =>
    request.post<void>(`/comment/${id}/like`)
}
