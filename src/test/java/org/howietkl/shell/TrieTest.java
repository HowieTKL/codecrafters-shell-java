package org.howietkl.shell;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrieTest {
  private static final Logger LOG = LoggerFactory.getLogger(TrieTest.class);

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void insert() {
    Trie trie = createExampleTrie();
    LOG.debug("{}", trie.toString());
    assertFalse(trie.isEmpty());
  }

  @Test
  void find() {
    Trie trie = createExampleTrie();
    assertTrue(trie.find("all"));
    assertTrue(trie.find("moon"));
    assertTrue(trie.find("moo"));
    assertTrue(trie.find("always"));

    assertFalse(trie.find("al"));
    assertFalse(trie.find("mo"));
    assertFalse(trie.find("oft"));
  }

  @Test
  void autocomplete() {
    Trie trie = createExampleTrie();
    assertEquals(List.of("moo", "moon", "moose"), trie.autocomplete("moo"));
  }


  private Trie createExampleTrie() {
    Trie trie = new Trie();

    trie.insert("moo");
    trie.insert("moon");
    trie.insert("moose");
    trie.insert("morning");
    trie.insert("more");
    trie.insert("most");
    trie.insert("mostly");

    trie.insert("often");
    trie.insert("of");

    trie.insert("all");
    trie.insert("always");

    return trie;
  }
}