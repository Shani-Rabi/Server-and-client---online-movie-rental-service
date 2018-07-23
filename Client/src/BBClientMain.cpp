

#include <iostream>

#include "../include/ConnectionHandler.h"

#include <stdlib.h>
#include "../include/task.h"
#include <boost/thread.hpp>


int main (int argc, char *argv[]) {


    if (argc < 3) {

        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler  connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        return 1;
    }
    task task1 (connectionHandler);
    boost::thread th1(boost::bind(&task::run,&task1));
//this is the first thread job.
    //first thread job is to read from the server a message. if the message is "ACK signout succeeded ,he changed the flag of should terminate to true and stop.

    while (!connectionHandler.shouldTerminate()){

        std::string answer;

        if (!connectionHandler.getLine(answer)) {
            break;
        }


        size_t len = answer.length();

        answer.resize(len - 1);
        std::cout<< answer << std::endl;
        if (answer.compare( "ACK signout succeeded")==0) {
            connectionHandler.setShouldTerminate(true);
            std::cout<<"Ready to exit. Press enter"<<std::endl;
            break;
        }


    }
    th1.join();
    connectionHandler.close();
    return 0;

}

