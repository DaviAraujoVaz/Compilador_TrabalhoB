# Compiladores LPS1 - Guia Rápido

## Quick Start

Execute todos os comandos abaixo em sequência na pasta raiz do projeto.

### Etapa 1: Compilar Java

```powershell
mkdir bin
javac -d bin *.java
```

### Etapa 2: Gerar Código C

```powershell
mkdir saida
java -cp bin CompilerA exemplo1.lps1 > saida/saida1_a.c
java -cp bin CompilerA exemplo2.lps1 > saida/saida2_a.c
java -cp bin CompilerB exemplo1.lps1 > saida/saida1_b.c
java -cp bin CompilerB exemplo2.lps1 > saida/saida2_b.c
```

### Etapa 3: Compilar C

```powershell
cd saida
gcc saida1_a.c -o saida1_a.exe
gcc saida1_b.c -o saida1_b.exe
gcc saida2_a.c -o saida2_a.exe
gcc saida2_b.c -o saida2_b.exe
```

### Etapa 4: Executar

```powershell
.\saida1_a.exe
.\saida1_b.exe
.\saida2_a.exe
.\saida2_b.exe
```

---

## Notas

- **CompilerA**: Tradução Dirigida por Sintaxe (geração misturada com análise)
- **CompilerB**: ASA + Percurso (constrói árvore e depois gera código)
- Arquivos de entrada: `exemplo1.lps1` e `exemplo2.lps1`
- Arquivos de saída: `saida/saida[1-2]_[a-b].c`
