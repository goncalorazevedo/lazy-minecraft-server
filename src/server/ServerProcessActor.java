package server;

import java.io.IOException;
import java.util.concurrent.*;
import server.Server.State;

public class ServerProcessActor {
    private final ServerProcess serverProcess;
    private final Server server;
    ScheduledExecutorService mailbox = Executors.newSingleThreadScheduledExecutor();

    private volatile State state;
    private ScheduledFuture<?> monitorHandle;

    ServerProcessActor(ServerProcess serverProcess, Server server) {
        this.serverProcess = serverProcess;
        this.server = server;
        submit(this::initialize);
    }

    public void submit(Runnable runnable) { mailbox.submit(runnable); }
    public ScheduledFuture<?> submit(Runnable runnable, int delay, TimeUnit unit) { return mailbox.schedule(runnable, delay, unit); }

    public void initialize() {
        state = server.isUp() ? State.RUNNING : State.STOPPED;
        if (state == State.RUNNING) {
            scheduleMonitor();
        }
    }

    public State getState() { return state; }

    public void launch() {
        if (state == State.RUNNING) return;
        try {
            serverProcess.start();
        } catch (IOException e) {
            System.err.println("Failed launching proc" + e);
            this.state = State.STOPPED;
            return;
        }
        this.state = State.RUNNING;
        scheduleMonitor();
    }

    public void stop() {
        if (state == State.STOPPED) return;
        this.serverProcess.stop();
        this.state = State.STOPPED;
        cancelMonitor();
    }

    private void monitor() {
        if (!server.isUp() || server.getConnections() == 0) {
            this.serverProcess.stop();
            state = State.STOPPED;
            cancelMonitor();
            return;
        }
        state = State.RUNNING;
        scheduleMonitor();
    }

    private void scheduleMonitor() {
        monitorHandle = mailbox.schedule(this::monitor, 30, TimeUnit.SECONDS);
    }

    private void cancelMonitor() {
        if (monitorHandle != null) {
            monitorHandle.cancel(false);
            monitorHandle = null;
        }
    }
}
