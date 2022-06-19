package com.geekbrains.cloud;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListFiles implements CloudMessage {

    private final List<String> files;
    private final String path;
    public ListFiles(Path path) throws IOException {
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        this.path= String.valueOf(path);

    }

    public String getPath() {
        return path;
    }
}
