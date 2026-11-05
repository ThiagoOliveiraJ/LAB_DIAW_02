import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import { useAuth } from './hooks/useAuth.jsx';
import Agentes from './pages/Agentes.jsx';
import Automoveis from './pages/Automoveis.jsx';
import Cadastro from './pages/Cadastro.jsx';
import ClienteEdicao from './pages/ClienteEdicao.jsx';
import ClienteNovo from './pages/ClienteNovo.jsx';
import Clientes from './pages/Clientes.jsx';
import Contratos from './pages/Contratos.jsx';
import Login from './pages/Login.jsx';
import PedidoDetalhe from './pages/PedidoDetalhe.jsx';
import PedidoForm from './pages/PedidoForm.jsx';
import Pedidos from './pages/Pedidos.jsx';

function Protegida({ perfil }) {
  const { usuario, carregando } = useAuth();
  if (carregando) return <p className="centro">Carregando…</p>;
  if (!usuario) return <Navigate to="/login" replace />;
  if (perfil && usuario.perfil !== perfil) return <Navigate to="/pedidos" replace />;
  return <Outlet />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro" element={<Cadastro />} />
      <Route element={<Protegida />}>
        <Route element={<Layout />}>
          <Route index element={<Navigate to="/pedidos" replace />} />
          <Route path="/pedidos" element={<Pedidos />} />
          <Route path="/pedidos/:id" element={<PedidoDetalhe />} />
          <Route path="/pedidos/:id/editar" element={<PedidoForm />} />
          <Route path="/contratos" element={<Contratos />} />
          <Route path="/automoveis" element={<Automoveis />} />
          <Route element={<Protegida perfil="CLIENTE" />}>
            <Route path="/pedidos/novo" element={<PedidoForm />} />
            <Route path="/perfil" element={<ClienteEdicao />} />
          </Route>
          <Route element={<Protegida perfil="AGENTE" />}>
            <Route path="/clientes" element={<Clientes />} />
            <Route path="/clientes/novo" element={<ClienteNovo />} />
            <Route path="/clientes/:id" element={<ClienteEdicao />} />
            <Route path="/agentes" element={<Agentes />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
