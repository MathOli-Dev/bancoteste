Montei o projeto em Java 17+ puro, sem frameworks, e testei de ponta a ponta: cadastro, login, depósito, PIX, transferência, extrato e bloqueio de usuário. O código está no banco-digital.zip.

Estrutura em camadas

banco/
├── Main.java                      ← apresentação (menu no console)
├── service/
│   ├── AutenticacaoService.java   ← cadastro, login, sessão
│   └── ContaService.java          ← saldo, depósito, transferência, PIX, extrato
├── repository/BancoRepository.java ← "banco de dados" em memória
├── model/                         ← Usuario, Conta, Transacao
├── security/SenhaHasher.java      ← hash de senha
└── exception/BancoException.java

A regra de ouro: o Main só lê e mostra dados, toda regra de negócio fica nos service, e o acesso aos dados fica no repository. Para trocar a memória por um banco real depois, você mexe só no repository.

Boas práticas de segurança no código

Senha: guardada como hash PBKDF2 com salt aleatório, nunca em texto puro. A comparação é em tempo constante.
Login: a mensagem de erro é genérica (“CPF ou senha inválidos”), então não revela se o CPF existe. Após 3 falhas o usuário é bloqueado.
Sessão: token aleatório gerado com SecureRandom, que expira em 15 minutos. Toda operação financeira valida o token antes de qualquer coisa.
Dinheiro: BigDecimal com no máximo 2 casas decimais. Valores negativos ou zero são recusados.
Concorrência: a transferência debita e credita dentro de synchronized, sempre travando as contas na mesma ordem para evitar deadlock.
Extrato: Transacao é um record imutável, e getExtrato() devolve uma cópia protegida.
PIX simulado: chave única por conta e limite de R$ 5.000 por operação.

Como rodar

mkdir out
javac -d out $(find src -name "*.java")
java -cp out banco.Main

No Windows, o README do zip traz o equivalente do comando de compilação.

Próximos passos para evoluir

Trocar o BancoRepository por JDBC com PostgreSQL ou H2.
Escrever testes JUnit para ContaService, principalmente saldo insuficiente e transferência para a mesma conta.
Expor os services como API REST com Spring Boot e usar JWT no lugar do token em memória.
