package banco.repository;

import banco.model.Conta;
import banco.model.Usuario;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** "Banco de dados" em memória. Em um projeto real, aqui entraria JDBC/JPA. */
public class BancoRepository {
    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<String, Conta> contasPorCpf = new ConcurrentHashMap<>();
    private final Map<String, Conta> contasPorNumero = new ConcurrentHashMap<>();
    private final Map<String, Conta> contasPorChavePix = new ConcurrentHashMap<>();
    private final AtomicInteger sequencia = new AtomicInteger(1000);

    public boolean salvarUsuario(Usuario u) { return usuarios.putIfAbsent(u.getCpf(), u) == null; }
    public Usuario buscarUsuario(String cpf) { return usuarios.get(cpf); }

    public String proximoNumeroConta() { return String.valueOf(sequencia.incrementAndGet()); }

    public void salvarConta(Conta c) {
        contasPorCpf.put(c.getCpfTitular(), c);
        contasPorNumero.put(c.getNumero(), c);
    }
    public Conta buscarContaPorCpf(String cpf) { return contasPorCpf.get(cpf); }
    public Conta buscarContaPorNumero(String numero) { return contasPorNumero.get(numero); }

    /** false se a chave já pertence a alguém. */
    public boolean registrarChavePix(String chave, Conta conta) {
        return contasPorChavePix.putIfAbsent(chave, conta) == null;
    }
    public Conta buscarContaPorChavePix(String chave) { return contasPorChavePix.get(chave); }
}
