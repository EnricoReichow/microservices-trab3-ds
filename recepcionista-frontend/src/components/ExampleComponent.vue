<template>
  <div class="container mt-5">
    <div class="row">
      <div class="col-12">
        <h1 class="text-center mb-4">Exemplo de Integração</h1>
        
        <!-- Card de demonstração do Bootstrap -->
        <div class="card shadow-sm mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Bootstrap + Poppins Font</h5>
          </div>
          <div class="card-body">
            <p class="card-text">
              Este card demonstra o Bootstrap funcionando com a fonte Poppins.
            </p>
            <button class="btn btn-primary me-2" @click="incrementCounter">
              Incrementar: {{ counter }}
            </button>
            <button class="btn btn-secondary" @click="resetCounter">
              Resetar
            </button>
          </div>
        </div>

        <!-- Demonstração de Vuex -->
        <div class="card shadow-sm mb-4">
          <div class="card-header bg-success text-white">
            <h5 class="mb-0">Vuex Store</h5>
          </div>
          <div class="card-body">
            <p>Valor do contador no Vuex: <strong>{{ counter }}</strong></p>
            <p class="text-muted small">
              O contador está sendo gerenciado pelo Vuex store
            </p>
          </div>
        </div>

        <!-- Demonstração de Axios -->
        <div class="card shadow-sm">
          <div class="card-header bg-info text-white">
            <h5 class="mb-0">Axios API</h5>
          </div>
          <div class="card-body">
            <p>Teste de requisição HTTP com Axios</p>
            <button 
              class="btn btn-info" 
              @click="testApiCall"
              :disabled="loading"
            >
              {{ loading ? 'Carregando...' : 'Testar API' }}
            </button>
            <div v-if="apiResponse" class="mt-3 alert alert-success">
              <strong>Resposta:</strong> {{ apiResponse }}
            </div>
            <div v-if="apiError" class="mt-3 alert alert-danger">
              <strong>Erro:</strong> {{ apiError }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import api from '../services/api'

const store = useStore()
const loading = ref(false)
const apiResponse = ref(null)
const apiError = ref(null)

// Computed property do Vuex
const counter = computed(() => store.state.counter)

const incrementCounter = () => {
  store.commit('INCREMENT')
}

const resetCounter = () => {
  store.commit('RESET_COUNTER')
}

const testApiCall = async () => {
  loading.value = true
  apiResponse.value = null
  apiError.value = null
  
  try {
    // Exemplo de chamada API - substitua com seu endpoint real
    const response = await api.get('/test')
    apiResponse.value = JSON.stringify(response.data)
  } catch (error) {
    apiError.value = error.message || 'Erro ao fazer requisição'
    console.error('Erro na API:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card {
  border-radius: 10px;
}

.card-header {
  font-weight: 600;
}

button {
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
}
</style>
