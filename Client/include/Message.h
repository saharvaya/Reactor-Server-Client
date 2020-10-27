#ifndef MESSAGE_H
#define MESSAGE_H

#include "string"

/**
 * Contains classes that represents different message types and their parameters
 * contains getters and setters for each type of message.
 */
class Message {
public:
    explicit
    Message();
    short getOpCode();
    void setOpCode(short opCode);
    bool isCompleted();
    void complete();

    const signed short NULL_OPCODE=-1;

private:
    short opCode;
    bool completed;
};

class NotificationMessage : public Message {
public:
    NotificationMessage();
    std::string getContent();
    std::string getPostingUser();
    std::string getPM();
    void setContent(std::string content);
    void setPostingUser(std::string postingUser);
    void setPM(bool PM);

private:
    const short notifOpCode = 9;
    bool PM;
    std::string postingUser;
    std::string content;
};

class ErrorMessage : public Message {
public:
    ErrorMessage();
    short getMessageCode();
    void setErrorMessageOpCode(short errOpCode);

private:
    const short erOpCode = 11;
    short errorMessageOpCode;
};

class AckMessage : public Message {
public:
    AckMessage();
    enum AckTypes { DEFAULT = 1, FOLLOW = 4, USERLIST = 7, STAT = 8 };
    void setType(AckTypes type);
    AckTypes getType();
    void setMessageAck(short &ack);
    short getMessageAck();

private:
    AckTypes type;
    short messageAck;
    const short ackOpCode = 10;
};

class FollowAck : public AckMessage {
public:
    FollowAck();
    void setUserList(std::string users);
    void setNumOfUsers(short numOfUsers);
    short getNumOfUsers();
    std::string getUsernameList();

private:
    short numOfUsers;
    std::string usernameList;
};

class UserlistAck : public AckMessage {
public:
    UserlistAck();
    void setUserList(std::string users);
    void setNumOfUsers(short numOfUsers);
    short getNumOfUsers();
    std::string getUsernameList();

private:
    short numOfUsers;
    std::string usernameList;
};

class StatAck : public AckMessage {
public:
    StatAck();
    void setNumPosts(short num);
    void setNumFollowers(short num);
    void setNumFollowing(short num);
    short getNumPosts();
    short getNumFollowers();
    short getNumFollowing();

private:
    short numPosts;
    short numFollowers;
    short numFollowing;
};

#endif
