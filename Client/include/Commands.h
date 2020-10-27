#ifndef COMMANDS_H
#define COMMANDS_H

#include <string>
#include <vector>
#include <set>
#include <algorithm>
#include <iostream>

class Commands {

public:
    Commands(); // Constructor
    virtual ~Commands() = default; //Destructor
    enum OpCodes { REGISTER = 1, LOGIN = 2, LOGOUT = 3, FOLLOW = 4, POST = 5, PM = 6, USERLIST = 7, STAT = 8};
    bool isValidCommand(std::string input);
    bool isLogoutCommand(std::string &command);
    std::vector<const char*>* getCommands();

private:
    void initCommands();
    std::vector<const char*> commands;
};


#endif
