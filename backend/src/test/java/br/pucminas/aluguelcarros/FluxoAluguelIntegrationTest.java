package br.pucminas.aluguelcarros;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoAluguelIntegrationTest {

    private static final String SENHA = "senha-segura-1";
    private static final AtomicInteger PROXIMO_CPF = new AtomicInteger(100_000_000);
    private static final AtomicInteger PROXIMO_CARRO = new AtomicInteger();

    @Autowired
    private MockMvc mvc;

    @Test
    void recursosExigemLogin() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/automoveis")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginInvalidoNaoAutentica() throws Exception {
        mvc.perform(json(post("/api/auth/login"), null, """
                {"login":"locadora","senha":"senha-errada"}"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Usuário ou senha inválidos."));
        mvc.perform(json(post("/api/auth/login"), null, """
                {"login":"nao-existe","senha":"senha-errada"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crudDeCliente() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        String login = "cli-" + UUID.randomUUID();
        String cpf = novoCpf();
        var criado = cadastrarCliente(login, cpf).andExpect(status().isCreated())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andExpect(jsonPath("$.rendimentos", hasSize(1)));
        int id = JsonPath.read(criado.andReturn().getResponse().getContentAsString(), "$.id");
        String cliente = logar(login, SENHA);

        mvc.perform(get("/api/clientes/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cpf").value(cpf));

        mvc.perform(json(put("/api/clientes/" + id), cliente, """
                {"nome":"Novo Nome","rg":"%s","cpf":"%s","endereco":"Rua B","profissao":"Analista",
                 "rendimentos":[{"entidadeEmpregadora":"X","valor":1000},
                                {"entidadeEmpregadora":"Y","valor":2000},
                                {"entidadeEmpregadora":"Z","valor":3000}]}"""
                .formatted(login.substring(0, 12), cpf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.rendimentos", hasSize(3)));

        mvc.perform(get("/api/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer " + agente))
                .andExpect(status().isOk());
        mvc.perform(get("/api/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/clientes/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/clientes/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + agente))
                .andExpect(status().isNotFound());
    }

    @Test
    void clienteAceitaNoMaximoTresRendimentosECpfValido() throws Exception {
        mvc.perform(json(post("/api/clientes"), null, """
                {"nome":"A","login":"limite-rendas","senha":"%s","rg":"RG-LIMITE","cpf":"12345678900",
                 "endereco":"Rua","profissao":"P",
                 "rendimentos":[{"entidadeEmpregadora":"1","valor":1},{"entidadeEmpregadora":"2","valor":1},
                                {"entidadeEmpregadora":"3","valor":1},{"entidadeEmpregadora":"4","valor":1}]}"""
                .formatted(SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").value("CPF inválido."))
                .andExpect(jsonPath("$.campos.rendimentos").exists());
    }

    @Test
    void cpfDuplicadoERejeitado() throws Exception {
        String cpf = novoCpf();
        cadastrarCliente("cli-" + UUID.randomUUID(), cpf).andExpect(status().isCreated());
        cadastrarCliente("cli-" + UUID.randomUUID(), cpf)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Já existe um cliente com este CPF."));
    }

    @Test
    void fluxoCompletoDoPedidoAteOContratoTransfereAPropriedade() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        String banco = logar("banco", "senha-de-teste");
        int bancoId = JsonPath.read(mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + banco))
                .andReturn().getResponse().getContentAsString(), "$.id");
        String cliente = novoClienteLogado();
        int carro = novoCarro(agente);
        int pedido = novoPedido(cliente, carro, 10, 15);

        mvc.perform(get("/api/pedidos/" + pedido).header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));

        mvc.perform(json(post("/api/pedidos/" + pedido + "/contrato"), agente, """
                {"tipo":"EMPRESA","valorTotal":900}"""))
                .andExpect(status().isUnprocessableContent());

        mvc.perform(json(post("/api/pedidos/" + pedido + "/avaliar"), cliente, """
                {"resultado":"POSITIVO","justificativa":"quero"}"""))
                .andExpect(status().isForbidden());

        mvc.perform(json(post("/api/pedidos/" + pedido + "/avaliar"), banco, """
                {"resultado":"POSITIVO","justificativa":"Renda compatível"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.parecer.agenteNome").value("Banco Aliança"));

        mvc.perform(json(post("/api/pedidos/" + pedido + "/contrato"), agente, """
                {"tipo":"BANCO","valorTotal":900}"""))
                .andExpect(status().isUnprocessableContent());

        mvc.perform(json(post("/api/pedidos/" + pedido + "/contrato"), agente, """
                {"tipo":"CLIENTE","valorTotal":900,
                 "credito":{"bancoId":%d,"valor":900,"taxaJurosMensal":1.5,"parcelas":12}}""".formatted(bancoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.credito.bancoNome").value("Banco Aliança"))
                .andExpect(jsonPath("$.automovel.proprietarioPerfil").value("CLIENTE"))
                .andExpect(jsonPath("$.automovel.alugavel").value(false));

        mvc.perform(get("/api/pedidos/" + pedido).header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(jsonPath("$.status").value("CONTRATADO"));
        mvc.perform(get("/api/contratos").header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(post("/api/pedidos/" + pedido + "/cancelar").header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(status().isUnprocessableContent());

        String outro = novoClienteLogado();
        mvc.perform(json(post("/api/pedidos"), outro, corpoPedido(carro, 30, 35)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void creditoSoPodeSerConcedidoPorBanco() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        int empresaId = JsonPath.read(mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + agente))
                .andReturn().getResponse().getContentAsString(), "$.id");
        int pedido = novoPedido(novoClienteLogado(), novoCarro(agente), 10, 12);
        aprovar(pedido, agente);

        mvc.perform(json(post("/api/pedidos/" + pedido + "/contrato"), agente, """
                {"tipo":"EMPRESA","valorTotal":500,
                 "credito":{"bancoId":%d,"valor":500,"taxaJurosMensal":1,"parcelas":3}}""".formatted(empresaId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("O crédito só pode ser concedido por um banco agente."));
    }

    @Test
    void automovelNaoPodeSerContratadoEmPeriodoSobreposto() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        int carro = novoCarro(agente);
        int primeiro = novoPedido(novoClienteLogado(), carro, 10, 20);
        int segundo = novoPedido(novoClienteLogado(), carro, 15, 25);
        aprovar(primeiro, agente);
        aprovar(segundo, agente);

        mvc.perform(json(post("/api/pedidos/" + primeiro + "/contrato"), agente, """
                {"tipo":"EMPRESA","valorTotal":900}""")).andExpect(status().isCreated());
        mvc.perform(json(post("/api/pedidos/" + segundo + "/contrato"), agente, """
                {"tipo":"EMPRESA","valorTotal":900}"""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("O automóvel já está contratado neste período."));
    }

    @Test
    void clienteNaoAcessaPedidoDeOutroCliente() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        int pedido = novoPedido(novoClienteLogado(), novoCarro(agente), 10, 12);
        String intruso = novoClienteLogado();

        mvc.perform(get("/api/pedidos/" + pedido).header(HttpHeaders.AUTHORIZATION, "Bearer " + intruso))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/pedidos/" + pedido + "/cancelar").header(HttpHeaders.AUTHORIZATION, "Bearer " + intruso))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/pedidos").header(HttpHeaders.AUTHORIZATION, "Bearer " + intruso))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void clienteModificaECancelaEnquantoEmAnalise() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        String cliente = novoClienteLogado();
        int carro = novoCarro(agente);
        int pedido = novoPedido(cliente, carro, 10, 12);

        mvc.perform(json(put("/api/pedidos/" + pedido), cliente, corpoPedido(carro, 20, 25)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataFim").value(LocalDate.now().plusDays(25).toString()));
        mvc.perform(post("/api/pedidos/" + pedido + "/cancelar").header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(json(put("/api/pedidos/" + pedido), cliente, corpoPedido(carro, 20, 26)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void pedidoAprovadoSoPodeSerModificadoPeloAgente() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        String cliente = novoClienteLogado();
        int carro = novoCarro(agente);
        int pedido = novoPedido(cliente, carro, 10, 12);
        aprovar(pedido, agente);

        mvc.perform(json(put("/api/pedidos/" + pedido), cliente, corpoPedido(carro, 20, 25)))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(json(put("/api/pedidos/" + pedido), agente, corpoPedido(carro, 20, 25)))
                .andExpect(status().isOk());
    }

    @Test
    void parecerNegativoReprovaEEncerraOPedido() throws Exception {
        String agente = logar("locadora", "senha-de-teste");
        int pedido = novoPedido(novoClienteLogado(), novoCarro(agente), 10, 12);

        mvc.perform(json(post("/api/pedidos/" + pedido + "/avaliar"), agente, """
                {"resultado":"NEGATIVO","justificativa":"Renda insuficiente"}"""))
                .andExpect(jsonPath("$.status").value("REPROVADO"));
        mvc.perform(json(post("/api/pedidos/" + pedido + "/contrato"), agente, """
                {"tipo":"EMPRESA","valorTotal":900}"""))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void apenasOProprietarioAlteraOAutomovel() throws Exception {
        String locadora = logar("locadora", "senha-de-teste");
        String banco = logar("banco", "senha-de-teste");
        int carro = novoCarro(locadora);

        mvc.perform(delete("/api/automoveis/" + carro).header(HttpHeaders.AUTHORIZATION, "Bearer " + banco))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/automoveis/" + carro).header(HttpHeaders.AUTHORIZATION, "Bearer " + locadora))
                .andExpect(status().isNoContent());
    }


    private static String novoCpf() {
        String cpf = String.valueOf(PROXIMO_CPF.getAndIncrement());
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++) {
                soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
            }
            cpf += (soma * 10 % 11) % 10;
        }
        return cpf;
    }

    private ResultActions cadastrarCliente(String login, String cpf) throws Exception {
        return mvc.perform(json(post("/api/clientes"), null, """
                {"nome":"Cliente Teste","login":"%s","senha":"%s","rg":"%s","cpf":"%s","endereco":"Rua A",
                 "profissao":"Dev","rendimentos":[{"entidadeEmpregadora":"ACME","valor":5000.50}]}"""
                .formatted(login, SENHA, login.substring(0, 12), cpf)));
    }

    private String novoClienteLogado() throws Exception {
        String login = "cli-" + UUID.randomUUID();
        cadastrarCliente(login, novoCpf()).andExpect(status().isCreated());
        return logar(login, SENHA);
    }

    private String logar(String login, String senha) throws Exception {
        String corpo = mvc.perform(json(post("/api/auth/login"), null, """
                {"login":"%s","senha":"%s"}""".formatted(login, senha)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    private int novoCarro(String agente) throws Exception {
        int n = PROXIMO_CARRO.incrementAndGet();
        String corpo = mvc.perform(json(post("/api/automoveis"), agente, """
                {"matricula":"T-%d","ano":2024,"marca":"Marca","modelo":"Modelo","placa":"TST%04d"}""".formatted(n, n)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private int novoPedido(String cliente, int carro, int inicio, int fim) throws Exception {
        String corpo = mvc.perform(json(post("/api/pedidos"), cliente, corpoPedido(carro, inicio, fim)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private void aprovar(int pedido, String agente) throws Exception {
        mvc.perform(json(post("/api/pedidos/" + pedido + "/avaliar"), agente, """
                {"resultado":"POSITIVO","justificativa":"Ok"}""")).andExpect(status().isOk());
    }

    private static String corpoPedido(int carro, int inicio, int fim) {
        return """
                {"automovelId":%d,"dataInicio":"%s","dataFim":"%s"}"""
                .formatted(carro, LocalDate.now().plusDays(inicio), LocalDate.now().plusDays(fim));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String token, String corpo) {
        if (token != null) {
            req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }
}
