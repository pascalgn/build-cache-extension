package com.github.pascalgn.maven.buildcache;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

@Component(role = BuildCache.class)
public class DefaultBuildCache implements BuildCache {
    @Requirement
    private Logger logger;

    private File cacheDirectory;
    private Map<MavenProject, byte[]> hashes;
    private Set<MavenProject> cached;

    @Override
    public void initialize(MavenSession session) {
        cacheDirectory = new File(session.getTopLevelProject().getBasedir(), ".cache");
        hashes = new HashMap<>();
        cached = new HashSet<>();

        for (MavenProject project : session.getProjects()) {
            List<byte[]> hashList = new ArrayList<>();
            hashList.add(HashCalculator.hash(project.getModel()));
            hashList.add(HashCalculator.hash(project.getBasedir(), excludes(project)));

            for (Entry<String, MavenProject> entry : new TreeMap<>(project.getProjectReferences()).entrySet()) {
                byte[] hash = hashes.get(entry.getValue());
                if (hash == null) {
                    throw new IllegalStateException("Could not get project reference hash: " + entry.getKey());
                }
                hashList.add(hash);
            }

            byte[] hash = HashCalculator.combine(hashList);
            hashes.put(project, hash);

            logger.debug("Project hash " + (project.getGroupId() + ":" + project.getArtifactId()) + " = "
                    + HashCalculator.toString(hash));

            File cache = cacheFile(project.getArtifact(), hash);
            if (cache.exists()) {
                cached.add(project);

                project.getArtifact().setFile(cache);
                project.getArtifact().setResolved(true);
            }
        }
    }

    private List<File> excludes(MavenProject project) {
        List<File> list = new ArrayList<>();
        list.add(cacheDirectory);
        list.add(new File(project.getBuild().getDirectory()));
        for (String module : project.getModules()) {
            list.add(new File(project.getBasedir(), module));
        }
        return list;
    }

    @Override
    public void cache(MavenProject project) {
        File artifactFile = project.getArtifact().getFile();
        if (artifactFile == null) {
            if (project.getArtifact().getType().equals("pom")) {
                artifactFile = project.getFile();
            } else {
                logger.debug("No artifact file: " + project);
                return;
            }
        }

        mkdir(cacheDirectory);

        byte[] hash = hashes.get(project);
        if (hash == null) {
            throw new IllegalStateException("No hash calculated: " + project);
        }

        File cache = cacheFile(project.getArtifact(), hash);
        if (cache.exists()) {
            return;
        }

        mkdir(cache.getParentFile());

        try {
            Files.copy(artifactFile.toPath(), cache.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Could not copy file to cache: " + project, e);
        }

        logger.debug("Cached project: " + (project.getGroupId() + ":" + project.getArtifactId()) + " = " + hash);
    }

    private File cacheFile(Artifact artifact, byte[] hash) {
        String directory = artifact.getGroupId() + "_" + artifact.getArtifactId() + "_" + artifact.getVersion()
                + (artifact.hasClassifier() ? "_" + artifact.getClassifier() : "");
        String file = HashCalculator.toString(hash) + "." + artifact.getType();
        return new File(cacheDirectory + File.separator + directory + File.separator + file);
    }

    private static void mkdir(File directory) {
        if (!directory.exists() && !directory.mkdir() && !directory.exists()) {
            throw new IllegalStateException("Could not create directory: " + directory);
        }
    }

    @Override
    public boolean isCached(MavenProject project) {
        return cached.contains(project);
    }
}
