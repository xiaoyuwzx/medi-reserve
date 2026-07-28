import { defineStore } from 'pinia'
import { patientApi } from '@/api/patient'
import type { DepartmentVO, Title } from '@/api/patient/patientApi'

export const useDictStore = defineStore('dict', {
  state: () => ({
    departments: [] as DepartmentVO[],
    titles: [] as Title[],
    isLoaded: false
  }),
  actions: {
    async loadDict() {
      if (this.isLoaded) return

      try {
        const [departments, titles] = await Promise.all([
          patientApi.patient.getAllDepartments(),
          patientApi.patient.getTitles()
        ])
        this.departments = departments as unknown as DepartmentVO[]
        this.titles = titles as unknown as Title[]
        this.isLoaded = true
      } catch (error) {
        console.error('字典数据加载失败', error)
      }
    }
  }
})
