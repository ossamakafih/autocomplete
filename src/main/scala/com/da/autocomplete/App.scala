package com.da.autocomplete
import scala.io.Source

object App {

  def readFile(path: String): List[String] = {
    val bufferSource = Source.fromFile(path)
    val lines =  bufferSource.getLines.toList

    bufferSource.close()

    lines
  }

  def main(args: Array[String]): Unit = {
    val trie = new Trie

    val lines = readFile("word")

    for (word <- lines){
      trie.insert(word)
    }
    trie.autocomplete("pr").foreach(word => println(word))
  }
}
