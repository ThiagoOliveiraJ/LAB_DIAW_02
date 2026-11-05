const BASE = import.meta.env.VITE_API_URL ?? '';
const CHAVE_TOKEN = 'aluguel.token';

let aoExpirar = () => {};

export const sessao = {
  token: () => sessionStorage.getItem(CHAVE_TOKEN),
  salvar: (token) => sessionStorage.setItem(CHAVE_TOKEN, token),
  limpar: () => sessionStorage.removeItem(CHAVE_TOKEN),
  aoExpirar: (fn) => {
    aoExpirar = fn;
  },
};

export class ErroApi extends Error {
  constructor(status, corpo) {
    super(corpo?.mensagem ?? corpo?.detail ?? 'Não foi possível concluir a operação.');
    this.status = status;
    this.campos = corpo?.campos ?? {};
  }
}

async function requisitar(metodo, caminho, corpo) {
  const token = sessao.token();
  let resposta;
  try {
    resposta = await fetch(`${BASE}/api${caminho}`, {
      method: metodo,
      headers: {
        ...(corpo !== undefined && { 'Content-Type': 'application/json' }),
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ErroApi(0, { mensagem: 'Servidor indisponível. Tente novamente em instantes.' });
  }

  const texto = await resposta.text();
  let dados = null;
  try {
    dados = texto ? JSON.parse(texto) : null;
  } catch {
    dados = null;
  }

  if (!resposta.ok) {
    if (resposta.status === 401 && token) {
      sessao.limpar();
      aoExpirar();
    }
    throw new ErroApi(resposta.status, dados);
  }
  return dados;
}

export const api = {
  get: (caminho) => requisitar('GET', caminho),
  post: (caminho, corpo) => requisitar('POST', caminho, corpo),
  put: (caminho, corpo) => requisitar('PUT', caminho, corpo),
  delete: (caminho) => requisitar('DELETE', caminho),
};
