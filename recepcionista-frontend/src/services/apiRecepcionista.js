import axios from 'axios'

const apiRecepcionista = axios.create({
  baseURL: import.meta.env.VITE_API_RECEPCIONISTA_URL || 'http://localhost:8080/api/recepcionista',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

apiRecepcionista.interceptors.request.use(
  (config) => {
    console.log('🚀 Requisição Recepcionista:', config.method.toUpperCase(), config.url)
    console.log('📦 Payload:', config.data)
    
    config.headers['Content-Type'] = 'application/json'
    
    if (config.data && typeof config.data !== 'string') {
      config.data = JSON.stringify(config.data)
    }
    
    return config
  },
  (error) => {
    console.error('❌ Erro na configuração da requisição:', error)
    return Promise.reject(error)
  }
)

apiRecepcionista.interceptors.response.use(
  (response) => {
    console.log('✅ Resposta Recepcionista recebida:', response.config.url, '- Status:', response.status)
    return response
  },
  (error) => {
    if (error.code === 'ECONNABORTED') {
      console.error('⏱️ Timeout: A requisição demorou muito tempo')
      error.message = 'A requisição demorou muito tempo. Verifique se o servidor está respondendo.'
    } else if (error.response) {
      console.error('❌ Erro na resposta:', error.response.data)
      console.error('Status:', error.response.status)
    } else if (error.request) {
      console.error('❌ Sem resposta do servidor:', error.request)
      error.message = 'Não foi possível conectar ao servidor. Verifique se o backend está rodando.'
    } else {
      console.error('❌ Erro:', error.message)
    }
    return Promise.reject(error)
  }
)

export default apiRecepcionista
