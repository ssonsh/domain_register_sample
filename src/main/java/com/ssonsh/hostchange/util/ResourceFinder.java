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
