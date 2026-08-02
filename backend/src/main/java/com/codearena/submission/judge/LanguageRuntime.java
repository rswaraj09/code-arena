package com.codearena.submission.judge;

import com.codearena.submission.Language;

import java.util.Map;

/**
 * Maps each supported language to the Docker image that runs it and the
 * exact shell commands to compile (if needed) and execute a submission.
 * Mirrors docs/docker-sandbox-guide.md — build these images with
 * `docker build -t codearena/<lang> docker/<lang>` before running the judge.
 */
public final class LanguageRuntime {

    public record Runtime(String dockerImage, String sourceFileName, String compileCommand, String runCommand) {
        public boolean requiresCompilation() {
            return compileCommand != null && !compileCommand.isBlank();
        }
    }

    private static final Map<Language, Runtime> RUNTIMES = Map.of(
            Language.JAVA, new Runtime(
                    "codearena/java",
                    "Main.java",
                    "javac Main.java",
                    "java -Xmx%dm Main"
            ),
            Language.PYTHON, new Runtime(
                    "codearena/python",
                    "main.py",
                    null,
                    "python3 main.py"
            ),
            Language.CPP, new Runtime(
                    "codearena/cpp",
                    "main.cpp",
                    "g++ -O2 -o main main.cpp",
                    "./main"
            ),
            Language.C, new Runtime(
                    "codearena/c",
                    "main.c",
                    "gcc -O2 -o main main.c",
                    "./main"
            ),
            Language.JAVASCRIPT, new Runtime(
                    "codearena/javascript",
                    "main.js",
                    null,
                    "node main.js"
            )
    );

    private LanguageRuntime() {
    }

    public static Runtime of(Language language) {
        Runtime runtime = RUNTIMES.get(language);
        if (runtime == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return runtime;
    }
}
