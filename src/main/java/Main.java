import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    Set<String> commands = new HashSet<>();
    commands.add("echo");
    commands.add("type");
    commands.add("exit");
    commands.add("pwd");
    commands.add("cd");

    File currentDir = new File("");

    while (true) {
      System.out.print("$ ");
      Scanner scanner = new Scanner(System.in);
      String input = scanner.nextLine();
      String[] inputs = input.split(" ");
      String cmd = inputs[0];

      switch (cmd) {
        case "exit" -> {
          System.exit(0);
        }
        case "echo" -> {
          String toEcho = input.substring(5);
          System.out.println(toEcho);
        }
        case "type" -> {
          String aCmd = inputs[1];
          if (commands.contains(aCmd)) {
            System.out.println(aCmd + " is a shell builtin");
          } else {
            String path = checkCommand(aCmd);
            if (path != null) {
              System.out.println(aCmd + " is " + path);
            } else {
              System.out.println(aCmd + ": not found");
            }
          }
        }
        case "pwd" -> {
          System.out.println(currentDir.getCanonicalPath());
        }
        case "cd" -> {
          File dir = getDir(inputs[1]);
          if (dir != null) {
            currentDir = dir;
          } else {
            System.out.println(inputs[1] + ": No such file or directory");
          }
        }
        default -> {
          if (checkCommand(cmd) != null) {
            Process process = Runtime.getRuntime().exec(inputs);
            process.getInputStream().transferTo(System.out);
            process.getErrorStream().transferTo(System.err);
          } else {
            System.out.println(input + ": command not found");
          }
        }
      }
    }

  }

  private static String checkCommand(String cmd) {
    String[] path = System.getenv("PATH").split(":");
    for (String p : path) {
      File f = new File(p, cmd);
      if (f.exists() && f.canExecute()) {
        return f.getAbsolutePath();
      }
    }
    return null;
  }

  private static File getDir(String path) {
    File f = new File(path);
    if (f.exists() && f.isDirectory()) {
      return f;
    }
    return null;
  }


}
