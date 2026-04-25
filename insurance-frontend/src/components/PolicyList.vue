<template>
  <div class="policy-list">
    <div class="card">
      <h2>保单列表</h2>
      
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="policies.length === 0" class="empty-state">
        暂无保单数据
      </div>
      
      <table v-else class="table">
        <thead>
          <tr>
            <th>保单号</th>
            <th>险种</th>
            <th>保额</th>
            <th>剩余额度</th>
            <th>有效期</th>
            <th>免赔额</th>
            <th>赔付比例</th>
            <th>保障类型</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="policy in policies" :key="policy.id">
            <td><strong>{{ policy.id }}</strong></td>
            <td>{{ policy.insuranceType }}</td>
            <td>¥{{ policy.totalAmount?.toLocaleString() }}</td>
            <td>
              <span :class="{ highlight: policy.remainingAmount < policy.totalAmount }">
                ¥{{ policy.remainingAmount?.toLocaleString() }}
              </span>
            </td>
            <td>
              {{ formatDate(policy.startDate) }} ~ {{ formatDate(policy.endDate) }}
            </td>
            <td>¥{{ policy.deductible?.toLocaleString() }}</td>
            <td>{{ (policy.payoutRatio * 100).toFixed(0) }}%</td>
            <td>
              <span v-for="type in policy.coverageTypes" :key="type" class="tag">
                {{ type }}
              </span>
            </td>
            <td>
              <span class="status-badge status-active">{{ policy.status }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div class="card">
      <h2>新增保单</h2>
      <form @submit.prevent="createPolicy">
        <div class="form-row">
          <div class="form-group">
            <label>客户ID</label>
            <input v-model="newPolicy.customerId" placeholder="例如: C1" required />
          </div>
          <div class="form-group">
            <label>险种</label>
            <input v-model="newPolicy.insuranceType" placeholder="例如: 意外险" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>保额 (元)</label>
            <input type="number" v-model.number="newPolicy.totalAmount" placeholder="100000" required />
          </div>
          <div class="form-group">
            <label>免赔额 (元)</label>
            <input type="number" v-model.number="newPolicy.deductible" placeholder="500" required />
          </div>
          <div class="form-group">
            <label>赔付比例</label>
            <input type="number" step="0.1" v-model.number="newPolicy.payoutRatio" placeholder="0.8" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>生效日期</label>
            <input type="date" v-model="newPolicy.startDate" required />
          </div>
          <div class="form-group">
            <label>到期日期</label>
            <input type="date" v-model="newPolicy.endDate" required />
          </div>
        </div>
        <div class="form-group">
          <label>保障类型 (用逗号分隔)</label>
          <input v-model="coverageTypesInput" placeholder="例如: 意外身故,意外伤残,意外医疗" required />
        </div>
        <button type="submit" class="btn btn-primary">创建保单</button>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'PolicyList',
  setup() {
    const loading = ref(true)
    const policies = ref([])
    const coverageTypesInput = ref('')
    
    const newPolicy = ref({
      customerId: 'C1',
      insuranceType: '',
      totalAmount: 100000,
      deductible: 500,
      payoutRatio: 0.8,
      startDate: '',
      endDate: '',
      status: 'ACTIVE'
    })
    
    const loadPolicies = async () => {
      try {
        loading.value = true
        const response = await axios.get('http://localhost:8002/api/policies')
        policies.value = response.data
      } catch (error) {
        console.error('加载保单失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    const createPolicy = async () => {
      try {
        const policy = {
          ...newPolicy.value,
          coverageTypes: coverageTypesInput.value.split(',').map(s => s.trim()),
          remainingAmount: newPolicy.value.totalAmount
        }
        
        await axios.post('http://localhost:8002/api/policies', policy)
        await loadPolicies()
        
        newPolicy.value = {
          customerId: 'C1',
          insuranceType: '',
          totalAmount: 100000,
          deductible: 500,
          payoutRatio: 0.8,
          startDate: '',
          endDate: '',
          status: 'ACTIVE'
        }
        coverageTypesInput.value = ''
        
        alert('保单创建成功！')
      } catch (error) {
        console.error('创建保单失败:', error)
        alert('创建保单失败: ' + (error.response?.data?.error || error.message))
      }
    }
    
    const formatDate = (dateStr) => {
      if (!dateStr) return '-'
      return dateStr
    }
    
    onMounted(() => {
      loadPolicies()
    })
    
    return {
      loading,
      policies,
      newPolicy,
      coverageTypesInput,
      loadPolicies,
      createPolicy,
      formatDate
    }
  }
}
</script>
