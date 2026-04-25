<template>
  <div class="payout-panel">
    <div class="card">
      <h2>赔付管理</h2>
      
      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
      
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="approvedClaims.length === 0" class="empty-state">
        暂无待赔付的理赔申请
      </div>
      
      <div v-else>
        <div v-for="claim in approvedClaims" :key="claim.id" class="card" style="border: 1px solid #e2e8f0; margin-bottom: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; gap: 10px;">
            <h4 style="margin: 0;">
              理赔单号: <strong>{{ claim.id }}</strong>
            </h4>
            <span class="status-badge status-approved">待赔付</span>
          </div>
          
          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 15px;">
            <div>
              <strong>保单号:</strong> {{ claim.policyId }}
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
            <div style="grid-column: span 2; background: #d4edda; padding: 10px; border-radius: 6px;">
              <strong style="color: #155724;">实际赔付金额:</strong>
              <span style="font-size: 18px; font-weight: 600; color: #155724;">
                ¥{{ claim.actualPayoutAmount?.toLocaleString() }}
              </span>
            </div>
          </div>
          
          <div v-if="getPolicyInfo(claim.policyId)" style="margin-bottom: 15px; padding: 15px; background: #f8f9fa; border-radius: 6px;">
            <h5 style="margin-bottom: 10px;">保单信息</h5>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px;">
              <div>
                <strong>险种:</strong> {{ getPolicyInfo(claim.policyId)?.insuranceType }}
              </div>
              <div>
                <strong>总保额:</strong> ¥{{ getPolicyInfo(claim.policyId)?.totalAmount?.toLocaleString() }}
              </div>
              <div>
                <strong>剩余额度:</strong> 
                <span :class="{ highlight: getPolicyInfo(claim.policyId)?.remainingAmount < claim.actualPayoutAmount }">
                  ¥{{ getPolicyInfo(claim.policyId)?.remainingAmount?.toLocaleString() }}
                </span>
              </div>
            </div>
            <div v-if="getPolicyInfo(claim.policyId)?.remainingAmount < claim.actualPayoutAmount" 
                 style="margin-top: 10px; color: #dc3545; font-weight: 500;">
              ⚠️ 保单剩余额度不足，将只赔付剩余额度: ¥{{ getPolicyInfo(claim.policyId)?.remainingAmount?.toLocaleString() }}
            </div>
          </div>
          
          <div class="action-buttons">
            <button class="btn btn-primary btn-sm" @click="createPayout(claim)">
              生成赔付单
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="card">
      <h2>待完成赔付</h2>
      
      <div v-if="pendingPayouts.length === 0" class="empty-state">
        暂无待完成的赔付
      </div>
      
      <table v-else class="table">
        <thead>
          <tr>
            <th>赔付单号</th>
            <th>理赔单号</th>
            <th>保单号</th>
            <th>赔付金额</th>
            <th>创建时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="payout in pendingPayouts" :key="payout.id">
            <td><strong>{{ payout.id }}</strong></td>
            <td>{{ payout.claimId }}</td>
            <td>{{ payout.policyId }}</td>
            <td>¥{{ payout.payoutAmount?.toLocaleString() }}</td>
            <td>{{ payout.createdAt }}</td>
            <td>
              <span class="status-badge status-pending">待赔付</span>
            </td>
            <td>
              <button class="btn btn-success btn-sm" @click="completePayout(payout)">
                完成赔付
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="card">
      <h2>已完成赔付记录</h2>
      
      <div v-if="completedPayouts.length === 0" class="empty-state">
        暂无已完成的赔付记录
      </div>
      
      <table v-else class="table">
        <thead>
          <tr>
            <th>赔付单号</th>
            <th>理赔单号</th>
            <th>保单号</th>
            <th>赔付金额</th>
            <th>创建时间</th>
            <th>完成时间</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="payout in completedPayouts" :key="payout.id">
            <td><strong>{{ payout.id }}</strong></td>
            <td>{{ payout.claimId }}</td>
            <td>{{ payout.policyId }}</td>
            <td>¥{{ payout.payoutAmount?.toLocaleString() }}</td>
            <td>{{ payout.createdAt }}</td>
            <td>{{ payout.paidAt }}</td>
            <td>
              <span class="status-badge status-paid">已完成</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'PayoutPanel',
  setup() {
    const claims = ref([])
    const payouts = ref([])
    const policies = ref([])
    const loading = ref(false)
    const message = ref('')
    const messageType = ref('')
    
    const approvedClaims = computed(() => {
      return claims.value.filter(c => c.status === 'APPROVED')
    })
    
    const pendingPayouts = computed(() => {
      return payouts.value.filter(p => p.status === 'PENDING')
    })
    
    const completedPayouts = computed(() => {
      return payouts.value.filter(p => p.status === 'COMPLETED')
    })
    
    const getPolicyInfo = (policyId) => {
      return policies.value.find(p => p.id === policyId)
    }
    
    const loadData = async () => {
      try {
        loading.value = true
        
        const [claimsRes, payoutsRes, policiesRes] = await Promise.all([
          axios.get('http://localhost:8002/api/claims'),
          axios.get('http://localhost:8002/api/payouts'),
          axios.get('http://localhost:8002/api/policies')
        ])
        
        claims.value = claimsRes.data
        payouts.value = payoutsRes.data
        policies.value = policiesRes.data
        
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    const createPayout = async (claim) => {
      try {
        const response = await axios.post('http://localhost:8002/api/payouts', {
          claimId: claim.id
        })
        
        message.value = `赔付单已生成！赔付单号: ${response.data.id}，金额: ¥${response.data.payoutAmount?.toLocaleString()}`
        messageType.value = 'success'
        
        await loadData()
        
      } catch (error) {
        message.value = '生成赔付单失败: ' + (error.response?.data?.error || error.message)
        messageType.value = 'error'
      }
    }
    
    const completePayout = async (payout) => {
      try {
        const response = await axios.post(`http://localhost:8002/api/payouts/${payout.id}/complete`)
        
        message.value = `赔付已完成！赔付单号: ${response.data.id}，金额: ¥${response.data.payoutAmount?.toLocaleString()}。保单剩余额度已更新。`
        messageType.value = 'success'
        
        await loadData()
        
      } catch (error) {
        message.value = '完成赔付失败: ' + (error.response?.data?.error || error.message)
        messageType.value = 'error'
      }
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      claims,
      payouts,
      policies,
      loading,
      message,
      messageType,
      approvedClaims,
      pendingPayouts,
      completedPayouts,
      getPolicyInfo,
      createPayout,
      completePayout
    }
  }
}
</script>
