import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;

public class Main {
    private static Map<String, String> dataStore = new HashMap<>();

  public static void main(String[] args) {
    int port = 6379;

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("New client connected");

        new Thread(() -> handleClient(clientSocket))
            .start(); // todo replace by threadpool later
      }
    } catch (IOException e) { // server level errors
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static void handleClient(Socket clientSocket) {
    try {
      while (true) {
          byte[] input = new byte[1024];
          clientSocket.getInputStream().read(input);
          String inputString = new String(input).trim();
          System.out.println("Received: " + inputString);
          
          if (inputString.contains("ECHO")) {
              handleEcho(inputString, clientSocket);
          } else if (inputString.contains("SET")) {
              handleSet(inputString, clientSocket);
          } else if (inputString.contains("GET")) {
              handleGet(inputString, clientSocket);
          } else {
            // Default response for PING or other commands
            clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
          }
      }
    } catch (IOException e) { // client level errors
      System.out.println("IOException: " + e.getMessage());
    }
  }

  private static void handleEcho(String inputString, Socket clientSocket) throws IOException {
      String[] lines = inputString.split("\\r?\\n");
      String message = "";
      if (lines.length > 0) {
          message = lines[lines.length - 1]; // Last line is the message
      }
      String output = "$" + message.length() + "\r\n" + message + "\r\n";
      clientSocket.getOutputStream().write(output.getBytes());
  }

  private static void handleSet(String inputString, Socket clientSocket) throws IOException {
      String[] lines = inputString.split("\\r?\\n");
      if (lines.length >= 7) {
          String key = lines[4];
          String value = lines[6];
          dataStore.put(key, value);
          String output = "+OK\r\n";
          clientSocket.getOutputStream().write(output.getBytes());
      } else {
          String output = "-ERR wrong number of arguments for 'set' command\r\n";
          clientSocket.getOutputStream().write(output.getBytes());
      }
  }

  private static void handleGet(String inputString, Socket clientSocket) throws IOException {
      String[] lines = inputString.split("\\r?\\n");
      if (lines.length >= 5) {
          String key = lines[4];
          String value = dataStore.get(key);
          if (value != null) {
              String output = "$" + value.length() + "\r\n" + value + "\r\n";
              clientSocket.getOutputStream().write(output.getBytes());
          } else {
              String output = "$-1\r\n"; // Null bulk string
              clientSocket.getOutputStream().write(output.getBytes());
          }
      } else {
          String output = "-ERR wrong number of arguments for 'get' command\r\n";
          clientSocket.getOutputStream().write(output.getBytes());
      }
  }
}