package bgu.spl181.net.impl.BBreactor;

import bgu.spl181.net.api.bidi.EncoDeco;
import bgu.spl181.net.api.bidi.SharedDataMovies;
import bgu.spl181.net.api.bidi.SharedDataUsers;
import bgu.spl181.net.api.bidi.protocolMovies;
import bgu.spl181.net.srv.Server;

/**
 * this class will simulate a executable server of reactor.
 */
public class ReactorMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);


        SharedDataMovies data = new SharedDataMovies();
        SharedDataUsers users = new SharedDataUsers();

        Server.<String>reactor(
                10,
                port, //port
                () -> new protocolMovies(data, users), //protocol factory
                () -> new EncoDeco()).serve();
    }
}
