package telran.net;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpServer implements Runnable {
    Protocol protocol;
    int port;
    final ExecutorService executor = Executors.newFixedThreadPool(4);

    public TcpServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }

    @Override
    public void run() {
        // add SocketTimeOut handling for shutdown
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(10);
            System.out.println("Server is listening on the port " + port);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    var session = new TcpClientServerSession(protocol, socket);
                    executor.submit(session);
                } catch (SocketTimeoutException e) {
                    if (executor.isShutdown()) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void shutdown() {
        // In the ExecutorService framework to provide shutdownNow
        // (to ignore all not proccessing client session)
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
