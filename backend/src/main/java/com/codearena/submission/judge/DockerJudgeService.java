package com.codearena.submission.judge;

import com.codearena.config.JudgeProperties;
import com.codearena.submission.Language;
import com.codearena.submission.Verdict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Executes one submission per call in a brand-new, disposable Docker
 * container. See docs/docker-sandbox-guide.md for the reasoning behind
 * every flag used here — this class is that guide's flags, wired into Java.
 *
 * Sentinel exit code 42 marks "compilation failed" so it can be told apart
 * from a genuine runtime crash without parsing stderr for language-specific
 * compiler wording.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DockerJudgeService implements JudgeService {

    private static final int COMPILE_FAILURE_EXIT_CODE = 42;
    private static final int TIMEOUT_EXIT_CODE = 124;
    private static final long CONTAINER_MEMORY_OVERHEAD_MB = 64; // headroom over the judged program's own limit

    private final JudgeProperties judgeProperties;

    @Override
    public JudgeResult execute(Language language, String code, String stdin, int timeLimitMs, int memoryLimitMb) {
        LanguageRuntime.Runtime runtime = LanguageRuntime.of(language);
        String submissionId = UUID.randomUUID().toString();
        Path workDir = Path.of(judgeProperties.workdir(), submissionId);

        try {
            Files.createDirectories(workDir);
            Files.writeString(workDir.resolve(runtime.sourceFileName()), code, StandardCharsets.UTF_8);
            Files.writeString(workDir.resolve("input.txt"), stdin == null ? "" : stdin, StandardCharsets.UTF_8);
            Files.writeString(workDir.resolve("run.sh"), buildRunScript(runtime, timeLimitMs, memoryLimitMb), StandardCharsets.UTF_8);

            return runContainer(submissionId, workDir, runtime, timeLimitMs);
        } catch (IOException e) {
            log.error("Failed to prepare submission workspace", e);
            return JudgeResult.of(Verdict.RUNTIME_ERROR, "", "Internal judge error while preparing the submission.", 0);
        } finally {
            cleanup(workDir);
        }
    }

    private String buildRunScript(LanguageRuntime.Runtime runtime, int timeLimitMs, int memoryLimitMb) {
        int timeoutSeconds = Math.max(1, (int) Math.ceil(timeLimitMs / 1000.0));
        String runCommand = runtime.runCommand().contains("%d")
                ? String.format(runtime.runCommand(), memoryLimitMb)
                : runtime.runCommand();

        StringBuilder script = new StringBuilder("#!/bin/sh\nset -e\n");
        if (runtime.requiresCompilation()) {
            script.append(runtime.compileCommand()).append(" 2> compile_err.txt\n");
            script.append("if [ $? -ne 0 ]; then cat compile_err.txt >&2; exit ")
                    .append(COMPILE_FAILURE_EXIT_CODE).append("; fi\n");
        }
        script.append("timeout ").append(timeoutSeconds).append("s ").append(runCommand).append(" < input.txt\n");
        return script.toString();
    }

    private JudgeResult runContainer(String submissionId, Path workDir, LanguageRuntime.Runtime runtime, int timeLimitMs) throws IOException {
        long containerMemoryMb = judgeProperties.memoryLimitMb() + CONTAINER_MEMORY_OVERHEAD_MB;

        List<String> command = new ArrayList<>(List.of(
                "docker", "run", "--rm",
                "--name", "submission-" + submissionId,
                "--memory=" + containerMemoryMb + "m",
                "--memory-swap=" + containerMemoryMb + "m",
                "--cpus=" + judgeProperties.cpuLimit(),
                "--pids-limit=" + judgeProperties.pidsLimit(),
                "--network", "none",
                "--read-only",
                "--tmpfs", "/tmp:rw,size=16m",
                "--cap-drop=ALL",
                "--security-opt=no-new-privileges",
                "-v", workDir.toAbsolutePath() + ":/submission:rw",
                "-w", "/submission",
                runtime.dockerImage(),
                "sh", "run.sh"
        ));

        File stdoutFile = workDir.resolve("stdout.txt").toFile();
        File stderrFile = workDir.resolve("stderr.txt").toFile();

        ProcessBuilder pb = new ProcessBuilder(command)
                .redirectOutput(stdoutFile)
                .redirectError(stderrFile);

        long startedAt = System.currentTimeMillis();
        Process process = pb.start();

        // Outer guard beyond the inner `timeout` — covers cases where the
        // container itself hangs (e.g. stuck pulling, cgroup weirdness)
        // rather than the judged program.
        boolean finished;
        try {
            finished = process.waitFor(judgeProperties.executionTimeoutSeconds() + 3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finished = false;
        }
        long runtimeMs = System.currentTimeMillis() - startedAt;

        if (!finished) {
            process.destroyForcibly();
            forceRemoveContainer(submissionId);
            return JudgeResult.of(Verdict.TIME_LIMIT_EXCEEDED, "", "Execution exceeded the time limit.", timeLimitMs);
        }

        int exitCode = process.exitValue();
        String stdout = readQuietly(stdoutFile);
        String stderr = readQuietly(stderrFile);

        if (exitCode == COMPILE_FAILURE_EXIT_CODE) {
            return JudgeResult.of(Verdict.COMPILATION_ERROR, stdout, stderr, runtimeMs);
        }
        if (exitCode == TIMEOUT_EXIT_CODE) {
            return JudgeResult.of(Verdict.TIME_LIMIT_EXCEEDED, stdout, stderr, runtimeMs);
        }
        if (exitCode != 0) {
            return JudgeResult.of(Verdict.RUNTIME_ERROR, stdout, stderr, runtimeMs);
        }

        // Ran to completion — caller compares stdout against expected output.
        return JudgeResult.of(Verdict.PENDING, stdout, stderr, runtimeMs);
    }

    private void forceRemoveContainer(String submissionId) {
        try {
            new ProcessBuilder("docker", "rm", "-f", "submission-" + submissionId)
                    .redirectErrorStream(true)
                    .start()
                    .waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Could not force-remove container submission-{}: {}", submissionId, e.getMessage());
        }
    }

    private String readQuietly(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private void cleanup(Path workDir) {
        try (var paths = Files.walk(workDir)) {
            paths.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException e) {
            log.warn("Could not clean up submission workspace {}: {}", workDir, e.getMessage());
        }
    }
}
