# Aubor Escala — Sistema de Controle de Escalas de Trabalho

Sistema backend Spring Boot com persistência PostgreSQL gerenciada por Flyway e frontend estático integrado.

---

## 🛠️ Requisitos

- **Java**: 21 LTS
- **PostgreSQL**: 15+
- **Maven**: embutido (`./mvnw` ou `mvnw.cmd`)

---

## ⚙️ Variáveis de Ambiente (Banco de Dados)

O sistema lê as configurações de conexão por variáveis de ambiente (com valores padrão para desenvolvimento local):

| Variável | Descrição | Padrão |
|---|---|---|
| `DB_HOST` | Host do banco de dados | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do database | `USUARIOS` |
| `DB_USER` | Usuário do banco | `postgres` |
| `DB_PASS` | Senha do banco | *(vazio)* |

---

## 🚀 Como Executar

### 1. Criar o banco no PostgreSQL
No terminal ou no pgAdmin / psql:
```sql
CREATE DATABASE "USUARIOS";
```
*(As tabelas, chaves estrangeiras e índices são criados automaticamente pelo Flyway na primeira inicialização).*

### 2. Definir as variáveis e iniciar a aplicação

**No Windows (PowerShell):**
```powershell
$env:DB_PASS="sua_senha_aqui"
.\mvnw.cmd spring-boot:run
```

**No Linux / macOS (Bash):**
```bash
export DB_PASS="sua_senha_aqui"
./mvnw spring-boot:run
```

### 3. Acessar o Sistema
Abra o navegador em:
```
http://localhost:8080
```

---

## 🔒 Segurança e Regras

1. **Senhas**: Armazenadas com hash **BCrypt**.
2. **Cadastro Público**: Todo novo usuário recebe perfil `USUARIO`. Promoção a `ADMIN` ou `ADM_SUPREMO` só por administradores autenticados.
3. **Turno**: Identificador único `id` (Integer) gerado pelo banco; cálculo de duração automático no servidor.
4. **Escala**: Dados de exibição gerados dinamicamente via JOIN (não persistidos como colunas soltas). Não permite escala de funcionários inativos nem turnos com sobreposição de horário no mesmo dia.
