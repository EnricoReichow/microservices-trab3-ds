import { createStore } from 'vuex'
import pacientes from './modules/pacientes'
import atendimentos from './modules/atendimentos'

const store = createStore({
  state: {
    // Estado global da aplicação
    currentView: 'home' // 'home', 'pacientes', 'atendimentos'
  },
  mutations: {
    // Mutações para alterar o estado
    SET_VIEW(state, view) {
      state.currentView = view
    }
  },
  actions: {
    // Ações assíncronas
    changeView({ commit }, view) {
      commit('SET_VIEW', view)
    }
  },
  getters: {
    // Getters para acessar o estado
    getCurrentView: (state) => state.currentView
  },
  modules: {
    // Registre seus módulos aqui
    pacientes,
    atendimentos
  }
})

export default store
