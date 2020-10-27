#ifndef CLIENTLISTENER_H_
#define CLIENTLISTENER_H_

#include <string>
#include <iostream>
#include <sstream>
#include <mutex>
#include <condition_variable>
#include "ConnectionHandler.h"
#include "MessageEncoderDecoder.h"
#include "Commands.h"

class ClientListener
{

public:
    ClientListener(ConnectionHandler* handler, MessageEncoderDecoder* encdec, std::mutex &mutex, std::condition_variable &cond); //Constructor
    virtual ~ClientListener() = default; //Destructore
    ClientListener(const ClientListener&) = default; //Copy Constructor
    ClientListener(ClientListener&&) = default; //Move Constructor
    ClientListener& operator=(const ClientListener&) = default; //Copy Assignment Operator
    ClientListener& operator=(ClientListener&&) = default; //Move Assignment Operator
    //Run method to start the client listener operation
    void run();

private:
    //Processes a given paramater string command line
    void processCommand(std::string &command);

    ConnectionHandler* handler;
    MessageEncoderDecoder* encoderDecoder;
    Commands commands;
    std::mutex & mutex;
    std::condition_variable & condition;
};

#endif