# Terminal Chat Application

A simple terminal-based chat application that enables two clients to communicate through a server.

## Quick Start

```bash
# Start the server (port 8080)
./build.sh srv 0.0.0.0 8080

# Connect first client
./build.sh con 192.168.1.100 8080

# Connect second client
./build.sh con 192.168.1.100 8080
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
- Real-time chat between connected clients
- Simple command `/exit` to disconnect
- Automatic IP detection with `self` parameter
- Connection status notifications

## How It Works

1. Server starts and waits for two clients
2. First client connects
3. Second client connects
4. Clients can now exchange messages
5. Messages show sender's hostname
6. Either client can exit with `/exit`

## Requirements

- Java Runtime Environment (JRE)
- Terminal/Command Prompt

## Notes

- Server must be running before clients can connect
- Maximum of two clients supported
- Clients will disconnect if server closes
