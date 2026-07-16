/**
 * 校验手机号格式
 * @param {string} phone - 手机号
 * @returns {boolean} 是否合法
 */
export function isValidPhone(phone) {
  if (!phone) return false
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 校验密码格式（6-20位字母数字组合）
 * @param {string} password - 密码
 * @returns {boolean} 是否合法
 */
export function isValidPassword(password) {
  if (!password) return false
  return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/.test(password)
}