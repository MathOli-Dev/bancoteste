package banco.service;

import banco.exception.BancoException;
import banco.model.Usuario;
import banco.repository.BancoRepository;
import banco.security.SenhaHasher;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutenticacaoService {
    private static final int MAX_TENTATIVAS = 3;
    private static final Duration VALIDADE_SESSAO = Duration.ofMinutes(15);

    private record Sessao(String cpf, Instant expira) {}

    private final BancoRepository repo;
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public AutenticacaoService(BancoRepository repo) { this.repo = repo; }

    public void cadastrar(String cpf, String nome, char[] senha) {
        if (!cpf.matches("\\d{11}")) throw new BancoException("CPF deve ter 11 dígitos.");
        if (nome.isBlank()) throw new BancoException("Nome é obrigatório.");
        String s = new String(senha);
        if (s.length() < 8 || !s.matches(".*[A-Za-z].*") || !s.matches(".*\\d.*"))
            throw new BancoException("Senha: mínimo 8 caracteres, com letras e números.");

        var usuario = new Usuario(cpf, nome.trim(), SenhaHasher.gerarHash(senha));
        if (!repo.salvarUsuario(usuario)) throw new BancoException("CPF já cadastrado.");
    }

    /** Retorna um token de sessão. */
    public String login(String cpf, char[] senha) {
        Usuario u = repo.buscarUsuario(cpf);
        // Mensagem genérica: não revela se o CPF existe ou não
        if (u == null) throw new BancoException("CPF ou senha inválidos.");
        if (u.isBloqueado()) throw new BancoException("Usuário bloqueado por excesso de tentativas.");

        if (!SenhaHasher.verificar(senha, u.getSenhaHash())) {
            u.registrarFalha(MAX_TENTATIVAS);
            throw new BancoException("CPF ou senha inválidos.");
        }
        u.zerarFalhas();

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, new Sessao(cpf, Instant.now().plus(VALIDADE_SESSAO)));
        return token;
    }

    /** Valida o token e devolve o CPF dono da sessão. */
    public String validar(String token) {
        Sessao s = token == null ? null : sessoes.get(token);
        if (s == null || Instant.now().isAfter(s.expira())) {
            if (token != null) sessoes.remove(token);
            throw new BancoException("Sessão inválida ou expirada. Faça login novamente.");
        }
        return s.cpf();
    }

    public void logout(String token) { if (token != null) sessoes.remove(token); }

    public String nomeDe(String cpf) { return repo.buscarUsuario(cpf).getNome(); }
}
