#include <MessageEncoderDecoder.h>

MessageEncoderDecoder::MessageEncoderDecoder() : messageLength(0), decodeStage(0), encodedBytes(nullptr), decodedBytes(nullptr), currentDecodedOpCode(nullptr), currentDecodedMessage(nullptr), decodedMessageOpCode(NULL_OPCODE), decodedAckMessageOpCode(NULL_OPCODE) {}

MessageEncoderDecoder::~MessageEncoderDecoder() {
    if(currentDecodedMessage != nullptr) {
        delete currentDecodedMessage;
        currentDecodedMessage = nullptr;
    }

    if(encodedBytes != nullptr) {
        delete encodedBytes;
        encodedBytes = nullptr;
    }

    if(decodedBytes != nullptr) {
        delete decodedBytes;
        decodedBytes = nullptr;
    }

    if(currentDecodedOpCode != nullptr) {
        delete currentDecodedOpCode;
        currentDecodedOpCode = nullptr;
    }
}

Message* MessageEncoderDecoder::decodeNextByte(byte &nextByte) {

    if(decodedBytes == nullptr)
        decodedBytes = new std::vector<byte>();
    decodeOpCode(nextByte);
    //Decode the next byte according to the current decoded message OpCode
    switch (decodedMessageOpCode) {

        case NOTIFICATION: decodeNotificationMessage(nextByte);
            break;
        case ACK: decodeAckMessage(nextByte);
            break;
        case ERROR: decodeErrorMessage(nextByte);
            break;
        default: break;
    }

    Message * message = currentDecodedMessage;
    if(currentDecodedMessage != nullptr && currentDecodedMessage->isCompleted())
    {
        decodedBytes->clear();
        currentDecodedOpCode->clear();
        currentDecodedMessage = nullptr;
        //Reset helper objects and return the message since it has been resolved
        return message;
    } else return nullptr;
}

void MessageEncoderDecoder::decodeOpCode(byte &nextByte) {
    //Decodes message OpCode from bytes to short value
    if(currentDecodedOpCode == nullptr)
        currentDecodedOpCode = new std::vector<byte>();
    if (currentDecodedOpCode->size() < OPCODE_SIZE)
        currentDecodedOpCode->push_back(nextByte);
    else if(decodedMessageOpCode == NULL_OPCODE)
        decodedMessageOpCode = bytesToShort(&(*currentDecodedOpCode)[0]);
}

void MessageEncoderDecoder::decodeAckMessage(byte &nextByte) {

    if(decodeStage == 0) {
        decodedBytes->push_back(nextByte);
        if(decodedBytes->size() == OPCODE_SIZE) {
            decodedAckMessageOpCode = bytesToShort(&(*decodedBytes)[0]);
            decodedBytes->clear();
            decodeStage++;
        }
        else return;
    }
    short ackType = -1;
    std::set<short> nonDefaultAckTypes;
    if(decodeStage == 1) {
        nonDefaultAckTypes.insert(AckMessage::AckTypes::FOLLOW);
        nonDefaultAckTypes.insert(AckMessage::AckTypes::STAT);
        nonDefaultAckTypes.insert(AckMessage::AckTypes::USERLIST);
        //Determine the type of the ack response
        ackType = (nonDefaultAckTypes.find(decodedAckMessageOpCode) != nonDefaultAckTypes.end()) ? decodedAckMessageOpCode : AckMessage::AckTypes::DEFAULT;
    }
    //Decode rest of the message by the type of the ack message received
    switch((ackType == -1) ? ((AckMessage*) currentDecodedMessage)->getMessageAck() : ackType) {
        case AckMessage::AckTypes::DEFAULT:
            if(currentDecodedMessage == nullptr)
                currentDecodedMessage = new AckMessage();
            ((AckMessage*)currentDecodedMessage) ->setMessageAck(decodedAckMessageOpCode);
            completeAndResetDecode();
            break;
        case AckMessage::AckTypes ::FOLLOW:
            decodeFollowAck(nextByte);
            break;
        case AckMessage::AckTypes::USERLIST:
            decodeUserlistAck(nextByte);
            break;
        case AckMessage::AckTypes::STAT:
            decodeStatAck(nextByte);
            break;
    }
}

void MessageEncoderDecoder::decodeFollowAck(byte &nextByte) {
    if(currentDecodedMessage == nullptr)
        currentDecodedMessage = new FollowAck();
    ((FollowAck*)currentDecodedMessage) ->setMessageAck(decodedAckMessageOpCode);
    //Decode the follow message next byte according to current decode stage
    switch (decodeStage) {
        case 1:
            decodeStage++;
            return;
        //Case 2 - decode num of users in user list (successfully followed user count)
        case 2:
            decodedBytes->push_back(nextByte);
            if(decodedBytes->size() == OPCODE_SIZE) {
                ((FollowAck *) currentDecodedMessage)->setNumOfUsers(bytesToShort(&(*decodedBytes)[0]));
                decodedBytes->clear();
                decodeStage++;
            }
            break;
        //Default - decode user names list (successfully followed user names)
        default:
            int userNum = decodeStage-3;
            if(nextByte == ZERO_BYTE) {
                userNum++;
                decodeStage++;
                if(userNum != ((FollowAck *) currentDecodedMessage)->getNumOfUsers()) {
                    decodedBytes->push_back(' ');
                    return;
                }
            }
            if(userNum != ((FollowAck *) currentDecodedMessage)->getNumOfUsers())
                decodedBytes->push_back(nextByte);

            if(userNum == ((FollowAck *) currentDecodedMessage)->getNumOfUsers()) {
                std::string userlist(decodedBytes->data(), decodedBytes->size());
                ((FollowAck *) currentDecodedMessage)->setUserList(std::move(userlist));
                completeAndResetDecode();
            }
            break;
    }
}

void MessageEncoderDecoder::decodeUserlistAck(byte &nextByte) {
    if(currentDecodedMessage == nullptr)
        currentDecodedMessage = new UserlistAck();
    ((UserlistAck*)currentDecodedMessage) ->setMessageAck(decodedAckMessageOpCode);
    //Decode the userlist message next byte according to current decode stage
    switch (decodeStage) {
        case 1:
            decodeStage++;
            return;
        //Case 2 - decode num of users in user list
        case 2:
            decodedBytes->push_back(nextByte);
            if(decodedBytes->size() == OPCODE_SIZE) {
                ((UserlistAck*) currentDecodedMessage)->setNumOfUsers(bytesToShort(&(*decodedBytes)[0]));
                decodedBytes->clear();
                decodeStage++;
            }
            break;
        //Default - decode user names list
        default:
            int userNum = decodeStage-3;
            if(nextByte == ZERO_BYTE) {
                userNum++;
                decodeStage++;
                if(userNum != ((UserlistAck*) currentDecodedMessage)->getNumOfUsers()) {
                    decodedBytes->push_back(' ');
                    return;
                }
            }
            if(userNum != ((UserlistAck*) currentDecodedMessage)->getNumOfUsers())
                decodedBytes->push_back(nextByte);

            if(userNum == ((UserlistAck*) currentDecodedMessage)->getNumOfUsers()) {
                std::string userlist(decodedBytes->data(), decodedBytes->size());
                ((UserlistAck*) currentDecodedMessage)->setUserList(std::move(userlist));
                completeAndResetDecode();
            }
            break;
    }
}

void MessageEncoderDecoder::decodeStatAck(byte &nextByte) {
    if(currentDecodedMessage == nullptr)
        currentDecodedMessage = new StatAck();
    ((StatAck*)currentDecodedMessage) ->setMessageAck(decodedAckMessageOpCode);
    //Decode the user statistics message next byte according to current decode stage
    switch (decodeStage) {
        case 1:
            decodeStage++;
            return;
        //Case 2 - decode num of posts by user
        case 2:
            decodedBytes->push_back(nextByte);
            if(decodedBytes->size() == OPCODE_SIZE) {
                ((StatAck*) currentDecodedMessage)->setNumPosts(bytesToShort(&(*decodedBytes)[0]));
                decodedBytes->clear();
                decodeStage++;
            }
            break;
        //Case 3 - decode num of user followers
        case 3:
            decodedBytes->push_back(nextByte);
            if(decodedBytes->size() == OPCODE_SIZE) {
                ((StatAck*) currentDecodedMessage)->setNumFollowers(bytesToShort(&(*decodedBytes)[0]));
                decodedBytes->clear();
                decodeStage++;
            }
            break;
        //Case 4 - decode num of users the user is following
        case 4:
            decodedBytes->push_back(nextByte);
            if(decodedBytes->size() == OPCODE_SIZE) {
                ((StatAck*) currentDecodedMessage)->setNumFollowing(bytesToShort(&(*decodedBytes)[0]));
                completeAndResetDecode();
            }
            break;
    }
}

void MessageEncoderDecoder::decodeNotificationMessage(byte &nextByte) {
    if(currentDecodedMessage == nullptr)
        currentDecodedMessage = new NotificationMessage();
    //Decode the notification message next byte according to current decode stage
    switch (decodeStage) {
        //Case 0 - decode notification message type (PM/Public)
        case 0: ((NotificationMessage*)currentDecodedMessage)->setPM(nextByte == 0);
        decodeStage++;
        break;
        //Case 1 - decode posting user name
        case 1:
            if(nextByte != ZERO_BYTE)
                decodedBytes->push_back(nextByte);
            else {
                std::string postingUser(decodedBytes->data(), decodedBytes->size());
                ((NotificationMessage*)currentDecodedMessage)->setPostingUser(std::move(postingUser));
                decodedBytes->clear();
                decodeStage++;
            }
            break;
        //Case 2 - decode message content
        case 2:
            if(nextByte != ZERO_BYTE)
                decodedBytes->push_back(nextByte);
            else {
                std::string content(decodedBytes->data(), decodedBytes->size());
                ((NotificationMessage*)currentDecodedMessage)->setContent(std::move(content));
                completeAndResetDecode();
            }
    }
}

void MessageEncoderDecoder::decodeErrorMessage(byte &nextByte) {
    if(currentDecodedMessage == nullptr)
        currentDecodedMessage = new ErrorMessage();
    decodedBytes->push_back(nextByte);
    //decode the OpCode the error message was sent for
    if(decodedBytes->size() == OPCODE_SIZE) {
        ((ErrorMessage *) currentDecodedMessage)->setErrorMessageOpCode(bytesToShort(&(*decodedBytes)[0]));
        completeAndResetDecode();
    }
}

void MessageEncoderDecoder::completeAndResetDecode() {
    currentDecodedMessage->complete();
    currentDecodedOpCode->clear();
    decodedBytes->clear();
    decodedAckMessageOpCode = NULL_OPCODE;
    decodedMessageOpCode = NULL_OPCODE;
    decodeStage = 0;
}


byte* MessageEncoderDecoder::encode(short &opCode, std::string &params) {
    if(encodedBytes == nullptr)
        encodedBytes = new std::vector<byte>();
    encodedBytes->clear();
    try {
        std::vector<std::string> parameters;
        //Encode a message according to the message OpCode
        switch (opCode) {
            case Commands::REGISTER: {
                parameters = parseCommandParameters(params);
                std::string username;
                std::string password;
                try {
                    username = parameters.at(0);
                    password = parameters.at(1);
                } catch (std::exception &e) {}
                return encodeRegisterCommand(opCode, username, password);
            }
            case Commands::LOGIN:{
                parameters = parseCommandParameters(params);
                std::string username;
                std::string password;
                try {
                    username = parameters.at(0);
                    password = parameters.at(1);
                } catch (std::exception &e) {}
                return encodeLoginCommand(opCode, username, password);
            }
            case Commands::LOGOUT:
                return encodeLogoutCommand(opCode);
            case Commands::FOLLOW:
                parameters = parseCommandParameters(params);
                return encodeFollowCommand(opCode, parameters);
            case Commands::POST:
                return encodePostCommand(opCode, params);
            case Commands::PM:
                return encodePMCommand(opCode, params);
            case Commands::USERLIST:
                return encodeUserlistCommand(opCode);
            case Commands::STAT:
                parameters = parseCommandParameters(params);
                return encodeStatCommand(opCode, parameters.at(0));
        }
    }catch (std::out_of_range &e){
        std::cerr << "INVALID COMMAND PARAMETERS: " << e.what() << std::endl;
    }
    return nullptr;
}

byte* MessageEncoderDecoder::encodeRegisterCommand(short &opCode, std::string &username, std::string &password) {
    if(username.empty() || password.empty())
        throw std::out_of_range("Did not provide username or password");

    //Message representation - OpCode | Username | Password
    appendShort(opCode, *encodedBytes);
    appendString(username, *encodedBytes);
    appendString(password, *encodedBytes);
    this->messageLength = (int)encodedBytes->size();
    return &(*encodedBytes)[0];
}

byte* MessageEncoderDecoder::encodeLoginCommand(short &opCode, std::string &username, std::string &password) {
    if(username.empty() || password.empty())
        throw std::out_of_range("Did not provide username or password");

    //Message representation - OpCode | Username | Password
    appendShort(opCode, *encodedBytes);
    appendString(username, *encodedBytes);
    appendString(password, *encodedBytes);
    this->messageLength = (int)encodedBytes->size();
    return &(*encodedBytes)[0];
}

byte* MessageEncoderDecoder::encodeLogoutCommand(short &opCode) {
    //Message representation - OpCode
    appendShort(opCode, *encodedBytes);
    this->messageLength = (int)encodedBytes->size();
    return &(*encodedBytes)[0];
}

byte* MessageEncoderDecoder::encodeFollowCommand(short &opCode, std::vector<std::string> parameters) {
    if(parameters.empty())
        throw std::out_of_range("Did not provide userlist");

    //Message representation - OpCode | Follow\Unfollow | NumOfUsers | UsernameList
    try {
        byte follow = (byte) atoi(parameters.at(0).c_str());
        auto numOfUsers = (short) std::stoi(parameters.at(1).c_str());

        appendShort(opCode, *encodedBytes);
        encodedBytes->push_back((byte) follow);
        appendShort(numOfUsers, *encodedBytes);
        for (unsigned int i = 2; i < ((unsigned int)numOfUsers + 2); i++)
            appendString(parameters.at(i), *encodedBytes);
        this->messageLength = (int)encodedBytes->size();
    } catch (std::exception & e) {
        resetEncoding();
        std::cerr << "Command Format error, probably missing parameters or wrong parameter use." << std::endl;
    }
    return &(*encodedBytes)[0];
}

byte* MessageEncoderDecoder::encodePostCommand(short &opCode, std::string &content) {
    if(content.empty())
        throw std::out_of_range("No message content provided.");

    //Message representation - OpCode | Content
    appendShort(opCode, *encodedBytes);
    appendString(content, *encodedBytes);
    this->messageLength = (int)encodedBytes->size();

    return &(*encodedBytes)[0];
}

byte* MessageEncoderDecoder::encodePMCommand(short &opCode, std::string &params) {

    std::string::size_type pos = params.find(' ',0);
    std::string username= params.substr(0,pos);
    if(username.empty())
        throw std::out_of_range("No recipient provided.");
    std::string content= params.substr(pos+1);
    if(content.empty())
        throw std::out_of_range("No message content provided.");

    //Message representation - OpCode | Recipient | Content
    appendShort(opCode, *encodedBytes);
    appendString(username, *encodedBytes);
    appendString(content, *encodedBytes);
    this->messageLength = (int)encodedBytes->size();

    return &(*encodedBytes)[0];
}

byte* MessageEncoderDecoder::encodeUserlistCommand(short &opCode) {

    //Message representation - OpCode
    appendShort(opCode, *encodedBytes);
    this->messageLength = (int)encodedBytes->size();
    return &(*encodedBytes)[0];
}

byte* MessageEncoderDecoder::encodeStatCommand(short &opCode, std::string &username) {

    if(username.empty())
        throw std::out_of_range("No username provided.");

    //Message representation - OpCode | Username
    appendShort(opCode, *encodedBytes);
    appendString(username, *encodedBytes);
    this->messageLength = (int)encodedBytes->size();

    return &(*encodedBytes)[0];
}

void MessageEncoderDecoder::resetEncoding() {
    encodedBytes->clear();
    messageLength = 0;
}

std::vector<std::string> MessageEncoderDecoder::parseCommandParameters(std::string &params) {
    std::vector<std::string> parameters;
    std::istringstream iss(params);
    std::string out;
    while (getline(iss, out, ' ')) {
        parameters.push_back(out);
    }
    if(parameters.empty())
        throw std::out_of_range("Missing command parameters.");
    return parameters;
}


short MessageEncoderDecoder::bytesToShort(char* bytesArr)
{
    auto result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

int MessageEncoderDecoder::getMessageLength() {
    return this->messageLength;
}

void MessageEncoderDecoder::appendShort(short &num, std::vector<byte> &target)
{
    target.push_back((num >> 8) & 0xFF);
    target.push_back(num & 0xFF);
}

void MessageEncoderDecoder::appendString(std::string &str, std::vector<byte> &target)
{
    std::vector<byte> bytes(str.begin(), str.end()+1);
    for(byte b : bytes)
        target.push_back(b);
}
