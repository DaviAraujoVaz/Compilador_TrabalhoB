FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jdk-headless build-essential ca-certificates diffutils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

RUN mkdir -p bin saida \
    && javac -encoding UTF-8 -d bin *.java \
    && java -cp bin CompilerA exemplo1.lps1 > saida/saida1_a.c \
    && java -cp bin CompilerA exemplo2.lps1 > saida/saida2_a.c \
    && java -cp bin CompilerB exemplo1.lps1 > saida/saida1_b.c \
    && java -cp bin CompilerB exemplo2.lps1 > saida/saida2_b.c \
    && gcc -std=c11 -Wall -Wextra -Werror saida/saida1_a.c -o saida/saida1_a \
    && gcc -std=c11 -Wall -Wextra -Werror saida/saida1_b.c -o saida/saida1_b \
    && gcc -std=c11 -Wall -Wextra -Werror saida/saida2_a.c -o saida/saida2_a \
    && gcc -std=c11 -Wall -Wextra -Werror saida/saida2_b.c -o saida/saida2_b

RUN printf '0\n3\n6\n9\n12\n' > expected1.txt \
    && printf '0\n' > expected_prime.txt \
    && printf '1\n' > expected_not_prime.txt \
    && printf '5\n3\n' | saida/saida1_a > out1a.txt \
    && printf '5\n3\n' | saida/saida1_b > out1b.txt \
    && printf '5\n' | saida/saida2_a > out2a_prime.txt \
    && printf '4\n' | saida/saida2_a > out2a_not_prime.txt \
    && printf '5\n' | saida/saida2_b > out2b_prime.txt \
    && printf '4\n' | saida/saida2_b > out2b_not_prime.txt \
    && diff -u expected1.txt out1a.txt \
    && diff -u expected1.txt out1b.txt \
    && diff -u expected_prime.txt out2a_prime.txt \
    && diff -u expected_not_prime.txt out2a_not_prime.txt \
    && diff -u expected_prime.txt out2b_prime.txt \
    && diff -u expected_not_prime.txt out2b_not_prime.txt

CMD ["bash", "-lc", "printf 'Exemplo 1 (A):\\n'; printf '5\\n3\\n' | saida/saida1_a; printf 'Exemplo 1 (B):\\n'; printf '5\\n3\\n' | saida/saida1_b; printf 'Exemplo 2 primo (A/B):\\n'; printf '5\\n' | saida/saida2_a; printf '5\\n' | saida/saida2_b; printf 'Exemplo 2 nao primo (A/B):\\n'; printf '4\\n' | saida/saida2_a; printf '4\\n' | saida/saida2_b"]
