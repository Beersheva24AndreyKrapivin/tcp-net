package telran.net;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TcpServer implements Runnable {
    // Protocol protocol;
    // int port;
    // final ExecutorService executor = Executors.newFixedThreadPool(4);

    // public TcpServer(Protocol protocol, int port) {
    //     this.protocol = protocol;
    //     this.port = port;
    // }

    // @Override
    // public void run() {
    //     // add SocketTimeOut handling for shutdown
    //     try (ServerSocket serverSocket = new ServerSocket(port)) {
    //         serverSocket.setSoTimeout(10);
    //         System.out.println("Server is listening on the port " + port);

    //         while (true) {
    //             try {
    //                 Socket socket = serverSocket.accept();
    //                 var session = new TcpClientServerSession(protocol, socket);
    //                 executor.submit(session);
    //             } catch (SocketTimeoutException e) {
    //                 if (executor.isShutdown()) {
    //                     break;
    //                 }
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }

    // public void shutdown() {
    //     // In the ExecutorService framework to provide shutdownNow
    //     // (to ignore all not proccessing client session)
    //     executor.shutdown();
    //     try {
    //         if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
    //             executor.shutdownNow();
    //         }
    //     } catch (InterruptedException ie) {
    //         executor.shutdownNow();
    //         Thread.currentThread().interrupt();
    //     }
    // }

    private static final int DEFAULT_SOCKET_TIMEOUT = 100;
    private static final int DEFAULT_IDLE_CONNECTION_TIMEOUT = 60000;
    private static final int DEFAULT_LIMIT_REQUESTS_PER_SEC = 5;
    private static final int DEFAULT_LIMIT_NON_OK_RESPONSES_IN_ROW = 10;
    Protocol protocol;
    int port;
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    int socketTimeout;
    int idleConnectionTimeout;
    int limitRequestsPerSecond;
    int limitNonOkResponsesInRow;

    public TcpServer(Protocol protocol, int port, int socketTimeout, int idleConnectionTimeout,
            int limitRequestsPerSecond, int limitNonOkResponsesInRow) {
        this.protocol = protocol;
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.idleConnectionTimeout = idleConnectionTimeout;
        this.limitRequestsPerSecond = limitRequestsPerSecond;
        this.limitNonOkResponsesInRow = limitNonOkResponsesInRow;
    }

    public TcpServer(Protocol protocol, int port, int idleConnectionTimeout) {
        this(protocol, port, DEFAULT_SOCKET_TIMEOUT, idleConnectionTimeout, DEFAULT_LIMIT_REQUESTS_PER_SEC,
                DEFAULT_LIMIT_NON_OK_RESPONSES_IN_ROW);
    }

    public TcpServer(Protocol protocol, int port) {
        this(protocol, port, DEFAULT_SOCKET_TIMEOUT, DEFAULT_IDLE_CONNECTION_TIMEOUT, DEFAULT_LIMIT_REQUESTS_PER_SEC,
                DEFAULT_LIMIT_NON_OK_RESPONSES_IN_ROW);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on the port " + port);
            serverSocket.setSoTimeout(socketTimeout);
            while (!executor.isShutdown()) {
                try {
                    Socket socket = serverSocket.accept();
                    socket.setSoTimeout(socketTimeout);
                    var session = new TcpClientServerSession(protocol, socket, this);
                    executor.execute(session);
                } catch (SocketTimeoutException e) {

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void shutdown() {
        executor.shutdownNow();

    }
    

}
