package br.facens.horascomplementares.controller;

import br.facens.horascomplementares.domain.exception.AlunoInexistenteException;
import br.facens.horascomplementares.domain.exception.CertificadoImutavelException;
import br.facens.horascomplementares.domain.exception.CertificadoInexistenteException;
import br.facens.horascomplementares.domain.exception.CategoriaInexistenteException;
import br.facens.horascomplementares.domain.exception.NegocioException;
import br.facens.horascomplementares.domain.exception.TransicaoStatusInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class RestExceptionHandler {

    public record ErroResponse(OffsetDateTime timestamp, int status, String erro, String mensagem) {
        static ErroResponse de(HttpStatus status, String mensagem) {
            return new ErroResponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), mensagem);
        }
    }

    @ExceptionHandler({AlunoInexistenteException.class, CategoriaInexistenteException.class,
            CertificadoInexistenteException.class})
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(NegocioException ex) {
        return responder(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({TransicaoStatusInvalidaException.class, CertificadoImutavelException.class})
    public ResponseEntity<ErroResponse> tratarConflito(NegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraDeNegocio(NegocioException ex) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Requisicao invalida");
        return ResponseEntity.badRequest().body(ErroResponse.de(HttpStatus.BAD_REQUEST, mensagem));
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, NegocioException ex) {
        return ResponseEntity.status(status).body(ErroResponse.de(status, ex.getMessage()));
    }
}
