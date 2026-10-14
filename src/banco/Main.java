package banco;

import banco.exception.BancoException;
import banco.model.Transacao;
import banco.repository.BancoRepository;
import banco.service.AutenticacaoService;
import banco.service.ContaService;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/** Camada de apresentação: só lê dados, chama os services e mostra resultados. */
public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        var repo = new BancoRepository();
        var auth = new AutenticacaoService(repo);
        var contas = new ContaService(repo, auth);

        System.out.println("=== BANCO DIGITAL ===");
        while (true) {
            System.out.print("\n1) Cadastrar  2) Entrar  0) Sair\n> ");
            switch (in.nextLine().trim()) {
                case "1" -> executar(() -> cadastrar(auth, contas));
                case "2" -> executar(() -> {
                    String cpf = ler("CPF: ");
                    String token = auth.login(cpf, ler("Senha: ").toCharArray());
                    menuConta(auth, contas, token, cpf);
                });
                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void cadastrar(AutenticacaoService auth, ContaService contas) {
        String cpf = ler("CPF (11 dígitos): ");
        String nome = ler("Nome: ");
        auth.cadastrar(cpf, nome, ler("Senha (mín. 8, letras e números): ").toCharArray());
        System.out.println("Conta criada! Número: " + contas.abrirConta(cpf).getNumero());
    }

    private static void menuConta(AutenticacaoService auth, ContaService contas, String token, String cpf) {
        System.out.println("\nBem-vindo(a), " + auth.nomeDe(cpf) + "! Sua conta: " + contas.numeroConta(token));
        while (true) {
            System.out.print("\n1) Saldo  2) Depositar  3) Transferir  4) Cadastrar chave PIX"
                    + "\n5) Enviar PIX  6) Extrato  0) Sair\n> ");
            String op = in.nextLine().trim();
            if (op.equals("0")) { auth.logout(token); return; }
            try {
                switch (op) {
                    case "1" -> System.out.println("Saldo: R$ " + contas.saldo(token));
                    case "2" -> { contas.depositar(token, lerValor()); System.out.println("Depósito realizado."); }
                    case "3" -> {
                        String destino = ler("Conta destino: ");
                        contas.transferir(token, destino, lerValor());
                        System.out.println("Transferência realizada.");
                    }
                    case "4" -> { contas.cadastrarChavePix(token, ler("Chave (e-mail, telefone...): ")); System.out.println("Chave cadastrada."); }
                    case "5" -> {
                        String chave = ler("Chave PIX destino: ");
                        contas.pix(token, chave, lerValor());
                        System.out.println("PIX enviado.");
                    }
                    case "6" -> mostrarExtrato(contas, token);
                    default -> System.out.println("Opção inválida.");
                }
            } catch (BancoException e) {
                System.out.println("Erro: " + e.getMessage());
                if (e.getMessage().startsWith("Sessão")) return; // sessão expirou
            } catch (NumberFormatException e) {
                System.out.println("Erro: valor numérico inválido.");
            }
        }
    }

    private static void mostrarExtrato(ContaService contas, String token) {
        System.out.println("\n--- EXTRATO ---");
        for (Transacao t : contas.extrato(token)) {
            System.out.printf("%s  %-22s %s R$ %8s  %s%n", t.data().format(FMT), t.tipo(),
                    t.ehSaida() ? "-" : "+", t.valor(), t.descricao());
        }
        System.out.println("Saldo atual: R$ " + contas.saldo(token));
    }

    private static String ler(String msg) { System.out.print(msg); return in.nextLine().trim(); }

    private static BigDecimal lerValor() { return new BigDecimal(ler("Valor: ").replace(',', '.')); }

    private static void executar(Runnable r) {
        try { r.run(); } catch (BancoException e) { System.out.println("Erro: " + e.getMessage()); }
    }
}
