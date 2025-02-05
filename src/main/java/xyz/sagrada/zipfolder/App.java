package xyz.sagrada.zipfolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
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

    @Option(names = {"-s", "--suffix"}, defaultValue = ".jpg,.gif,.png,.webp", split = ",",
        description = "Images suffixes")
    private List<String> suffixes;

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
        Path zipFilePath = Path.of(targetDir, file.getName() + ".zip");
        List<File> fileList = Optional.ofNullable(file.listFiles()).stream().flatMap(Stream::of).toList();
        File ehviewer = fileList.stream().filter(e -> ".ehviewer".equals(e.getName())).findFirst().orElse(null);
        if (ehviewer == null) {
            System.out.println(file.getName() + " not exists .ehviewer file");
            return;
        }

        int imagePages;
        AtomicInteger imageCount = new AtomicInteger(0);
        try (FileInputStream inputStream = new FileInputStream(ehviewer)) {
            String[] lines = new String(inputStream.readAllBytes()).split("\n");
            imagePages = Integer.parseInt(lines[7]);
            for (File image : fileList) {
                suffixes.stream().filter(e -> image.getName().endsWith(e)).findAny()
                    .ifPresent(e -> imageCount.incrementAndGet());
            }
            if (imagePages != imageCount.get()) {
                System.out.println(file.getName() + " image count not match .ehviewer");
                return;
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (onlyTest) {
            return;
        }
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            for (File newFile : fileList) {
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
