package server;

import java.io.File;
import java.io.IOException;

public class ServerProcess {
    private final String executable;
    private final String workingDir;
    private final String outputFile;

    private Process process;

    public ServerProcess(String exe, String workingDir, String outputFile) {
        this.executable = exe;
        this.workingDir = workingDir;
        this.outputFile = outputFile;
    }

    public void start() throws IOException {
        var pb = new ProcessBuilder(executable)
                .directory(new File(workingDir))
                .redirectOutput(ProcessBuilder.Redirect.appendTo(new File(outputFile)))
                .redirectErrorStream(true);
        process = pb.start();
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}
