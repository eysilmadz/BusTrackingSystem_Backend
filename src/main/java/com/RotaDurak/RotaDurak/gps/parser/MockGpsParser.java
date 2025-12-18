package com.RotaDurak.RotaDurak.gps.parser;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MockGpsParser {
    public Map<String, String> parse(String raw) {
        return Arrays.stream(raw.split(","))
                .map(kv -> kv.split(":"))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(
                        arr -> arr[0].trim(),
                        arr -> arr[1].trim()
                ));
    }
}


