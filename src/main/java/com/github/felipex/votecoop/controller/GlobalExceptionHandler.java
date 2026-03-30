package com.github.felipex.votecoop.controller;

import com.github.felipex.votecoop.dto.response.ItemSelecao;
import com.github.felipex.votecoop.dto.response.TelaSelecao;
import com.github.felipex.votecoop.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<TelaSelecao> handleApp(AppException ex) {
        return ResponseEntity
                .status(ex.tipo.status)
                .body(telaErro(ex.tipo.titulo, ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public TelaSelecao handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return telaErro("Método Não Permitido", "O método " + ex.getMethod() + " não é suportado para este endereço.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public TelaSelecao handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return telaErro("Parâmetro Inválido", "O parâmetro '" + ex.getName() + "' deve ser um número.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public TelaSelecao handleNotFound(NoResourceFoundException ex) {
        return telaErro("Página Não Encontrada", "O endereço acessado não existe.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public TelaSelecao handleNotReadable(HttpMessageNotReadableException ex) {
        String mensagem = "Valor inválido no campo 'opcao'. Use SIM ou NAO.";
        return telaErro("Dados Inválidos", mensagem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public TelaSelecao handleValidacao(MethodArgumentNotValidException ex) {
        List<ItemSelecao> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ItemSelecao(e.getDefaultMessage(), ""))
                .toList();
        return new TelaSelecao("Dados Inválidos", erros);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public TelaSelecao handleGenerico(Exception ex) {
        log.error("Erro inesperado", ex);
        return telaErro("Erro Interno", "Ocorreu um erro inesperado. Tente novamente.");
    }

    private TelaSelecao telaErro(String titulo, String mensagem) {
        return new TelaSelecao(titulo, List.of(new ItemSelecao(mensagem, "")));
    }
}
