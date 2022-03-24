package com.ssonsh.hostchange.register;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalLinuxDomainRegister implements DomainRegister {
    @SneakyThrows
    @Override
    public void register(String schemaName) {
        log.info("linux register -> {}", schemaName);
    }
}
