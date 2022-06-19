package com.geekbrains.cloud;

import lombok.Data;

@Data
public class PathUpRequest implements CloudMessage {

    private final String pathUp;
}
