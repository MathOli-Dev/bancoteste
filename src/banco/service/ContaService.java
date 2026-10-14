package banco.service;

import banco.exception.BancoException;
import banco.model.Conta;
import banco.model.Transacao;
import banco.model.Transacao.Tipo;
import banco.repository.BancoRepository;
import java.math.BigDecimal;
import java.util.List;

public class ContaService {
    private static final BigDecimal LIMITE_PIX = new BigDecimal("5000.00");

    private final BancoRepository repo;
    private final AutenticacaoService auth;

    public ContaService(BancoRepository repo, AutenticacaoService auth) {
        this.repo = repo;
        this.auth = auth;
    }

    public Conta abrirConta(String cpf) {
        Conta c = new Conta(repo.proximoNumeroConta(), cpf);
        repo.salvarConta(c);
        return c;
    }

    // Toda operação começa validando o token: sem sessão válida, nada acontece.
    private Conta contaLogada(String token) {
        return repo.buscarContaPorCpf(auth.validar(token));
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) throw new BancoException("Valor deve ser maior que zero.");
        if (valor.scale() > 2) throw new BancoException("Use no máximo 2 casas decimais.");
    }

    public String numeroConta(String token) { return contaLogada(token).getNumero(); }

    public BigDecimal saldo(String token) { return contaLogada(token).getSaldo(); }

    public List<Transacao> extrato(String token) { return contaLogada(token).getExtrato(); }

    public void depositar(String token, BigDecimal valor) {
        validarValor(valor);
        contaLogada(token).creditar(valor, Tipo.DEPOSITO, "Depósito");
    }

    public void transferir(String token, String numeroDestino, BigDecimal valor) {
        validarValor(valor);
        Conta origem = contaLogada(token);
        Conta destino = repo.buscarContaPorNumero(numeroDestino);
        if (destino == null) throw new BancoException("Conta de destino não encontrada.");
        movimentar(origem, destino, valor, Tipo.TRANSFERENCIA_ENVIADA, Tipo.TRANSFERENCIA_RECEBIDA);
    }

    public void cadastrarChavePix(String token, String chave) {
        Conta conta = contaLogada(token);
        if (chave == null || chave.isBlank()) throw new BancoException("Chave inválida.");
        if (conta.getChavePix() != null) throw new BancoException("Você já tem uma chave PIX.");
        if (!repo.registrarChavePix(chave.trim(), conta)) throw new BancoException("Chave já em uso.");
        conta.setChavePix(chave.trim());
    }

    public void pix(String token, String chave, BigDecimal valor) {
        validarValor(valor);
        if (valor.compareTo(LIMITE_PIX) > 0) throw new BancoException("Limite por PIX: R$ " + LIMITE_PIX);
        Conta origem = contaLogada(token);
        Conta destino = repo.buscarContaPorChavePix(chave == null ? "" : chave.trim());
        if (destino == null) throw new BancoException("Chave PIX não encontrada.");
        movimentar(origem, destino, valor, Tipo.PIX_ENVIADO, Tipo.PIX_RECEBIDO);
    }

    /** Debita e credita de forma atômica. */
    private void movimentar(Conta origem, Conta destino, BigDecimal valor, Tipo tipoSaida, Tipo tipoEntrada) {
        if (origem.getNumero().equals(destino.getNumero()))
            throw new BancoException("Origem e destino não podem ser a mesma conta.");

        // Trava sempre na mesma ordem (menor número primeiro) para evitar deadlock
        // quando duas transferências cruzadas acontecem ao mesmo tempo.
        Conta primeira = origem.getNumero().compareTo(destino.getNumero()) < 0 ? origem : destino;
        Conta segunda = (primeira == origem) ? destino : origem;

        synchronized (primeira) {
            synchronized (segunda) {
                origem.debitar(valor, tipoSaida, "Para conta " + destino.getNumero());
                destino.creditar(valor, tipoEntrada, "De conta " + origem.getNumero());
            }
        }
    }
}
