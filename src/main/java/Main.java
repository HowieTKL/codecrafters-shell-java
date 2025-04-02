import java.util.Scanner;

public class Main {
  public static void main(String[] args) throws Exception {
    while (true) {
      System.out.print("$ ");
      Scanner scanner = new Scanner(System.in);
      String input = scanner.nextLine();
      if ("exit 0".equals(input)) {
        System.exit(0);
      }
      System.out.println(input + ": command not found");
    }
  }

}
