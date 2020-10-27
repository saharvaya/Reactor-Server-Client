#include <ServerListener.h>

ServerListener::ServerListener(ConnectionHandler * handler, MessageEncoderDecoder* encoderDecoder, std::mutex &mutex, std::condition_variable &cond) : handler(handler), encoderDecoder(encoderDecoder), mutex(mutex), condition(cond) {
}

void ServerListener::run()
{
    bool terminate = false;
    byte currentByte[1];
    while (!terminate)
    {
        try {
            //Get one byte each time via connection handler
            if (handler->getBytes(currentByte, 1)) {
                //decode the next byte received, A Message will composed with bytes decoded when it is completed
                Message* message = encoderDecoder->decodeNextByte(currentByte[0]);
                if (message != nullptr) {
                    //If a message has been composed, display the message to the user
                    displayMessage(message);
                    if(isApprovedLogoutMessage(message)) {
                        //If the message decoded is a positive response to LOGOUT command terminate the connection, and stop listener operation
                        terminate = true;
                        handler->close();
                    }
                    condition.notify_all(); //Notify on waiting condition in Client Listener
                    delete message; //delete the message decoded since it has already been displayed to user
                }
            } else throw std::runtime_error("Server communication error.");
        } catch (std::exception &e) {
            terminate = true;
            handler->close();
            std::cerr << "Error receiving packet from server: " << e.what() << std::endl;
        }
    }
}

bool ServerListener::isApprovedLogoutMessage(Message *message) {
    //Return whether the parameter message is an ACK message received as a response to a LOGOUT message
    return message->getOpCode() == MessageEncoderDecoder::ACK && ((AckMessage*)message)->getMessageAck() == Commands::LOGOUT;
}

void ServerListener::displayMessage(Message *message) {
    short opCode = message->getOpCode();

    //Display the response message according to the message OpCode
    switch(opCode) {
        case MessageEncoderDecoder::NOTIFICATION : {
            auto* notification = (NotificationMessage *) message;
            std::cout << "NOTIFICATION " << notification->getPM() << " " << notification->getPostingUser() << " "
                      << notification->getContent() << std::endl;
            break;
        }
        case MessageEncoderDecoder::ERROR : {
            auto* error = (ErrorMessage *) message;
            std::cout << "ERROR " << error->getMessageCode() << std::endl;
            break;
        }
        case MessageEncoderDecoder::ACK : {
            auto* ack = (AckMessage*) message;
            std::cout << "ACK " << ack->getMessageAck();
            switch (ack->getType())
            {
                case AckMessage::DEFAULT: break;
                case AckMessage::FOLLOW:{
                    auto* followAck = (FollowAck*) message;
                    std::cout << " " << followAck->getNumOfUsers() << " " << followAck->getUsernameList();
                    break;
                }
                case AckMessage::USERLIST:{
                    auto* listAck = (UserlistAck*) message;
                    std::cout << " " << listAck->getNumOfUsers() << " " << listAck->getUsernameList();
                    break;
                }
                case AckMessage::STAT:{
                    auto* statAck = (StatAck*) message;
                    std::cout << " " << statAck->getNumPosts() << " " << statAck->getNumFollowers() << " " << statAck->getNumFollowing();
                    break;
                }
            }
            std::cout << std::endl;
        }
        default: break;
    }
}