# Diagrama de Casos de Uso (v1)

O Mermaid não tem um tipo próprio para casos de uso. Aqui, os retângulos são atores, as formas arredondadas são casos de uso e as setas pontilhadas com rótulo são `include`/`extend`.

```mermaid
flowchart LR
    Vis[/"👤 Visitante"/]
    Cli[/"👤 Cliente"/]
    Age[/"👤 Agente"/]
    Emp[/"👤 Empresa"/]
    Ban[/"👤 Banco"/]

    Emp -- "é um" --> Age
    Ban -- "é um" --> Age

    subgraph Sistema["Gestão de pedidos e contratos"]
        UC01(["UC01 Cadastrar-se como cliente"])
        UC02(["UC02 Fazer login"])
        UC03(["UC03 Manter dados de cliente"])
        UC04(["UC04 Introduzir pedido"])
        UC05(["UC05 Consultar pedido e status"])
        UC06(["UC06 Modificar pedido"])
        UC07(["UC07 Cancelar pedido"])
        UC08(["UC08 Avaliar pedido"])
        UC09(["UC09 Executar contrato"])
        UC10(["UC10 Conceder crédito"])
        UC11(["UC11 Consultar contratos"])
        UC12(["UC12 Manter automóveis"])
        UC13(["UC13 Cadastrar agente"])
        UC14(["UC14 Registrar propriedade do automóvel"])
    end

    Vis --> UC01
    Vis --> UC02
    Cli --> UC03
    Cli --> UC04
    Cli --> UC05
    Cli --> UC06
    Cli --> UC07
    Cli --> UC11
    Age --> UC03
    Age --> UC05
    Age --> UC06
    Age --> UC08
    Age --> UC09
    Age --> UC11
    Age --> UC12
    Age --> UC13
    Ban --> UC10
    UC09 -. "include" .-> UC14
    UC10 -. "extend" .-> UC09
```
