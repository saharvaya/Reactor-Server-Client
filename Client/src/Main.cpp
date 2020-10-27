#include <ConnectionHandler.h>
#include <ClientListener.h>
#include <ServerListener.h>
#include <iostream>
#include <fstream>
#include <thread>
#include <mutex>
#include <condition_variable>

int main(int argc, char **argv)
{
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " <host_ip> <port>" << std::endl << std::endl;
        return -1;
    }
    std::string host_ip = argv[1]; //Server IP address
    auto port = (short) atoi(argv[2]); //Port to connect to

    ConnectionHandler* connectionHandler = new ConnectionHandler(host_ip, port);
    if (!connectionHandler->connect()) { //Try to establish connection to server using the ip,port
        delete connectionHandler;
        std::cerr << "Cannot connect to " << host_ip  << ":" << port << " , check for ip address and port validity." << std::endl;
        return 1;
    }
    auto* encdec = new MessageEncoderDecoder();

    std::mutex mutex;
    std::condition_variable condition;
    ClientListener clientListener(connectionHandler, encdec ,mutex, condition);
    ServerListener serverListener(connectionHandler, encdec, mutex, condition);
    //Start two threads, Client Listeners - reads user input, Server Listener - reads server received bytes
    std::thread t1(&ClientListener::run, &clientListener);
    std::thread t2(&ServerListener::run, &serverListener);

    t1.join();
    t2.join();

    //After threads finished, close the connection and delete created objects
    connectionHandler->close();
    delete encdec;
    delete connectionHandler;
    return 0;
}