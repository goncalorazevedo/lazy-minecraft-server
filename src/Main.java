import proxy.ProxyServer;
import server.Server;
import server.ServerProcess;

public class Main {
    static void main(String[] args) {
        var listenPort  = Integer.parseInt(args[0]);
        var targetHost  = args[1];
        var targetPort  = Integer.parseInt(args[2]);

        var exePath     = args[3];
        var workDirPath = args[4];
        var outputFile  = args[5];


        var serverProcess = new ServerProcess(exePath, workDirPath, outputFile);
        var server = new Server(targetHost, targetPort, serverProcess);
        var proxyServer = new ProxyServer(listenPort, server);
        proxyServer.run();
    }
}
