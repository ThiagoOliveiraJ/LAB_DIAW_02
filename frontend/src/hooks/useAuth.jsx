import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, sessao } from '../services/api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [carregando, setCarregando] = useState(Boolean(sessao.token()));

  useEffect(() => {
    sessao.aoExpirar(() => setUsuario(null));
    if (!sessao.token()) return;
    api.get('/auth/me')
      .then(setUsuario)
      .catch(() => {})
      .finally(() => setCarregando(false));
  }, []);

  const entrar = useCallback(async (login, senha) => {
    const resposta = await api.post('/auth/login', { login, senha });
    sessao.salvar(resposta.token);
    setUsuario(resposta.usuario);
    return resposta.usuario;
  }, []);

  const sair = useCallback(() => {
    sessao.limpar();
    setUsuario(null);
  }, []);

  const valor = useMemo(
    () => ({ usuario, carregando, entrar, sair, isAgente: usuario?.perfil === 'AGENTE' }),
    [usuario, carregando, entrar, sair],
  );
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
