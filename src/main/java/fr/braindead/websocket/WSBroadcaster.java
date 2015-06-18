package fr.braindead.websocket;

import fr.braindead.websocket.server.WebSocketClient;
import fr.braindead.websocket.server.WebSocketServer;
import fr.braindead.websocket.server.WebSocketServerImpl;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by leiko on 18/06/15.
 */
public class WSBroadcaster {

    private WebSocketServer server;

    public WSBroadcaster(URI uri) {
        this.server = new WebSocketServerImpl(uri);
        Map<String, Set<WebSocketClient>> rooms = new HashMap<>();
        this.server.onConnect((path, c) -> {
            // add it to the proper room
            Set<WebSocketClient> room = rooms.get(path);
            if (room == null) {
                room = new HashSet<>();
                rooms.put(path, room);
            }
            room.add(c);

            // on message
            c.onMessage(msg -> rooms.get(path)
                    .forEach(client -> {
                        if (client != c) {
                            client.send(msg);
                        }
                    }));

            // on error
            c.onError(Throwable::printStackTrace);

            // on close
            c.onClose(() -> {
                rooms.get(path).remove(c);
                if (rooms.get(path).isEmpty()) {
                    rooms.remove(path);
                }
            });
        });
    }

    public void start() {
        this.server.start();
    }

    public void stop() {
        this.server.stop();
    }
}
