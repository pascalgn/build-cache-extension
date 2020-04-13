package com.github.pascalgn.maven.buildcache;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HashCalculator {
    public static byte[] hash(File directory, List<File> excludes) {
        SortedMap<Path, byte[]> hashes;
        List<Path> excludePaths = excludes.stream().map(File::toPath).collect(Collectors.toList());
        try {
            hashes = calculateHashes(directory.toPath(), excludePaths);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to hash directory: " + directory, e);
        }
        return hashes.values().stream().reduce(HashCalculator::combine)
                .orElseGet(() -> createMessageDigest().digest());
    }

    private static SortedMap<Path, byte[]> calculateHashes(Path directory, List<Path> excludes) throws IOException {
        SortedMap<Path, byte[]> hashes = new TreeMap<>();
        Stream<Path> stream = Files.find(directory, Integer.MAX_VALUE, (path, attributes) -> {
            if (!attributes.isRegularFile()) {
                return false;
            }

            for (Path exclude : excludes) {
                if (path.startsWith(exclude)) {
                    return false;
                }
            }

            return true;
        });
        Iterator<Path> iterator = stream.iterator();
        while (iterator.hasNext()) {
            Path path = iterator.next();
            hashes.put(path, createMessageDigest().digest(Files.readAllBytes(path)));
        }
        return hashes;
    }

    public static byte[] hash(Model model) {
        model.setProperties(new SortedProperties(model.getProperties()));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            new MavenXpp3Writer().write(output, model);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return createMessageDigest().digest(output.toByteArray());
    }

    public static byte[] combine(byte[] hash1, byte[] hash2) {
        MessageDigest digest = createMessageDigest();
        digest.update(hash1);
        return digest.digest(hash2);
    }

    public static byte[] combine(List<byte[]> hashes) {
        MessageDigest digest = createMessageDigest();
        for (byte[] hash : hashes) {
            digest.update(hash);
        }
        return digest.digest();
    }

    private static MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create MessageDigest", e);
        }
    }

    public static String toString(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash).toLowerCase();
    }

    private static class SortedProperties extends Properties {
        private static final long serialVersionUID = 1L;

        public SortedProperties(Properties properties) {
            putAll(properties);
        }

        public Set<Object> keySet() {
            return new TreeSet<>(super.keySet());
        }
    }
}
