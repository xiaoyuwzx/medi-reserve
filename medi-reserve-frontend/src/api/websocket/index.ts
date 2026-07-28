import { Api } from './websocketApi'
import { websocketInstance } from '@/api/instances'

const api = new Api()
api.instance = websocketInstance
export const websocketApi = api