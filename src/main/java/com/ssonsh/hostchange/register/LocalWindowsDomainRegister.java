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
