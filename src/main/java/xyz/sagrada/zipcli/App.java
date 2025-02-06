package xyz.sagrada.zipcli;

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import xyz.sagrada.zipcli.sub.CheckZipFile;
import xyz.sagrada.zipcli.sub.ZipFolder;

@Command(name = "zipcli", mixinStandardHelpOptions = true, version = "1.0", description = "Zip folder one By one",
    subcommands = {ZipFolder.class, CheckZipFile.class})
public class App implements Callable<Integer> {

    private static final String ERASE_ROW = "\033[2K\r";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    public synchronized static void log(PrintStream stream, String message) {
        stream.print(ERASE_ROW);
        stream.print(message);
    }

    public static void error(String message) {
        log(System.err, message + "\n");
    }

    public static void info(String message) {
        log(System.out, message + "\n");
    }

    public synchronized static void processing(String message, AtomicInteger current, int total) {
        log(System.out, message + " processing: " + current.incrementAndGet() + "/" + total);
    }

    @Override
    public Integer call() {
        System.out.println("zipcli need 'folder' 'check'");
        return 0;
    }

}
