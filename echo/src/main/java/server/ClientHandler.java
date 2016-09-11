/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.Log;
import shared.ProtocolStrings;

/**
 *
 * @author jens
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final PrintWriter writer;
    private final EchoServer server;
    private final Scanner input;
    private String name;
    public boolean loggedIn = false;

    public ClientHandler(Socket socket, Scanner input, PrintWriter writer, EchoServer server) {
        this.socket = socket;
        this.writer = writer;
        this.server = server;
        this.input = input;
    }

    public static ClientHandler handle(Socket socket, EchoServer server) throws IOException {
        Scanner input = new Scanner(socket.getInputStream());
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        return new ClientHandler(socket, input, writer, server);
    }

    public void run() {
        try {
            while (true) {
                String message = input.nextLine();
                String[] msg = message.split(":");
                System.out.println("Received: " + message);

                if (!loggedIn) {
                    if (msg[0].equals(ProtocolStrings.LOGIN)) {
                        loggedIn = true;
                        name = msg[1];
                        printClientList();
                    }
                } else {

                    switch (msg[0]) {
                        case "MSG":
                            if (msg[0].equals(ProtocolStrings.MSG) && (msg[1].equals(""))) {
                                server.sendMulticast(ProtocolStrings.MSGRES + msg[2]);
                            } else {
                                String[] users = msg[1].split(",");
                                for (ClientHandler client : server.getClientHandlers()) {
                                    for (String user : users) {
                                        if (client.getName().equals(user)) {
                                            client.sendMessage(ProtocolStrings.MSGRES + msg[2]);
                                        }
                                    }
                                }
                            }
                            break;
                        case "LOGOUT":
                            if (msg[0].equals(ProtocolStrings.LOGOUT)) {
                                try {
                                    writer.println(ProtocolStrings.LOGOUT);//Echo the stop message back to the client for a nice closedown
                                    socket.close();

                                } catch (IOException ex) {
                                    Logger.getLogger(Log.logFileName).log(Level.INFO, "IOException caught in LogOut Case: ", ex.getMessage());
                                }
                            }
                            break;
                        default: {
                            System.out.println("    default    ");
                        }

                    }
                }

            }

        } finally {
            try {
                writer.println(ProtocolStrings.LOGOUT);//Echo the stop message back to the client for a nice closedown
                socket.close();
                server.removeHandler(this);
                printClientList();
                System.out.println("Closed a Connection");
            } catch (IOException ex) {
                Logger.getLogger(Log.logFileName).log(Level.INFO, "IOException caught in Finally, when trying to close connection: " + ex.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        System.out.println("Sending " + message);
        writer.println(message);
        writer.flush();
    }

    public void printClientList() {
        ArrayList<String> clientList = new ArrayList<>();

        for (ClientHandler c : server.getClientHandlers()) {
            if (c.getName() != null) {
                clientList.add(c.getName());
            }
        }

        server.sendMulticast("CLIENTLIST:" + String.join(",", clientList));
    }

    public String getName() {
        return name;
    }

}
