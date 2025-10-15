package io.github.tasoula.intershop.controller;


import io.github.tasoula.intershop.exceptions.PaymentException;
import io.github.tasoula.intershop.exceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@ControllerAdvice
public class ExceptionController {

    // Обработка 404
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleNoSuchElementException(NoSuchElementException ex, Model model) {
        return Mono.just("exceptions/not-found.html")
                .doOnNext(item -> {
                    model.addAttribute("exception", ex);
                });
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        return Mono.just("exceptions/not-found.html")
                .doOnNext(item -> {
                    model.addAttribute("exception", ex);
                });
    }

    // Обработка 400
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        return Mono.just("exceptions/invalid-arguments.html")
                .doOnNext(item -> {
            model.addAttribute("exception", e);
        });
    }

    @ExceptionHandler({PaymentException.class})
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public Mono<String> handlePaymentException(PaymentException e, Model model) {
        return Mono.just("exceptions/payment-exception.html")
                .doOnNext(item -> {
                    model.addAttribute("exception", e);
                });
    }


    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<String> handleException(Exception e, Model model) {
        return Mono.just("exceptions/oops.html")
                .doOnNext(item -> {
                    model.addAttribute("exception", e);
                });
    }
}

