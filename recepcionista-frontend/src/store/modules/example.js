// Exemplo de módulo Vuex
// Você pode criar módulos separados para diferentes partes da aplicação

const state = {
  // Estado específico deste módulo
  items: []
}

const mutations = {
  SET_ITEMS(state, items) {
    state.items = items
  }
}

const actions = {
  async fetchItems({ commit }) {
    try {
      // Exemplo de ação assíncrona
      // const response = await api.get('/items')
      // commit('SET_ITEMS', response.data)
    } catch (error) {
      console.error('Erro ao buscar items:', error)
    }
  }
}

const getters = {
  getItems: (state) => state.items
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
