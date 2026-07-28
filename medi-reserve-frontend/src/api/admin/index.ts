import { Api } from './adminApi'
import request from '@/api/request'

export const adminApi = new Api(request as any)