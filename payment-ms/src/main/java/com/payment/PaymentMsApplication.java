package com.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.payment", "ccf.ccf"})
public class PaymentMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMsApplication.class, args);
    }

}
