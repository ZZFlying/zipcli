package xyz.sagrada.zipcli.sub;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "check", mixinStandardHelpOptions = true, version = "1.0", description = "Test zip file valid")
public class CheckZipFile implements Callable<Integer> {

    @Parameters(index = "0", description = "Source folder dir")
    private String sourceDir;

    @Option(names = {"-e", "--exclude"}, defaultValue = "", split = ",", description = "Exclude Zip name")
    private List<String> excludes;

    @Option(names = {"-s", "--suffix"}, defaultValue = ".jpg,.gif,.png,.webp", split = ",",
        description = "Images suffixes")
    private List<String> suffixes;

    @Option(names = {"-t", "--only-test"}, defaultValue = "false", description = "Only test zip file valid")
    private Boolean onlyTest;

    public CheckZipFile() {
    }

    public CheckZipFile(String sourceDir, List<String> excludes, List<String> suffixes, Boolean onlyTest) {
        this.sourceDir = sourceDir;
        this.excludes = excludes;
        this.suffixes = suffixes;
        this.onlyTest = onlyTest;
    }

    @Override
    public Integer call() {
        Path sourcePath = Path.of(sourceDir);
        try {
            Files.list(sourcePath).map(Path::toFile).filter(e -> !excludes.contains(e.getName())).parallel()
                .forEach(this::checkValid);
        }
        catch (Exception e) {
            System.err.println("test zip file error: " + e.getMessage());
        }
        return 0;
    }

    public boolean checkValid(File file) {
        String gid = file.getName().split("-")[0];
        try (ZipFile zipFile = new ZipFile(file)) {
            List<? extends ZipEntry> zipEntryList = zipFile.stream().toList();
            String[] lines = null;
            AtomicInteger pageCount = new AtomicInteger(0);
            InputStream inputStream;
            for (var entry : zipEntryList) {
                inputStream = zipFile.getInputStream(entry);
                if (entry.getName().equals(".ehviewer")) {
                    lines = new String(inputStream.readAllBytes()).split("\n");
                }
                suffixes.stream().filter(e -> entry.getName().endsWith(e)).findFirst()
                    .ifPresent(e -> pageCount.incrementAndGet());
                inputStream.close();
            }
            if (onlyTest) {
                return true;
            }
            if (lines == null) {
                System.err.println(gid + " .ehviewer not exist");
                return false;
            }
            if (lines.length != (8 + pageCount.get())) {
                System.err.println(gid + " .ehviewer not valid");
                return false;
            }
        }
        catch (IOException e) {
            System.err.println(gid + " check error: " + e.getMessage());
            return false;
        }
        System.out.println(gid + " OK");
        return true;
    }

}
