<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { adminApi } from '@/api/admin'

const treeRef = ref<InstanceType<typeof ElTree>>()
const treeData = ref<TreeNode[]>([])
const roles = ref<RoleData[]>([])
const selectedRoleId = ref<number | null>(null)
const loading = ref(false)
const saving = ref(false)

interface TreeNode {
  id: number
  code: string
  name: string
  type: number
  sortOrder?: number
  children?: TreeNode[]
}

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

  const cleanEmpty = (nodes: TreeNode[]) => {
    for (const node of nodes) {
      if (node.children?.length === 0) delete node.children
      else if (node.children) cleanEmpty(node.children)
    }
  }
  cleanEmpty(roots)

  return roots
}

interface RoleData {
  id: number
  name: string
  code: string
  permissionIds: number[]
}

const typeMap: Record<number, { text: string; type: 'primary' | 'warning' | 'info' }> = {
  1: { text: '菜单', type: 'primary' },
  2: { text: '按钮', type: 'warning' },
  3: { text: '接口', type: 'info' }
}

async function loadData() {
  loading.value = true
  try {
    const [permRes, roleRes] = await Promise.all([
      adminApi.admin.getPermissionTree(),
      adminApi.admin.getAllRolesWithPermissions()
    ])
    const permData = (permRes as unknown as TreeNode[]) ?? []
    treeData.value = permData.length > 0 && permData[0].children !== undefined
      ? permData
      : buildTreeByCode(permData)
    roles.value = (roleRes as unknown as RoleData[]) ?? []
  } catch {
    treeData.value = []
    roles.value = []
  } finally {
    loading.value = false
  }
}

function selectRole(roleId: number) {
  selectedRoleId.value = roleId
  const role = roles.value.find(r => r.id === roleId)
  nextTick(() => {
    if (treeRef.value) {
      treeRef.value.setCheckedKeys([])
      if (role?.permissionIds) {
        treeRef.value.setCheckedKeys(role.permissionIds)
      }
    }
  })
}

async function handleSave() {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择一个角色')
    return
  }

  const checkedKeys = treeRef.value?.getCheckedKeys() as number[] ?? []
  saving.value = true
  try {
    await adminApi.admin.updateRolePermissions(selectedRoleId.value, {
      permissionIds: checkedKeys
    })
    ElMessage.success('权限更新成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="role-perm-page">
    <h2 class="page-title">角色权限</h2>

    <div class="role-perm-layout" v-loading="loading">
      <!-- 左侧角色列表 -->
      <div class="role-list">
        <div class="section-title">角色列表</div>
        <div
          v-for="role in roles"
          :key="role.id"
          class="role-item"
          :class="{ active: selectedRoleId === role.id }"
          @click="selectRole(role.id)"
        >
          <el-tag :type="role.code === 'SUPER_ADMIN' ? 'danger' : 'primary'" size="small">
            {{ role.name }}
          </el-tag>
        </div>
        <el-empty v-if="roles.length === 0" description="暂无角色" />
      </div>

      <!-- 右侧权限树 -->
      <div class="perm-tree">
        <div class="section-title">权限分配</div>
        <el-tree
          v-if="selectedRoleId"
          ref="treeRef"
          :data="treeData"
          node-key="id"
          :props="{ children: 'children' }"
          show-checkbox
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
            </span>
          </template>
        </el-tree>
        <el-empty v-else description="请先选择一个角色" />

        <div v-if="selectedRoleId" class="save-row">
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存权限
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.role-perm-page {
  max-width: 1100px;
  padding: 24px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  color: #303133;
}

.role-perm-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.role-list {
  width: 200px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.role-item {
  padding: 8px 0;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
}

.role-item:last-child {
  border-bottom: none;
}

.role-item.active {
  background: #ecf5ff;
  margin: 0 -16px;
  padding: 8px 16px;
}

.perm-tree {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  min-height: 400px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}

.node-name {
  font-size: 13px;
  color: #303133;
}

.save-row {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>