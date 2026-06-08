FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jdk-headless build-essential ca-certificates diffutils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mkdir -p bin saida log \
    && javac -encoding UTF-8 -d bin *.java \
    && java -cp bin CompilerA exemplo1.lps1 \
    && java -cp bin CompilerA exemplo2.lps1 \
    && java -cp bin CompilerB exemplo1.lps1 \
    && java -cp bin CompilerB exemplo2.lps1 \
    && java -cp bin CompilerA erro_sintaxe.lps1 || true \
    && java -cp bin CompilerA erro_semantico.lps1 \
    && gcc -Wall -Wextra saida/exemplo1_A.c -o saida/exemplo1_A \
    && gcc -Wall -Wextra saida/exemplo1_B.c -o saida/exemplo1_B \
    && gcc -Wall -Wextra saida/exemplo2_A.c -o saida/exemplo2_A \
    && gcc -Wall -Wextra saida/exemplo2_B.c -o saida/exemplo2_B \
    && gcc -Wall -Wextra saida/erro_semantico_A.c -o saida/erro_semantico_A

RUN printf '0\n3\n6\n9\n12\n' > expected1.txt \
    && printf '0\n' > expected_prime.txt \
    && printf '1\n' > expected_not_prime.txt \
    && printf '5\n3\n' | saida/exemplo1_A > out1a.txt \
    && printf '5\n3\n' | saida/exemplo1_B > out1b.txt \
    && printf '5\n' | saida/exemplo2_A > out2a_prime.txt \
    && printf '4\n' | saida/exemplo2_A > out2a_not_prime.txt \
    && printf '5\n' | saida/exemplo2_B > out2b_prime.txt \
    && printf '4\n' | saida/exemplo2_B > out2b_not_prime.txt \
    && diff -u expected1.txt out1a.txt \
    && diff -u expected1.txt out1b.txt \
    && diff -u expected_prime.txt out2a_prime.txt \
    && diff -u expected_not_prime.txt out2a_not_prime.txt \
    && diff -u expected_prime.txt out2b_prime.txt \
    && diff -u expected_not_prime.txt out2b_not_prime.txt

CMD ["bash", "-lc", "printf 'Exemplo 1 (A):\\n'; printf '5\\n3\\n' | saida/exemplo1_A; printf 'Exemplo 1 (B):\\n'; printf '5\\n3\\n' | saida/exemplo1_B; printf 'Exemplo 2 primo (A/B):\\n'; printf '5\\n' | saida/exemplo2_A; printf '5\\n' | saida/exemplo2_B; printf 'Exemplo 2 nao primo (A/B):\\n'; printf '4\\n' | saida/exemplo2_A; printf '4\\n' | saida/exemplo2_B; printf '\\n[Log de Erro de Sintaxe]:\\n'; cat log/erro_sintaxe_A.log; printf '\\nRodando erro semantico (Vai causar Crash):\\n'; saida/erro_semantico_A || true"]
