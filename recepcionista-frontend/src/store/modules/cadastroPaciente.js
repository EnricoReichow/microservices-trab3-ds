import apiRecepcionista from '../../services/apiRecepcionista'

const state = {
  loading: false,
  error: null,
  successMessage: null
}

const mutations = {
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  SET_ERROR(state, error) {
    state.error = error
  },
  SET_SUCCESS_MESSAGE(state, message) {
    state.successMessage = message
  },
  CLEAR_MESSAGES(state) {
    state.error = null
    state.successMessage = null
  }
}

const actions = {
  async cadastrarPaciente({ commit }, pacienteData) {
    commit('SET_LOADING', true)
    commit('CLEAR_MESSAGES')
    try {
      const response = await apiRecepcionista.post('', pacienteData)
      commit('SET_SUCCESS_MESSAGE', response.data || 'Paciente cadastrado com sucesso!')
      return { success: true }
    } catch (error) {
      const errorMessage = error.response?.data || error.message || 'Erro ao cadastrar paciente'
      commit('SET_ERROR', errorMessage)
      console.error('Erro ao cadastrar paciente:', error)
      return { success: false, error: errorMessage }
    } finally {
      commit('SET_LOADING', false)
    }
  },
  clearMessages({ commit }) {
    commit('CLEAR_MESSAGES')
  }
}

const getters = {
  isLoading: (state) => state.loading,
  getError: (state) => state.error,
  getSuccessMessage: (state) => state.successMessage
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
