package connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectorClient {
    PrintWriter toServer;
    BufferedReader fromServer;
    Socket socket;

    public ConnectorClient(int port) throws IOException {
        socket = new Socket("localhost", port);
        toServer = new PrintWriter(socket.getOutputStream(), true);
        fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String communicateServer(String myString) throws IOException {
        System.out.println(myString);
        toServer.println(myString);
        String answer = fromServer.readLine();
        System.out.println(answer);
        return answer;
    }
}
