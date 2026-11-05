import { useState } from 'react';
import { formatarCpf } from '../utils/formatar.js';
import { Aviso, Campo } from './ui.jsx';

const MAX_RENDIMENTOS = 3;

export default function ClienteForm({ cliente, rotuloEnviar, aoEnviar }) {
  const edicao = Boolean(cliente);
  const [dados, setDados] = useState({
    nome: cliente?.nome ?? '',
    login: '',
    senha: '',
    rg: cliente?.rg ?? '',
    cpf: cliente ? formatarCpf(cliente.cpf) : '',
    endereco: cliente?.endereco ?? '',
    profissao: cliente?.profissao ?? '',
  });
  const [rendimentos, setRendimentos] = useState(cliente?.rendimentos ?? []);
  const [erro, setErro] = useState(null);
  const [campos, setCampos] = useState({});
  const [enviando, setEnviando] = useState(false);

  const entrada = (nome) => ({
    value: dados[nome],
    onChange: (e) => setDados({ ...dados, [nome]: e.target.value }),
  });

  const alterarRendimento = (indice, campo, valor) =>
    setRendimentos(rendimentos.map((r, i) => (i === indice ? { ...r, [campo]: valor } : r)));

  async function enviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);
    setCampos({});
    const { login, senha, ...resto } = dados;
    const corpo = {
      ...resto,
      ...(edicao ? {} : { login }),
      ...(senha ? { senha } : {}),
      rendimentos,
    };
    try {
      await aoEnviar(corpo);
    } catch (e) {
      setErro(e.message);
      setCampos(e.campos ?? {});
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={enviar} className="formulario">
      <Aviso erro={erro} />
      <fieldset>
        <legend>Identificação</legend>
        <div className="grade">
          <Campo rotulo="Nome completo" erro={campos.nome}>
            <input required autoComplete="name" {...entrada('nome')} />
          </Campo>
          <Campo rotulo="CPF" erro={campos.cpf}>
            <input required inputMode="numeric" placeholder="000.000.000-00" {...entrada('cpf')} />
          </Campo>
          <Campo rotulo="RG" erro={campos.rg}>
            <input required {...entrada('rg')} />
          </Campo>
          <Campo rotulo="Profissão" erro={campos.profissao}>
            <input required {...entrada('profissao')} />
          </Campo>
          <Campo rotulo="Endereço" erro={campos.endereco}>
            <input required autoComplete="street-address" {...entrada('endereco')} />
          </Campo>
        </div>
      </fieldset>

      <fieldset>
        <legend>
          Rendimentos <small>(até {MAX_RENDIMENTOS})</small>
        </legend>
        {rendimentos.map((rendimento, i) => (
          <div className="linha-rendimento" key={i}>
            <Campo rotulo="Entidade empregadora" erro={campos[`rendimentos[${i}].entidadeEmpregadora`]}>
              <input
                required
                value={rendimento.entidadeEmpregadora}
                onChange={(e) => alterarRendimento(i, 'entidadeEmpregadora', e.target.value)}
              />
            </Campo>
            <Campo rotulo="Valor mensal (R$)" erro={campos[`rendimentos[${i}].valor`]}>
              <input
                required
                type="number"
                min="0.01"
                step="0.01"
                value={rendimento.valor}
                onChange={(e) => alterarRendimento(i, 'valor', e.target.value)}
              />
            </Campo>
            <button
              type="button"
              className="botao secundario"
              onClick={() => setRendimentos(rendimentos.filter((_, j) => j !== i))}
            >
              Remover
            </button>
          </div>
        ))}
        <button
          type="button"
          className="botao secundario"
          disabled={rendimentos.length >= MAX_RENDIMENTOS}
          onClick={() => setRendimentos([...rendimentos, { entidadeEmpregadora: '', valor: '' }])}
        >
          Adicionar rendimento
        </button>
      </fieldset>

      <fieldset>
        <legend>Acesso</legend>
        <div className="grade">
          {!edicao && (
            <Campo rotulo="Usuário" erro={campos.login}>
              <input required minLength={3} autoComplete="username" {...entrada('login')} />
            </Campo>
          )}
          <Campo
            rotulo={edicao ? 'Nova senha' : 'Senha'}
            erro={campos.senha}
            dica={edicao ? 'Deixe em branco para manter a atual.' : 'Mínimo de 8 caracteres.'}
          >
            <input
              type="password"
              required={!edicao}
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
              {...entrada('senha')}
            />
          </Campo>
        </div>
      </fieldset>

      <div className="acoes">
        <button className="botao" disabled={enviando}>
          {enviando ? 'Salvando…' : rotuloEnviar}
        </button>
      </div>
    </form>
  );
}
