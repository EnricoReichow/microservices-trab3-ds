const state = {
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
