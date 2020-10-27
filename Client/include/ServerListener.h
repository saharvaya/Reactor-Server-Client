#ifndef SERVERLISTENER_H
#define SERVERLISTENER_H

#include <mutex>
#include <condition_variable>
#include "ClientListener.h"

class ServerListener
{
public:
    ServerListener(ConnectionHandler* handler, MessageEncoderDecoder* encdec, std::mutex &mutex, std::condition_variable &cond); //Constructor
    virtual ~ServerListener() = default;    //Destructor
    ServerListener(const ServerListener&) = default; //Copy Constructor
    ServerListener(ServerListener&&) = default; //Move Constructor
    ServerListener& operator=(const ServerListener&) = default; //Copy Assignment Operator
    ServerListener& operator=(ServerListener&&) = default; //Move Assignment Operator
    void run();

private:
    void displayMessage(Message * message);
    bool isApprovedLogoutMessage(Message * message);

    ConnectionHandler* handler;
    MessageEncoderDecoder* encoderDecoder;
    std::mutex & mutex;
    std::condition_variable & condition;
};

#endif
