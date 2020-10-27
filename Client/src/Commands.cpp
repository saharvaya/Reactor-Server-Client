#include <Commands.h>

Commands::Commands() : commands() {
    initCommands();
}

void Commands::initCommands() {
    //Init the legal commands vector
    commands.push_back("REGISTER");
    commands.push_back("LOGIN");
    commands.push_back("LOGOUT");
    commands.push_back("FOLLOW");
    commands.push_back("POST");
    commands.push_back("PM");
    commands.push_back("USERLIST");
    commands.push_back("STAT");
}

bool Commands::isValidCommand(std::string input)
{
    //Checks the first word in the input parameter is a legal command (contained in the legal commands vector)
    size_t index = input.find_first_of(' ');
    if (index == std::string::npos)
        index = input.size();

    std::string command = input.substr(0, index);
    return find(commands.begin(), commands.end(), command) != commands.end();
}

bool Commands::isLogoutCommand(std::string &command) {
    return command == "LOGOUT";
}

std::vector<const char*>* Commands::getCommands() {
    return &commands;
}
