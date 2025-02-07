package xyz.sagrada.zipcli;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.sagrada.zipcli.sub.CheckZipFile;
import xyz.sagrada.zipcli.sub.ZipFolder;

@Command(name = "zipcli", mixinStandardHelpOptions = true, version = "1.0", description = "Zip folder one By one",
    subcommands = {ZipFolder.class, CheckZipFile.class})
public class App implements Callable<Integer> {

    public static final String ERASE_ROW = "\033[2K\r";

    public static final String RED_FONT = "\033[31m";

    public static final String RESET = "\033[0m";

    @Option(names = {"-q", "--quite"}, defaultValue = "false", description = "Only print error message and processing")
    private static Boolean quite;

    @Option(names = {"-qq", "--more-quite"}, defaultValue = "false", description = "Only print error message")
    private static Boolean moreQuite;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    public synchronized static void log(String message) {
        System.out.print(ERASE_ROW);
        System.out.print(message);
        System.out.flush();
    }

    public static void error(String message) {
        log(RED_FONT + message + "\n" + RESET);
    }

    public static void info(String message) {
        if (quite || moreQuite) {
            return;
        }
        log(message + "\n");
    }

    public synchronized static void processing(String message, AtomicInteger current, int total) {
        if (moreQuite) {
            return;
        }
        log(message + " processing: " + current.incrementAndGet() + "/" + total);
    }

    @Override
    public Integer call() {
        System.out.println("zipcli need 'folder' 'check'");
        return 0;
    }

}
