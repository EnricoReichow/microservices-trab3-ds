<template>
  <div class="home-container">
    <div class="content-wrapper">
      <h1 class="hospital-title">Hospital Pronto Atendimento</h1>
      
      <div class="buttons-container">
        <button 
          class="btn btn-primary btn-lg nav-button"
          @click="goToPacientes"
        >
          <i class="bi bi-people-fill"></i>
          Ver Pacientes
        </button>
        
        <button 
          class="btn btn-primary btn-lg nav-button"
          @click="goToAtendimentos"
        >
          <i class="bi bi-clipboard2-pulse-fill"></i>
          Ver Atendimentos
        </button>

        <button 
          class="btn btn-success btn-lg nav-button"
          @click="abrirModalCadastro"
        >
          <i class="bi bi-person-plus-fill"></i>
          Cadastrar Novo Paciente
        </button>

        <button 
          class="btn btn-warning btn-lg nav-button"
          @click="cadastrar10Pacientes"
          :disabled="cadastrandoLote"
        >
          <span v-if="cadastrandoLote">
            <span class="spinner-border spinner-border-sm me-2" role="status"></span>
            {{ mensagemCadastro }}
          </span>
          <span v-else>
            <i class="bi bi-people me-2"></i>
            Cadastrar 10 Pacientes
          </span>
        </button>
      </div>

      <!-- Mensagem de sucesso do cadastro em lote -->
      <div v-if="mostrarMensagemSucesso" class="alert alert-success mt-4 fade-in" role="alert">
        <i class="bi bi-check-circle-fill me-2"></i>
        {{ mensagemSucesso }}
      </div>
    </div>

    <!-- Modal de Cadastro -->
    <CadastroPacienteModal />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useStore } from 'vuex'
import { Modal } from 'bootstrap'
import CadastroPacienteModal from './CadastroPacienteModal.vue'
import { gerarPacienteAleatorio } from '../utils/geradorPacientes'

const store = useStore()
const cadastrandoLote = ref(false)
const mensagemCadastro = ref('')
const mostrarMensagemSucesso = ref(false)
const mensagemSucesso = ref('')

const goToPacientes = () => {
  store.dispatch('changeView', 'pacientes')
}

const goToAtendimentos = () => {
  store.dispatch('changeView', 'atendimentos')
}

const abrirModalCadastro = () => {
  const modalElement = document.getElementById('cadastroPacienteModal')
  const modal = new Modal(modalElement)
  modal.show()
}

const cadastrar10Pacientes = async () => {
  cadastrandoLote.value = true
  mostrarMensagemSucesso.value = false
  let cadastradosComSucesso = 0
  const totalPacientes = 10

  for (let i = 1; i <= totalPacientes; i++) {
    mensagemCadastro.value = `Cadastrando paciente ${i} de ${totalPacientes}...`
    
    const paciente = gerarPacienteAleatorio()
    
    try {
      const result = await store.dispatch('cadastroPaciente/cadastrarPaciente', paciente)
      if (result.success) {
        cadastradosComSucesso++
      }
    } catch (error) {
      console.error(`Erro ao cadastrar paciente ${i}:`, error)
      // Continua mesmo se der erro
    }
    
    // Aguarda 500ms antes de cadastrar o próximo
    if (i < totalPacientes) {
      await new Promise(resolve => setTimeout(resolve, 500))
    }
  }

  cadastrandoLote.value = false
  mensagemCadastro.value = ''
  
  // Mostra mensagem de sucesso
  if (cadastradosComSucesso === totalPacientes) {
    mensagemSucesso.value = 'Todos os pacientes foram cadastrados!'
  } else {
    mensagemSucesso.value = `${cadastradosComSucesso} de ${totalPacientes} pacientes foram cadastrados.`
  }
  
  mostrarMensagemSucesso.value = true
  
  // Esconde a mensagem após 5 segundos
  setTimeout(() => {
    mostrarMensagemSucesso.value = false
  }, 5000)
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem;
}

.content-wrapper {
  text-align: center;
  background: white;
  padding: 4rem 3rem;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  width: 100%;
}

.hospital-title {
  color: #2c3e50;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 3rem;
  font-family: 'Poppins', sans-serif;
}

.buttons-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.nav-button {
  font-size: 1.25rem;
  padding: 1rem 2rem;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.nav-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.btn-success {
  background-color: #28a745;
  border-color: #28a745;
}

.btn-success:hover {
  background-color: #218838;
  border-color: #1e7e34;
}

.btn-warning {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000;
  font-weight: 700;
}

.btn-warning:hover {
  background-color: #e0a800;
  border-color: #d39e00;
  color: #000;
}

.btn-warning:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.fade-in {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 576px) {
  .hospital-title {
    font-size: 1.8rem;
  }
  
  .nav-button {
    font-size: 1rem;
  }
}
</style>
