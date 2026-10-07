const TOKEN_KEY = 'campus-token'
const USER_KEY = 'campus-user'
const ADMIN_TOKEN_KEY = 'campus-admin-token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY) || ''
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function saveSession(login) {
  localStorage.setItem(TOKEN_KEY, login.token)
  localStorage.setItem(USER_KEY, JSON.stringify({
    userId: login.userId,
    username: login.username,
    nickname: login.nickname,
    avatar: login.avatar
  }))
}

export function saveAdminSession(login) {
  localStorage.setItem(ADMIN_TOKEN_KEY, login.token)
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function clearAdminSession() {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
}

export async function request(path, options = {}) {
  const { admin, ...rest } = options
  const headers = { ...(rest.headers || {}) }
  const isForm = rest.body instanceof FormData
  if (rest.body && !isForm && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }
  const token = admin ? getAdminToken() : getToken()
  if (token) headers.token = token
  const response = await fetch(path, { ...rest, headers })
  if (response.status === 401) throw new Error('请先登录')
  const result = await response.json()
  if (result.code !== 1) throw new Error(result.msg || '请求失败')
  return result.data
}

export function uploadFile(file) {
  const body = new FormData()
  body.append('file', file)
  return request('/user/files/upload', { method: 'POST', body })
}
