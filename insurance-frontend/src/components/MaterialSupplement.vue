<template>
  <div class="material-supplement">
    <div class="card">
      <h2>材料补充</h2>
      
      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
      
      <div v-if="claimsLoading" class="loading">加载中...</div>
      
      <div v-else-if="pendingClaims.length === 0" class="empty-state">
        暂无需要补充材料的理赔申请
      </div>
      
      <div v-else>
        <div v-for="claim in pendingClaims" :key="claim.id" class="card" style="border: 1px solid #e2e8f0; margin-bottom: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
            <h4 style="margin: 0;">
              理赔单号: <strong>{{ claim.id }}</strong> (保单号: {{ claim.policyId }})
            </h4>
            <span class="status-badge status-pending">待补充材料</span>
          </div>
          
          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 15px;">
            <div>
              <strong>事故时间:</strong> {{ claim.accidentTime }}
            </div>
            <div>
              <strong>事故类型:</strong> {{ claim.accidentType }}
            </div>
            <div>
              <strong>申请金额:</strong> ¥{{ claim.requestedAmount?.toLocaleString() }}
            </div>
            <div>
              <strong>计算金额:</strong> ¥{{ claim.calculatedAmount?.toLocaleString() }}
            </div>
          </div>
          
          <div style="margin-bottom: 15px;">
            <strong>已提交材料:</strong>
            <div v-if="claim.materials && claim.materials.length > 0" style="margin-top: 8px;">
              <span v-for="mat in claim.materials" :key="mat" class="tag" style="background: #d4edda; color: #155724;">
                {{ mat }}
              </span>
            </div>
            <div v-else style="margin-top: 8px; color: #999;">
              暂无已提交材料
            </div>
          </div>
          
          <div style="margin-bottom: 15px;">
            <strong style="color: #dc3545;">必需材料 (请确保全部提交):</strong>
            <div style="margin-top: 8px;">
              <span v-for="mat in requiredMaterials" :key="mat" 
                    :class="['tag', claim.materials?.includes(mat) ? 'status-active' : 'status-pending']"
                    style="background: claim.materials?.includes(mat) ? '#d4edda' : '#fff3cd'; 
                           color: claim.materials?.includes(mat) ? '#155724' : '#856404';">
                {{ mat }} {{ claim.materials?.includes(mat) ? '✓' : '(缺失)' }}
              </span>
            </div>
          </div>
          
          <div class="form-group" style="margin-bottom: 10px;">
            <label>选择要补充的材料 (可多选):</label>
            <div class="checkbox-group">
              <div v-for="material in availableMaterials(claim)" :key="material" class="checkbox-item">
                <input type="checkbox" :value="material" 
                       :checked="isMaterialSelected(claim.id, material)"
                       @change="toggleMaterial(claim.id, material)"
                       :id="'mat-' + claim.id + '-' + material" />
                <label :for="'mat-' + claim.id + '-' + material">{{ material }}</label>
              </div>
            </div>
          </div>
          
          <button class="btn btn-primary btn-sm" 
                  :disabled="!hasSelectedMaterials(claim.id)"
                  @click="supplementMaterials(claim)">
            补充材料
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, reactive, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'MaterialSupplement',
  setup() {
    const claims = ref([])
    const claimsLoading = ref(false)
    const requiredMaterials = ref([])
    const message = ref('')
    const messageType = ref('')
    const selectedMaterials = reactive({})
    
    const pendingClaims = computed(() => {
      return claims.value.filter(c => c.status === 'PENDING_MATERIALS')
    })
    
    const loadClaims = async () => {
      try {
        claimsLoading.value = true
        const response = await axios.get('http://localhost:8002/api/claims')
        claims.value = response.data
      } catch (error) {
        console.error('加载理赔申请失败:', error)
      } finally {
        claimsLoading.value = false
      }
    }
    
    const loadRequiredMaterials = async () => {
      try {
        const response = await axios.get('http://localhost:8002/api/claims/required-materials')
        requiredMaterials.value = response.data
      } catch (error) {
        console.error('加载必需材料失败:', error)
      }
    }
    
    const availableMaterials = (claim) => {
      if (!claim.materials) return requiredMaterials.value
      return requiredMaterials.value.filter(m => !claim.materials.includes(m))
    }
    
    const isMaterialSelected = (claimId, material) => {
      return selectedMaterials[claimId]?.includes(material) || false
    }
    
    const toggleMaterial = (claimId, material) => {
      if (!selectedMaterials[claimId]) {
        selectedMaterials[claimId] = []
      }
      
      const index = selectedMaterials[claimId].indexOf(material)
      if (index > -1) {
        selectedMaterials[claimId].splice(index, 1)
      } else {
        selectedMaterials[claimId].push(material)
      }
    }
    
    const hasSelectedMaterials = (claimId) => {
      return selectedMaterials[claimId]?.length > 0
    }
    
    const supplementMaterials = async (claim) => {
      try {
        const materialsToAdd = selectedMaterials[claim.id] || []
        if (materialsToAdd.length === 0) return
        
        const response = await axios.post(
          `http://localhost:8002/api/claims/${claim.id}/materials`,
          materialsToAdd
        )
        
        message.value = '材料补充成功！新状态: ' + response.data.status
        messageType.value = 'success'
        
        selectedMaterials[claim.id] = []
        
        await loadClaims()
        
      } catch (error) {
        message.value = '补充材料失败: ' + (error.response?.data?.error || error.message)
        messageType.value = 'error'
      }
    }
    
    onMounted(() => {
      loadClaims()
      loadRequiredMaterials()
    })
    
    return {
      claims,
      claimsLoading,
      pendingClaims,
      requiredMaterials,
      message,
      messageType,
      selectedMaterials,
      availableMaterials,
      isMaterialSelected,
      toggleMaterial,
      hasSelectedMaterials,
      supplementMaterials
    }
  }
}
</script>
