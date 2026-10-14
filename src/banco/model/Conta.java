package banco.model;

import banco.exception.BancoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Conta {
    private final String numero;
    private final String cpfTitular;
    private BigDecimal saldo = BigDecimal.ZERO;   // BigDecimal: nunca use double para dinheiro
    private String chavePix;
    private final List<Transacao> extrato = new ArrayList<>();

    public Conta(String numero, String cpfTitular) {
        this.numero = numero;
        this.cpfTitular = cpfTitular;
    }

    public String getNumero() { return numero; }
    public String getCpfTitular() { return cpfTitular; }
    public synchronized BigDecimal getSaldo() { return saldo; }
    public synchronized String getChavePix() { return chavePix; }
    public synchronized void setChavePix(String chave) { this.chavePix = chave; }

    public synchronized void creditar(BigDecimal valor, Transacao.Tipo tipo, String descricao) {
        saldo = saldo.add(valor);
        extrato.add(new Transacao(LocalDateTime.now(), tipo, valor, descricao));
    }

    public synchronized void debitar(BigDecimal valor, Transacao.Tipo tipo, String descricao) {
        if (saldo.compareTo(valor) < 0) throw new BancoException("Saldo insuficiente.");
        saldo = saldo.subtract(valor);
        extrato.add(new Transacao(LocalDateTime.now(), tipo, valor, descricao));
    }

    /** Devolve uma cópia: quem chama não consegue alterar o extrato original. */
    public synchronized List<Transacao> getExtrato() {
        return Collections.unmodifiableList(new ArrayList<>(extrato));
    }
}
