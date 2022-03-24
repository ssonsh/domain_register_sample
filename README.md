# domain_register_sample

# 도메인 등록 기능

### 요구사항

- 제품 계약을 하게 되면 제품을 구매한 고객사를 위한 도메인을 생성해야 한다.
- 프로퍼티 정보를 바탕으로 Window 환경, Linux 환경, AWS 환경에 따라 도메인을 등록해야 한다.

현재 개발 환경은 `**LOCAL_WINDOW**` 프로퍼티로 처리한다.

- Local 환경에서 PC의 hosts 정보를 변경하여 도메인을 추가하도록 한다.

---

우선 hosts 정보를 변경하는 bat 파일을 생성하자.

**add_hosts.bat**

```java
echo 127.0.0.1 my_domain >> %SystemRoot%\system32\drivers\etc\hosts
```

위 bat 파일을 실행한 후 hosts 파일을 보게되면

가장 하단에 “127.0.0.1” IP에 대해 my_domain 이 추가되어 있는 것을 볼 수 있다.

하지만 현재 요구사항은 고객사 도메인을 등록해야 한다. 

→ 위에서 생성한 .bat 파일을 그대로 등록하게 되면 고객사마다의 도메인을 등록할 수 없다.

bat 파일을 생성하지만

- 읽어와서 도메인영역을 원하는 값(고객사 도메인)으로 변경하여 처리할 수 있도록 한다.

**add_hosts.bat**

```java
echo 127.0.0.1 **tenant**.my_domain >> %SystemRoot%\system32\drivers\etc\hosts
```

위와 같이 정의하였고 Command 명령을 수행할 때 명령을 Replace 하여 도메인 영역을 고객사 도메인으로 변경하여 처리한다.

- ex. “tenant” 명칭을 “고객사 ssonsh” 로 변경한다
    
    <aside>
    🎈 echo 127.0.0.1 **`ssonsh`**.my_domain >> %SystemRoot%\system32\drivers\etc\hosts
    
    </aside>
    

**필요한 기능**

리소스 정보 조회 → 명령 Command 로 Parse (String)

```java
new ClassPathResource(resourcePath);
new FileUrlResource(resourcePath);

```

명령 Command 수행 (환경별로 상이 → 현재 환경에선 Windows 로 가정한다)

```java
Runtime.getRuntime().exec("cmd /c " + command);
```

---

리소스 경로를 매개변수로 받아와 리소스를 Read 하고 반환하는 UtilityClass

**ResourceFinder.java**

```java
package com.ssonsh.hostchange.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

@UtilityClass
public class ResourceFinder {

    @SneakyThrows
    public static Resource findResource(String resourcePath){
        Resource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            resource = new FileUrlResource(resourcePath);
        }
        return resource;
    }
}
```

**DomainRegister를 구성한다.**

- LocalWindow, LocalLinux, AWS 와 같이 Concrete Register Class들이 구현될 수 있으며
- 현재 환경에선 LocalWindow 에 한정하여 개발하고 테스트한다.

![image](https://user-images.githubusercontent.com/18654358/160028353-94747610-6d48-4433-a549-971c12a9705f.png)


DomainRegister.java

```java
package com.ssonsh.hostchange.register;

public interface DomainRegister {
    void register(String schemaName);
}
```

**`LocalWindowDomainRegister`.java**

```java
package com.ssonsh.hostchange.register;

import com.ssonsh.hostchange.util.ResourceFinder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class LocalWindowsDomainRegister implements DomainRegister {

    @SneakyThrows
    @Override
    public void register(String schemaName) {
        executeCommands(findCommandByBatFileName(schemaName));
    }

    private void executeCommands(List<String> commands) {
        commands.forEach(command -> {
            try {
                Runtime.getRuntime().exec("cmd /c " + command);
                log.info("[ADD Complete] {}", command);
            }
            catch (IOException e) {
                log.error(e.getMessage());
            }
        });
    }

    private List<String> findCommandByBatFileName(String schemaName) {
        List<String> commands = new ArrayList<>();
        try (Stream<String> lines = Files.lines(ResourceFinder.findResource("/bat/hosts_add.bat").getFile().toPath(),
                                                StandardCharsets.UTF_8)) {
            lines.forEach(line -> commands.add(line.replaceAll("tenant", schemaName)));
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }

        commands.forEach(command -> log.info("[command] {}", command));
        return commands;
    }

}
```

LocalLinuxDomainRegister.java

```java
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
```

AWSDomainRegister.java

```java
package com.ssonsh.hostchange.register;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AWSDomainRegister implements DomainRegister {
    @SneakyThrows
    @Override
    public void register(String schemaName) {
        log.info("aws route53 register -> {}", schemaName);
    }
}
```

---

프로퍼티 설정 정보에 따라 등록되는 스프링 빈을 관리하도록 한다.

**DomainRegisterConfig.java**

```java
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
```

```java
package com.ssonsh.hostchange.config;

public enum RegisterEnvType {
    LOCAL_WINDOW,
    LOCAL_LINUX,
    AWS
}
```

- 프로퍼티는 “register.env” 라는 Key로 Value 가 관리 될 것을 가정한다.
    
    ```java
    register:
      env: LOCAL_WINDOW
    /////////////////////////
    register:
      env: LOCAL_LINUX
    /////////////////////////
    register:
      env: LOCAL_AWS
    ```
    

---

테스트 코드

DomainRegisterTest.java

- SpringBootTest 애노테이션의 properties 값을 변경해가며 환경별로 동작하는 결과를 테스트 한다.
