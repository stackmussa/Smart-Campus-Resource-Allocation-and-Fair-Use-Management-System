const API = 'http://localhost:8080/api';

async function apiFetch(endpoint, options) {
    options = options || {};
    const response = await fetch(API + endpoint, Object.assign({}, options, {
        credentials: 'include',
        headers: Object.assign({'Content-Type': 'application/json'}, options.headers || {})
    }));
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
        throw new Error('Server returned non-JSON response for: ' + endpoint + ' (status: ' + response.status + ')');
    }
    const data = await response.json();
    if (!response.ok || !data.success) {
        throw new Error(data.error || 'Request failed');
    }
    return data.data;
}

function showToast(message, type) {
    type = type || 'success';
    var container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.style.cssText = 'position:fixed;top:20px;right:20px;z-index:9999;display:flex;flex-direction:column;gap:10px;';
        document.body.appendChild(container);
    }
    var colors = {success:'#22c55e', error:'#ef4444', info:'#4f8ef7'};
    var icons  = {success:'check-circle-fill', error:'x-circle-fill', info:'info-circle-fill'};
    var toast = document.createElement('div');
    toast.style.cssText = 'padding:13px 18px;border-radius:10px;color:white;font-size:14px;font-weight:500;min-width:260px;max-width:340px;' +
        'box-shadow:0 4px 16px rgba(0,0,0,0.18);display:flex;align-items:center;gap:10px;' +
        'background:' + (colors[type] || colors.info) + ';font-family:Inter,sans-serif;' +
        'animation:slideIn 0.3s ease;';
    toast.innerHTML = '<i class="bi bi-' + (icons[type] || icons.info) + '"></i><span>' + message + '</span>';
    container.appendChild(toast);
    setTimeout(function(){ if (toast.parentNode) toast.remove(); }, 3200);
}

function setLoading(show) {
    var overlay = document.getElementById('loading-overlay');
    if (show) {
        if (overlay) return;
        overlay = document.createElement('div');
        overlay.id = 'loading-overlay';
        overlay.style.cssText = 'position:fixed;inset:0;background:rgba(255,255,255,0.75);display:flex;align-items:center;justify-content:center;z-index:9998;';
        overlay.innerHTML = '<div style="width:40px;height:40px;border:4px solid #e2e8f0;border-top-color:#4f8ef7;border-radius:50%;animation:spin 0.8s linear infinite;"></div>';
        document.body.appendChild(overlay);
    } else {
        if (overlay) overlay.remove();
    }
}

function formatDateTime(iso) {
    if (!iso) return '—';
    var d = new Date(iso);
    return d.toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'}) +
        ' ' + d.toLocaleTimeString('en-US',{hour:'numeric',minute:'2-digit'});
}

function formatTime(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleTimeString('en-US',{hour:'numeric',minute:'2-digit'});
}

function formatDate(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'});
}

function statusBadge(status) {
    var colors = {
        CONFIRMED:'#22c55e', PENDING:'#f59e0b', QUEUED:'#4f8ef7',
        NO_SHOW:'#ef4444', OVERRIDDEN:'#6b7280', ACTIVE:'#8b5cf6',
        RESTRICTED:'#ef4444', EXAM_BLOCKED:'#f97316',
        CANCELLED:'#6b7280', AVAILABLE:'#22c55e', UNAVAILABLE:'#ef4444'
    };
    var c = colors[status] || '#6b7280';
    return '<span style="background:'+c+';color:white;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600;white-space:nowrap;">'+status+'</span>';
}

function circularProgress(value, max, size) {
    size = size || 80;
    var pct = Math.min(100, Math.max(0, (value / max) * 100));
    var r = (size / 2) - 8;
    var circ = 2 * Math.PI * r;
    var offset = circ - (pct / 100) * circ;
    var color = pct > 70 ? '#22c55e' : pct > 40 ? '#f59e0b' : '#ef4444';
    return '<svg width="'+size+'" height="'+size+'" style="display:block;">' +
        '<circle cx="'+(size/2)+'" cy="'+(size/2)+'" r="'+r+'" fill="none" stroke="#e5e7eb" stroke-width="8"/>' +
        '<circle cx="'+(size/2)+'" cy="'+(size/2)+'" r="'+r+'" fill="none" stroke="'+color+'" stroke-width="8" ' +
        'stroke-dasharray="'+circ.toFixed(2)+'" stroke-dashoffset="'+offset.toFixed(2)+'" ' +
        'stroke-linecap="round" transform="rotate(-90 '+(size/2)+' '+(size/2)+')" />' +
        '<text x="'+(size/2)+'" y="'+(size/2+6)+'" text-anchor="middle" font-size="16" font-weight="bold" fill="'+color+'">'+Math.round(value)+'</text>' +
        '</svg>';
}

async function checkAuth(requiredRoles) {
    try {
        var user = await apiFetch('/auth/me');
        if (requiredRoles) {
            var roles = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
            if (!roles.includes(user.role)) {
                window.location.href = '/login.html';
                return null;
            }
        }
        return user;
    } catch(e) {
        window.location.href = '/login.html';
        return null;
    }
}

async function logout() {
    try { await apiFetch('/auth/logout', {method:'POST'}); } catch(e){}
    window.location.href = '/login.html';
}

/* Inject keyframe animations */
(function(){
    var s = document.createElement('style');
    s.textContent = '@keyframes slideIn{from{transform:translateX(110%);opacity:0}to{transform:translateX(0);opacity:1}}@keyframes spin{to{transform:rotate(360deg)}}';
    document.head.appendChild(s);
})();
