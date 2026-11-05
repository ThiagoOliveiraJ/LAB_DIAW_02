import { Link, Navigate } from 'react-router-dom';
import ClienteForm from '../components/ClienteForm.jsx';
import { useAuth } from '../hooks/useAuth.jsx';
import { api } from '../services/api.js';

export default function Cadastro() {
  const { usuario, entrar } = useAuth();
  if (usuario) return <Navigate to="/pedidos" replace />;

  async function cadastrar(corpo) {
    await api.post('/clientes', corpo);
    await entrar(corpo.login, corpo.senha);
  }

  return (
    <div className="acesso acesso-largo">
      <section className="acesso-cartao">
        <h1>Cadastro de cliente</h1>
        <p>Os dados de rendimento são usados pelos agentes na análise financeira dos seus pedidos.</p>
        <ClienteForm rotuloEnviar="Criar conta" aoEnviar={cadastrar} />
        <p className="rodape-acesso">
          Já tem acesso? <Link to="/login">Entrar</Link>
        </p>
      </section>
    </div>
  );
}
