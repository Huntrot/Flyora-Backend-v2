package org.example.flyora_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {

    @Bean
    public PayOS payOS() {
        return new PayOS(
            "4370621a-be81-4a53-bdda-6046763f433a",
            "3dc81b4b-255a-416a-a007-881821442b20",      
            "29fbe7cb4109b098634b0a3dd62fba43abfda3a8460431ec6daa08c8e56bc27a"
        );
    }
}
