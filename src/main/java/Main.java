import java.util.Scanner;

public class Main {
  public static void main(String[] args) throws Exception {
    while (true) {
      System.out.print("$ ");
      Scanner scanner = new Scanner(System.in);
      String input = scanner.nextLine();
      if ("exit 0".equals(input)) {
        System.exit(0);
      } else if (input.startsWith("echo")) {
        String toEcho = input.substring(5);
        System.out.println(toEcho);
      } else if (input.startsWith("type")) {
        String cmd = input.substring(5);
        switch (cmd) {
          case "echo", "type", "exit" -> System.out.println(cmd + " is a shell builtin");
          default -> System.out.println(cmd + ": not found");
        }
      } else {
        System.out.println(input + ": command not found");
      }
    }
  }

}
