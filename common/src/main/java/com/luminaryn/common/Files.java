package com.luminaryn.common;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Files {
    public static String getExtension(final String filename) {
        Optional extIs = Optional.ofNullable(filename)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String f) {
                        return f.contains(".");
                    }
                })
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String f) {
                        return f.substring(filename.lastIndexOf(".") + 1);
                    }
                });
        if (extIs.isPresent()) {
            return ((String)extIs.get()).toUpperCase();
        }
        else {
            return "";
        }
    }
}
