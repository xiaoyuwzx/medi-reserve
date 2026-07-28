import { Api } from './doctorApi'
import { doctorInstance } from '@/api/instances'

const api = new Api()
api.instance = doctorInstance
export const doctorApi = api