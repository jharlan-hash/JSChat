# JSChat
**J**ava/**J**ack **S**overn **C**hat

A simple terminal-based chat application that enables two clients to communicate through a server.

## Quick Start

```bash
# Start the server (port 1000)
java -jar Server.java

# Connect first client
java -jar Client.java

# Connect second client
java -jar Client.java
```
### *Important* running Client.java will automatically try to connect you to my Oracle VM on port 1000. If this is not what you want, run java -jar Client.java custom <your_server_ip>

## Features

- Server supports two simultaneous clients
- End-to-end AES encryption with RSA key exchange
- Real-time chat between connected clients
- Use `/exit` to disconnect
- Use `/nick <nickname>` to change your display name 
- Connection status notifications

## Planned Features
- **Be able to connect more than two clients**
- Better user interface
- Let the server stay open after clients disconnect
