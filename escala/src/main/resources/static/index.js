// API relativa — funciona em qualquer host, não só localhost:8080
const API_URL = '/api';

// Estado da aplicação
let currentUser = null;
let cachedFuncionarios = [];
let cachedTurnos = [];
let cachedEscalas = [];

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    // Data local (não UTC) — evita "ontem" no Brasil no fim do dia
    const today = getLocalDateString();
    const filterDateInput = document.getElementById('filter-date');
    if (filterDateInput) filterDateInput.value = today;
    const escalaDataInput = document.getElementById('escala-data');
    if (escalaDataInput) escalaDataInput.value = today;

    // Verifica sessão salva
    const savedUser = sessionStorage.getItem('nexus_user');
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        showAppScreen();
    } else {
        showAuthScreen();
    }
});

/** Retorna data local no formato YYYY-MM-DD, sem depender de UTC */
function getLocalDateString() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/* --------------------------------------------------------------------------
   Helper genérico para extrair mensagem de erro de uma resposta
   -------------------------------------------------------------------------- */
async function extractErrorMessage(response, fallback) {
    try {
        const error = await response.json();
        return error.erro || error.message || fallback;
    } catch (e) {
        return `${fallback} (status ${response.status})`;
    }
}

/* --------------------------------------------------------------------------
   1. NAVEGAÇÃO E AUTENTICAÇÃO
   -------------------------------------------------------------------------- */
function switchAuthTab(tab) {
    const loginBtn = document.getElementById('tab-login-btn');
    const regBtn = document.getElementById('tab-register-btn');
    const formLogin = document.getElementById('form-login');
    const formRegister = document.getElementById('form-register');

    if (tab === 'login') {
        loginBtn.classList.add('active');
        regBtn.classList.remove('active');
        formLogin.classList.remove('hidden');
        formRegister.classList.add('hidden');
    } else {
        regBtn.classList.add('active');
        loginBtn.classList.remove('active');
        formRegister.classList.remove('hidden');
        formLogin.classList.add('hidden');
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('login-email').value;
    const senha = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_URL}/usuarios/login`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email, senha})
        });

        if (response.status === 401) {
            throw new Error('E-mail ou senha inválidos');
        }
        if (!response.ok) {
            const msg = await extractErrorMessage(response, 'Falha na autenticação');
            throw new Error(msg);
        }

        const user = await response.json();
        currentUser = user;
        sessionStorage.setItem('nexus_user', JSON.stringify(user));

        showToast(`Bem-vindo(a), ${user.nome}!`, 'success');
        showAppScreen();

    } catch (err) {
        showToast(err.message, 'error');
        console.error('Erro no login:', err);
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const nome = document.getElementById('reg-nome').value;
    const email = document.getElementById('reg-email').value;
    const senha = document.getElementById('reg-password').value;
    // Perfil NÃO é enviado — servidor sempre define como USUARIO

    try {
        const response = await fetch(`${API_URL}/usuarios`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({nome, email, senha})
        });

        if (!response.ok) {
            const msg = await extractErrorMessage(response, 'Erro ao criar conta');
            throw new Error(msg);
        }

        showToast('Conta criada com sucesso! Faça login para acessar.', 'success');
        switchAuthTab('login');
        document.getElementById('login-email').value = email;

    } catch (err) {
        showToast(err.message, 'error');
        console.error('Erro no cadastro:', err);
    }
}

function handleLogout() {
    sessionStorage.removeItem('nexus_user');
    currentUser = null;
    showAuthScreen();
    showToast('Você saiu do sistema.', 'success');
}

function showAuthScreen() {
    document.getElementById('auth-screen').classList.remove('hidden');
    document.getElementById('app-screen').classList.add('hidden');
}

function showAppScreen() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app-screen').classList.remove('hidden');

    const nameSpan = document.getElementById('user-display-name');
    const roleBadge = document.getElementById('user-display-role');
    const avatar = document.getElementById('user-avatar');

    if (currentUser) {
        // textContent em vez de innerHTML — evita XSS com nomes maliciosos
        nameSpan.textContent = currentUser.nome;
        roleBadge.textContent = currentUser.perfil || 'USUARIO';
        roleBadge.className = `badge ${currentUser.perfil === 'ADM_SUPREMO' || currentUser.perfil === 'ADMIN' ? 'badge-admin' : 'badge-user'}`;
        avatar.textContent = currentUser.nome.charAt(0).toUpperCase();

        const isAdmin = currentUser.perfil === 'ADM_SUPREMO' || currentUser.perfil === 'ADMIN';
        document.querySelectorAll('.admin-only').forEach(el => {
            if (isAdmin) el.classList.remove('hidden');
            else el.classList.add('hidden');
        });
    }

    loadDashboardData();
}

/* --------------------------------------------------------------------------
   2. CARREGAMENTO DE DADOS DO BACKEND
   -------------------------------------------------------------------------- */
async function loadDashboardData() {
    await Promise.all([
        loadFuncionarios(),
        loadTurnos(),
        loadEscalasPorData()
    ]);
}

async function loadFuncionarios() {
    try {
        const response = await fetch(`${API_URL}/funcionarios`);
        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (response.ok) {
            cachedFuncionarios = await response.json();
            document.getElementById('stat-total-func').textContent = cachedFuncionarios.length;
            renderFuncionariosModal();
            populateFuncionarioSelect();
        } else {
            showToast('Erro ao carregar funcionários', 'error');
        }
    } catch (err) {
        console.error('Erro ao carregar funcionários:', err);
    }
}

async function loadTurnos() {
    try {
        const response = await fetch(`${API_URL}/turnos`);
        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (response.ok) {
            cachedTurnos = await response.json();
            document.getElementById('stat-total-turnos').textContent = cachedTurnos.length;
            renderTurnosModal();
            populateTurnoSelect();
        } else {
            showToast('Erro ao carregar turnos', 'error');
        }
    } catch (err) {
        console.error('Erro ao carregar turnos:', err);
    }
}

async function loadEscalasPorData() {
    const dataInput = document.getElementById('filter-date').value;
    if (!dataInput) return;

    document.getElementById('current-date-title').textContent = formatDateBR(dataInput);

    try {
        const response = await fetch(`${API_URL}/escalas?data=${dataInput}`);
        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (response.ok) {
            cachedEscalas = await response.json();
            document.getElementById('stat-total-escalas').textContent = cachedEscalas.length;
            document.getElementById('count-escalas-badge').textContent = `${cachedEscalas.length} escalas agendadas`;
            renderEscalasTable(cachedEscalas);
        } else {
            showToast('Erro ao carregar escalas', 'error');
            renderEscalasTable([]);
        }
    } catch (err) {
        console.error('Erro ao carregar escalas:', err);
        showToast('Erro ao carregar escalas. Verifique se o backend está ligado.', 'error');
    }
}

/* --------------------------------------------------------------------------
   3. RENDERIZAÇÃO DE TABELAS — usa textContent para evitar XSS
   -------------------------------------------------------------------------- */
function renderEscalasTable(escalas) {
    const tbody = document.getElementById('escalas-tbody');
    tbody.innerHTML = '';

    if (escalas.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 7;
        td.style.cssText = 'text-align:center;color:var(--text-muted);padding:30px';
        td.textContent = 'Nenhuma escala agendada para esta data.';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    const isAdmin = currentUser && (currentUser.perfil === 'ADM_SUPREMO' || currentUser.perfil === 'ADMIN');

    escalas.forEach(esc => {
        const tr = document.createElement('tr');

        const tdNome = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = esc.nomeFuncionario || 'Desconhecido';
        tdNome.appendChild(strong);

        const tdCargo = document.createElement('td');
        const span = document.createElement('span');
        span.className = 'text-muted';
        span.textContent = esc.cargoFuncionario || '-';
        tdCargo.appendChild(span);

        const tdTurno = document.createElement('td');
        tdTurno.textContent = esc.nomeTurno || '-';

        const tdHorario = document.createElement('td');
        const code = document.createElement('code');
        code.textContent = `${esc.horaInicioTurno || '--'} - ${esc.horaFimTurno || '--'}`;
        tdHorario.appendChild(code);

        const tdStatus = document.createElement('td');
        const statusSpan = document.createElement('span');
        statusSpan.className = `status-tag status-${esc.status}`;
        statusSpan.textContent = esc.status;
        tdStatus.appendChild(statusSpan);

        const tdObs = document.createElement('td');
        tdObs.textContent = esc.observacao || '-';

        const tdAcoes = document.createElement('td');
        tdAcoes.className = 'text-right';
        if (!isAdmin) tdAcoes.classList.add('hidden');
        if (isAdmin) {
            const btn = document.createElement('button');
            btn.className = 'btn btn-outline-danger btn-sm';
            btn.title = 'Excluir Escala';
            btn.innerHTML = '<i class="fa-solid fa-trash"></i>';
            btn.addEventListener('click', () => deletarEscala(esc.id));
            tdAcoes.appendChild(btn);
        }

        tr.append(tdNome, tdCargo, tdTurno, tdHorario, tdStatus, tdObs, tdAcoes);
        tbody.appendChild(tr);
    });
}

function renderFuncionariosModal() {
    const tbody = document.getElementById('func-tbody');
    tbody.innerHTML = '';

    if (cachedFuncionarios.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 5;
        td.textContent = 'Nenhum funcionário ativo cadastrado.';
        td.style.textAlign = 'center';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    cachedFuncionarios.forEach(f => {
        const tr = document.createElement('tr');

        const tdNome = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = f.nome;
        tdNome.appendChild(strong);

        const tdCargo = document.createElement('td');
        tdCargo.textContent = f.cargo;

        const tdCpf = document.createElement('td');
        const code = document.createElement('code');
        code.textContent = f.cpf;
        tdCpf.appendChild(code);

        const tdTel = document.createElement('td');
        tdTel.textContent = f.telefone || '-';

        const tdAcao = document.createElement('td');
        tdAcao.className = 'text-right';
        const btn = document.createElement('button');
        btn.className = 'btn btn-outline-danger btn-sm';
        btn.title = 'Desativar';
        btn.innerHTML = '<i class="fa-solid fa-user-slash"></i>';
        btn.addEventListener('click', () => desativarFuncionario(f.id));
        tdAcao.appendChild(btn);

        tr.append(tdNome, tdCargo, tdCpf, tdTel, tdAcao);
        tbody.appendChild(tr);
    });
}

function renderTurnosModal() {
    const tbody = document.getElementById('turnos-tbody');
    tbody.innerHTML = '';

    if (cachedTurnos.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 5;
        td.textContent = 'Nenhum turno cadastrado.';
        td.style.textAlign = 'center';
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    cachedTurnos.forEach(t => {
        const tr = document.createElement('tr');

        const tdNome = document.createElement('td');
        const strong = document.createElement('strong');
        strong.textContent = t.nome;
        tdNome.appendChild(strong);

        const tdInicio = document.createElement('td');
        const codeI = document.createElement('code');
        codeI.textContent = t.horaInicio;
        tdInicio.appendChild(codeI);

        const tdFim = document.createElement('td');
        const codeF = document.createElement('code');
        codeF.textContent = t.horaFim;
        tdFim.appendChild(codeF);

        const tdDuracao = document.createElement('td');
        tdDuracao.textContent = `${t.duracaoHoras}h`;

        const tdAcao = document.createElement('td');
        tdAcao.className = 'text-right';
        const btn = document.createElement('button');
        btn.className = 'btn btn-outline-danger btn-sm';
        btn.title = 'Excluir';
        btn.innerHTML = '<i class="fa-solid fa-trash"></i>';
        btn.addEventListener('click', () => deletarTurno(t.id));
        tdAcao.appendChild(btn);

        tr.append(tdNome, tdInicio, tdFim, tdDuracao, tdAcao);
        tbody.appendChild(tr);
    });
}

function populateFuncionarioSelect() {
    const select = document.getElementById('escala-func-id');
    select.innerHTML = '';
    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = 'Selecione o Funcionário...';
    select.appendChild(placeholder);

    if (cachedFuncionarios.length === 0) {
        const opt = document.createElement('option');
        opt.disabled = true;
        opt.textContent = '— Nenhum funcionário ativo cadastrado —';
        select.appendChild(opt);
        return;
    }

    cachedFuncionarios.forEach(f => {
        const opt = document.createElement('option');
        opt.value = f.id;
        opt.textContent = `${f.nome} (${f.cargo})`;
        select.appendChild(opt);
    });
}

function populateTurnoSelect() {
    const select = document.getElementById('escala-turno-id');
    select.innerHTML = '';
    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = 'Selecione o Turno...';
    select.appendChild(placeholder);

    if (cachedTurnos.length === 0) {
        const opt = document.createElement('option');
        opt.disabled = true;
        opt.textContent = '— Nenhum turno cadastrado —';
        select.appendChild(opt);
        return;
    }

    cachedTurnos.forEach(t => {
        const opt = document.createElement('option');
        opt.value = t.id;
        opt.textContent = `${t.nome} (${t.horaInicio} às ${t.horaFim})`;
        select.appendChild(opt);
    });
}

/* --------------------------------------------------------------------------
   4. AÇÕES DE CADASTRO E EXCLUSÃO
   -------------------------------------------------------------------------- */
async function handleSalvarEscala(event) {
    event.preventDefault();
    const funcionarioId = parseInt(document.getElementById('escala-func-id').value);
    const turnoId = parseInt(document.getElementById('escala-turno-id').value);
    const data = document.getElementById('escala-data').value;
    const status = document.getElementById('escala-status').value;
    const observacao = document.getElementById('escala-obs').value;

    try {
        const response = await fetch(`${API_URL}/escalas`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({funcionarioId, turnoId, data, status, observacao})
        });

        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (!response.ok) {
            const msg = await extractErrorMessage(response, 'Erro ao salvar escala');
            throw new Error(msg);
        }

        showToast('Escala agendada com sucesso!', 'success');
        closeModal('modal-escala');
        document.getElementById('filter-date').value = data;
        loadEscalasPorData();

    } catch (err) {
        showToast(err.message, 'error');
        console.error('Erro ao salvar escala:', err);
    }
}

async function handleSalvarFuncionario(event) {
    event.preventDefault();
    const nome = document.getElementById('func-nome').value;
    const cargo = document.getElementById('func-cargo').value;
    const cpf = document.getElementById('func-cpf').value;
    const telefone = document.getElementById('func-telefone').value;
    const email = document.getElementById('func-email').value;

    try {
        const response = await fetch(`${API_URL}/funcionarios`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            // cargaHorariaSemanal e ativo definidos no servidor, não enviados pelo cliente
            body: JSON.stringify({nome, cargo, cpf, telefone, email})
        });

        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (!response.ok) {
            const msg = await extractErrorMessage(response, 'Erro ao cadastrar funcionário');
            throw new Error(msg);
        }

        showToast('Funcionário cadastrado!', 'success');
        document.getElementById('form-novo-func').reset();
        loadFuncionarios();

    } catch (err) {
        showToast(err.message, 'error');
        console.error('Erro ao cadastrar funcionário:', err);
    }
}

async function handleSalvarTurno(event) {
    event.preventDefault();
    const nome = document.getElementById('turno-nome').value;
    const horaInicio = document.getElementById('turno-inicio').value;
    const horaFim = document.getElementById('turno-fim').value;
    // duracaoHoras NÃO é enviada — calculada no servidor

    try {
        const response = await fetch(`${API_URL}/turnos`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({nome, horaInicio, horaFim})
        });

        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (!response.ok) {
            const msg = await extractErrorMessage(response, 'Erro ao cadastrar turno');
            throw new Error(msg);
        }

        showToast('Turno criado com sucesso!', 'success');
        document.getElementById('form-novo-turno').reset();
        loadTurnos();

    } catch (err) {
        showToast(err.message, 'error');
        console.error('Erro ao cadastrar turno:', err);
    }
}

async function deletarEscala(id) {
    if (!confirm('Deseja realmente remover esta escala?')) return;
    try {
        const response = await fetch(`${API_URL}/escalas/${id}`, {method: 'DELETE'});
        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (response.ok) {
            showToast('Escala removida.', 'success');
            loadEscalasPorData();
        } else {
            const msg = await extractErrorMessage(response, 'Erro ao excluir escala');
            showToast(msg, 'error');
        }
    } catch (err) {
        showToast('Erro ao excluir escala', 'error');
        console.error('Erro ao excluir escala:', err);
    }
}

async function desativarFuncionario(id) {
    if (!confirm('Deseja desativar este funcionário? Ele não será mais escalável.')) return;
    try {
        const response = await fetch(`${API_URL}/funcionarios/${id}/desativar`, {method: 'PATCH'});
        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (response.ok) {
            showToast('Funcionário desativado.', 'success');
            loadFuncionarios();
        } else {
            const msg = await extractErrorMessage(response, 'Erro ao desativar funcionário');
            showToast(msg, 'error');
        }
    } catch (err) {
        showToast('Erro ao desativar funcionário', 'error');
        console.error('Erro ao desativar funcionário:', err);
    }
}

async function deletarTurno(id) {
    if (!confirm('Deseja excluir este turno?')) return;
    try {
        const response = await fetch(`${API_URL}/turnos/${id}`, {method: 'DELETE'});
        if (response.status === 401) {
            handleLogout();
            return;
        }
        if (response.ok) {
            showToast('Turno removido.', 'success');
            loadTurnos();
        } else {
            const msg = await extractErrorMessage(response, 'Erro ao excluir turno');
            showToast(msg, 'error');
        }
    } catch (err) {
        showToast('Erro ao excluir turno', 'error');
        console.error('Erro ao excluir turno:', err);
    }
}

/* --------------------------------------------------------------------------
   5. UTILITÁRIOS, FILTROS & TEMAS
   -------------------------------------------------------------------------- */
function openModal(modalId) {
    document.getElementById(modalId).classList.remove('hidden');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.add('hidden');
}

function resetFilters() {
    document.getElementById('filter-date').value = getLocalDateString();
    document.getElementById('filter-search').value = '';
    loadEscalasPorData();
}

function filterEscalasTable() {
    const term = document.getElementById('filter-search').value.toLowerCase();
    const filtered = cachedEscalas.filter(esc =>
        (esc.nomeFuncionario && esc.nomeFuncionario.toLowerCase().includes(term)) ||
        (esc.cargoFuncionario && esc.cargoFuncionario.toLowerCase().includes(term)) ||
        (esc.nomeTurno && esc.nomeTurno.toLowerCase().includes(term))
    );
    renderEscalasTable(filtered);
}

function toggleTheme() {
    const html = document.documentElement;
    const icon = document.getElementById('theme-icon');
    const isDark = html.getAttribute('data-theme') === 'dark';

    if (isDark) {
        html.setAttribute('data-theme', 'light');
        icon.className = 'fa-solid fa-sun';
    } else {
        html.setAttribute('data-theme', 'dark');
        icon.className = 'fa-solid fa-moon';
    }
}

function formatDateBR(dateStr) {
    if (!dateStr) return '';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = document.createElement('i');
    icon.className = `fa-solid fa-${type === 'success' ? 'circle-check' : 'circle-exclamation'}`;
    toast.appendChild(icon);
    toast.appendChild(document.createTextNode(` ${message}`));
    container.appendChild(toast);

    setTimeout(() => toast.remove(), 4000);
}