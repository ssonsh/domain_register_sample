package com.ssonsh.hostchange.register;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
// LOCAL_WINDOW
//@SpringBootTest(properties = {"register.env=LOCAL_WINDOW"})

// LOCAL_LINUX
// @SpringBootTest(properties = {"register.env=LOCAL_LINUX"})

// AWS
@SpringBootTest(properties = {"register.env=AWS"})
class DomainRegisterTest {
    @Autowired
    private DomainRegister domainRegister;

    @Test
    void test01(){
        log.info("domainRegister class = {}", domainRegister.getClass());
        domainRegister.register("ssh001");
    }
}