package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.api.bidi.EncoDeco;
import bgu.spl181.net.api.bidi.SharedDataMovies;
import bgu.spl181.net.api.bidi.SharedDataUsers;
import bgu.spl181.net.api.bidi.protocolMovies;
import bgu.spl181.net.srv.Server;

/**
 * this class will simulate a executable server of Threadperclient.
 */
public class TPCMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        SharedDataMovies data = new SharedDataMovies(); //one shared object
        SharedDataUsers users=new SharedDataUsers();
        Server.<String>threadPerClient(
                port, //port
                () -> new protocolMovies(data,users), //protocol factory
                () -> new EncoDeco() //message encoder decoder factory
        ).serve();

    }
}