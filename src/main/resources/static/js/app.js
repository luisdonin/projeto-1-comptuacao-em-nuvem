const API_URL = '/api/sensores';
let currentMode = 'all';
let isEditing = false;

document.addEventListener('DOMContentLoaded', () => {
    carregarTodos();
});

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.2s';
        setTimeout(() => toast.remove(), 200);
    }, 3500);
}

function setQueryMode(mode, btnElement) {
    currentMode = mode;
    document.querySelectorAll('.mode-btn').forEach(btn => btn.classList.remove('active'));
    if (btnElement) btnElement.classList.add('active');

    const gSensor = document.getElementById('groupSensorId');
    const gLoc = document.getElementById('groupLocalizacao');
    const gTime = document.getElementById('groupTimestamp');
    const hint = document.getElementById('queryHint');

    gSensor.style.display = 'none';
    gLoc.style.display = 'none';
    gTime.style.display = 'none';

    if (mode === 'all') {
        hint.textContent = 'Realiza uma leitura completa de todos os itens cadastrados na tabela DynamoDB (Scan).';
    } else if (mode === 'loc') {
        gLoc.style.display = 'flex';
        hint.textContent = 'Consulta no Global Secondary Index (LocalizacaoIndex) filtrando por localização.';
    } else if (mode === 'sensor') {
        gSensor.style.display = 'flex';
        hint.textContent = 'Consulta na tabela principal filtrando pela Partition Key do sensor.';
    } else if (mode === 'rcu') {
        gSensor.style.display = 'flex';
        gLoc.style.display = 'flex';
        hint.textContent = 'Query na partição do sensor com FilterExpression de localização (otimização de RCU).';
    } else if (mode === 'item') {
        gSensor.style.display = 'flex';
        gTime.style.display = 'flex';
        hint.textContent = 'Busca pontual exata (GetItem) informando a chave primária composta (sensorId + timestamp).';
    }
}

function limparFiltros() {
    document.getElementById('qSensorId').value = '';
    document.getElementById('qLocalizacao').value = '';
    document.getElementById('qTimestamp').value = '';
    setQueryMode('all', document.querySelector('.mode-btn'));
    carregarTodos();
}

async function carregarTodos() {
    try {
        const res = await fetch(API_URL);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        renderTable(data);
        showToast(`${data.length} registro(s) carregado(s)`, 'success');
    } catch (err) {
        console.error(err);
        showToast(`Falha ao carregar registros: ${err.message}`, 'error');
    }
}

async function executarConsulta(e) {
    e.preventDefault();
    const sensorId = document.getElementById('qSensorId').value.trim();
    const localizacao = document.getElementById('qLocalizacao').value.trim();
    const timestamp = document.getElementById('qTimestamp').value.trim();

    let url = API_URL;

    if (currentMode === 'loc') {
        if (!localizacao) {
            showToast('Informe a localização para consulta.', 'warning');
            return;
        }
        url = `${API_URL}/localizacao/${encodeURIComponent(localizacao)}`;
    } else if (currentMode === 'sensor') {
        if (!sensorId) {
            showToast('Informe o Sensor ID para consulta.', 'warning');
            return;
        }
        url = `${API_URL}/sensor/${encodeURIComponent(sensorId)}`;
    } else if (currentMode === 'rcu') {
        if (!sensorId || !localizacao) {
            showToast('Informe o Sensor ID e a Localização.', 'warning');
            return;
        }
        url = `${API_URL}/sensor/${encodeURIComponent(sensorId)}?localizacao=${encodeURIComponent(localizacao)}`;
    } else if (currentMode === 'item') {
        if (!sensorId || !timestamp) {
            showToast('Informe o Sensor ID e o Timestamp.', 'warning');
            return;
        }
        url = `${API_URL}/${encodeURIComponent(sensorId)}/${encodeURIComponent(timestamp)}`;
    }

    try {
        const res = await fetch(url);
        if (res.status === 404) {
            renderTable([]);
            showToast('Nenhum registro encontrado.', 'info');
            return;
        }
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        renderTable(data);
        const count = Array.isArray(data) ? data.length : (data ? 1 : 0);
        showToast(`Consulta concluída: ${count} resultado(s)`, 'success');
    } catch (err) {
        console.error(err);
        showToast(`Erro na consulta: ${err.message}`, 'error');
    }
}

function renderTable(data) {
    const items = Array.isArray(data) ? data : (data ? [data] : []);
    document.getElementById('recordCountBadge').textContent = `${items.length} registro(s)`;

    atualizarEstatisticas(items);

    const tbody = document.getElementById('tableBody');
    if (items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-state">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="8" x2="12" y2="12"></line>
                        <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <div>Nenhum registro encontrado para a consulta executada.</div>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = items.map(item => `
        <tr>
            <td class="mono"><strong>${escapeHtml(item.sensorId || '')}</strong></td>
            <td class="mono">${escapeHtml(item.timestamp || '')}</td>
            <td><span class="loc-tag">${escapeHtml(item.localizacao || '')}</span></td>
            <td><span class="val-badge">${item.valor !== null && item.valor !== undefined ? Number(item.valor).toFixed(2) : '-'}</span></td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-secondary btn-sm" onclick='abrirEdicao(${JSON.stringify(item)})' title="Editar leitura">
                        Editar
                    </button>
                    <button class="btn btn-ghost btn-sm" style="color: var(--danger);" onclick="deletarItem('${escapeHtml(item.sensorId)}', '${escapeHtml(item.timestamp)}')" title="Excluir leitura">
                        Excluir
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function atualizarEstatisticas(items) {
    const total = items.length;
    const uniqueSensors = new Set(items.map(i => i.sensorId).filter(Boolean)).size;
    const uniqueLocs = new Set(items.map(i => i.localizacao).filter(Boolean)).size;
    const avg = total > 0
        ? (items.reduce((acc, cur) => acc + (Number(cur.valor) || 0), 0) / total).toFixed(2)
        : '0.00';

    document.getElementById('statTotal').innerHTML = `${total} <span class="stat-sub">leituras</span>`;
    document.getElementById('statSensors').innerHTML = `${uniqueSensors} <span class="stat-sub">sensores</span>`;
    document.getElementById('statLocations').innerHTML = `${uniqueLocs} <span class="stat-sub">setores</span>`;
    document.getElementById('statAvg').innerHTML = `${avg} <span class="stat-sub">unidades</span>`;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function openCreateModal() {
    isEditing = false;
    document.getElementById('modalTitle').textContent = 'Nova Leitura de Sensor';
    document.getElementById('btnSalvarModal').textContent = 'Salvar Leitura';
    document.getElementById('recordForm').reset();
    preencherAgora();
    document.getElementById('formSensorId').readOnly = false;
    document.getElementById('formTimestamp').readOnly = false;
    document.getElementById('recordModal').classList.add('active');
}

function abrirEdicao(item) {
    isEditing = true;
    document.getElementById('modalTitle').textContent = 'Editar Leitura de Sensor';
    document.getElementById('btnSalvarModal').textContent = 'Atualizar Leitura';
    document.getElementById('formSensorId').value = item.sensorId || '';
    document.getElementById('formTimestamp').value = item.timestamp || '';
    document.getElementById('formLocalizacao').value = item.localizacao || '';
    document.getElementById('formValor').value = item.valor !== undefined ? item.valor : '';
    document.getElementById('recordModal').classList.add('active');
}

function fecharModal() {
    document.getElementById('recordModal').classList.remove('active');
}

function fecharModalSeBackdrop(e) {
    if (e.target.id === 'recordModal') {
        fecharModal();
    }
}

function preencherAgora() {
    document.getElementById('formTimestamp').value = new Date().toISOString();
}

async function salvarRegistro(e) {
    e.preventDefault();
    const sensorId = document.getElementById('formSensorId').value.trim();
    const timestamp = document.getElementById('formTimestamp').value.trim();
    const localizacao = document.getElementById('formLocalizacao').value.trim();
    const valor = parseFloat(document.getElementById('formValor').value);

    if (!sensorId || !timestamp || !localizacao || isNaN(valor)) {
        showToast('Preencha todos os campos obrigatórios.', 'warning');
        return;
    }

    const payload = { sensorId, timestamp, localizacao, valor };
    const method = isEditing ? 'PUT' : 'POST';

    try {
        const res = await fetch(API_URL, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        showToast(isEditing ? 'Leitura atualizada com sucesso.' : 'Leitura criada com sucesso.', 'success');
        fecharModal();
        carregarTodos();
    } catch (err) {
        console.error(err);
        showToast(`Falha ao salvar: ${err.message}`, 'error');
    }
}

async function deletarItem(sensorId, timestamp) {
    if (!confirm(`Remover permanentemente a leitura do sensor "${sensorId}" em "${timestamp}"?`)) {
        return;
    }

    try {
        const res = await fetch(`${API_URL}/${encodeURIComponent(sensorId)}/${encodeURIComponent(timestamp)}`, {
            method: 'DELETE'
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        showToast('Registro excluído com sucesso.', 'success');
        carregarTodos();
    } catch (err) {
        console.error(err);
        showToast(`Falha ao excluir: ${err.message}`, 'error');
    }
}
