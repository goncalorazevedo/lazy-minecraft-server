package server;

import java.io.File;
import java.io.IOException;

public class ServerProcess {

    private final String executable;
    private final String workingDir;
    private final String outputFile;

    private final Object lock = new Object();
    private volatile Process process;
    private State state;

    public ServerProcess(String exe, String workingDir, String outputFile) {
        this.executable = exe;
        this.workingDir = workingDir;
        this.outputFile = outputFile;
        this.state = State.STOPPED;
    }

    private enum State { STOPPED, STARTING, RUNNING, STOPPING }

    public void launchServer() throws IOException {
        synchronized (lock) {
            if (state == State.RUNNING || state == State.STARTING) {
                return;
            }
            state = State.STARTING;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(executable)
                    .directory(new File(workingDir))
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(new File(outputFile)))
                    .redirectErrorStream(true);
            synchronized (lock) {
                process = pb.start();
                state = State.RUNNING;
            }
        } catch (IOException e) {
            synchronized (lock) {
                state = State.STOPPED;
            }
            throw e; // let the caller see it
        }
    }

    public void stopServer() {
        synchronized (lock) {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            state = State.STOPPED;
        }
    }
}
