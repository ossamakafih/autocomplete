package com.da.autocomplete
import org.scalatest.{BeforeAndAfter, FunSuite}

class AutoCompleteTest extends FunSuite with BeforeAndAfter {






  test("insert char in trie"){
    val trie = new Trie
    trie.insert("o")

    assert(trie.children.head._2.value.get == "o")
  }

  test("insert word in trie"){
    val trie = new Trie
    trie.insert("amazon")
    val child1: (Char, Trie) = trie.children.head
    val child2: (Char, Trie) = child1._2.children.head
    val child3: (Char, Trie) = child2._2.children.head
    val child4: (Char, Trie) = child3._2.children.head
    assert(child1._1 === 'a')
    assert(child2._1 === 'm')
    assert(child3._1 === 'a')
  }

  test("autocomplete"){
    val trie = new Trie
    trie.insert("project runway")
    trie.insert("progenex")
    trie.insert("progeria")
    trie.insert("proactive")
    assert(trie.autocomplete("pr").head === "proactive")
    assert(trie.autocomplete("pr").last === "project runway")
  }

}
