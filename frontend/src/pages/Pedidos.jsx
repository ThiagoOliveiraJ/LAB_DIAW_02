import { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import { Aviso, StatusPedido, Vazio } from '../components/ui.jsx';
import { useAuth } from '../hooks/useAuth.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';
import { formatarData, STATUS_PEDIDO } from '../utils/formatar.js';

export default function Pedidos() {
  const { isAgente } = useAuth();
  const { dados: pedidos, erro, carregando } = useCarregar(useCallback(() => api.get('/pedidos'), []));
  const [filtro, setFiltro] = useState('');

  const visiveis = (pedidos ?? []).filter((p) => !filtro || p.status === filtro);

  return (
    <>
      <div className="cabecalho">
        <div>
          <h1>Pedidos de aluguel</h1>
          <p>{isAgente ? 'Analise os pedidos e execute os contratos aprovados.' : 'Acompanhe o status dos seus pedidos.'}</p>
        </div>
        {!isAgente && (
          <Link className="botao" to="/pedidos/novo">
            Novo pedido
          </Link>
        )}
      </div>
      <Aviso erro={erro} />
      <div className="filtros" role="group" aria-label="Filtrar por status">
        <button type="button" className={filtro === '' ? 'ativo' : ''} onClick={() => setFiltro('')}>
          Todos
        </button>
        {Object.entries(STATUS_PEDIDO).map(([valor, rotulo]) => (
          <button key={valor} type="button" className={filtro === valor ? 'ativo' : ''} onClick={() => setFiltro(valor)}>
            {rotulo}
          </button>
        ))}
      </div>
      {carregando && <p>Carregando…</p>}
      {!carregando && !erro && visiveis.length === 0 && <Vazio>Nenhum pedido encontrado.</Vazio>}
      {visiveis.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Nº</th>
                {isAgente && <th>Cliente</th>}
                <th>Automóvel</th>
                <th>Período</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {visiveis.map((p) => (
                <tr key={p.id}>
                  <td>
                    <Link to={`/pedidos/${p.id}`}>#{p.id}</Link>
                  </td>
                  {isAgente && <td>{p.clienteNome}</td>}
                  <td>
                    {p.automovel.marca} {p.automovel.modelo} <small>{p.automovel.placa}</small>
                  </td>
                  <td>
                    {formatarData(p.dataInicio)} a {formatarData(p.dataFim)}
                  </td>
                  <td>
                    <StatusPedido status={p.status} />
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
