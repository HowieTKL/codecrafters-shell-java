import org.howietkl.shell.Trie;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);


  public static void main2(String[] args) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1");
    processBuilder.inheritIO();
    Process rawMode = processBuilder.start();

    try (InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(inputStreamReader);) {
      while (true) {
        System.out.print("$ ");
        System.out.flush();
        StringBuilder buf = new StringBuilder();
        while (true) {
          int c = in.read();
          if (c == '\t') {
            LOG.debug("tab");
          } else if (c == '\r' || c == '\n') {
            System.out.println();
            break;
          } else if (c == 127 || c == '\b') {
            if (!buf.isEmpty()) {
              buf.deleteCharAt(buf.length() - 1);
              System.out.print("\b \b");
            }
          } else {
            System.out.print((char) c);
            buf.append((char) c);
          }
        }
        if ("exit".equals(buf.toString().trim())) {
          break;
        }
      }
    }

  }



  public static void main(String[] args) throws Exception {
    Set<String> commands = new HashSet<>();
    commands.add("echo");
    commands.add("type");
    commands.add("exit");
    commands.add("pwd");
    commands.add("cd");

    Trie trie = new Trie();
    commands.forEach(trie::insert);


    ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "stty -echo -icanon min 1 < /dev/tty");
    processBuilder.directory(new File("").getCanonicalFile());
    Process rawMode = processBuilder.start();
    rawMode.waitFor();

    try (InputStreamReader inputStreamReader = new InputStreamReader(System.in);
         BufferedReader in = new BufferedReader(inputStreamReader);) {
      while (true) {
        StringBuilder buf = new StringBuilder();
        System.out.print("$ ");
        while (true) {
          int ch = in.read();
          if (ch == '\t') {
            List<String> auto = trie.autocomplete(buf.toString());
            if (auto.size() == 1) {
              String remaining = auto.getFirst().substring(buf.toString().length()) + " ";
              buf.append(remaining);
              System.out.print(remaining);
            }
          } else if (ch == '\r' || ch == '\n') {
            System.out.println();
            break;
          } else if (ch == 127 || ch == '\b') {
            if (!buf.isEmpty()) {
              System.out.print("\b \b");
              buf.deleteCharAt(buf.length() - 1);
            }
          } else {
            buf.append((char) ch);
            System.out.print((char) ch);
            System.out.flush();
          }
        }

        String input = buf.toString();
        Deque<String> inputs = split(input);
        if (inputs.isEmpty()) {
          continue;
        }

        String cmd = inputs.removeFirst();

        switch (cmd) {
          case "exit" -> {
            System.exit(0);
          }
          case "type" -> {
            String param = inputs.removeFirst();
            if (commands.contains(param)) {
              System.out.println(param + " is a shell builtin");
            } else {
              String path = checkCommand(param);
              if (path != null) {
                System.out.println(param + " is " + path);
              } else {
                System.out.println(param + ": not found");
              }
            }
          }
          case "pwd" -> {
            System.out.println(processBuilder.directory());
          }
          case "cd" -> {
            String param = inputs.removeFirst();
            File dir = getDir(processBuilder.directory(), param);
            if (dir != null) {
              processBuilder.directory(dir);
            } else {
              System.out.println(param + ": No such file or directory");
            }
          }
          default -> {
            if (checkCommand(cmd) != null) {
              inputs.addFirst(cmd);
              OutputStream out = System.out;
              OutputStream err = System.err;
              if (inputs.contains(">") || inputs.contains("1>") || inputs.contains("2>") ||
                  inputs.contains(">>") || inputs.contains("1>>") || inputs.contains("2>>")) {
                Iterator<String> i = inputs.iterator();
                while (i.hasNext()) {
                  String param = i.next();
                  switch (param) {
                    case ">", "1>" -> {
                      i.remove();
                      if (i.hasNext()) {
                        out = new FileOutputStream(i.next());
                        i.remove();
                      }
                    }
                    case "2>" -> {
                      i.remove();
                      if (i.hasNext()) {
                        err = new FileOutputStream(i.next());
                        i.remove();
                      }
                    }
                    case ">>", "1>>" -> {
                      i.remove();
                      if (i.hasNext()) {
                        out = new FileOutputStream(i.next(), true);
                        i.remove();
                      }
                    }
                    case "2>>" -> {
                      i.remove();
                      if (i.hasNext()) {
                        err = new FileOutputStream(i.next(), true);
                        i.remove();
                      }
                    }
                  }
                }
              }
              processBuilder.command(inputs.toArray(new String[0]));
              Process process = processBuilder.start();
              process.getInputStream().transferTo(out);
              process.getErrorStream().transferTo(err);
              System.out.flush();
              process.waitFor();
            } else {
              System.out.println(input + ": command not found");
            }
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

  private static File getDir(File currentDir, String path) throws IOException {
    // home path ~
    if (path.startsWith("~")) {
      path = path.replace("~", System.getenv("HOME"));
    }

    // relative paths
    File f = new File(path);
    if (!f.isAbsolute()) {
      f = new File(currentDir, path).getCanonicalFile();
    }

    if (f.exists() && f.isDirectory()) {
      return f;
    }
    return null;
  }

  private static Deque<String> split(String input) {
    Deque<String> args = new ArrayDeque<>();
    StringBuilder arg = new StringBuilder();
    boolean inQuotes = false;
    boolean inDoubleQuotes = false;
    boolean escapingInDoubleQuotes = false;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c == '\'' && !inDoubleQuotes) {
        inQuotes = !inQuotes;
      } else if (inQuotes) {
        arg.append(c);
      } else if (c == '"') {
        inDoubleQuotes = !inDoubleQuotes;
      } else if (inDoubleQuotes) {
        if (c == '\\') {
          c = input.charAt(++i);
          switch (c) {
            case '\\', '$', '"' -> arg.append(c);
            default -> arg.append('\\').append(c);
          }
        } else {
          arg.append(c);
        }
      } else if (Character.isWhitespace(c)) {
        if (!arg.isEmpty()) {
          args.add(arg.toString());
          arg = new StringBuilder();
        }
      } else {
        if (c == '\\') {
          arg.append(input.charAt(++i));
        } else {
          arg.append(c);
        }
      }
    }
    if (!arg.isEmpty()) {
      args.add(arg.toString());
    }
    return args;
  }

}
