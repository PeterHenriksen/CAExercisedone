package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.Log;
import shared.ProtocolStrings;

public class EchoClient {

    Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;

    public void connect(String address, int port) throws UnknownHostException, IOException {
        this.port = port;
        serverAddress = InetAddress.getByName(address);
        socket = new Socket(serverAddress, port);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void send(String msg) {
        System.out.println("Sending " + msg);
        output.println(msg);
    }

    public void stop() throws IOException {
        output.println(ProtocolStrings.LOGOUT);
    }

    public String receive() {
        String msg = input.nextLine();
        String[] cmd = msg.split(":");
        if (cmd[0].equals(ProtocolStrings.MSGRES) || cmd[0].equals(ProtocolStrings.CLIENTLIST)) {
            return msg;

        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        int port = 7777;
        String ip = "localhost";
        if (args.length == 2) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        }
        try {
            EchoClient tester = new EchoClient();
            tester.connect(ip, port);
            System.out.println("Sending 'Hello world'");
            tester.send("Hello World");
            System.out.println("Waiting for a reply");
            System.out.println("Received: " + tester.receive()); //Important Blocking call         
            tester.stop();
            //System.in.read();      
        } catch (UnknownHostException ex) {
            Logger.getLogger(Log.logFileName).log(Level.INFO, "UnkownHost Exception was caught in EchoClient.main");        
        } catch (IOException ex) {
            Logger.getLogger(Log.logFileName).log(Level.INFO, "IOException was caught in EchoClient.main");
        }
    }
}
