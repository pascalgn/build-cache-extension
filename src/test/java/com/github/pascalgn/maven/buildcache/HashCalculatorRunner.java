package com.github.pascalgn.maven.buildcache;

import java.io.File;
import java.util.Collections;

public class HashCalculatorRunner {
    public static void main(String[] args) {
        File directory = new File(args[0]);
        byte[] hash = HashCalculator.hash(directory, Collections.emptyList());
        System.out.println(HashCalculator.toString(hash));
    }
}
