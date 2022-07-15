package fr.jmini.utils.mvnutils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Objects;

public class Maven {

    public static final String MAVEN_CENTRAL_BASE_URL = "https://repo.maven.apache.org/maven2/";

    public static final String JAR_EXTENSION = ".jar";
    public static final String POM_EXTENSION = ".pom";

    public static String jarMavenCentralUrl(MavenArtifact artifact) {
        return mavenCentralUrl(artifact, JAR_EXTENSION);
    }

    public static String pomMavenCentralUrl(MavenArtifact artifact) {
        return mavenCentralUrl(artifact, POM_EXTENSION);
    }

    public static String mavenCentralUrl(MavenArtifact artifact, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(MAVEN_CENTRAL_BASE_URL);
        sb.append(subPathInMavenRepo(artifact, extension));
        return sb.toString();
    }

    public static Path fileInMavenRepository(Path repository, MavenArtifact artifact, String extension) {
        return repository.resolve(subPathInMavenRepo(artifact, extension));
    }

    public static Path jarFileInMavenRepository(Path repository, MavenArtifact artifact) {
        return repository.resolve(subPathInMavenRepo(artifact, JAR_EXTENSION));
    }

    public static Path pomFileInMavenRepository(Path repository, MavenArtifact artifact) {
        return repository.resolve(subPathInMavenRepo(artifact, POM_EXTENSION));
    }

    public static String jarSubPathInMavenRepo(MavenArtifact artifact) {
        return subPathInMavenRepo(artifact, JAR_EXTENSION);
    }

    public static String pomSubPathInMavenRepo(MavenArtifact artifact) {
        return subPathInMavenRepo(artifact, POM_EXTENSION);
    }

    public static String subPathInMavenRepo(MavenArtifact artifact, String extension) {
        // See https://github.com/eclipse/aether-core/blob/aether-0.9.1.v20140329/aether-util/src/main/java/org/eclipse/aether/util/repository/layout/MavenDefaultLayout.java#L42
        StringBuilder sb = new StringBuilder();
        sb.append(artifact.getGroupId()
                .replace('.', '/'));
        sb.append('/');
        sb.append(artifact.getArtifactId());
        sb.append('/');
        sb.append(artifact.getVersion());
        sb.append('/');
        sb.append(artifact.getArtifactId());
        sb.append('-');
        sb.append(artifact.getVersion());
        if (artifact.getClassifier() != null) {
            sb.append('-');
            sb.append(artifact.getClassifier());
        }
        sb.append(extension);
        return sb.toString();
    }

    public static void writeArmoredFiles(Path repository, MavenArtifact artifact, String extension, Algorithm... algorithms) throws IOException {
        byte[] bytes = readFileBytes(repository, artifact, extension);
        writeArmoredFiles(repository, artifact, extension, bytes, algorithms);
    }

    private static byte[] readFileBytes(Path repository, MavenArtifact artifact, String extension) throws IOException {
        Path file = fileInMavenRepository(repository, artifact, extension);
        if (!Files.exists(file)) {
            throw new RuntimeException("Could not find file at the expected location: " + file.toAbsolutePath());
        }
        return Files.readAllBytes(file);
    }

    public static void writeArmoredFiles(Path repository, MavenArtifact artifact, String extension, byte[] bytes, Algorithm... algorithms) {
        for (Algorithm algorithm : algorithms) {
            String hash = calculateHash(bytes, algorithm);
            writeFileToRepository(repository, artifact, extension + algorithm.getExtension(), hash);
        }
    }

    public static Path writeFileToRepository(Path repository, MavenArtifact artifact, String extension, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return writeFileToRepository(repository, artifact, extension, bytes);
    }

    public static Path writeFileToRepository(Path repository, MavenArtifact artifact, String extension, byte[] bytes) {
        Path outputFile = fileInMavenRepository(repository, artifact, extension);
        try {
            Files.createDirectories(outputFile.getParent());
            Files.write(outputFile, bytes);
            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException("Could not write the file corresponding to " + artifact + " with extension '" + extension + "'", e);
        }
    }

    public static void writeFileToRepositoryWithArmoredFiles(Path repository, MavenArtifact artifact, String extension, String content, Algorithm... algorithms) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        writeFileToRepositoryWithArmoredFiles(repository, artifact, extension, bytes, algorithms);
    }

    public static void writeFileToRepositoryWithArmoredFiles(Path repository, MavenArtifact artifact, String extension, Path file, Algorithm... algorithms) {
        try {
            byte[] bytes = Files.readAllBytes(file);
            writeFileToRepositoryWithArmoredFiles(repository, artifact, extension, bytes, algorithms);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + file, e);
        }
    }

    public static void writeFileToRepositoryWithArmoredFiles(Path repository, MavenArtifact artifact, String extension, byte[] bytes, Algorithm... algorithms) {
        writeFileToRepository(repository, artifact, extension, bytes);
        writeArmoredFiles(repository, artifact, extension, bytes, algorithms);
    }

    public static void verifyArmoredFiles(Path repository, MavenArtifact artifact, String extension, Algorithm... algorithms) throws IOException {
        byte[] bytes = readFileBytes(repository, artifact, extension);
        for (Algorithm algorithm : algorithms) {
            String expectedHash = calculateHash(bytes, algorithm);
            String currentHash = new String(readFileBytes(repository, artifact, extension + algorithm.getExtension()), StandardCharsets.UTF_8);
            if (!Objects.equals(expectedHash, currentHash)) {
                throw new RuntimeException("The " + algorithm + " hash in the armored file for " + artifact + " with extension '" + extension + "' does not match the calculated hash");
            }
        }
        writeArmoredFiles(repository, artifact, extension, bytes, algorithms);
    }

    public static String calculateHash(byte[] bytes, Algorithm algorithm) {
        MessageDigest digest = algorithm.getMessageDigest();
        byte[] encodedhash = digest.digest(bytes);
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private Maven() {
    }
}
