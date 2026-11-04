# Histórias de Usuário

Cada história indica o caso de uso correspondente no [diagrama de casos de uso](./uml/v1/casos-de-uso.md).

## Acesso

**HU01: Cadastro de cliente (UC01).** Como visitante, quero me cadastrar informando meus dados e rendimentos, para poder usar o sistema.
- RG, CPF e usuário não podem se repetir, e o CPF precisa ser válido.
- São aceitos no máximo 3 rendimentos, cada um com entidade empregadora e valor.

**HU02: Login (UC02).** Como usuário cadastrado, quero entrar com usuário e senha, para acessar o sistema.
- Sem login, todas as funcionalidades são bloqueadas, exceto o cadastro de cliente.
- Credenciais incorretas geram sempre a mesma mensagem, sem indicar qual delas está errada.
- A sessão expira em 8 horas.

## Cliente

**HU03: Manter meus dados (UC03).** Como cliente, quero consultar, alterar e excluir meu cadastro, para mantê-lo correto.
- Um cliente não acessa os dados de outro.
- Um cliente com pedidos não pode ser excluído.

**HU04: Introduzir pedido (UC04).** Como cliente, quero pedir o aluguel de um automóvel para um período, para que ele seja analisado.
- O pedido começa com o status "Em análise".
- O início não pode estar no passado, e o fim não pode ser anterior ao início.
- O automóvel precisa estar disponível, sem contrato no período.

**HU05: Consultar pedidos (UC05).** Como cliente, quero ver meus pedidos e seus status, para acompanhar o andamento.
- O cliente vê somente os próprios pedidos, incluindo parecer e contrato quando houver.

**HU06: Modificar pedido (UC06).** Como cliente, quero alterar meu pedido enquanto ele está em análise, para corrigir automóvel, datas ou observações.

**HU07: Cancelar pedido (UC07).** Como cliente, quero cancelar um pedido em análise ou aprovado, caso desista do aluguel.
- Pedidos já contratados, reprovados ou cancelados não podem ser cancelados.

## Agente (empresa ou banco)

**HU08: Avaliar pedido (UC08).** Como agente, quero emitir um parecer financeiro com base nos rendimentos do cliente, para decidir se o pedido segue adiante.
- O parecer é positivo ou negativo e exige justificativa.
- O positivo leva o pedido a "Aprovado", e o negativo, a "Reprovado".

**HU09: Modificar pedido (UC06).** Como agente, quero modificar pedidos em análise ou aprovados, para ajustar as condições do aluguel.

**HU10: Executar contrato (UC09, UC14).** Como agente, quero executar o contrato de um pedido aprovado, para formalizar o aluguel.
- Apenas pedidos com parecer positivo podem virar contrato.
- O tipo de contrato define o proprietário do automóvel: cliente, empresa ou banco.
- O mesmo automóvel não pode ter contratos em períodos sobrepostos.

**HU11: Associar crédito (UC10).** Como agente, quero associar um contrato de crédito ao aluguel, para registrar o financiamento concedido.
- O crédito é sempre concedido por um banco agente.
- Contratos do tipo "banco" exigem crédito.

**HU12: Manter clientes (UC03).** Como agente, quero listar, cadastrar, alterar e excluir clientes, para manter a base atualizada.

**HU13: Manter automóveis (UC12).** Como agente, quero cadastrar, alterar e excluir automóveis, com matrícula, ano, marca, modelo e placa.
- Apenas o proprietário altera ou exclui o automóvel.
- Um automóvel vinculado a pedidos não pode ser excluído.

**HU14: Cadastrar agentes (UC13).** Como agente, quero cadastrar novas empresas e bancos, para ampliar os agentes aderentes.

## Todos os usuários

**HU15: Consultar contratos (UC11).** Como usuário, quero consultar os contratos executados: o cliente vê os dele, e o agente vê todos.
