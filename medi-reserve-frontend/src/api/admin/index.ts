import { Api } from './adminApi'
import { adminInstance } from '@/api/instances'

const api = new Api()
api.instance = adminInstance
export const adminApi = api