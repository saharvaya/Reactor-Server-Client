#include <Message.h>

Message::Message() : opCode(NULL_OPCODE), completed(false) {}

void Message::setOpCode(short opCode) {
    this->opCode = opCode;
}

short Message::getOpCode() {
    return opCode;
}

bool Message::isCompleted() {
    return completed;
}

void Message::complete() {
    completed = true;
}

NotificationMessage::NotificationMessage() : Message(), PM(false), postingUser(), content() {
    this->setOpCode(notifOpCode);
}

std::string NotificationMessage::getContent() {
    return content;
}

std::string NotificationMessage::getPostingUser() {
    return  postingUser;
}

std::string NotificationMessage::getPM() {
    return (PM) ? "PM" : "Public";
}

void NotificationMessage::setContent(std::string content) {
    this->content = std::move(content);
}

void NotificationMessage::setPostingUser(std::string postingUser) {
    this->postingUser = std::move(postingUser);
}

void NotificationMessage::setPM(bool PM) {
    this->PM = PM;
}

AckMessage::AckMessage() : Message(), type(AckMessage::AckTypes::DEFAULT), messageAck(NULL_OPCODE) {
    this->setOpCode(ackOpCode);
}

AckMessage::AckTypes AckMessage::getType() {
    return type;
}

void AckMessage::setMessageAck(short &ack) {
    this->messageAck = ack;
}

short AckMessage::getMessageAck() {
    return messageAck;
}

void AckMessage::setType(AckMessage::AckTypes type) {
    this->type = type;
}

FollowAck::FollowAck() : AckMessage(), numOfUsers(0), usernameList()  {
    this->setType(AckMessage::AckTypes::FOLLOW);
}

void FollowAck::setUserList(std::string users) {
    this->usernameList = std::move(users);
}

void FollowAck::setNumOfUsers(short numOfUsers) {
    this->numOfUsers = numOfUsers;
}

short FollowAck::getNumOfUsers() {
    return numOfUsers;
}

std::string FollowAck::getUsernameList() {
    return usernameList;
}

UserlistAck::UserlistAck() : AckMessage(), numOfUsers(0), usernameList() {
    this->setType(AckMessage::AckTypes::USERLIST);
}

void UserlistAck::setUserList(std::string users) {
    this->usernameList = std::move(users);
}

void UserlistAck::setNumOfUsers(short numOfUsers) {
    this->numOfUsers = numOfUsers;
}

short UserlistAck::getNumOfUsers() {
    return numOfUsers;
}

std::string UserlistAck::getUsernameList() {
    return usernameList;
}

StatAck::StatAck() : AckMessage(), numPosts(0), numFollowers(0), numFollowing(0)  {
    this->setType(AckMessage::AckTypes::STAT);
}

void StatAck::setNumPosts(short num) {
    this->numPosts = num;
}

void StatAck::setNumFollowers(short num) {
    this->numFollowers = num;
}

void StatAck::setNumFollowing(short num) {
    this->numFollowing = num;
}

short StatAck::getNumPosts() {
    return numPosts;
}

short StatAck::getNumFollowers() {
    return numFollowers;
}

short StatAck::getNumFollowing() {
    return numFollowing;
}

ErrorMessage::ErrorMessage() : Message(), errorMessageOpCode(NULL_OPCODE) {
    this->setOpCode(erOpCode);
}

short ErrorMessage::getMessageCode() {
    return errorMessageOpCode;
}

void ErrorMessage::setErrorMessageOpCode(short errOpCode) {
    this->errorMessageOpCode = errOpCode;
}

