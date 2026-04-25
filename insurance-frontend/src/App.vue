<template>
  <div class="app">
    <header class="header">
      <h1>保险理赔审核和赔付系统</h1>
      <div class="nav">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          :class="['nav-btn', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>
    </header>
    
    <main class="main">
      <PolicyList v-if="activeTab === 'policies'" />
      <ClaimForm v-if="activeTab === 'claim'" />
      <MaterialSupplement v-if="activeTab === 'materials'" />
      <ReviewPanel v-if="activeTab === 'review'" />
      <PayoutPanel v-if="activeTab === 'payout'" />
    </main>
  </div>
</template>

<script>
import { ref } from 'vue'
import PolicyList from './components/PolicyList.vue'
import ClaimForm from './components/ClaimForm.vue'
import MaterialSupplement from './components/MaterialSupplement.vue'
import ReviewPanel from './components/ReviewPanel.vue'
import PayoutPanel from './components/PayoutPanel.vue'

export default {
  name: 'App',
  components: {
    PolicyList,
    ClaimForm,
    MaterialSupplement,
    ReviewPanel,
    PayoutPanel
  },
  setup() {
    const activeTab = ref('policies')
    const tabs = [
      { key: 'policies', label: '保单列表' },
      { key: 'claim', label: '理赔申请' },
      { key: 'materials', label: '材料补充' },
      { key: 'review', label: '审核' },
      { key: 'payout', label: '赔付' }
    ]
    
    return {
      activeTab,
      tabs
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.app {
  min-height: 100vh;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 40px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header h1 {
  font-size: 24px;
  margin-bottom: 15px;
}

.nav {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.nav-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  background: rgba(255,255,255,0.2);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.nav-btn:hover {
  background: rgba(255,255,255,0.3);
}

.nav-btn.active {
  background: white;
  color: #667eea;
  font-weight: 600;
}

.main {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  margin-bottom: 20px;
}

.card h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.table th,
.table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.table tr:hover {
  background: #f8f9fa;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active { background: #d4edda; color: #155724; }
.status-pending { background: #fff3cd; color: #856404; }
.status-approved { background: #cce5ff; color: #004085; }
.status-rejected { background: #f8d7da; color: #721c24; }
.status-paid { background: #e2e3e5; color: #383d41; }

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover {
  background: #218838;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover {
  background: #c82333;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.message {
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  font-size: 14px;
}

.message-success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message-error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-item input[type="checkbox"] {
  width: auto;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e9ecef;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 5px;
  margin-bottom: 5px;
}

.highlight {
  background: #fff3cd;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}
</style>
