package xyz.sagrada.zipcli;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import xyz.sagrada.zipcli.sub.CheckZipFile;
import xyz.sagrada.zipcli.sub.ZipFolder;

@Command(name = "zipcli", mixinStandardHelpOptions = true, version = "1.0", description = "Zip folder one By one",
    subcommands = {ZipFolder.class, CheckZipFile.class})
public class App implements Callable<Integer> {

    public static void main(String[] args) {
        args = new String[] {"check", "./Zip"};
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.println("zipcli need 'folder' 'check'");
        return 0;
    }

}
