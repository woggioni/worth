package net.woggioni.wson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Foo {

    @TempDir
    Path tempDir;

    @BeforeEach
    void beforeEach() {
        Assertions.assertTrue(Files.isDirectory(this.tempDir));
    }

    @Test
    void throwsErrorWhenTargetFileExists() throws IOException {
        Path output = Files.createFile(
                tempDir.resolve("output.txt")
        );
    }

}
