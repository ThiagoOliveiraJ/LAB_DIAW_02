import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Aviso, Campo } from '../components/ui.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';
import { hojeIso } from '../utils/formatar.js';

export default function PedidoForm() {
  const { id } = useParams();
  const navegar = useNavigate();
  const [dados, setDados] = useState({ automovelId: '', dataInicio: '', dataFim: '', observacoes: '' });
  const [erro, setErro] = useState(null);
  const [campos, setCampos] = useState({});
  const [enviando, setEnviando] = useState(false);

  const { dados: automoveis, erro: erroAutomoveis } = useCarregar(
    useCallback(() => api.get('/automoveis?alugaveis=true'), []),
  );

  useEffect(() => {
    if (!id) return;
    api.get(`/pedidos/${id}`)
      .then((p) =>
        setDados({
          automovelId: String(p.automovel.id),
          dataInicio: p.dataInicio,
          dataFim: p.dataFim,
          observacoes: p.observacoes ?? '',
        }),
      )
      .catch((e) => setErro(e.message));
  }, [id]);

  const entrada = (nome) => ({
    value: dados[nome],
    onChange: (e) => setDados({ ...dados, [nome]: e.target.value }),
  });

  async function enviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);
    setCampos({});
    const corpo = { ...dados, automovelId: Number(dados.automovelId) };
    try {
      const pedido = id ? await api.put(`/pedidos/${id}`, corpo) : await api.post('/pedidos', corpo);
      navegar(`/pedidos/${pedido.id}`);
    } catch (e) {
      setErro(e.message);
      setCampos(e.campos);
      setEnviando(false);
    }
  }

  return (
    <>
      <div className="cabecalho">
        <h1>{id ? `Modificar pedido #${id}` : 'Novo pedido de aluguel'}</h1>
      </div>
      <form onSubmit={enviar} className="formulario painel">
        <Aviso erro={erro ?? erroAutomoveis} />
        <Campo rotulo="Automóvel" erro={campos.automovelId}>
          <select required {...entrada('automovelId')}>
            <option value="">Selecione…</option>
            {(automoveis ?? []).map((a) => (
              <option key={a.id} value={a.id}>
                {a.marca} {a.modelo} {a.ano} · {a.placa}
              </option>
            ))}
          </select>
        </Campo>
        <div className="grade">
          <Campo rotulo="Início" erro={campos.dataInicio}>
            <input required type="date" min={id ? undefined : hojeIso()} {...entrada('dataInicio')} />
          </Campo>
          <Campo rotulo="Fim" erro={campos.dataFim}>
            <input required type="date" min={dados.dataInicio || hojeIso()} {...entrada('dataFim')} />
          </Campo>
        </div>
        <Campo rotulo="Observações" erro={campos.observacoes}>
          <textarea rows="3" maxLength={500} {...entrada('observacoes')} />
        </Campo>
        <div className="acoes">
          <button className="botao" disabled={enviando}>
            {enviando ? 'Salvando…' : id ? 'Salvar alterações' : 'Enviar pedido'}
          </button>
          <Link className="botao secundario" to={id ? `/pedidos/${id}` : '/pedidos'}>
            Voltar
          </Link>
        </div>
      </form>
    </>
  );
}
