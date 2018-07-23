# Flags
CC=g++
CFLAGS  = -std=c++11 
BFLAGS  = -lboost_thread -lboost_system
LFLAGS  = -L/usr/lib


# All Targets
all: Client

# Tool invocations
Client: bin/BBClientMain.o bin/ConnectionHandler.o bin/task.o
	@echo 'Building targets'
	@echo 'Invoking: C++ Linker'
	$(CC) -o bin/BBclient bin/BBClientMain.o bin/ConnectionHandler.o bin/task.o $(LFLAGS) $(BFLAGS)
	@echo 'Finished building targets: BBclient'
	@echo ' '
	
# BBClientMain
bin/BBClientMain.o: src/BBClientMain.cpp
	$(CC) -g -Wall -Weffc++  $(CFLAGS) -c -Iinclude src/BBClientMain.cpp -o bin/BBClientMain.o


# ConnectionHandler
bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	$(CC) -g -Wall -Weffc++  $(CFLAGS) -c -Iinclude src/ConnectionHandler.cpp -o bin/ConnectionHandler.o


# task
bin/task.o: src/task.cpp
	$(CC) -g -Wall -Weffc++  $(CFLAGS) -c -Iinclude src/task.cpp -o bin/task.o

# Clean the build directory
clean: 
	rm -f bin/*
