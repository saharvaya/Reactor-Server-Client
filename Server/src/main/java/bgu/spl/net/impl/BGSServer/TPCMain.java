/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You must enter a port!");
            return;
        }
        int port = Integer.valueOf(args[0]); //Port number
        ServerData data = new ServerData(); //Shared to all protocols current server information object.

        Server.threadPerClient(
                port, //port
                () -> new BidiMessagingProtocolImpl(data), //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        ).serve();
    }
}