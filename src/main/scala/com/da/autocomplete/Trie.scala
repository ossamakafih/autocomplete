package com.da.autocomplete

class Trie  {

  var children: Map[Char, Trie] = Map()
  var value: Option[String] = None


  def add(char: Char): Trie = {
    val trie = new Trie
    children = children + (char -> trie)
    trie
  }


  def insert(word: String) = {
    val child = DeepestChild(prefix = word,
      defaultWhenNotFound = (char, trie) => trie.add(char))
    child.value = Some(word)
  }


  def contains(word: String): Boolean = {
    var current = Option(this)
    word.foreach { (char: Char) =>
      current = current.get.children.get(char)
    }
    current match {
      case Some(x) if x.value.nonEmpty => true
      case None => false
    }
  }


  def allValues: Set[String] = {
    value.fold[Set[String]](Set.empty)(Set(_)) ++ children.flatMap{ case(_, trie) => trie.allValues}
  }


  def autocomplete(prefix: String): collection.mutable.SortedSet[String] = {
    val child = DeepestChild(prefix,
      defaultWhenNotFound = (_,_) => new Trie)
    collection.mutable.SortedSet(child.allValues.toList: _*)
  }

  private def DeepestChild(prefix: String, defaultWhenNotFound: (Char, Trie) => Trie): Trie = {
    var current = this
    prefix.foreach { (char: Char) =>
      current = current.children.getOrElse(char, defaultWhenNotFound(char, current))
    }
    current
  }

  override def toString: String = {
    children.map { case (char, trie) =>
      s"{$char: ${trie.toString}${trie.value.map(v => s"'$v'").getOrElse("")}}"
    }.mkString(",")
  }

}


