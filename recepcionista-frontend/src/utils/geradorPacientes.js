// Arrays de nomes - você pode adicionar 100 nomes em cada depois
export const nomesMasculinos = [
  'João', 'Pedro', 'Lucas', 'Gabriel', 'Felipe',
  'Rafael', 'Bruno', 'Carlos', 'Daniel', 'Eduardo',
  'Fernando', 'Gustavo', 'Henrique', 'Igor', 'José',
  'Leonardo', 'Marcelo', 'Nathan', 'Otávio', 'Paulo'
  // Adicione mais 80 nomes aqui
]

export const nomesFemininos = [
  'Maria', 'Ana', 'Julia', 'Beatriz', 'Camila',
  'Carolina', 'Daniela', 'Eduarda', 'Fernanda', 'Gabriela',
  'Helena', 'Isabela', 'Juliana', 'Larissa', 'Letícia',
  'Mariana', 'Natália', 'Olivia', 'Paula', 'Rafaela'
  // Adicione mais 80 nomes aqui
]

export const sobrenomes = [
  'Silva', 'Santos', 'Oliveira', 'Souza', 'Rodrigues',
  'Ferreira', 'Alves', 'Pereira', 'Lima', 'Gomes',
  'Costa', 'Ribeiro', 'Martins', 'Carvalho', 'Rocha',
  'Almeida', 'Nascimento', 'Araújo', 'Fernandes', 'Barbosa'
  // Adicione mais 80 sobrenomes aqui
]

export const sintomas = [
  'Infarto',
  'Dor de cabeça',
  'Perda dos movimentos de um lado (princípio AVC)',
  'Dor de barriga',
  'Pressão alta',
  'Vômito náusea',
  'Dor muscular'
]

export const tiposSanguineos = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-']

export const estadosCivis = ['Casado', 'Viúvo']

// Função para gerar data de nascimento aleatória
export function gerarDataNascimento() {
  const hoje = new Date()
  const anoAtual = hoje.getFullYear()
  const idadeMinima = 0
  const idadeMaxima = 100
  
  const idade = Math.floor(Math.random() * (idadeMaxima - idadeMinima + 1)) + idadeMinima
  const ano = anoAtual - idade
  const mes = Math.floor(Math.random() * 12) + 1
  const dia = Math.floor(Math.random() * 28) + 1 // Usando 28 para evitar problemas com meses
  
  return `${String(dia).padStart(2, '0')}/${String(mes).padStart(2, '0')}/${ano}`
}

// Função para calcular idade a partir de uma data DD/MM/YYYY
export function calcularIdade(dataNascimento) {
  const [dia, mes, ano] = dataNascimento.split('/').map(Number)
  const hoje = new Date()
  const nascimento = new Date(ano, mes - 1, dia)
  
  let idade = hoje.getFullYear() - nascimento.getFullYear()
  const mesAtual = hoje.getMonth()
  const mesNascimento = nascimento.getMonth()
  
  if (mesAtual < mesNascimento || (mesAtual === mesNascimento && hoje.getDate() < nascimento.getDate())) {
    idade--
  }
  
  return idade
}

// Função para gerar um paciente aleatório
export function gerarPacienteAleatorio() {
  // Escolhe aleatoriamente se será masculino ou feminino
  const isMasculino = Math.random() > 0.5
  
  // Seleciona nome baseado no sexo
  const nomesArray = isMasculino ? nomesMasculinos : nomesFemininos
  const primeiroNome = nomesArray[Math.floor(Math.random() * nomesArray.length)]
  const sobrenome = sobrenomes[Math.floor(Math.random() * sobrenomes.length)]
  const nome = `${primeiroNome} ${sobrenome}`
  
  // Gera data de nascimento
  const dataNascimento = gerarDataNascimento()
  const idade = calcularIdade(dataNascimento)
  
  // Define estado civil baseado na idade
  let estadoCivil
  if (idade >= 18) {
    estadoCivil = estadosCivis[Math.floor(Math.random() * estadosCivis.length)]
  } else {
    estadoCivil = 'Solteiro'
  }
  
  return {
    nome,
    sexoBiologico: isMasculino ? 'Masculino' : 'Feminino',
    dataNascimento,
    tipoSanguineo: tiposSanguineos[Math.floor(Math.random() * tiposSanguineos.length)],
    estadoCivil,
    sintomas: sintomas[Math.floor(Math.random() * sintomas.length)],
    medicoResponsavel: null
  }
}
