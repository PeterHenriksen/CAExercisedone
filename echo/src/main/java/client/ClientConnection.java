/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author jens
 */
public class ClientConnection implements AutoCloseable {
    
    private final EchoClient client;
    private final ExecutorService observerPool;
    private final List<Consumer<String>> observers = new CopyOnWriteArrayList<>();
    
    private ClientConnection(EchoClient client) {
        this.client = client;
        observerPool = Executors.newCachedThreadPool();
        observerPool.submit(() -> {
            Stream.generate(client::receive)
                    .forEach(this::notifyObservers);
        });
    }
    
    public void addObserver(Consumer<String> consumer) {
        observers.add(consumer);
    }
    
    public void removeObserver(Consumer<String> consumer) {
        observers.remove(consumer);
    }
    
    public void notifyObservers(String message) {
        observers.forEach(c -> c.accept(message));
    }
    
    public static ClientConnection createConnection(String host, int port) throws IOException {
        EchoClient client = new EchoClient();
        client.connect(host, port);
        return new ClientConnection(client);
    }
    
    public void send(String message) {
        client.send(message);
    }
    
    @Override
    public void close() throws Exception {
        observerPool.shutdown();
        client.stop();
    }
    
}
