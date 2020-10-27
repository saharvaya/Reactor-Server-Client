#ifndef MESSAGEENCODERDECODER_H_
#define MESSAGEENCODERDECODER_H_

#include <vector>
#include <string>
#include <sstream>
#include <stdio.h>
#include <algorithm>
#include <set>
#include <iostream>
#include "Message.h"
#include "Commands.h"

using byte = char;

class MessageEncoderDecoder
{
public:
    MessageEncoderDecoder(); // Constructor
    virtual ~MessageEncoderDecoder(); //Destructor
    MessageEncoderDecoder(const MessageEncoderDecoder&) = default; //Copy Constructor
    MessageEncoderDecoder(MessageEncoderDecoder&&) = default; //Move Constructor
    MessageEncoderDecoder& operator=(const MessageEncoderDecoder&) = default; //Copy Assignment Operator
    MessageEncoderDecoder& operator=(MessageEncoderDecoder&&) = default; //Move Assignment Operator
    Message* decodeNextByte(byte &nextByte);
    void decodeOpCode(byte &nextByte);
    byte* encode(short &opCode, std::string &params);
    int getMessageLength();
    enum expectedMessages { NOTIFICATION = 9, ACK = 10, ERROR = 11 };

private:
    //Functions to decode different types of messages according to the byte received
    void decodeNotificationMessage(byte &nextByte);
    void decodeAckMessage(byte &nextByte);
    void decodeFollowAck(byte &nextByte);
    void decodeUserlistAck(byte &nextByte);
    void decodeStatAck(byte &nextByte);
    void decodeErrorMessage(byte &nextByte);
    //Completes the current decoded message, and resets to wait for next message
    void completeAndResetDecode();
    //Functions to encode different types of messages to bytes according to user input
    byte * encodeRegisterCommand(short &opCode, std::string &username, std::string &password);
    byte * encodeLoginCommand(short &opCode, std::string &username, std::string &password);
    byte * encodeLogoutCommand(short &opCode);
    byte * encodeFollowCommand(short &opCode, std::vector<std::string> users);
    byte * encodePostCommand(short &opCode, std::string &content);
    byte * encodePMCommand(short &opCode, std::string &params);
    byte * encodeUserlistCommand(short &opCode);
    byte * encodeStatCommand(short &opCode, std::string &username);
    //Seperates received command parameters to seperate strings and return a vector of those strings
    std::vector<std::string> parseCommandParameters(std::string &params);
    //Resets message encoding
    void resetEncoding();
    //Helper functions to append short and string values to a vector of bytes
    void appendShort(short &num, std::vector<byte> &target);
    void appendString(std::string &str, std::vector<byte> &target);
    //Helper function to return a short from a bytes array value
    short bytesToShort(char* bytesArr);

    const signed short NULL_OPCODE = -1;
    const unsigned short OPCODE_SIZE = 2;
    const byte ZERO_BYTE = '\0';
    int messageLength;
    int decodeStage;
    std::vector<byte>* encodedBytes;
    std::vector<byte>* decodedBytes;
    std::vector<byte>* currentDecodedOpCode;
    Message* currentDecodedMessage;
    short decodedMessageOpCode;
    short decodedAckMessageOpCode;

};

#endif