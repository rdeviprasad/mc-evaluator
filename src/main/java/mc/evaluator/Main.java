package mc.evaluator;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please provide the root directory of the Java project and the main class name.");
            return;
        }

        String directory = args[0];
        String mainClass = args[1];

        JavaRunner javaRunner = new JavaRunner(directory, mainClass);
        if (javaRunner.compileJavaFiles()) {
            javaRunner.runJavaClass();
        }
    }
}

class JavaRunner {

    private final String directory;
    private final String mainClass;

    public JavaRunner(String directory, String mainClass) {
        this.directory = directory.endsWith(File.separator) ? directory : directory + File.separator;
        this.mainClass = mainClass;
    }

    boolean compileJavaFiles() {
        try {
            List<String> javaFiles = getJavaFiles(new File(directory));

            if (javaFiles.isEmpty()) {
                System.out.println("No Java files found to compile.");
                return false;
            }

            List<String> command = new ArrayList<>();
            command.add("javac");
            command.addAll(javaFiles);

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                printProcessOutput(process);
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> getJavaFiles(File root) {
        List<String> javaFiles = new ArrayList<>();
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(getJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file.getAbsolutePath());
                }
            }
        }
        return javaFiles;
    }

    void runJavaClass() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", directory, mainClass);
            Process process = pb.start();

            printProcessOutput(process);

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printProcessOutput(Process process) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }
        }
    }
}
