import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { Aviso, Campo } from '../components/ui.jsx';
import { useAuth } from '../hooks/useAuth.jsx';

export default function Login() {
  const { usuario, entrar } = useAuth();
  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  if (usuario) return <Navigate to="/pedidos" replace />;

  async function enviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErro(null);
    try {
      await entrar(login, senha);
    } catch (e) {
      setErro(e.message);
      setEnviando(false);
    }
  }

  return (
    <div className="acesso">
      <section className="acesso-cartao">
        <img src="/logo.svg" alt="" width="48" height="48" />
        <h1>Rota Certa</h1>
        <p>Peça, acompanhe e aprove aluguéis de automóveis em um só lugar.</p>
        <form onSubmit={enviar} className="formulario">
          <Aviso erro={erro} />
          <Campo rotulo="Usuário">
            <input required autoFocus autoComplete="username" value={login} onChange={(e) => setLogin(e.target.value)} />
          </Campo>
          <Campo rotulo="Senha">
            <input
              required
              type="password"
              autoComplete="current-password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </Campo>
          <button className="botao largo" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
        </form>
        <p className="rodape-acesso">
          Ainda não tem acesso? <Link to="/cadastro">Cadastre-se como cliente</Link>
        </p>
      </section>
    </div>
  );
}
