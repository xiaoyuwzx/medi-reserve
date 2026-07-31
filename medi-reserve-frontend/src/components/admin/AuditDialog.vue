<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  visible: boolean
  loading: boolean
}>()

const emit = defineEmits<{
  confirm: [reason: string]
  cancel: []
}>()

const reason = ref('')

function handleConfirm() {
  if (!reason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  emit('confirm', reason.value.trim())
}

function handleCancel() {
  reason.value = ''
  emit('cancel')
}

watch(() => props.visible, (val) => {
  if (!val) {
    reason.value = ''
  }
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="审核驳回"
    width="480px"
    :close-on-click-modal="false"
    @close="handleCancel"
  >
    <div class="dialog-body">
      <p class="dialog-tip">请填写驳回原因：</p>
      <el-input
        v-model="reason"
        type="textarea"
        :rows="4"
        maxlength="200"
        show-word-limit
        placeholder="请填写驳回原因（必填，最多200字）"
      />
    </div>
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="danger" :loading="loading" @click="handleConfirm">
        确认驳回
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-body {
  padding: 8px 0;
}

.dialog-tip {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
}
</style>