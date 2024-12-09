package telran.net;

import java.util.Scanner;

import org.junit.jupiter.api.Test;

public class TcpClientServerShutdownTest {
    @Test
    void tcpClientServerShutdownTest() {
        Protocol protocol = new ProtocolImpl();
        TcpServer tcpServer = new TcpServer(protocol, 4000);

        Thread serverThread = new Thread(tcpServer);
        serverThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if ("shutdown".equalsIgnoreCase(command)) {
                tcpServer.shutdown();
                break;
            }
        }

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            System.out.println("Server thread interrupted: " + e.getMessage());
        }

        System.out.println("Server has finished.");
    }
}
