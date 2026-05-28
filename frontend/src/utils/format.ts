export function formatDateTime(value?: string | null) {
  if (!value) return '未记录'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function formatFileSize(value?: number | null) {
  if (!value) return '未知大小'
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

export function photoDownloadUrl(id?: number | string | null) {
  return id ? `http://localhost:8080/api/v1/photos/${id}/download` : ''
}

export function groupByDay<T extends { shotAt?: string; createTime?: string }>(items: T[]) {
  return items.reduce<Record<string, T[]>>((groups, item) => {
    const value = item.shotAt || item.createTime || ''
    const key = value
      ? new Date(value).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
      : '未知日期'
    groups[key] = groups[key] || []
    groups[key].push(item)
    return groups
  }, {})
}

export function triggerPhotoDownload(id: number) {
  window.open(photoDownloadUrl(id), '_blank')
}
