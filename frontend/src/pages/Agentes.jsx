import { useCallback, useState } from 'react';
import { Aviso, Campo, Vazio } from '../components/ui.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';
import { formatarCnpj, TIPO_AGENTE } from '../utils/formatar.js';

const VAZIO = { nome: '', login: '', senha: '', tipo: 'EMPRESA', cnpj: '' };

export default function Agentes() {
  const { dados: agentes, erro, carregando, recarregar } = useCarregar(useCallback(() => api.get('/agentes'), []));
  const [aberto, setAberto] = useState(false);
  const [dados, setDados] = useState(VAZIO);
  const [erroForm, setErroForm] = useState(null);
  const [campos, setCampos] = useState({});

  const entrada = (nome) => ({
    value: dados[nome],
    onChange: (e) => setDados({ ...dados, [nome]: e.target.value }),
  });

  async function enviar(evento) {
    evento.preventDefault();
    setErroForm(null);
    setCampos({});
    try {
      await api.post('/agentes', dados);
      setDados(VAZIO);
      setAberto(false);
      recarregar();
    } catch (e) {
      setErroForm(e.message);
      setCampos(e.campos);
    }
  }

  return (
    <>
      <div className="cabecalho">
        <div>
          <h1>Agentes</h1>
          <p>Empresas e bancos aderentes. Novos agentes são cadastrados por um agente existente.</p>
        </div>
        {!aberto && (
          <button type="button" className="botao" onClick={() => setAberto(true)}>
            Novo agente
          </button>
        )}
      </div>
      <Aviso erro={erro} />
      {aberto && (
        <form onSubmit={enviar} className="formulario painel">
          <h2>Novo agente</h2>
          <Aviso erro={erroForm} />
          <div className="grade">
            <Campo rotulo="Razão social" erro={campos.nome}>
              <input required {...entrada('nome')} />
            </Campo>
            <Campo rotulo="Tipo" erro={campos.tipo}>
              <select {...entrada('tipo')}>
                <option value="EMPRESA">Empresa</option>
                <option value="BANCO">Banco</option>
              </select>
            </Campo>
            <Campo rotulo="CNPJ" erro={campos.cnpj}>
              <input required {...entrada('cnpj')} />
            </Campo>
            <Campo rotulo="Usuário" erro={campos.login}>
              <input required minLength={3} autoComplete="off" {...entrada('login')} />
            </Campo>
            <Campo rotulo="Senha" erro={campos.senha} dica="Mínimo de 8 caracteres.">
              <input required type="password" minLength={8} maxLength={72} autoComplete="new-password" {...entrada('senha')} />
            </Campo>
          </div>
          <div className="acoes">
            <button className="botao">Cadastrar</button>
            <button type="button" className="botao secundario" onClick={() => setAberto(false)}>
              Fechar
            </button>
          </div>
        </form>
      )}
      {carregando && <p>Carregando…</p>}
      {agentes?.length === 0 && <Vazio>Nenhum agente cadastrado.</Vazio>}
      {agentes?.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Tipo</th>
                <th>CNPJ</th>
                <th>Usuário</th>
              </tr>
            </thead>
            <tbody>
              {agentes.map((a) => (
                <tr key={a.id}>
                  <td>{a.nome}</td>
                  <td>{TIPO_AGENTE[a.tipo]}</td>
                  <td>{formatarCnpj(a.cnpj)}</td>
                  <td>{a.login}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
