import api from '../../services/api'

const state = {
  atendimentos: [],
  loading: false,
  error: null
}

const mutations = {
  SET_ATENDIMENTOS(state, atendimentos) {
    state.atendimentos = atendimentos
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  }
}

const actions = {
  async fetchAtendimentos({ commit }) {
    commit('SET_LOADING', true)
    commit('SET_ERROR', null)
    try {
      const response = await api.get('/atendimentos')
      commit('SET_ATENDIMENTOS', response.data)
    } catch (error) {
      commit('SET_ERROR', error.message || 'Erro ao carregar atendimentos')
      console.error('Erro ao buscar atendimentos:', error)
    } finally {
      commit('SET_LOADING', false)
    }
  }
}

const getters = {
  getAtendimentos: (state) => state.atendimentos,
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
