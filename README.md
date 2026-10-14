# Banco Digital (Java 17+)

Compilar e executar (na pasta do projeto):

    mkdir out
    javac -d out $(find src -name "*.java")     # no Windows: dir /s /b src\*.java > fontes.txt && javac -d out @fontes.txt
    java -cp out banco.Main
