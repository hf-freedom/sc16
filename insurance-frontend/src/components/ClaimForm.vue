<template>
  <div class="claim-form">
    <div class="card">
      <h2>理赔申请</h2>
      
      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
      
      <form @submit.prevent="submitClaim">
        <div class="form-group">
          <label>选择保单</label>
          <select v-model="claim.policyId" required>
            <option value="">请选择保单</option>
            <option v-for="policy in policies" :key="policy.id" :value="policy.id">
              {{ policy.id }} - {{ policy.insuranceType }} (保额: ¥{{ policy.totalAmount?.toLocaleString() }})
            </option>
          </select>
        </div>
        
        <div v-if="selectedPolicy" class="card" style="background: #f8f9fa; margin-bottom: 20px;">
          <h4>保单信息</h4>
          <p><strong>险种:</strong> {{ selectedPolicy.insuranceType }}</p>
          <p><strong>保额:</strong> ¥{{ selectedPolicy.totalAmount?.toLocaleString() }}</p>
          <p><strong>剩余额度:</strong> ¥{{ selectedPolicy.remainingAmount?.toLocaleString() }}</p>
          <p><strong>有效期:</strong> {{ selectedPolicy.startDate }} ~ {{ selectedPolicy.endDate }}</p>
          <p><strong>免赔额:</strong> ¥{{ selectedPolicy.deductible?.toLocaleString() }}</p>
          <p><strong>赔付比例:</strong> {{ (selectedPolicy.payoutRatio * 100).toFixed(0) }}%</p>
          <p><strong>保障类型:</strong> {{ selectedPolicy.coverageTypes?.join(', ') }}</p>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>事故时间</label>
            <input type="datetime-local" v-model="accidentDateTime" required />
          </div>
          <div class="form-group">
            <label>事故类型</label>
            <select v-model="claim.accidentType" required>
              <option value="">请选择事故类型</option>
              <option v-for="type in availableCoverageTypes" :key="type" :value="type">
                {{ type }}
              </option>
            </select>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>申请金额 (元)</label>
            <input type="number" v-model.number="claim.requestedAmount" placeholder="请输入申请金额" required />
          </div>
        </div>
        
        <div class="form-group">
          <label>事故描述</label>
          <textarea v-model="claim.accidentDescription" rows="3" placeholder="请描述事故情况..."></textarea>
        </div>
        
        <div class="form-group">
          <label>已提交材料 (多选)</label>
          <div class="checkbox-group">
            <div v-for="material in requiredMaterials" :key="material" class="checkbox-item">
              <input type="checkbox" :value="material" v-model="claim.materials" :id="'mat-' + material" />
              <label :for="'mat-' + material">{{ material }}</label>
            </div>
          </div>
        </div>
        
        <div v-if="claim.requestedAmount && selectedPolicy" class="card" style="background: #e7f3ff; margin-bottom: 20px;">
          <h4>预计赔付计算</h4>
          <p><strong>申请金额:</strong> ¥{{ claim.requestedAmount?.toLocaleString() }}</p>
          <p><strong>减去免赔额 (¥{{ selectedPolicy.deductible?.toLocaleString() }}):</strong> ¥{{ Math.max(0, claim.requestedAmount - selectedPolicy.deductible).toLocaleString() }}</p>
          <p><strong>赔付比例 ({{ (selectedPolicy.payoutRatio * 100).toFixed(0) }}%):</strong> ¥{{ calculatedAmount?.toLocaleString() }}</p>
          <p v-if="needsManualReview" style="color: #dc3545; font-weight: 600;">
            ⚠️ 该理赔金额超过5万元，需要人工复核
          </p>
        </div>
        
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? '提交中...' : '提交理赔申请' }}
        </button>
      </form>
    </div>
    
    <div class="card">
      <h2>理赔申请列表</h2>
      
      <div v-if="claimsLoading" class="loading">加载中...</div>
      
      <div v-else-if="claims.length === 0" class="empty-state">
        暂无理赔申请
      </div>
      
      <table v-else class="table">
        <thead>
          <tr>
            <th>理赔单号</th>
            <th>保单号</th>
            <th>事故时间</th>
            <th>事故类型</th>
            <th>申请金额</th>
            <th>计算金额</th>
            <th>材料状态</th>
            <th>人工复核</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="claim in claims" :key="claim.id">
            <td><strong>{{ claim.id }}</strong></td>
            <td>{{ claim.policyId }}</td>
            <td>{{ claim.accidentTime }}</td>
            <td>{{ claim.accidentType }}</td>
            <td>¥{{ claim.requestedAmount?.toLocaleString() }}</td>
            <td>¥{{ claim.calculatedAmount?.toLocaleString() }}</td>
            <td>
              <span :class="['status-badge', claim.status === 'PENDING_MATERIALS' ? 'status-pending' : 'status-active']">
                {{ claim.status === 'PENDING_MATERIALS' ? '待补充' : '已齐全' }}
              </span>
            </td>
            <td>
              <span v-if="claim.needsManualReview">
                <span :class="['status-badge', claim.isManualReviewed ? 'status-active' : 'status-pending']">
                  {{ claim.isManualReviewed ? '已复核' : '待复核' }}
                </span>
              </span>
              <span v-else>-</span>
            </td>
            <td>
              <span :class="['status-badge', getStatusClass(claim.status)]">
                {{ getStatusText(claim.status) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'

export default {
  name: 'ClaimForm',
  setup() {
    const policies = ref([])
    const claims = ref([])
    const claimsLoading = ref(false)
    const requiredMaterials = ref([])
    const submitting = ref(false)
    const message = ref('')
    const messageType = ref('')
    const accidentDateTime = ref('')
    
    const claim = ref({
      policyId: '',
      accidentType: '',
      requestedAmount: null,
      accidentDescription: '',
      materials: []
    })
    
    const selectedPolicy = computed(() => {
      return policies.value.find(p => p.id === claim.value.policyId)
    })
    
    const availableCoverageTypes = computed(() => {
      return selectedPolicy.value?.coverageTypes || []
    })
    
    const calculatedAmount = computed(() => {
      if (!claim.value.requestedAmount || !selectedPolicy.value) return null
      const afterDeductible = Math.max(0, claim.value.requestedAmount - selectedPolicy.value.deductible)
      return afterDeductible * selectedPolicy.value.payoutRatio
    })
    
    const needsManualReview = computed(() => {
      return calculatedAmount.value && calculatedAmount.value >= 50000
    })
    
    const loadPolicies = async () => {
      try {
        const response = await axios.get('http://localhost:8002/api/policies')
        policies.value = response.data.filter(p => p.status === 'ACTIVE')
      } catch (error) {
        console.error('加载保单失败:', error)
      }
    }
    
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
    
    const submitClaim = async () => {
      try {
        submitting.value = true
        message.value = ''
        
        const claimData = {
          policyId: claim.value.policyId,
          accidentTime: new Date(accidentDateTime.value).toISOString(),
          accidentType: claim.value.accidentType,
          requestedAmount: claim.value.requestedAmount,
          accidentDescription: claim.value.accidentDescription,
          materials: claim.value.materials
        }
        
        const response = await axios.post('http://localhost:8002/api/claims', claimData)
        
        message.value = '理赔申请提交成功！状态: ' + response.data.status
        messageType.value = 'success'
        
        claim.value = {
          policyId: '',
          accidentType: '',
          requestedAmount: null,
          accidentDescription: '',
          materials: []
        }
        accidentDateTime.value = ''
        
        await loadClaims()
        await loadPolicies()
        
      } catch (error) {
        message.value = '提交失败: ' + (error.response?.data?.error || error.message)
        messageType.value = 'error'
      } finally {
        submitting.value = false
      }
    }
    
    const getStatusClass = (status) => {
      const map = {
        'PENDING_MATERIALS': 'status-pending',
        'PENDING_REVIEW': 'status-pending',
        'APPROVED': 'status-approved',
        'REJECTED': 'status-rejected',
        'PAID': 'status-paid'
      }
      return map[status] || 'status-pending'
    }
    
    const getStatusText = (status) => {
      const map = {
        'PENDING_MATERIALS': '待补充材料',
        'PENDING_REVIEW': '待审核',
        'APPROVED': '已审核通过',
        'REJECTED': '已拒赔',
        'PAID': '已赔付'
      }
      return map[status] || status
    }
    
    watch(selectedPolicy, (newVal) => {
      if (newVal && !claim.value.accidentType) {
        claim.value.accidentType = ''
      }
    })
    
    onMounted(() => {
      loadPolicies()
      loadClaims()
      loadRequiredMaterials()
    })
    
    return {
      policies,
      claims,
      claimsLoading,
      requiredMaterials,
      submitting,
      message,
      messageType,
      claim,
      accidentDateTime,
      selectedPolicy,
      availableCoverageTypes,
      calculatedAmount,
      needsManualReview,
      submitClaim,
      getStatusClass,
      getStatusText
    }
  }
}
</script>
