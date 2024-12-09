package telran.net;

import java.net.*;

import org.json.JSONObject;

import java.io.*;
import static telran.net.TcpConfigurationProperties.*;

public class TcpClientServerSession implements Runnable {
    Protocol protocol;
    Socket socket;

    private static final int IDLE_TIMEOUT = 10_000;
    private static final int MAX_ERRORS = 10; 
    private static final int MAX_REQUESTS_PER_SECOND = 10; 

    private int errorCount = 0;
    private long lastRequestTime = System.currentTimeMillis();
    private int requestCount = 0;
    private int timeoutCount = 0;

    public TcpClientServerSession(Protocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }

    @Override
    public void run() {
        // add SocketTimeoutException handler for both graceful
        // shutdown and dos attacks prevention
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream writer = new PrintStream(socket.getOutputStream())) {
            String request = null;
            socket.setSoTimeout(10);

            while ((request = reader.readLine()) != null) {
                if (getRequestCount() > MAX_REQUESTS_PER_SECOND) {
                    writer.println("Many requests. Session closed.");
                    break;
                }

                String response = protocol.getResponseWithJSON(request);
                writer.println(response);

                if (isErrorResponse(response)) {
                    errorCount++;
                    if (errorCount >= MAX_ERRORS) {
                        writer.println("Many errors. Session closed.");
                        break;
                    }
                }

            }

        } catch (SocketTimeoutException e) {
            timeoutCount = timeoutCount + 10;
            if (timeoutCount > IDLE_TIMEOUT) {
                try {
                    socket.close();
                } catch (Exception ex) {
                    //
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                //
            }
        }
    }

    private int getRequestCount() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime > 1000) {
            lastRequestTime = currentTime;
            requestCount = 0;
        }
        return ++requestCount;
    }

    private boolean isErrorResponse(String responseJSON) {
        JSONObject jsonObj = new JSONObject(responseJSON);
        ResponseCode responseCode = jsonObj.getEnum(ResponseCode.class, RESPONSE_CODE_FIELD);
        return responseCode != ResponseCode.OK;
    }

}
