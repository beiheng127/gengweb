export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: number
  status: number
}

export interface Meme {
  id?: number
  title: string
  originTime: string
  boomTime: string
  content: string
  mediaType: number
  mediaUrl: string
  audioUrl?: string
  viewCount?: number
  likeCount?: number
  dislikeCount?: number
  currentVersion?: number
  status: number
  createUserId?: number
  createTime?: string
  updateTime?: string
  createUserName?: string
  tags?: Tag[]
}

export interface MemeVersion {
  id?: number
  memeId: number
  version: number
  title: string
  content: string
  mediaUrl: string
  audioUrl?: string
  auditStatus: number
  auditUserId?: number
  rejectReason?: string
  createUserId: number
  createTime?: string
}

export interface Tag {
  id?: number
  name: string
  type: number
  useCount?: number
  createTime?: string
}

export interface Comment {
  id?: number
  memeId: number
  userId: number
  content: string
  likeCount?: number
  createTime?: string
  userName?: string
  userAvatar?: string
}

export interface PaginationParams {
  current: number
  size: number
  status?: number
  keyword?: string
}

export interface PaginationResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
