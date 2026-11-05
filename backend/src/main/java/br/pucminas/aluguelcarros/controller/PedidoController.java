package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.ContratoRequest;
import br.pucminas.aluguelcarros.dto.ContratoResponse;
import br.pucminas.aluguelcarros.dto.ParecerRequest;
import br.pucminas.aluguelcarros.dto.PedidoRequest;
import br.pucminas.aluguelcarros.dto.PedidoResponse;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import br.pucminas.aluguelcarros.service.ContratoService;
import br.pucminas.aluguelcarros.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidos;
    private final ContratoService contratos;

    public PedidoController(PedidoService pedidos, ContratoService contratos) {
        this.pedidos = pedidos;
        this.contratos = contratos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest request, @AuthenticationPrincipal Jwt jwt) {
        return pedidos.criar(request, UsuarioAutenticado.de(jwt).id());
    }

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return pedidos.listar(UsuarioAutenticado.de(jwt));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidos.buscar(id, UsuarioAutenticado.de(jwt));
    }

    @PutMapping("/{id}")
    public PedidoResponse modificar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return pedidos.modificar(id, request, UsuarioAutenticado.de(jwt));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return pedidos.cancelar(id, UsuarioAutenticado.de(jwt));
    }

    @PostMapping("/{id}/avaliar")
    @PreAuthorize("hasRole('AGENTE')")
    public PedidoResponse avaliar(@PathVariable Long id, @Valid @RequestBody ParecerRequest request,
                                  @AuthenticationPrincipal Jwt jwt) {
        return pedidos.avaliar(id, request, UsuarioAutenticado.de(jwt).id());
    }

    @PostMapping("/{id}/contrato")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('AGENTE')")
    public ContratoResponse executarContrato(@PathVariable Long id, @Valid @RequestBody ContratoRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        return contratos.executar(id, request, UsuarioAutenticado.de(jwt).id());
    }

    @GetMapping("/{id}/contrato")
    public ContratoResponse buscarContrato(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return contratos.buscarPorPedido(id, UsuarioAutenticado.de(jwt));
    }
}
