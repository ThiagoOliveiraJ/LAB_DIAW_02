import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.jsx';
import { TIPO_AGENTE } from '../utils/formatar.js';

export default function Layout() {
  const { usuario, isAgente, sair } = useAuth();
  const papel = isAgente ? `Agente · ${TIPO_AGENTE[usuario.tipoAgente]}` : 'Cliente';

  return (
    <div className="app">
      <header className="topo">
        <div className="marca">
          <img src="/logo.svg" alt="" width="32" height="32" />
          <span>Rota Certa</span>
        </div>
        <nav aria-label="Principal">
          <NavLink to="/pedidos">Pedidos</NavLink>
          <NavLink to="/contratos">Contratos</NavLink>
          <NavLink to="/automoveis">Automóveis</NavLink>
          {isAgente && <NavLink to="/clientes">Clientes</NavLink>}
          {isAgente && <NavLink to="/agentes">Agentes</NavLink>}
          {!isAgente && <NavLink to="/perfil">Meus dados</NavLink>}
        </nav>
        <div className="conta">
          <span>
            <strong>{usuario.nome}</strong>
            <small>{papel}</small>
          </span>
          <button type="button" className="botao secundario" onClick={sair}>
            Sair
          </button>
        </div>
      </header>
      <main className="conteudo">
        <Outlet />
      </main>
    </div>
  );
}
