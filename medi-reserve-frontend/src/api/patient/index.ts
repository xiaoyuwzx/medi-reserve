import { Api } from './patientApi'
import request from '@/api/request'

export const patientApi = new Api(request as any)