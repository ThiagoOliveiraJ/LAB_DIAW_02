import { useCallback, useState } from 'react';
import { Aviso, BotaoConfirmar, Campo, Vazio } from '../components/ui.jsx';
import { useAuth } from '../hooks/useAuth.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';

const VAZIO = { matricula: '', ano: '', marca: '', modelo: '', placa: '' };

export default function Automoveis() {
  const { usuario, isAgente } = useAuth();
  const { dados: automoveis, erro, carregando, recarregar } = useCarregar(useCallback(() => api.get('/automoveis'), []));
  const [formulario, setFormulario] = useState(null);
  const [erroAcao, setErroAcao] = useState(null);

  async function excluir(id) {
    setErroAcao(null);
    try {
      await api.delete(`/automoveis/${id}`);
      recarregar();
    } catch (e) {
      setErroAcao(e.message);
    }
  }

  return (
    <>
      <div className="cabecalho">
        <h1>Automóveis</h1>
        {isAgente && !formulario && (
          <button type="button" className="botao" onClick={() => setFormulario(VAZIO)}>
            Novo automóvel
          </button>
        )}
      </div>
      <Aviso erro={erro ?? erroAcao} />
      {formulario && (
        <FormAutomovel
          key={formulario.id ?? 'novo'}
          inicial={formulario}
          aoFechar={() => setFormulario(null)}
          aoSalvar={() => {
            setFormulario(null);
            recarregar();
          }}
        />
      )}
      {carregando && <p>Carregando…</p>}
      {automoveis?.length === 0 && <Vazio>Nenhum automóvel cadastrado.</Vazio>}
      {automoveis?.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Marca e modelo</th>
                <th>Ano</th>
                <th>Placa</th>
                <th>Matrícula</th>
                <th>Proprietário</th>
                {isAgente && <th aria-label="Ações" />}
              </tr>
            </thead>
            <tbody>
              {automoveis.map((a) => (
                <tr key={a.id}>
                  <td>
                    {a.marca} {a.modelo}
                  </td>
                  <td>{a.ano}</td>
                  <td>{a.placa}</td>
                  <td>{a.matricula}</td>
                  <td>{a.proprietarioNome}</td>
                  {isAgente && (
                    <td>
                      {a.proprietarioId === usuario.id && (
                        <div className="acoes-linha">
                          <button type="button" className="botao secundario" onClick={() => setFormulario(a)}>
                            Editar
                          </button>
                          <BotaoConfirmar rotulo="Excluir" confirmacao="Confirmar" aoConfirmar={() => excluir(a.id)} />
                        </div>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

function FormAutomovel({ inicial, aoFechar, aoSalvar }) {
  const [dados, setDados] = useState(inicial);
  const [erro, setErro] = useState(null);
  const [campos, setCampos] = useState({});

  const entrada = (nome) => ({
    value: dados[nome],
    onChange: (e) => setDados({ ...dados, [nome]: e.target.value }),
  });

  async function enviar(evento) {
    evento.preventDefault();
    setErro(null);
    setCampos({});
    const corpo = {
      matricula: dados.matricula,
      ano: Number(dados.ano),
      marca: dados.marca,
      modelo: dados.modelo,
      placa: dados.placa,
    };
    try {
      if (inicial.id) await api.put(`/automoveis/${inicial.id}`, corpo);
      else await api.post('/automoveis', corpo);
      aoSalvar();
    } catch (e) {
      setErro(e.message);
      setCampos(e.campos);
    }
  }

  return (
    <form onSubmit={enviar} className="formulario painel">
      <h2>{inicial.id ? 'Editar automóvel' : 'Novo automóvel'}</h2>
      <Aviso erro={erro} />
      <div className="grade">
        <Campo rotulo="Marca" erro={campos.marca}>
          <input required {...entrada('marca')} />
        </Campo>
        <Campo rotulo="Modelo" erro={campos.modelo}>
          <input required {...entrada('modelo')} />
        </Campo>
        <Campo rotulo="Ano" erro={campos.ano}>
          <input required type="number" min="1950" max="2100" {...entrada('ano')} />
        </Campo>
        <Campo rotulo="Placa" erro={campos.placa}>
          <input required placeholder="ABC1D23" {...entrada('placa')} />
        </Campo>
        <Campo rotulo="Matrícula" erro={campos.matricula}>
          <input required {...entrada('matricula')} />
        </Campo>
      </div>
      <div className="acoes">
        <button className="botao">Salvar</button>
        <button type="button" className="botao secundario" onClick={aoFechar}>
          Fechar
        </button>
      </div>
    </form>
  );
}
