import { Api } from './websocketApi'
import request from '@/api/request'

export const websocketApi = new Api(request as any)