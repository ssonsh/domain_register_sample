package com.ssonsh.hostchange.config;

import com.ssonsh.hostchange.register.AWSDomainRegister;
import com.ssonsh.hostchange.register.DomainRegister;
import com.ssonsh.hostchange.register.LocalLinuxDomainRegister;
import com.ssonsh.hostchange.register.LocalWindowsDomainRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class DomainRegisterConfig {

    private final Environment env;

    @Bean
    public DomainRegister domainRegister() {
        switch (registerEnv()) {
            case LOCAL_WINDOW:
                return new LocalWindowsDomainRegister();
            case LOCAL_LINUX:
                return new LocalLinuxDomainRegister();
            case AWS:
                return new AWSDomainRegister();
            default:
                return null;
        }
    }

    private RegisterEnvType registerEnv() {
        return RegisterEnvType.valueOf(env.getProperty("register.env"));
    }
}
