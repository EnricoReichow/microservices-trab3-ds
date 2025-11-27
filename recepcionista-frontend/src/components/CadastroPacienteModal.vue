<template>
  <div 
    class="modal fade" 
    id="cadastroPacienteModal" 
    tabindex="-1" 
    aria-labelledby="cadastroPacienteModalLabel" 
    aria-hidden="true"
    data-bs-backdrop="static"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header bg-primary text-white">
          <h5 class="modal-title" id="cadastroPacienteModalLabel">
            <i class="bi bi-person-plus-fill me-2"></i>
            Cadastrar Novo Paciente
          </h5>
          <button 
            type="button" 
            class="btn-close btn-close-white" 
            @click="fecharModal"
            :disabled="loading"
          ></button>
        </div>
        
        <div class="modal-body">
          <div v-if="successMessage" class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i>
            {{ successMessage }}
            <button type="button" class="btn-close" @click="limparMensagens"></button>
          </div>

          <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            {{ error }}
            <button type="button" class="btn-close" @click="limparMensagens"></button>
          </div>

          <form @submit.prevent="handleSubmit">
            <div class="row">
              <div class="col-md-12 mb-3">
                <label for="nome" class="form-label">Nome Completo <span class="text-danger">*</span></label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="nome" 
                  v-model="form.nome"
                  required
                  :disabled="loading"
                >
              </div>

              <div class="col-md-6 mb-3">
                <label for="sexoBiologico" class="form-label">Sexo Biológico <span class="text-danger">*</span></label>
                <select 
                  class="form-select" 
                  id="sexoBiologico" 
                  v-model="form.sexoBiologico"
                  required
                  :disabled="loading"
                >
                  <option value="">Selecione...</option>
                  <option value="Masculino">Masculino</option>
                  <option value="Feminino">Feminino</option>
                </select>
              </div>

              <div class="col-md-6 mb-3">
                <label for="dataNascimento" class="form-label">Data de Nascimento <span class="text-danger">*</span></label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="dataNascimento" 
                  v-model="form.dataNascimento"
                  placeholder="DD/MM/AAAA"
                  required
                  :disabled="loading"
                  @input="formatarData"
                  maxlength="10"
                >
              </div>

              <div class="col-md-6 mb-3">
                <label for="tipoSanguineo" class="form-label">Tipo Sanguíneo <span class="text-danger">*</span></label>
                <select 
                  class="form-select" 
                  id="tipoSanguineo" 
                  v-model="form.tipoSanguineo"
                  required
                  :disabled="loading"
                >
                  <option value="">Selecione...</option>
                  <option value="A+">A+</option>
                  <option value="A-">A-</option>
                  <option value="B+">B+</option>
                  <option value="B-">B-</option>
                  <option value="AB+">AB+</option>
                  <option value="AB-">AB-</option>
                  <option value="O+">O+</option>
                  <option value="O-">O-</option>
                </select>
              </div>

              <div class="col-md-6 mb-3">
                <label for="estadoCivil" class="form-label">Estado Civil <span class="text-danger">*</span></label>
                <select 
                  class="form-select" 
                  id="estadoCivil" 
                  v-model="form.estadoCivil"
                  required
                  :disabled="loading"
                >
                  <option value="">Selecione...</option>
                  <option value="Solteiro">Solteiro</option>
                  <option value="Casado">Casado</option>
                  <option value="Divorciado">Divorciado</option>
                  <option value="Viúvo">Viúvo</option>
                </select>
              </div>

              <div class="col-md-12 mb-3">
                <label for="sintomas" class="form-label">Sintomas <span class="text-danger">*</span></label>
                <select 
                  class="form-select" 
                  id="sintomas" 
                  v-model="form.sintomas"
                  required
                  :disabled="loading"
                >
                  <option value="">Selecione...</option>
                  <option value="Infarto">Infarto</option>
                  <option value="Dor de cabeça">Dor de cabeça</option>
                  <option value="Perda dos movimentos de um lado (princípio AVC)">Perda dos movimentos de um lado (princípio AVC)</option>
                  <option value="Dor de barriga">Dor de barriga</option>
                  <option value="Pressão alta">Pressão alta</option>
                  <option value="Vômito náusea">Vômito náusea</option>
                  <option value="Dor muscular">Dor muscular</option>
                </select>
              </div>
            </div>
          </form>
        </div>
        
        <div class="modal-footer">
          <button 
            type="button" 
            class="btn btn-secondary" 
            @click="fecharModal"
            :disabled="loading"
          >
            <i class="bi bi-x-circle me-1"></i>
            Cancelar
          </button>
          <button 
            type="button" 
            class="btn btn-primary" 
            @click="handleSubmit"
            :disabled="loading"
          >
            <span v-if="loading">
              <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              Cadastrando...
            </span>
            <span v-else>
              <i class="bi bi-check-circle me-1"></i>
              Cadastrar
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { Modal } from 'bootstrap'

const store = useStore()

const form = ref({
  nome: '',
  sexoBiologico: '',
  dataNascimento: '',
  tipoSanguineo: '',
  estadoCivil: '',
  sintomas: '',
  medicoResponsavel: null
})

const loading = computed(() => store.getters['cadastroPaciente/isLoading'])
const error = computed(() => store.getters['cadastroPaciente/getError'])
const successMessage = computed(() => store.getters['cadastroPaciente/getSuccessMessage'])

const formatarData = (event) => {
  let value = event.target.value.replace(/\D/g, '')
  
  if (value.length >= 2) {
    value = value.substring(0, 2) + '/' + value.substring(2)
  }
  if (value.length >= 5) {
    value = value.substring(0, 5) + '/' + value.substring(5, 9)
  }
  
  form.value.dataNascimento = value
}

const limparFormulario = () => {
  form.value = {
    nome: '',
    sexoBiologico: '',
    dataNascimento: '',
    tipoSanguineo: '',
    estadoCivil: '',
    sintomas: '',
    medicoResponsavel: null
  }
}

const limparMensagens = () => {
  store.dispatch('cadastroPaciente/clearMessages')
}

const fecharModal = () => {
  const modalElement = document.getElementById('cadastroPacienteModal')
  const modalInstance = Modal.getInstance(modalElement)
  if (modalInstance) {
    modalInstance.hide()
  }
  limparFormulario()
  limparMensagens()
}

const handleSubmit = async () => {
  limparMensagens()
  
  const result = await store.dispatch('cadastroPaciente/cadastrarPaciente', form.value)
  
  if (result.success) {
    limparFormulario()
    setTimeout(() => {
      fecharModal()
    }, 2000)
  }
}
</script>

<style scoped>
.form-label {
  font-weight: 600;
  color: #2c3e50;
  font-family: 'Poppins', sans-serif;
}

.form-control, .form-select {
  border-radius: 8px;
  border: 1px solid #ced4da;
  padding: 0.6rem 0.75rem;
  font-family: 'Poppins', sans-serif;
}

.form-control:focus, .form-select:focus {
  border-color: #0d6efd;
  box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
}

.modal-content {
  border-radius: 15px;
  border: none;
}

.modal-header {
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  padding: 1.5rem;
}

.modal-body {
  padding: 2rem;
}

.modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #dee2e6;
}

.text-danger {
  color: #dc3545;
}

.btn {
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  border-radius: 8px;
  padding: 0.5rem 1.5rem;
}
</style>
