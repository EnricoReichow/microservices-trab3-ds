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
      </div>
    </div>
  </div>
</template>

<script setup>
import { useStore } from 'vuex'

const store = useStore()

const goToPacientes = () => {
  store.dispatch('changeView', 'pacientes')
}

const goToAtendimentos = () => {
  store.dispatch('changeView', 'atendimentos')
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

@media (max-width: 576px) {
  .hospital-title {
    font-size: 1.8rem;
  }
  
  .nav-button {
    font-size: 1rem;
  }
}
</style>
