#include <ClientListener.h>

ClientListener::ClientListener(ConnectionHandler * handler, MessageEncoderDecoder* encdec, std::mutex &mutex, std::condition_variable &cond) : handler(handler), encoderDecoder(encdec), commands(), mutex(mutex), condition(cond) {
}

void ClientListener::run()
{
    std::string command;
    while (!handler->isTerminatedConnection())
    {
        getline(std::cin, command); //Get user client input from keyboard
        if(handler->isTerminatedConnection()) {
            std::cout << "SERVER CONNECTION CLOSED!" << std::endl;
            break;
        }
        else if (commands.isValidCommand(command)) //Check the user has entered a valid command
            processCommand(command);
        else std::cout << "INVALID COMMAND : " << command << std::endl;
    }
}

void ClientListener::processCommand(std::string &command) {

    size_t index = command.find_first_of(' ');
    std::string params;

    //Seperate command parameters from the specific command
    if (index == std::string::npos)
        index = command.size();
    else params = command.substr(index + 1, command.size());

    //Seperate the command operation from the rest of the string
    std::string operation = command.substr(0, index);
    //Finds the specific command in the valid commands list and get the OpCode corresponding with the command
    auto iter = find(commands.getCommands()->begin(), commands.getCommands()->end(), operation);
    short opCode = (short)(distance(commands.getCommands()->begin(), iter) + 1);

    //Encode the command to bytes
    byte* packet = encoderDecoder -> encode(opCode, params);
    int packetLength = encoderDecoder -> getMessageLength();

    if(packet != nullptr)
        try {
            //Send bytes via connection handler
            if(!handler->sendBytes(packet, packetLength))
                throw std::exception();
            if(commands.isLogoutCommand(command)) {
                //If the command was a LOGOUT command wait on current thread, will be notified when an ack/error will be received
                std::unique_lock<std::mutex> lock (mutex);
                condition.wait(lock);
            }
        } catch (std::exception &e) {
            std::cout << "Error sending command to server: " << e.what() << std::endl;
        }
}

