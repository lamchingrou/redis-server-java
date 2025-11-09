import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    int port = 6379;
    boolean listening = true;

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      serverSocket.setReuseAddress(true);

      while (listening) {
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
              // For ECHO command, extract the message to echo back
              // Simple parsing: find the last line which should be the message
              String[] lines = inputString.split("\\r?\\n");
              String message = "";
              if (lines.length > 0) {
                  message = lines[lines.length - 1]; // Last line is the message
              }
              String output = "$" + message.length() + "\r\n" + message + "\r\n";
              clientSocket.getOutputStream().write(output.getBytes());
          } else {
            // Default response for PING or other commands
            clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
          }
      }
    } catch (IOException e) { // client level errors
      System.out.println("IOException: " + e.getMessage());
    }
  }
}