# Recepcionista Frontend

Projeto Vue 3 + Vite configurado com Vuex, Bootstrap, Axios e Google Fonts (Poppins).

## 🚀 Tecnologias

- **Vue 3** - Framework JavaScript progressivo
- **Vite** - Build tool e dev server
- **Vuex** - Gerenciamento de estado
- **Bootstrap 5** - Framework CSS
- **Axios** - Cliente HTTP para requisições
- **Google Fonts (Poppins)** - Tipografia

## 📦 Estrutura do Projeto

```
src/
├── assets/          # Recursos estáticos (imagens, etc)
├── components/      # Componentes Vue reutilizáveis
├── services/        # Serviços (API, etc)
│   └── api.js      # Configuração do Axios
├── store/          # Vuex store
│   ├── index.js    # Store principal
│   └── modules/    # Módulos da store
├── App.vue         # Componente raiz
├── main.js         # Ponto de entrada
└── style.css       # Estilos globais
```

## 🛠️ Instalação

```bash
# Instalar dependências
npm install

# Rodar em desenvolvimento
npm run dev

# Build para produção
npm run build

# Preview da build de produção
npm run preview
```

## ⚙️ Configuração

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto (use `.env.example` como referência):

```env
VITE_API_BASE_URL=http://localhost:3000/api
```

### Axios

O Axios está configurado em `src/services/api.js` com:
- Base URL configurável via variável de ambiente
- Interceptors para requisições e respostas
- Tratamento de erros global
- Timeout de 10 segundos

Exemplo de uso:
```javascript
import api from '@/services/api'

// GET request
const response = await api.get('/endpoint')

// POST request
const response = await api.post('/endpoint', { data })
```

### Vuex

A store do Vuex está em `src/store/index.js`. Para criar novos módulos:

1. Crie um arquivo em `src/store/modules/`
2. Importe e registre no `src/store/index.js`

Exemplo de módulo em `src/store/modules/example.js`

### Bootstrap

Bootstrap está importado globalmente no `main.js`. Você pode usar todas as classes do Bootstrap em seus componentes:

```vue
<template>
  <div class="container">
    <button class="btn btn-primary">Botão</button>
  </div>
</template>
```

### Google Fonts - Poppins

A fonte Poppins está configurada no `index.html` e aplicada globalmente no `style.css`. Pesos disponíveis: 300, 400, 500, 600, 700.

## 📝 Scripts Disponíveis

- `npm run dev` - Inicia o servidor de desenvolvimento
- `npm run build` - Cria build de produção
- `npm run preview` - Preview da build de produção

## 🔗 Links Úteis

- [Vue 3 Documentation](https://vuejs.org/)
- [Vite Documentation](https://vitejs.dev/)
- [Vuex Documentation](https://vuex.vuejs.org/)
- [Bootstrap Documentation](https://getbootstrap.com/)
- [Axios Documentation](https://axios-http.com/)
