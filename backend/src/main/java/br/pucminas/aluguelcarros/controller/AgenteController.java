package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.AgenteRequest;
import br.pucminas.aluguelcarros.dto.AgenteResponse;
import br.pucminas.aluguelcarros.model.TipoAgente;
import br.pucminas.aluguelcarros.service.AgenteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agentes")
@PreAuthorize("hasRole('AGENTE')")
public class AgenteController {

    private final AgenteService service;

    public AgenteController(AgenteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgenteResponse cadastrar(@Valid @RequestBody AgenteRequest request) {
        return service.cadastrar(request);
    }

    @GetMapping
    public List<AgenteResponse> listar(@RequestParam(required = false) TipoAgente tipo) {
        return service.listar(tipo);
    }
}
