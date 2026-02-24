# CSC 3374 - Mini Project 1
**Presented by: Afaf Bentakhou, Hajar Mrabet, Imane Boughamza**

A multi-threaded chat application implemented in Java with JavaFX GUI, demonstrating TCP socket programming, concurrent client handling, and a clean user interface for both server and client applications.

## Overview

This project implements a real-time chat system using TCP sockets in Java. It features a server application that can handle multiple concurrent clients, and a client application with an intuitive JavaFX interface. The application demonstrates fundamental networking concepts, multi-threading, and GUI development in Java.

## Features

### Server Application
- **Multi-client support**: Handles multiple simultaneous client connections
- **Real-time monitoring**: Live view of connected users with color-coded display
- **Activity logging**: Timestamped logs of all server events
- **User management**: Track join/leave events and broadcast notifications
- **Control interface**: Start/stop server, clear logs, monitor status

### Client Application
- **Username-based authentication**: Join chat with a custom username
- **Read-only mode**: Option to join as observer without messaging privileges
- **Message broadcasting**: Send messages to all connected users
- **System commands**:
  - `allUsers` - List all active users
  - `bye` or `end` - Disconnect from chat
- **Visual message styling**: Different styling for own messages, peer messages, and system messages
- **Connection status indicator**: Visual feedback of connection state

## Architecture

### Components

#### Client Side
- `Client.java` - Core client logic with socket management
- `ClientApplication.java` - JavaFX application entry point
- `ClientController.java` - GUI controller and event handling
- `ClientLauncher.java` - Main class with command-line argument parsing
- `MessageListener.java` - Interface for message reception

#### Server Side
- `Server.java` - Main server class with connection acceptance
- `ServerApplication.java` - JavaFX application entry point
- `ServerController.java` - Server GUI controller
- `ServerLauncher.java` - Main class with configuration loading
- `ClientHandler.java` - Per-client thread for message handling

### Network Protocol
- **TCP/IP** communication over configurable port (default: 3333)
- **Message format**: Plain text with newline termination
- **First message**: Username identification
- **Special commands**: `allUsers`, `bye`, `end`

## Getting Started

### Prerequisites

#### Required Software
- **Java Development Kit (JDK) 11 or higher**
- **JavaFX SDK** (if using JDK 11+)
- **Maven** 

#### Verify Installation
Open a terminal/command prompt and run:
```bash
java -version
````
### Commands to Run
Open a terminal/command prompt and run:
```bash
cmd to run server: java -jar server-1.0-SNAPSHOT.jar
````
```bash
cmd to run client: java -jar client-1.0-SNAPSHOT.jar [ServerIPAddress] [PortNumber]
````
