package com.geekbrains.cloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ListF implements CloudMessage {
    private final List<String> files;
    private final String path;


    public ListF(Path path) throws IOException {
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        this.path= String.valueOf(path);

    }

    public List<String> getFiles() {
        return files;
    }

    public String getPath() {
        return path;
    }
}
