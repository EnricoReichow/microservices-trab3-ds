<template>
  <div class="page-container">
    <div class="header">
      <h1 class="page-title">Hospital Pronto Atendimento</h1>
      <button class="btn btn-secondary back-button" @click="goBack">
        <i class="bi bi-arrow-left"></i> Voltar
      </button>
    </div>

    <div class="content">
      <h2 class="section-title">Lista de Pacientes</h2>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Carregando...</span>
        </div>
        <p class="mt-3">Carregando pacientes...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="alert alert-danger" role="alert">
        <i class="bi bi-exclamation-triangle-fill"></i>
        {{ error }}
      </div>

      <!-- Tabela -->
      <div v-else class="table-container">
        <div class="table-wrapper">
          <table class="table table-hover">
            <thead class="table-header">
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Sexo Biológico</th>
                <th>Data Nascimento</th>
                <th>Tipo Sanguíneo</th>
                <th>Estado Civil</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="paciente in pacientes" :key="paciente.id">
                <td>{{ paciente.id }}</td>
                <td class="fw-semibold">{{ paciente.nome }}</td>
                <td>{{ paciente.sexoBiologico || '-' }}</td>
                <td>{{ formatDate(paciente.dataNascimento) }}</td>
                <td>{{ paciente.tipoSanguineo || '-' }}</td>
                <td>{{ paciente.estadoCivil || '-' }}</td>
              </tr>
              <tr v-if="pacientes.length === 0">
                <td colspan="6" class="text-center py-4 text-muted">
                  Nenhum paciente encontrado
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

const pacientes = computed(() => store.getters['pacientes/getPacientes'])
const loading = computed(() => store.getters['pacientes/isLoading'])
const error = computed(() => store.getters['pacientes/getError'])

onMounted(() => {
  store.dispatch('pacientes/fetchPacientes')
})

const goBack = () => {
  store.dispatch('changeView', 'home')
}

const formatDate = (date) => {
  if (!date) return '-'
  // Se vier no formato ISO, converte para dd/mm/yyyy
  if (date.includes('-')) {
    const [year, month, day] = date.split('-')
    return `${day}/${month}/${year}`
  }
  return date
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  background: white;
  padding: 1.5rem 2rem;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.page-title {
  color: #2c3e50;
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  font-family: 'Poppins', sans-serif;
}

.back-button {
  padding: 0.5rem 1.5rem;
  font-weight: 600;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
}

.content {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.section-title {
  color: #2c3e50;
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  font-family: 'Poppins', sans-serif;
  text-align: center;
}

.table-container {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 1rem;
}

.table-wrapper {
  max-height: 600px;
  overflow-y: auto;
  border-radius: 8px;
}

.table {
  margin-bottom: 0;
  background: white;
}

.table-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #0d6efd;
  color: white;
}

.table-header th {
  font-weight: 600;
  border: none;
  padding: 1rem;
  font-family: 'Poppins', sans-serif;
}

.table tbody tr {
  transition: background-color 0.2s ease;
}

.table tbody tr:hover {
  background-color: #f0f7ff;
}

.table tbody td {
  padding: 0.875rem 1rem;
  vertical-align: middle;
}

/* Scrollbar customizado */
.table-wrapper::-webkit-scrollbar {
  width: 8px;
}

.table-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 10px;
}

.table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #555;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .table-wrapper {
    max-height: 500px;
  }
}
</style>
