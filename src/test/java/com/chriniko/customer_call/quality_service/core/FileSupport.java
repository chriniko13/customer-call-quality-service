package com.chriniko.customer_call.quality_service.core;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileSupport {

    public static String getResource(String name) {
        try {
            URI uri = FileSupport.class.getClassLoader().getResource(name).toURI();
            Path path = Paths.get(uri);
            return Files.lines(path).collect(Collectors.joining());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
