package net.sxmaa.u25core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class GitRevisionChecker {

    public static class GitInfo {
        public final String revision;
        public final boolean isDirty;
        public final boolean isAvailable;
        public final String errorMessage;

        private GitInfo(String revision, boolean isDirty, boolean isAvailable, String errorMessage) {
            this.revision = revision;
            this.isDirty = isDirty;
            this.isAvailable = isAvailable;
            this.errorMessage = errorMessage;
        }

        public static GitInfo available(String revision, boolean isDirty) {
            return new GitInfo(revision, isDirty, true, null);
        }

        public static GitInfo unavailable(String errorMessage) {
            return new GitInfo("unknown", false, false, errorMessage);
        }

        @Override
        public String toString() {
            if (isAvailable) {
                return revision + (isDirty ? "-dirty" : "");
            }
            return "unknown (" + errorMessage + ")";
        }
    }

    public static GitInfo getGitInfo() {
        try {
            // Get the directory where the JAR is running from
            String urlPath = GitRevisionChecker.class.getProtectionDomain().getCodeSource().getLocation().toString();

            // Remove everything after "!/" if present (class path inside JAR) & prefix
            int jarSeparator = urlPath.indexOf("!/");
            if (jarSeparator != -1) {
                urlPath = urlPath.substring(0, jarSeparator);
            }
            if (urlPath.startsWith("jar:"))
                urlPath = urlPath.substring(4);

            File jarFile = Paths.get(new java.net.URI(urlPath)).toFile();
            File jarLocation = jarFile.isDirectory() ? jarFile : jarFile.getParentFile();

            if (jarLocation == null) {
                return GitInfo.unavailable("could not determine JAR location");
            }

            // Check if git binary is available
            if (!isGitAvailable()) {
                return GitInfo.unavailable("git binary not found");
            }

            // Get the revision - git will search up the directory tree for .git
            String revision = executeGitCommand(jarLocation, "rev-parse", "--short", "HEAD");
            if (revision == null) {
                return GitInfo.unavailable("not a git repository");
            }

            // Check if working directory is dirty
            boolean isDirty = isWorkingDirectoryDirty(jarLocation);

            return GitInfo.available(revision.trim(), isDirty);

        } catch (Exception e) {
            return GitInfo.unavailable(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static boolean isGitAvailable() {
        try {
            Process process = new ProcessBuilder("git", "--version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWorkingDirectoryDirty(File directory) {
        try {
            // Check for uncommitted changes in the mods/ directory only
            String status = executeGitCommand(directory, "status", "--porcelain", "mods/");
            return status != null && !status.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private static String executeGitCommand(File directory, String... args) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("git");
            for (String arg : args) {
                pb.command().add(arg);
            }
            pb.directory(directory);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return null;
            }

            return output.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
