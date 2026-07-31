<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const treeData = ref<TreeNode[]>([])
const loading = ref(false)

interface TreeNode {
  id: number
  code: string
  name: string
  type: number
  sortOrder?: number
  children?: TreeNode[]
}

const typeMap: Record<number, { text: string; type: 'primary' | 'warning' | 'info' }> = {
  1: { text: '菜单', type: 'primary' },
  2: { text: '按钮', type: 'warning' },
  3: { text: '接口', type: 'info' }
}

/** 基于权限 code 的层级关系构建树（admin:audit → admin:audit:view） */
function buildTreeByCode(list: TreeNode[]): TreeNode[] {
  const map = new Map<string, TreeNode>()
  const roots: TreeNode[] = []

  for (const item of list) {
    map.set(item.code, { ...item, children: [] })
  }

  const sorted = [...map.values()].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))

  for (const item of sorted) {
    const lastColon = item.code.lastIndexOf(':')
    if (lastColon === -1) {
      roots.push(item)
    } else {
      const parentCode = item.code.substring(0, lastColon)
      const parent = map.get(parentCode)
      if (parent?.children) {
        parent.children.push(item)
      } else {
        roots.push(item)
      }
    }
  }

  // 清理空 children
  const cleanEmpty = (nodes: TreeNode[]) => {
    for (const node of nodes) {
      if (node.children?.length === 0) delete node.children
      else if (node.children) cleanEmpty(node.children)
    }
  }
  cleanEmpty(roots)

  return roots
}

async function loadTree() {
  loading.value = true
  try {
    const res = await adminApi.admin.getPermissionTree()
    const data = (res as unknown as TreeNode[]) ?? []

    if (data.length > 0 && data[0].children !== undefined) {
      treeData.value = data
    } else {
      treeData.value = buildTreeByCode(data)
    }
  } catch {
    treeData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTree()
})
</script>

<template>
  <div class="perm-tree-page">
    <h2 class="page-title">权限树</h2>

    <div class="tree-container" v-loading="loading">
      <el-tree
        :data="treeData"
        node-key="id"
        :props="{ children: 'children' }"
        default-expand-all
        empty-text="暂无权限数据"
      >
        <template #default="{ data }">
          <span class="tree-node">
            <span class="node-name">{{ data.name }}</span>
            <el-tag
              :type="typeMap[data.type]?.type ?? 'info'"
              size="small"
            >
              {{ typeMap[data.type]?.text ?? '未知' }}
            </el-tag>
            <span class="node-code">{{ data.code }}</span>
          </span>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<style scoped>
.perm-tree-page {
  max-width: 900px;
  padding: 24px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.tree-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  min-height: 300px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-name {
  font-size: 14px;
  color: #303133;
}

.node-code {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style>