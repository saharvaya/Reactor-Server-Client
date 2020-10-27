/****************************
 * Submitters:
 * Itay Bouganim, 305278384
 * Sahar Vaya, 205583453
 ***************************/
package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Server;

public class ReactorMain {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("You must enter a port and thread amount to run!");
            return;
        }
        int port = Integer.parseInt(args[0]); //Port number
        int numOfThreads = Integer.parseInt(args[1]); //Amount of threads to run
        ServerData data = new ServerData(); //Shared to all protocols current server information object.

        Server.reactor(
                numOfThreads, //thread count
                port, //port
                () -> new BidiMessagingProtocolImpl(data), //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        ).serve();
    }
}