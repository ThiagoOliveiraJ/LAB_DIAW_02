import { useCallback, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Aviso, BotaoConfirmar, Campo, StatusPedido } from '../components/ui.jsx';
import { useAuth } from '../hooks/useAuth.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';
import { formatarData, formatarDataHora, formatarMoeda, TIPO_CONTRATO } from '../utils/formatar.js';

export default function PedidoDetalhe() {
  const { id } = useParams();
  const { isAgente } = useAuth();
  const buscar = useCallback(async () => {
    const pedido = await api.get(`/pedidos/${id}`);
    const contrato = pedido.status === 'CONTRATADO' ? await api.get(`/pedidos/${id}/contrato`) : null;
    return { pedido, contrato };
  }, [id]);
  const { dados, erro, carregando, recarregar } = useCarregar(buscar);
  const [erroAcao, setErroAcao] = useState(null);

  if (carregando) return <p>Carregando…</p>;
  if (erro) return <Aviso erro={erro} />;
  const { pedido, contrato } = dados;

  const podeModificar = pedido.status === 'EM_ANALISE' || (isAgente && pedido.status === 'APROVADO');
  const podeCancelar = !isAgente && ['EM_ANALISE', 'APROVADO'].includes(pedido.status);

  async function cancelar() {
    setErroAcao(null);
    try {
      await api.post(`/pedidos/${id}/cancelar`);
      recarregar();
    } catch (e) {
      setErroAcao(e.message);
    }
  }

  return (
    <>
      <div className="cabecalho">
        <div>
          <h1>
            Pedido #{pedido.id} <StatusPedido status={pedido.status} />
          </h1>
          <p>Criado em {formatarDataHora(pedido.criadoEm)}</p>
        </div>
        <div className="acoes">
          {podeModificar && (
            <Link className="botao secundario" to={`/pedidos/${id}/editar`}>
              Modificar
            </Link>
          )}
          {podeCancelar && (
            <BotaoConfirmar rotulo="Cancelar pedido" confirmacao="Confirmar cancelamento" aoConfirmar={cancelar} />
          )}
        </div>
      </div>
      <Aviso erro={erroAcao} />

      <section className="painel">
        <h2>Dados do pedido</h2>
        <dl className="dados">
          <div>
            <dt>Cliente</dt>
            <dd>{pedido.clienteNome}</dd>
          </div>
          <div>
            <dt>Automóvel</dt>
            <dd>
              {pedido.automovel.marca} {pedido.automovel.modelo} {pedido.automovel.ano}
            </dd>
          </div>
          <div>
            <dt>Placa</dt>
            <dd>{pedido.automovel.placa}</dd>
          </div>
          <div>
            <dt>Período</dt>
            <dd>
              {formatarData(pedido.dataInicio)} a {formatarData(pedido.dataFim)}
            </dd>
          </div>
          {pedido.observacoes && (
            <div className="largo">
              <dt>Observações</dt>
              <dd>{pedido.observacoes}</dd>
            </div>
          )}
        </dl>
      </section>

      {pedido.parecer && (
        <section className="painel">
          <h2>Parecer financeiro</h2>
          <dl className="dados">
            <div>
              <dt>Resultado</dt>
              <dd>{pedido.parecer.resultado === 'POSITIVO' ? 'Positivo' : 'Negativo'}</dd>
            </div>
            <div>
              <dt>Agente</dt>
              <dd>{pedido.parecer.agenteNome}</dd>
            </div>
            <div>
              <dt>Emitido em</dt>
              <dd>{formatarDataHora(pedido.parecer.emitidoEm)}</dd>
            </div>
            <div className="largo">
              <dt>Justificativa</dt>
              <dd>{pedido.parecer.justificativa}</dd>
            </div>
          </dl>
        </section>
      )}

      {contrato && (
        <section className="painel">
          <h2>Contrato #{contrato.id}</h2>
          <dl className="dados">
            <div>
              <dt>Tipo</dt>
              <dd>{TIPO_CONTRATO[contrato.tipo]}</dd>
            </div>
            <div>
              <dt>Valor total</dt>
              <dd>{formatarMoeda(contrato.valorTotal)}</dd>
            </div>
            <div>
              <dt>Executado por</dt>
              <dd>{contrato.agenteNome}</dd>
            </div>
            <div>
              <dt>Proprietário do automóvel</dt>
              <dd>{contrato.automovel.proprietarioNome}</dd>
            </div>
            {contrato.credito && (
              <div className="largo">
                <dt>Crédito</dt>
                <dd>
                  {contrato.credito.bancoNome}: {formatarMoeda(contrato.credito.valor)} em {contrato.credito.parcelas}x,
                  juros de {Number(contrato.credito.taxaJurosMensal).toLocaleString('pt-BR')}% a.m.
                </dd>
              </div>
            )}
          </dl>
        </section>
      )}

      {isAgente && pedido.status === 'EM_ANALISE' && <FormParecer id={id} aoConcluir={recarregar} clienteId={pedido.clienteId} />}
      {isAgente && pedido.status === 'APROVADO' && <FormContrato id={id} aoConcluir={recarregar} />}
    </>
  );
}

function FormParecer({ id, clienteId, aoConcluir }) {
  const [justificativa, setJustificativa] = useState('');
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const { dados: cliente } = useCarregar(useCallback(() => api.get(`/clientes/${clienteId}`), [clienteId]));

  async function emitir(resultado) {
    if (!justificativa.trim()) {
      setErro('Informe a justificativa do parecer.');
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      await api.post(`/pedidos/${id}/avaliar`, { resultado, justificativa });
      aoConcluir();
    } catch (e) {
      setErro(e.message);
      setEnviando(false);
    }
  }

  return (
    <section className="painel destaque">
      <h2>Análise financeira</h2>
      {cliente && (
        <div className="renda">
          <p>
            <strong>{cliente.nome}</strong>, {cliente.profissao}
          </p>
          {cliente.rendimentos.length === 0 && <p>Nenhum rendimento informado.</p>}
          <ul>
            {cliente.rendimentos.map((r, i) => (
              <li key={i}>
                {r.entidadeEmpregadora}: {formatarMoeda(r.valor)}
              </li>
            ))}
          </ul>
        </div>
      )}
      <div className="formulario">
        <Aviso erro={erro} />
        <Campo rotulo="Justificativa">
          <textarea rows="3" maxLength={500} value={justificativa} onChange={(e) => setJustificativa(e.target.value)} />
        </Campo>
        <div className="acoes">
          <button type="button" className="botao" disabled={enviando} onClick={() => emitir('POSITIVO')}>
            Parecer positivo
          </button>
          <button type="button" className="botao perigo" disabled={enviando} onClick={() => emitir('NEGATIVO')}>
            Parecer negativo
          </button>
        </div>
      </div>
    </section>
  );
}

function FormContrato({ id, aoConcluir }) {
  const { usuario } = useAuth();
  const agenteBanco = usuario.tipoAgente === 'BANCO';
  const [tipo, setTipo] = useState(agenteBanco ? 'BANCO' : 'EMPRESA');
  const [valorTotal, setValorTotal] = useState('');
  const [comCredito, setComCredito] = useState(false);
  const [credito, setCredito] = useState({ bancoId: '', valor: '', taxaJurosMensal: '', parcelas: '' });
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const { dados: bancos } = useCarregar(useCallback(() => api.get('/agentes?tipo=BANCO'), []));

  const exibirCredito = comCredito || tipo === 'BANCO';
  const entrada = (nome) => ({
    value: credito[nome],
    onChange: (e) => setCredito({ ...credito, [nome]: e.target.value }),
  });

  async function enviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);
    const corpo = {
      tipo,
      valorTotal,
      ...(exibirCredito && { credito: { ...credito, bancoId: Number(credito.bancoId) } }),
    };
    try {
      await api.post(`/pedidos/${id}/contrato`, corpo);
      aoConcluir();
    } catch (e) {
      const detalhes = Object.values(e.campos ?? {}).join(' ');
      setErro(detalhes || e.message);
      setEnviando(false);
    }
  }

  return (
    <section className="painel destaque">
      <h2>Execução do contrato</h2>
      <form onSubmit={enviar} className="formulario">
        <Aviso erro={erro} />
        <div className="grade">
          <Campo rotulo="Tipo de contrato" dica="Define em nome de quem o automóvel será registrado.">
            <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
              <option value="CLIENTE">{TIPO_CONTRATO.CLIENTE}</option>
              {!agenteBanco && <option value="EMPRESA">{TIPO_CONTRATO.EMPRESA}</option>}
              <option value="BANCO">{TIPO_CONTRATO.BANCO}</option>
            </select>
          </Campo>
          <Campo rotulo="Valor total (R$)">
            <input required type="number" min="0.01" step="0.01" value={valorTotal} onChange={(e) => setValorTotal(e.target.value)} />
          </Campo>
        </div>
        {tipo !== 'BANCO' && (
          <label className="opcao">
            <input type="checkbox" checked={comCredito} onChange={(e) => setComCredito(e.target.checked)} />
            Associar a um contrato de crédito
          </label>
        )}
        {exibirCredito && (
          <div className="grade">
            <Campo rotulo="Banco">
              <select required {...entrada('bancoId')}>
                <option value="">Selecione…</option>
                {(bancos ?? []).map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.nome}
                  </option>
                ))}
              </select>
            </Campo>
            <Campo rotulo="Valor do crédito (R$)">
              <input required type="number" min="0.01" step="0.01" {...entrada('valor')} />
            </Campo>
            <Campo rotulo="Juros ao mês (%)">
              <input required type="number" min="0" step="0.01" {...entrada('taxaJurosMensal')} />
            </Campo>
            <Campo rotulo="Parcelas">
              <input required type="number" min="1" step="1" {...entrada('parcelas')} />
            </Campo>
          </div>
        )}
        <div className="acoes">
          <button className="botao" disabled={enviando}>
            {enviando ? 'Executando…' : 'Executar contrato'}
          </button>
        </div>
      </form>
    </section>
  );
}
