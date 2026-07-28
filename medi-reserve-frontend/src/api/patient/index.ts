import { Api } from './patientApi'
import { patientInstance } from '@/api/instances'

const api = new Api()
api.instance = patientInstance
export const patientApi = api