package fr.jmini.utils.mvnutils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class MavenTest {

    @Test
    void exampleMavenCentralUrlConstant() throws Exception {
        //tag::MavenCentralUrlConstant[]
        String url = Maven.MAVEN_CENTRAL_BASE_URL;
        //end::MavenCentralUrlConstant[]
        assertThat(url)
                .as("maven central url")
                .isEqualTo("https://repo.maven.apache.org/maven2/");
    }

    @Test
    void examplePomMavenCentralUrl() throws Exception {
        //tag::PomMavenCentralUrl[]
        MavenArtifact artifact = new MavenArtifact("fr.jmini.utils", "mvn-utils", "1.0.0");
        String url = Maven.pomMavenCentralUrl(artifact);
        //end::PomMavenCentralUrl[]
        assertThat(url)
                .as("url of the pom in maven central for " + artifact)
                .isEqualTo("https://repo.maven.apache.org/maven2/fr/jmini/utils/mvn-utils/1.0.0/mvn-utils-1.0.0.pom");
    }

    @Test
    void exampleJarMavenCentralUrl() throws Exception {
        //tag::JarMavenCentralUrl[]
        MavenArtifact artifact = new MavenArtifact("fr.jmini.utils", "mvn-utils", "1.0.0");
        String url = Maven.jarMavenCentralUrl(artifact);
        //end::JarMavenCentralUrl[]
        assertThat(url)
                .as("url of the jar in maven central for " + artifact)
                .isEqualTo("https://repo.maven.apache.org/maven2/fr/jmini/utils/mvn-utils/1.0.0/mvn-utils-1.0.0.jar");
    }

    @Test
    void exampleMavenCentralUrl() throws Exception {
        //tag::MavenCentralUrl[]
        MavenArtifact artifact = new MavenArtifact("fr.jmini.utils", "mvn-utils", "1.0.0");
        String url = Maven.mavenCentralUrl(artifact, ".jar");
        //end::MavenCentralUrl[]
        assertThat(url)
                .as("url of the armored ASCII file in maven central for " + artifact)
                .isEqualTo("https://repo.maven.apache.org/maven2/fr/jmini/utils/mvn-utils/1.0.0/mvn-utils-1.0.0.jar");
    }

    @Test
    void examplePomPathInRepository() throws Exception {
        //tag::PomPathInRepository[]
        Path repository = Paths.get("/tmp/repository");
        MavenArtifact artifact = new MavenArtifact("fr.jmini.utils", "mvn-utils", "1.0.0");
        Path pomFile = Maven.pomFileInMavenRepository(repository, artifact);
        //end::PomPathInRepository[]
        assertThat(pomFile)
                .as("path pom file of " + artifact)
                .extracting(Path::toString)
                .isEqualTo("/tmp/repository/fr/jmini/utils/mvn-utils/1.0.0/mvn-utils-1.0.0.pom");
    }

    @Test
    void exampleJarPathInRepository() throws Exception {
        //tag::JarPathInRepository[]
        Path repository = Paths.get("/tmp/repository");
        MavenArtifact artifact = new MavenArtifact("fr.jmini.utils", "mvn-utils", "1.0.0");
        Path jarFile = Maven.jarFileInMavenRepository(repository, artifact);
        //end::JarPathInRepository[]
        assertThat(jarFile)
                .as("sub-path of " + artifact)
                .extracting(Path::toString)
                .isEqualTo("/tmp/repository/fr/jmini/utils/mvn-utils/1.0.0/mvn-utils-1.0.0.jar");
    }

    @Test
    void examplePathInRepository() throws Exception {
        //tag::PathInRepository[]
        Path repository = Paths.get("/tmp/repository");
        MavenArtifact artifact = new MavenArtifact("fr.jmini.utils", "mvn-utils", "1.0.0");
        Path jarFile = Maven.fileInMavenRepository(repository, artifact, ".jar");
        //end::PathInRepository[]
        assertThat(jarFile)
                .as("sub-path of " + artifact)
                .extracting(Path::toString)
                .isEqualTo("/tmp/repository/fr/jmini/utils/mvn-utils/1.0.0/mvn-utils-1.0.0.jar");
    }

    @Test
    void exampleWritePomFileToRepository() throws Exception {
        Path repository = Files.createTempDirectory("test");
        //tag::WritePomFileToRepository[]
        MavenArtifact artifact = new MavenArtifact("fr.jmini.example", "tmp", "1.0-SNAPSHOT");
        String pomContent = ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "\n"
                + "    <groupId>fr.jmini.example</groupId>\n"
                + "    <artifactId>tmp</artifactId>\n"
                + "    <version>1.0-SNAPSHOT</version>\n"
                + "    <packaging>pom</packaging>\n"
                + "</project>";
        Maven.writeFileToRepository(repository, artifact, ".pom", pomContent);
        //end::WritePomFileToRepository[]
        Path pomFile = Maven.pomFileInMavenRepository(repository, artifact);
        assertThat(pomFile)
                .isRegularFile()
                .hasContent(pomContent);
    }

    @Test
    void exampleArmoredFilesInRepository() throws Exception {
        Path repository = Files.createTempDirectory("test");
        MavenArtifact a = new MavenArtifact("fr.jmini.utils", "example", "1.0-SNAPSHOT");
        String content = "Test";
        Path jarFile = Maven.writeFileToRepository(repository, a, ".jar", content);
        assertThat(jarFile)
                .isRegularFile()
                .isEqualTo(repository.resolve("fr/jmini/utils/example/1.0-SNAPSHOT/example-1.0-SNAPSHOT.jar"));

        //tag::ArmoredFilesInRepository[]
        MavenArtifact artifact = new MavenArtifact("fr.jmini.utils", "example", "1.0-SNAPSHOT");
        Path file = repository.resolve("fr/jmini/utils/example/1.0-SNAPSHOT/example-1.0-SNAPSHOT.jar");
        if (!Files.exists(file)) {
            fail(".jar file is supposed to exist");
        }

        Maven.writeArmoredFiles(repository, artifact, ".jar", Algorithm.MD_5, Algorithm.SHA_1, Algorithm.SHA_256, Algorithm.SHA_512); // <1>

        Path folder = file.getParent();
        if (!Files.exists(folder.resolve("example-1.0-SNAPSHOT.jar.md5"))) {
            fail(".jar.md5 file is supposed to exist");
        }
        if (!Files.exists(folder.resolve("example-1.0-SNAPSHOT.jar.sha1"))) {
            fail(".jar.sha1 file is supposed to exist");
        }
        if (!Files.exists(folder.resolve("example-1.0-SNAPSHOT.jar.sha256"))) {
            fail(".jar.sha256 file is supposed to exist");
        }
        if (!Files.exists(folder.resolve("example-1.0-SNAPSHOT.jar.sha512"))) {
            fail(".jar.sha512 file is supposed to exist");
        }
        Maven.verifyArmoredFiles(repository, artifact, ".jar", Algorithm.MD_5, Algorithm.SHA_1, Algorithm.SHA_256, Algorithm.SHA_512); // <2>
        //end::ArmoredFilesInRepository[]
    }

    @Test
    void testMavenCentralUrl() throws Exception {
        MavenArtifact a = new MavenArtifact("org.eclipse.platform", "org.eclipse.ant.core", "3.5.600");
        assertThat(Maven.mavenCentralUrl(a, ".jar"))
                .as("url of the jar in maven central for " + a)
                .isEqualTo("https://repo.maven.apache.org/maven2/org/eclipse/platform/org.eclipse.ant.core/3.5.600/org.eclipse.ant.core-3.5.600.jar");

        assertThat(Maven.mavenCentralUrl(a, ".pom"))
                .as("url of the pom in maven central for " + a)
                .isEqualTo("https://repo.maven.apache.org/maven2/org/eclipse/platform/org.eclipse.ant.core/3.5.600/org.eclipse.ant.core-3.5.600.pom");

        assertThat(Maven.mavenCentralUrl(a, ".jar.asc"))
                .as("url of the armored ASCII file of the jar in maven central for " + a)
                .isEqualTo("https://repo.maven.apache.org/maven2/org/eclipse/platform/org.eclipse.ant.core/3.5.600/org.eclipse.ant.core-3.5.600.jar.asc");
    }

    @Test
    void testWithQualifier() throws Exception {
        MavenArtifact a = new MavenArtifact("org.openapitools.openapistylevalidator", "openapi-style-validator-cli", "1.8", "all");
        assertThat(Maven.jarMavenCentralUrl(a))
                .as("url of the jar in maven central for " + a)
                .isEqualTo("https://repo.maven.apache.org/maven2/org/openapitools/openapistylevalidator/openapi-style-validator-cli/1.8/openapi-style-validator-cli-1.8-all.jar");
        assertThat(Maven.jarSubPathInMavenRepo(a))
                .as("sub path of the jar in maven central for " + a)
                .isEqualTo("org/openapitools/openapistylevalidator/openapi-style-validator-cli/1.8/openapi-style-validator-cli-1.8-all.jar");
    }

    @Test
    void testCalculateHash() throws Exception {
        byte[] string = "test".getBytes(StandardCharsets.UTF_8);

        String md5 = Maven.calculateHash(string, Algorithm.MD_5);
        assertThat(md5).isEqualTo("098f6bcd4621d373cade4e832627b4f6");

        String sha1 = Maven.calculateHash(string, Algorithm.SHA_1);
        assertThat(sha1).isEqualTo("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");

        String sha256 = Maven.calculateHash(string, Algorithm.SHA_256);
        assertThat(sha256).isEqualTo("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08");

        String sha512 = Maven.calculateHash(string, Algorithm.SHA_512);
        assertThat(sha512).isEqualTo("ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff");
    }

    @Test
    void testVerifyArmoredFiles() throws Exception {
        Path repository = Files.createTempDirectory("test");
        MavenArtifact a = new MavenArtifact("fr.jmini.utils", "tmp", "1.0-SNAPSHOT");
        String pomContent = ""
                + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
                + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "\n"
                + "    <groupId>fr.jmini.example</groupId>\n"
                + "    <artifactId>tmp</artifactId>\n"
                + "    <version>1.0-SNAPSHOT</version>\n"
                + "    <packaging>pom</packaging>\n"
                + "</project>";
        Path pomFile = Maven.writeFileToRepository(repository, a, ".pom", pomContent);
        assertThat(pomFile)
                .isRegularFile()
                .hasContent(pomContent);

        Path md5File = Maven.writeFileToRepository(repository, a, ".pom.md5", "1234wrong");
        assertThat(md5File)
                .isRegularFile()
                .hasContent("1234wrong");

        assertThatThrownBy(() -> Maven.verifyArmoredFiles(repository, a, ".pom", Algorithm.MD_5))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("The MD_5 hash in the armored file for MavenArtifact [groupId=fr.jmini.utils, artifactId=tmp, version=1.0-SNAPSHOT, classifier=null] with extension '.pom' does not match the calculated hash");

        assertThatThrownBy(() -> Maven.verifyArmoredFiles(repository, a, ".pom", Algorithm.SHA_1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Could not find file at the expected location:")
                .hasMessageEndingWith("fr/jmini/utils/tmp/1.0-SNAPSHOT/tmp-1.0-SNAPSHOT.pom.sha1");
    }

}
