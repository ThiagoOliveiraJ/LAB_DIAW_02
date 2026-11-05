import { useNavigate } from 'react-router-dom';
import ClienteForm from '../components/ClienteForm.jsx';
import { api } from '../services/api.js';

export default function ClienteNovo() {
  const navegar = useNavigate();
  return (
    <>
      <div className="cabecalho">
        <h1>Novo cliente</h1>
      </div>
      <section className="painel">
        <ClienteForm
          rotuloEnviar="Cadastrar cliente"
          aoEnviar={async (corpo) => {
            await api.post('/clientes', corpo);
            navegar('/clientes');
          }}
        />
      </section>
    </>
  );
}
