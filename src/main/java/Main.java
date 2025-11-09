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
          if (inputString.contains("ECHO")) {
              String[] parts = inputString.split(" ", 2);
              String output = "$" + parts[1].length() + "\r\n" + parts[1] + "\r\n";
              clientSocket.getOutputStream().write(output.getBytes());
          } else {
            System.out.println("Received: " + inputString);
            clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
          }
      }
    } catch (IOException e) { // client level errors
      System.out.println("IOException: " + e.getMessage());
    }
  }
}