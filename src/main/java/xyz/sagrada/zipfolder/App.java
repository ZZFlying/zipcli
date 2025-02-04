package xyz.sagrada.zipfolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "zipFolder", mixinStandardHelpOptions = true, version = "1.0", description = "Zip folder one By one")
public class App implements Runnable {

    @Parameters(index = "0", description = "Source folder dir")
    private String sourceDir;

    @Parameters(index = "1", description = "Target folder dir")
    private String targetDir;

    @Option(names = {"-t", "--only-test"}, defaultValue = "false")
    private Boolean onlyTest;

    @Option(names = {"-e", "--exclude"}, defaultValue = ".stfolder,.nomedia", split = ",",
        description = "Exclude folder name")
    private List<String> excludes;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        Path sourcePath = Path.of(sourceDir);
        Path targetPath = Path.of(targetDir);
        Map<String, File> zipMap = new HashMap<>();
        Map<String, File> newFileMap = new HashMap<>();
        try {
            Files.list(targetPath).map(Path::toFile).filter(e -> !excludes.contains(e.getName()))
                .forEach(file -> {
                    String gid = file.getName().split("-", 2)[0];
                    zipMap.put(gid, file);
                });
            Files.list(sourcePath).map(Path::toFile).filter(e -> !excludes.contains(e.getName()))
                .forEach(file -> {
                    String gid = file.getName().split("-", 2)[0];
                    if (!zipMap.containsKey(gid)) {
                        newFileMap.put(gid, file);
                    }
                });
            newFileMap.values().parallelStream().forEach(this::zipFolder);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void zipFolder(File file) {
        System.out.println(file.getName());
        if (onlyTest) {
            return;
        }
        Path zipFilePath = Path.of(targetDir, file.getName() + ".zip");
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            List<File> newFileList = Files.list(file.toPath()).map(Path::toFile).toList();
            for (File newFile : newFileList) {
                ZipEntry zipFile = new ZipEntry(newFile.getName());
                outputStream.putNextEntry(zipFile);
                try (FileInputStream bookInputStream = new FileInputStream(newFile)) {
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = bookInputStream.read(bytes)) >= 0) {
                        outputStream.write(bytes, 0, length);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
