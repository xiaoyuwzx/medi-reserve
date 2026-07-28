import { Api } from './doctorApi'
import request from '@/api/request'

export const doctorApi = new Api(request as any)