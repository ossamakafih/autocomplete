# autocomplete

This repository manages the datawarehouse (DWH) with this organization:
- **src/** : source code and unit test
- **project/** : plugins and build properties
- **target/** : for getting the jar after compiling and building project
- **build.sbt** : describe the build definition 

## 1. Conception
- The data structure used in the project is called Trie. It’s very similar to a tree where each node stores a single character. To add a new word, we’ll look at the word character by character. For the word “square”, we could think of our path as root -> “s” -> “q” -> “u” -> “a” -> “r” -> “e”. Where each “->” denotes a child access. As we recurse through each character node, we take off one character after each level. Finally, when we are at the last character, we mark it as the end of the word.

- Scala is used as programming language

## 2. Test
- Unit test are in **src/test** subdirectory. For runnig the test, the command  **sbt test** is used from sbt shell


## 3. Run
- The file containing the main is **App.scala** from package **com.da.autocomplete**, the program take a **txt file** in input containing a list of word and give in output the autocomplition.
