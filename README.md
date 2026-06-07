# Compiladores LPS1

Implementacao do trabalho final de Compiladores para a linguagem LPS1.

- `CompilerA.java`: analisador sintatico com geracao de codigo C misturada as rotinas do parser.
- `CompilerB.java`: analisador sintatico que constroi uma ASA e gera codigo C nos metodos dos nos da ASA.
- A saida dos compiladores e o codigo C impresso na saida padrao com `System.out.println`.
- Entradas de exemplo: `exemplo1.lps1` e `exemplo2.lps1`.

## Executar com Java e GCC locais

```powershell
mkdir bin -ErrorAction SilentlyContinue
mkdir saida -ErrorAction SilentlyContinue

javac -d bin *.java

java -cp bin CompilerA exemplo1.lps1 > saida/saida1_a.c
java -cp bin CompilerA exemplo2.lps1 > saida/saida2_a.c
java -cp bin CompilerB exemplo1.lps1 > saida/saida1_b.c
java -cp bin CompilerB exemplo2.lps1 > saida/saida2_b.c

gcc -std=c11 -Wall -Wextra -Werror saida/saida1_a.c -o saida/saida1_a.exe
gcc -std=c11 -Wall -Wextra -Werror saida/saida1_b.c -o saida/saida1_b.exe
gcc -std=c11 -Wall -Wextra -Werror saida/saida2_a.c -o saida/saida2_a.exe
gcc -std=c11 -Wall -Wextra -Werror saida/saida2_b.c -o saida/saida2_b.exe
```

## Executar com Docker

Use este caminho quando a maquina nao tiver `javac` e `gcc` instalados no PATH.

```powershell
docker build -t compilador-lps1 .
docker run --rm compilador-lps1
```

O `docker build` compila os dois compiladores, gera os quatro arquivos C, compila os C com `-Wall -Wextra -Werror` e compara as saidas esperadas. O comando `docker run` mostra:

- exemplo 1 em A e B com entrada `5` e `3`, imprimindo `0 3 6 9 12`;
- exemplo 2 em A e B com entrada `5`, imprimindo `0` porque 5 e primo;
- exemplo 2 em A e B com entrada `4`, imprimindo `1` porque 4 nao e primo.
