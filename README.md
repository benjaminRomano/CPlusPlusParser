# CPlusPlusParser

A Parser for C++ using Java. Created using the grammar found [here](http://www.nongnu.org/hcb/).

#####How to compile:
```bash
$ javac src/org/bromano/cplusplusparser/parser/*.java src/org/bromano/cplusplusparser/scanner/*.java src/org/bromano/cplusplusparser/Main.java
$ jar cvfm cplusplusparser.jar src/META-INF/MANIFEST.mf -C src .
```
#####How to run:
```bash
$ java -jar cplusplusparser.jar "path\to\file" 
OR
$ java -jar cplusplusparser.jar < "path\to\file"
```
Example output for a very simple c++ file:
```c++
int main() {
    char  * s = "s";
}
```

Token Stream:

![image](https://cloud.githubusercontent.com/assets/9221137/11496234/27070072-97d6-11e5-984e-bd3b4a27776b.png)

AST:

![image](https://cloud.githubusercontent.com/assets/9221137/11496246/3da2f584-97d6-11e5-9170-5dbff76712c1.png)
