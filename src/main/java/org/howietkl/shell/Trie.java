package org.howietkl.shell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Trie {

  private static class Node {
    private final Map<Character, Node> children = new HashMap<>();
    private boolean isWord;

    public Map<Character, Node> getChildren() {
      return children;
    }

    public void setEndOfWord(boolean endOfWord) {
      isWord = endOfWord;
    }

    public boolean isEndOfWord() {
      return isWord;
    }

    @Override
    public String toString() {
      return (isEndOfWord() ? "*" : "") + children.toString();
    }
  }

  private final Node root = new Node();

  public void insert(String word) {
    Node current = root;

    for (char ch: word.toCharArray()) {
      current = current.getChildren().computeIfAbsent(ch, c -> new Node());
    }
    current.setEndOfWord(true);
  }

  public boolean find(String word) {
    Node current = root;
    for (int i = 0; i < word.length(); i++) {
      char ch = word.charAt(i);
      Node node = current.getChildren().get(ch);
      if (node == null) {
        return false;
      }
      current = node;
    }
    return current.isEndOfWord();
  }

  public boolean isEmpty() {
    return root.getChildren().isEmpty();
  }

  public List<String> autocomplete(String prefix) {
    List<String> results = new LinkedList<>();
    Node current = root;
    for (char c : prefix.toCharArray()) {
      if (!current.children.containsKey(c)) {
        return results;
      }
      current = current.children.get(c);
    }
    autocomplete(current, results, prefix);
    return results;
  }

  private void autocomplete(Node node, List<String> res, String prefix) {
    if (node.isEndOfWord()) {
      res.add(prefix);
    }
    for (char c : node.children.keySet()) {
      autocomplete(node.children.get(c), res, prefix + c);
    }
  }

  @Override
  public String toString() {
    return root.toString();
  }

}
