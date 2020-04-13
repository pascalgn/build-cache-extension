package com.github.pascalgn.maven.buildcache;

import org.apache.maven.cli.MavenCli;

import java.io.File;

public class ExtensionRunner {
    public static void main(String[] args) {
        String root = new File("src/it/multi-module").getAbsolutePath();
        System.setProperty("maven.multiModuleProjectDirectory", root);
        new MavenCli().doMain(new String[]{"clean", "install"}, root, null, null);
    }
}
