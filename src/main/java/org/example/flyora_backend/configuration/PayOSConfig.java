package org.example.flyora_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {

    @Bean
    public PayOS payOS() {
        return new PayOS(
            "c76bb3a5-fb30-4643-a33d-e36d0f18f1b2",
            "b4be97a8-d346-4128-8494-22c8f931023f",      
            "0505c89b495c811311f0c02ad65f4ef0efbb8a5d2e4c2556fd2ca004086207c6"
        );
    }
}
