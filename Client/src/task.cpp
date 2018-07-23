//
// Created by shani on 1/9/18.
//

#include <stdlib.h>

#include "../include/ConnectionHandler.h"
#include "../include/task.h"

task::task(ConnectionHandler & connectionHandler):_connectionHandler(connectionHandler){

}
// this task will be taken by the second thread.
//his job is to read from the user and forward the message to the server.
//he will stop when the flag should terminate is true.
// the first thread will change the flag to true .
void task::run() {

    while (!_connectionHandler.shouldTerminate()  && (!std::cin.eof()) ) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if(!_connectionHandler.shouldTerminate() && !_connectionHandler.sendLine(line))
            break;
    }
}





