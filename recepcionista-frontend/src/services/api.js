import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 30000, 
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    console.log('Requisição:', config.method.toUpperCase(), config.url)
    console.log('Payload:', config.data)
    
    config.headers['Content-Type'] = 'application/json'
    
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

api.interceptors.response.use(
  (response) => {
    console.log('Resposta recebida:', response.config.url, '- Status:', response.status)
    return response
  },
  (error) => {
    if (error.code === 'ECONNABORTED') {
      console.error('Timeout: A requisição demorou muito tempo')
      error.message = 'A requisição demorou muito tempo. Verifique se o servidor está respondendo.'
    } else if (error.response) {
      console.error('Erro na resposta:', error.response.data)
      console.error('Status:', error.response.status)
    } else if (error.request) {
      console.error('Sem resposta do servidor:', error.request)
      error.message = 'Não foi possível conectar ao servidor. Verifique se o backend está rodando.'
    } else {
      console.error('Erro:', error.message)
    }
    return Promise.reject(error)
  }
)

export default api
