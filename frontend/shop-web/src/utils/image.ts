export const PLACEHOLDER_IMAGE = '/catalog/placeholder.svg'

export function imageOrPlaceholder(url?: string | null) {
  return url && url.trim() ? url : PLACEHOLDER_IMAGE
}
