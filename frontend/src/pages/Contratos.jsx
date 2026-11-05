import { useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Aviso, Vazio } from '../components/ui.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';
import { formatarDataHora, formatarMoeda, TIPO_CONTRATO } from '../utils/formatar.js';

export default function Contratos() {
  const { dados: contratos, erro, carregando } = useCarregar(useCallback(() => api.get('/contratos'), []));

  return (
    <>
      <div className="cabecalho">
        <h1>Contratos</h1>
      </div>
      <Aviso erro={erro} />
      {carregando && <p>Carregando…</p>}
      {contratos?.length === 0 && <Vazio>Nenhum contrato executado até agora.</Vazio>}
      {contratos?.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Nº</th>
                <th>Pedido</th>
                <th>Cliente</th>
                <th>Automóvel</th>
                <th>Tipo</th>
                <th>Valor</th>
                <th>Crédito</th>
                <th>Assinado em</th>
              </tr>
            </thead>
            <tbody>
              {contratos.map((c) => (
                <tr key={c.id}>
                  <td>#{c.id}</td>
                  <td>
                    <Link to={`/pedidos/${c.pedidoId}`}>#{c.pedidoId}</Link>
                  </td>
                  <td>{c.clienteNome}</td>
                  <td>
                    {c.automovel.marca} {c.automovel.modelo}
                  </td>
                  <td>{TIPO_CONTRATO[c.tipo]}</td>
                  <td>{formatarMoeda(c.valorTotal)}</td>
                  <td>{c.credito ? c.credito.bancoNome : '—'}</td>
                  <td>{formatarDataHora(c.assinadoEm)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
