CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread

all: BGSClient

BGSClient: bin/Message.o bin/Commands.o bin/ClientListener.o bin/ConnectionHandler.o bin/MessageEncoderDecoder.o bin/ServerListener.o bin/Main.o 
	@echo 'Linking BGS Client'
	g++ -o bin/BGSclient bin/Message.o bin/Commands.o bin/ClientListener.o bin/ConnectionHandler.o bin/MessageEncoderDecoder.o bin/ServerListener.o bin/Main.o $(LDFLAGS)
	@echo 'BGS Client compiled and linked.'

bin/ServerListener.o: src/ServerListener.cpp
	g++ $(CFLAGS) -o bin/ServerListener.o src/ServerListener.cpp

bin/MessageEncoderDecoder.o: src/MessageEncoderDecoder.cpp
	g++ $(CFLAGS) -o bin/MessageEncoderDecoder.o src/MessageEncoderDecoder.cpp

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp
	
bin/ClientListener.o: src/ClientListener.cpp
	g++ $(CFLAGS) -o bin/ClientListener.o src/ClientListener.cpp
	
bin/Commands.o: src/Commands.cpp
	g++ $(CFLAGS) -o bin/Commands.o src/Commands.cpp
	
bin/Message.o: src/Message.cpp
	g++ $(CFLAGS) -o bin/Message.o src/Message.cpp

bin/Main.o: src/Main.cpp
	g++ $(CFLAGS) -o bin/Main.o src/Main.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
