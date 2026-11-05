import { cloneElement, useEffect, useId, useRef, useState } from 'react';
import { STATUS_PEDIDO } from '../utils/formatar.js';

export function Campo({ rotulo, erro, children, dica }) {
  const id = useId();
  const idDescricao = `${id}-descricao`;
  const descricao = erro ?? dica;
  return (
    <div className="campo">
      <label htmlFor={id}>{rotulo}</label>
      {cloneElement(children, {
        id,
        'aria-invalid': erro ? true : undefined,
        'aria-describedby': descricao ? idDescricao : undefined,
      })}
      {descricao && (
        <small id={idDescricao} className={erro ? 'erro-campo' : undefined}>
          {descricao}
        </small>
      )}
    </div>
  );
}

export function Aviso({ erro, sucesso }) {
  const elemento = useRef(null);
  const mensagem = erro ?? sucesso;

  useEffect(() => {
    if (mensagem) elemento.current?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }, [mensagem]);

  if (!mensagem) return null;
  return (
    <p ref={elemento} className={erro ? 'aviso' : 'sucesso'} role={erro ? 'alert' : 'status'}>
      {mensagem}
    </p>
  );
}

export function StatusPedido({ status }) {
  return <span className={`status status-${status.toLowerCase()}`}>{STATUS_PEDIDO[status]}</span>;
}

export function Vazio({ children }) {
  return <p className="vazio">{children}</p>;
}

export function BotaoConfirmar({ rotulo, confirmacao, aoConfirmar }) {
  const [armado, setArmado] = useState(false);
  return armado ? (
    <span className="confirmar">
      <button
        type="button"
        className="botao perigo"
        onClick={() => {
          setArmado(false);
          aoConfirmar();
        }}
      >
        {confirmacao}
      </button>
      <button type="button" className="botao secundario" onClick={() => setArmado(false)}>
        Voltar
      </button>
    </span>
  ) : (
    <button type="button" className="botao secundario" onClick={() => setArmado(true)}>
      {rotulo}
    </button>
  );
}
