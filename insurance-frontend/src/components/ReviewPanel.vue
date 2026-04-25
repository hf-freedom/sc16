<template>
  <div class="review-panel">
    <div class="card">
      <h2>理赔审核</h2>
      
      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
      
      <div v-if="claimsLoading" class="loading">加载中...</div>
      
      <div v-else-if="reviewableClaims.length === 0" class="empty-state">
        暂无需要审核的理赔申请
      </div>
      
      <div v-else>
        <div v-for="claim in reviewableClaims" :key="claim.id" class="card" style="border: 1px solid #e2e8f0; margin-bottom: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; gap: 10px;">
            <h4 style="margin: 0;">
              理赔单号: <strong>{{ claim.id }}</strong>
            </h4>
            <div style="display: flex; gap: 10px;">
              <span v-if="claim.needsManualReview && !claim.isManualReviewed" class="status-badge status-pending">
                需人工复核
              </span>
              <span class="status-badge status-pending">
                {{ getStatusText(claim.status) }}
              </span>
            </div>
          </div>
          
          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 15px;">
            <div>
              <strong>保单号:</strong> {{ claim.policyId }}
            </div>
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
              <strong>免赔额:</strong> ¥{{ claim.deductibleApplied?.toLocaleString() }}
            </div>
            <div>
              <strong>赔付比例:</strong> {{ (claim.ratioApplied * 100).toFixed(0) }}%
            </div>
            <div style="grid-column: span 2; background: #e7f3ff; padding: 10px; border-radius: 6px;">
              <strong style="color: #004085;">计算赔付金额:</strong> 
              <span style="font-size: 18px; font-weight: 600; color: #004085;">
                ¥{{ claim.calculatedAmount?.toLocaleString() }}
              </span>
              <span v-if="claim.calculatedAmount >= 50000" style="margin-left: 10px; color: #dc3545;">
                (高金额理赔)
              </span>
            </div>
          </div>
          
          <div v-if="claim.accidentDescription" style="margin-bottom: 15px;">
            <strong>事故描述:</strong> {{ claim.accidentDescription }}
          </div>
          
          <div style="margin-bottom: 15px;">
            <strong>已提交材料:</strong>
            <div style="margin-top: 8px;">
              <span v-for="mat in claim.materials" :key="mat" class="tag">
                {{ mat }}
              </span>
            </div>
          </div>
          
          <div v-if="claim.needsManualReview && !claim.isManualReviewed" style="margin-bottom: 15px; padding: 15px; background: #fff3cd; border-radius: 6px;">
            <strong style="color: #856404;">⚠️ 需要人工复核</strong>
            <p style="margin-top: 8px; color: #856404; font-size: 14px;">
              该理赔金额超过5万元，请先进行人工复核。复核通过后才能进行正式审核。
            </p>
            <div class="form-group" style="margin-top: 10px; margin-bottom: 10px;">
              <label>复核意见:</label>
              <textarea v-model="reviewComments[claim.id]" rows="2" placeholder="请输入复核意见..."></textarea>
            </div>
            <div class="action-buttons">
              <button class="btn btn-success btn-sm" @click="manualReview(claim, true)">
                复核通过
              </button>
              <button class="btn btn-danger btn-sm" @click="manualReview(claim, false)">
                复核不通过
              </button>
            </div>
          </div>
          
          <div v-if="(claim.status === 'PENDING_REVIEW' && (!claim.needsManualReview || claim.isManualReviewed))" style="margin-bottom: 15px;">
            <div class="form-group" style="margin-bottom: 10px;">
              <label>审核意见 (拒赔时必填):</label>
              <textarea v-model="reviewComments[claim.id]" rows="2" placeholder="请输入审核意见..."></textarea>
            </div>
            <div class="action-buttons">
              <button class="btn btn-success btn-sm" @click="reviewClaim(claim, true)">
                审核通过
              </button>
              <button class="btn btn-danger btn-sm" @click="reviewClaim(claim, false)">
                审核拒赔
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="card">
      <h2>已审核理赔列表</h2>
      
      <div v-if="reviewedClaims.length === 0" class="empty-state">
        暂无已审核的理赔申请
      </div>
      
      <table v-else class="table">
        <thead>
          <tr>
            <th>理赔单号</th>
            <th>保单号</th>
            <th>事故类型</th>
            <th>申请金额</th>
            <th>计算金额</th>
            <th>实际赔付</th>
            <th>人工复核</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="claim in reviewedClaims" :key="claim.id">
            <td><strong>{{ claim.id }}</strong></td>
            <td>{{ claim.policyId }}</td>
            <td>{{ claim.accidentType }}</td>
            <td>¥{{ claim.requestedAmount?.toLocaleString() }}</td>
            <td>¥{{ claim.calculatedAmount?.toLocaleString() }}</td>
            <td>¥{{ claim.actualPayoutAmount?.toLocaleString() || '-' }}</td>
            <td>
              <span v-if="claim.needsManualReview">
                <span :class="['status-badge', claim.isManualReviewed ? 'status-active' : 'status-rejected']">
                  {{ claim.isManualReviewed ? '已复核' : '未复核' }}
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
import { ref, computed, reactive, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'ReviewPanel',
  setup() {
    const claims = ref([])
    const claimsLoading = ref(false)
    const message = ref('')
    const messageType = ref('')
    const reviewComments = reactive({})
    
    const reviewableClaims = computed(() => {
      return claims.value.filter(c => 
        c.status === 'PENDING_REVIEW' || 
        (c.status === 'PENDING_MATERIALS' && c.needsManualReview && !c.isManualReviewed)
      )
    })
    
    const reviewedClaims = computed(() => {
      return claims.value.filter(c => 
        c.status === 'APPROVED' || c.status === 'REJECTED' || c.status === 'PAID'
      )
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
    
    const manualReview = async (claim, approved) => {
      try {
        const response = await axios.post(
          `http://localhost:8002/api/claims/${claim.id}/review`,
          {
            approved,
            comment: reviewComments[claim.id] || (approved ? '人工复核通过' : '人工复核不通过')
          }
        )
        
        message.value = approved ? '人工复核通过！' : '人工复核不通过，已拒赔。'
        messageType.value = 'success'
        
        reviewComments[claim.id] = ''
        
        await loadClaims()
        
      } catch (error) {
        message.value = '复核失败: ' + (error.response?.data?.error || error.message)
        messageType.value = 'error'
      }
    }
    
    const reviewClaim = async (claim, approved) => {
      try {
        const response = await axios.post(
          `http://localhost:8002/api/claims/${claim.id}/review`,
          {
            approved,
            comment: reviewComments[claim.id] || (approved ? '审核通过' : '审核拒赔')
          }
        )
        
        message.value = approved ? 
          `审核通过！实际赔付金额: ¥${response.data.actualPayoutAmount?.toLocaleString()}` : 
          '审核拒赔成功。'
        messageType.value = 'success'
        
        reviewComments[claim.id] = ''
        
        await loadClaims()
        
      } catch (error) {
        message.value = '审核失败: ' + (error.response?.data?.error || error.message)
        messageType.value = 'error'
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
    
    onMounted(() => {
      loadClaims()
    })
    
    return {
      claims,
      claimsLoading,
      reviewableClaims,
      reviewedClaims,
      message,
      messageType,
      reviewComments,
      manualReview,
      reviewClaim,
      getStatusClass,
      getStatusText
    }
  }
}
</script>
