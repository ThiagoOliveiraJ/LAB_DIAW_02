const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export const formatarMoeda = (valor) => moeda.format(valor);

export const formatarData = (iso) => iso.split('-').reverse().join('/');

export const formatarDataHora = (iso) =>
  new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export const formatarCpf = (cpf) => cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');

export const formatarCnpj = (cnpj) =>
  cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');

export const hojeIso = () => {
  const agora = new Date();
  const local = new Date(agora.getTime() - agora.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
};

export const STATUS_PEDIDO = {
  EM_ANALISE: 'Em análise',
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  CANCELADO: 'Cancelado',
  CONTRATADO: 'Contratado',
};

export const TIPO_CONTRATO = {
  CLIENTE: 'Propriedade do cliente',
  EMPRESA: 'Propriedade da empresa',
  BANCO: 'Propriedade do banco',
};

export const TIPO_AGENTE = { EMPRESA: 'Empresa', BANCO: 'Banco' };
