package io.github.tasoula.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication
@ComponentScan(
        basePackages = {"io.github.tasoula.payment_service", "io.github.tasoula.server"},
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
