import { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import { Aviso, BotaoConfirmar, Vazio } from '../components/ui.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';
import { formatarCpf, formatarMoeda } from '../utils/formatar.js';

export default function Clientes() {
  const { dados: clientes, erro, carregando, recarregar } = useCarregar(useCallback(() => api.get('/clientes'), []));
  const [erroAcao, setErroAcao] = useState(null);

  async function excluir(id) {
    setErroAcao(null);
    try {
      await api.delete(`/clientes/${id}`);
      recarregar();
    } catch (e) {
      setErroAcao(e.message);
    }
  }

  return (
    <>
      <div className="cabecalho">
        <h1>Clientes</h1>
        <Link className="botao" to="/clientes/novo">
          Novo cliente
        </Link>
      </div>
      <Aviso erro={erro ?? erroAcao} />
      {carregando && <p>Carregando…</p>}
      {clientes?.length === 0 && <Vazio>Nenhum cliente cadastrado.</Vazio>}
      {clientes?.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>RG</th>
                <th>Profissão</th>
                <th>Renda total</th>
                <th aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {clientes.map((c) => (
                <tr key={c.id}>
                  <td>{c.nome}</td>
                  <td>{formatarCpf(c.cpf)}</td>
                  <td>{c.rg}</td>
                  <td>{c.profissao}</td>
                  <td>{formatarMoeda(c.rendimentos.reduce((soma, r) => soma + Number(r.valor), 0))}</td>
                  <td>
                    <div className="acoes-linha">
                      <Link className="botao secundario" to={`/clientes/${c.id}`}>
                        Editar
                      </Link>
                      <BotaoConfirmar rotulo="Excluir" confirmacao="Confirmar" aoConfirmar={() => excluir(c.id)} />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
