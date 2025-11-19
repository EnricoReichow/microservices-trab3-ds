<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import HomePage from './components/HomePage.vue'
import PacientesPage from './components/PacientesPage.vue'
import AtendimentosPage from './components/AtendimentosPage.vue'

const store = useStore()
const currentView = computed(() => store.getters.getCurrentView)

const currentComponent = computed(() => {
  const view = currentView.value
  console.log('Current view:', view) // Debug
  switch (view) {
    case 'pacientes':
      return PacientesPage
    case 'atendimentos':
      return AtendimentosPage
    default:
      return HomePage
  }
})
</script>

<template>
  <div id="app">
    <component :is="currentComponent" />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
