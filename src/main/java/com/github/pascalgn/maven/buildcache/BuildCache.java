package com.github.pascalgn.maven.buildcache;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public interface BuildCache {
    void initialize(MavenSession mavenSession);

    void cache(MavenProject mavenProject);

    boolean isCached(MavenProject mavenProject);
}
