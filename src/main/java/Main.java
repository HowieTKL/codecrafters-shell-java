import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
  public static void main(String[] args) throws Exception {
    Set<String> commands = new HashSet<>();
    commands.add("echo");
    commands.add("type");
    commands.add("exit");

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
        if (commands.contains(cmd)) {
          System.out.println(cmd + " is a shell builtin");
        } else {
          System.out.println(cmd + ": not found");
        }
      } else {
        System.out.println(input + ": command not found");
      }
    }
  }

}
