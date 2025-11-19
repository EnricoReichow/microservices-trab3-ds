import api from '../../services/api'

const state = {
  pacientes: [],
  loading: false,
  error: null
}

const mutations = {
  SET_PACIENTES(state, pacientes) {
    state.pacientes = pacientes
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  }
}

const actions = {
  async fetchPacientes({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await api.get('/pacientes')
      commit('SET_PACIENTES', response.data)
    } catch (error) {
      commit('SET_ERROR', error.message || 'Erro ao carregar pacientes')
      console.error('Erro ao buscar pacientes:', error)
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

const getters = {
  getPacientes: (state) => state.pacientes,
  isLoading: (state) => state.loading,
  getError: (state) => state.error
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
