package br.pucminas.aluguelcarros.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public record Erro(String mensagem, Map<String, String> campos) {
        static Erro de(String mensagem) {
            return new Erro(mensagem, Map.of());
        }
    }

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<Erro> regraNegocio(RegraNegocioException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(Erro.de(e.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<Erro> naoEncontrado(RecursoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Erro.de(e.getMessage()));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<Erro> credenciais(CredenciaisInvalidasException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Erro.de(e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Erro> acessoNegado(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Erro.de("Você não tem permissão para esta operação."));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<Erro> conflitoConcorrencia(OptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Erro.de("O registro foi alterado por outro usuário. Recarregue e tente novamente."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Erro> integridade(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Erro.de("A operação viola uma restrição de dados (registro duplicado ou em uso)."));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new Erro("Dados inválidos.", campos));
    }
}
