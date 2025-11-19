import { createApp } from 'vue'
import App from './App.vue'
import store from './store'

// Importar Bootstrap CSS e JS
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Importar estilos customizados
import './style.css'

const app = createApp(App)

app.use(store)

app.mount('#app')

