# Compiladores LPS1

Implementacao do trabalho final de Compiladores para a linguagem LPS1.

- `CompilerA.java`: analisador sintatico com geracao de codigo C misturada as rotinas do parser.
- `CompilerB.java`: analisador sintatico que constroi uma ASA e gera codigo C nos metodos dos nos da ASA.
- O codigo C gerado e salvo automaticamente na pasta `saida`, enquanto o rastro (trace) da analise sintatica e impresso na saida padrao e salvo em arquivos `.log` na pasta `log`.
- Entradas de exemplo: 
  - Corretas: `exemplo1.lps1` e `exemplo2.lps1`.
  - Incorretas (para testes de erro): `erro_sintaxe.lps1` e `erro_semantico.lps1`.

## Executar com Java e GCC locais

```powershell
mkdir bin -ErrorAction SilentlyContinue
mkdir saida -ErrorAction SilentlyContinue
mkdir log -ErrorAction SilentlyContinue

javac -d bin CompilerA.java CompilerB.java

java -cp bin CompilerA exemplo1.lps1
java -cp bin CompilerA exemplo2.lps1
java -cp bin CompilerB exemplo1.lps1
java -cp bin CompilerB exemplo2.lps1

java -cp bin CompilerA erro_sintaxe.lps1
java -cp bin CompilerA erro_semantico.lps1

gcc -std=c11 -Wall -Wextra saida/exemplo1_A.c -o saida/exemplo1_A.exe
gcc -std=c11 -Wall -Wextra saida/exemplo1_B.c -o saida/exemplo1_B.exe
gcc -std=c11 -Wall -Wextra saida/exemplo2_A.c -o saida/exemplo2_A.exe
gcc -std=c11 -Wall -Wextra saida/exemplo2_B.c -o saida/exemplo2_B.exe
```

## Executar com Docker

Use este caminho quando a maquina nao tiver `javac` e `gcc` instalados no PATH.

```powershell
docker build -t compilador-lps1 .
docker run --rm compilador-lps1
```

O `docker build` compila os dois compiladores, gera os quatro arquivos C, compila os C com `-Wall -Wextra` (sem `-Werror` por causa da chamada a função `gets()`) e compara as saidas esperadas. O comando `docker run` mostra:

- exemplo 1 em A e B com entrada `5` e `3`, imprimindo `0 3 6 9 12`;
- exemplo 2 em A e B com entrada `5`, imprimindo `0` porque 5 e primo;
- exemplo 2 em A e B com entrada `4`, imprimindo `1` porque 4 nao e primo.
