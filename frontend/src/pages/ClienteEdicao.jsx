import { useCallback, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClienteForm from '../components/ClienteForm.jsx';
import { Aviso, BotaoConfirmar } from '../components/ui.jsx';
import { useAuth } from '../hooks/useAuth.jsx';
import { useCarregar } from '../hooks/useCarregar.js';
import { api } from '../services/api.js';

export default function ClienteEdicao() {
  const { id: idDaRota } = useParams();
  const { usuario, sair } = useAuth();
  const navegar = useNavigate();
  const id = idDaRota ?? usuario.id;
  const proprioCliente = !idDaRota;

  const { dados: cliente, erro, carregando } = useCarregar(useCallback(() => api.get(`/clientes/${id}`), [id]));
  const [mensagem, setMensagem] = useState(null);
  const [erroExclusao, setErroExclusao] = useState(null);

  async function salvar(corpo) {
    setMensagem(null);
    await api.put(`/clientes/${id}`, corpo);
    if (proprioCliente) setMensagem('Dados atualizados.');
    else navegar('/clientes');
  }

  async function excluir() {
    setErroExclusao(null);
    try {
      await api.delete(`/clientes/${id}`);
      if (proprioCliente) sair();
      else navegar('/clientes');
    } catch (e) {
      setErroExclusao(e.message);
    }
  }

  if (carregando) return <p>Carregando…</p>;
  if (erro) return <Aviso erro={erro} />;

  return (
    <>
      <div className="cabecalho">
        <div>
          <h1>{proprioCliente ? 'Meus dados' : `Editar ${cliente.nome}`}</h1>
          <p>Usuário: {cliente.login}</p>
        </div>
        <BotaoConfirmar
          rotulo={proprioCliente ? 'Excluir minha conta' : 'Excluir cliente'}
          confirmacao="Confirmar exclusão"
          aoConfirmar={excluir}
        />
      </div>
      <Aviso erro={erroExclusao} />
      <Aviso sucesso={mensagem} />
      <section className="painel">
        <ClienteForm cliente={cliente} rotuloEnviar="Salvar alterações" aoEnviar={salvar} />
      </section>
    </>
  );
}
