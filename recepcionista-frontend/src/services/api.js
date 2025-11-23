import axios from 'axios'

// Configuração base do Axios
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 30000, 
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor de requisição
api.interceptors.request.use(
  (config) => {
    console.log('Requisição:', config.method.toUpperCase(), config.url)
    console.log('Payload:', config.data)
    
    config.headers['Content-Type'] = 'application/json'
    
    // Se os dados não forem string, converte para JSON
    if (config.data && typeof config.data !== 'string') {
      config.data = JSON.stringify(config.data)
    }
    return config
  },
  (error) => {
    console.error('Erro na configuração da requisição:', error)
    return Promise.reject(error)
  }
)

// Interceptor de resposta
api.interceptors.response.use(
  (response) => {
    console.log('Resposta recebida:', response.config.url, '- Status:', response.status)
    return response
  },
  (error) => {
    // Tratamento de erros global
    if (error.code === 'ECONNABORTED') {
      console.error('Timeout: A requisição demorou muito tempo')
      error.message = 'A requisição demorou muito tempo. Verifique se o servidor está respondendo.'
    } else if (error.response) {
      // O servidor respondeu com um status code fora do range 2xx
      console.error('Erro na resposta:', error.response.data)
      console.error('Status:', error.response.status)
    } else if (error.request) {
      // A requisição foi feita mas não houve resposta
      console.error('Sem resposta do servidor:', error.request)
      error.message = 'Não foi possível conectar ao servidor. Verifique se o backend está rodando.'
    } else {
      // Algo aconteceu na configuração da requisição
      console.error('Erro:', error.message)
    }
    return Promise.reject(error)
  }
)

export default api
