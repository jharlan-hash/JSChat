# JSChat

JSChat is a fully encrypted terminal-based chat application built in Java. It provides secure communication between clients using AES and RSA encryption. The project consists of a server and a client, both of which must be running for communication to occur.

## Features
- **End-to-End Encryption**: Uses AES for message encryption and RSA for key exchange.
- **Terminal-Based**: Simple command-line interface for ease of use.
- **Multi-Client Support**: The server can handle multiple clients at once.
- **Automatic Key Exchange**: Clients and the server securely exchange encryption keys at connection.

## Requirements
- Java 11 or later

## Installation
1. Clone or extract the project.
2. Navigate to the project directory.
3. Compile the project using:
   ```sh
   javac -d out src/main/java/com/jacksovern/**/*.java
   ```

## Usage
### Starting the Server
Run the server using:
```sh
java -cp out com.jacksovern.Server.Server
```

### Starting a Client
Run the client using:
```sh
java -cp out com.jacksovern.Client.Client [server-ip]
```
Replace `[server-ip]` with the server's IP address. If omitted, the default is `localhost`.

## Project Structure
```
jacksovern/
├── Client/
│   ├── Client.java
│   ├── AES.java
│   ├── RSA.java
│   └── ChatUtils.java
├── Server/
│   ├── Server.java
│   ├── ServerClient.java
│   └── Message.java
├── JSChat.java
```
- `JSChat.java`: Entry point for the application.
- `Server/`: Contains server-side logic.
- `Client/`: Contains client-side logic.

## Security
- **AES Encryption**: Encrypts messages before transmission.
- **RSA Encryption**: Handles secure key exchange between clients and server.
- **Message Integrity**: Ensures that messages cannot be tampered with during transmission.

## License
This project is licensed under the MIT License.

## Author
Developed by Jack Sovern.
