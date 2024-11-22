# JSChat
**J**ava/**J**ack **S**overn **C**hat

A simple terminal-based chat application that enables two clients to communicate through a server.

## Quick Start

```bash
# Start the server (port 8080)
./build.sh srv localhost 8080

# Connect first client
./build.sh con localhost 8080

# Connect second client
./build.sh con localhost 8080
```

## Usage

```bash
./build.sh <mode> <ip> <port>
```

### Parameters

- `mode`: 
  - `srv` - Start a server
  - `con` - Connect as a client
- `ip`:
  - Use the server's IP address
  - Use `self` to automatically detect local IP
- `port`:
  - Any available port number

### Example Commands

```bash
# Start server on port 9000
./build.sh srv 0.0.0.0 9000

# Connect to local server
./build.sh con self 9000

# Connect to remote server
./build.sh con 192.168.1.100 9000
```

## Features

- Server supports two simultaneous clients
- End-to-end RSA encryption
- Real-time chat between connected clients
- Use `/exit` to disconnect
- Use `/nick <nickname>` to change your display name 
- Automatic IP detection with `self` parameter
- Connection status notifications

## Planned Features
- **Be able to connect more than two clients**
- Better user interface
- Something better than build.sh
- Let the server stay open after clients disconnect
