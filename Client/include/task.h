//
// Created by shani on 1/9/18.
//


#ifndef MOVIECLIENT_TASKLISTENTOKEYBOARD_H
#define MOVIECLIENT_TASKLISTENTOKEYBOARD_H

#include "../include/ConnectionHandler.h"

class task {

private:
    ConnectionHandler & _connectionHandler;

public:
    task(ConnectionHandler & connectionHandler);
    void run();
};


#endif //MOVIECLIENT_TASKLISTENTOKEYBOARD_H
