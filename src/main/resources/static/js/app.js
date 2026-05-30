// Lani Coffee Common JS Library
// Manages: Cart LocalStorage, Toast Notifications, Session Handling, Currency Formatting

document.addEventListener('DOMContentLoaded', () => {
    updateNavCartCount();
    checkCurrentUser();
    
    // Style nav on scroll
    const navWrapper = document.querySelector('.navbar-wrapper');
    if (navWrapper) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) {
                navWrapper.classList.add('scrolled');
            } else {
                navWrapper.classList.remove('scrolled');
            }
        });
    }
});

// Toast Notifications Helper
function showToast(message, type = 'success') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let iconClass = 'ri-checkbox-circle-line';
    if (type === 'error') iconClass = 'ri-error-warning-line';
    if (type === 'warning') iconClass = 'ri-alert-line';
    
    toast.innerHTML = `<i class="${iconClass}"></i> <span>${message}</span>`;
    container.appendChild(toast);
    
    // Animate in
    setTimeout(() => toast.classList.add('show'), 100);
    
    // Remove after 3.5s
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

// Currency Formatter (VND)
function formatPrice(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// --- SESSION / USER LOGIN STATE HANDLERS ---
let loggedInUser = null;

function checkCurrentUser() {
    fetch('/api/users/me')
        .then(res => {
            if (res.ok) return res.json();
            throw new Error('Chưa đăng nhập');
        })
        .then(user => {
            loggedInUser = user;
            updateNavAuthUI(user);
        })
        .catch(() => {
            loggedInUser = null;
            updateNavAuthUI(null);
        });
}

function updateNavAuthUI(user) {
    const authActionsBox = document.getElementById('auth-actions-box');
    if (!authActionsBox) return;

    if (user) {
        let adminBadge = '';
        const role = user.role.toLowerCase();
        if (role === 'admin') {
            adminBadge = `<a href="/admin/dashboard" class="nav-btn outline" style="margin-right: 8px;"><i class="ri-dashboard-line"></i> Dashboard</a>`;
        } else if (role === 'staff') {
            adminBadge = `<a href="/admin/orders" class="nav-btn outline" style="margin-right: 8px;"><i class="ri-dashboard-line"></i> Quản lý đơn</a>`;
        }
        
        authActionsBox.innerHTML = `
            ${adminBadge}
            <div style="display: flex; align-items: center; gap: 12px;">
                <a href="/my-orders" class="nav-link" style="font-weight:600; font-size:14px; margin-right:8px;"><i class="ri-history-line"></i> Đơn hàng</a>
                <span style="font-size: 14px; font-weight: 600; color: var(--primary);"><i class="ri-user-smile-line"></i> Chào, ${user.username}</span>
                <button onclick="handleLogout()" class="nav-btn outline" style="padding: 6px 12px; font-size: 13px;"><i class="ri-logout-box-r-line"></i> Đăng xuất</button>
            </div>
        `;
    } else {
        authActionsBox.innerHTML = `
            <a href="/login" class="nav-btn outline">Đăng nhập</a>
            <a href="/register" class="nav-btn primary">Đăng ký</a>
        `;
    }
}

function handleLogout() {
    fetch('/api/users/logout', { method: 'POST' })
        .then(() => {
            showToast('Đã đăng xuất tài khoản!');
            localStorage.removeItem('lani_user');
            setTimeout(() => window.location.href = '/', 1000);
        })
        .catch(err => {
            showToast('Lỗi đăng xuất!', 'error');
        });
}

// --- CART DATABASE MANAGEMENT (ASYNC) ---

function getCart() {
    return fetch('/api/cart').then(res => {
        if (!res.ok) throw new Error('Not logged in');
        return res.json();
    }).catch(() => []);
}

function addToCart(productId, name, price, img, quantity = 1) {
    if (!loggedInUser) {
        showToast('Vui lòng đăng nhập để thêm vào giỏ hàng!', 'warning');
        setTimeout(() => window.location.href = '/login', 1500);
        return;
    }
    
    fetch('/api/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId, quantity })
    })
    .then(res => {
        if (!res.ok) return res.text().then(err => { throw new Error(err); });
        return res.json();
    })
    .then(() => {
        showToast(`Đã thêm ${quantity} '${name}' vào giỏ hàng!`);
        updateNavCartCount();
    })
    .catch(err => showToast(err.message, 'error'));
}

function removeFromCart(productId) {
    fetch(`/api/cart/remove/${productId}`, { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                updateNavCartCount();
                if (typeof loadCartItems === 'function') loadCartItems();
                if (typeof renderCheckoutSummary === 'function') renderCheckoutSummary();
            }
        });
}

function updateQuantity(productId, quantity) {
    if (quantity <= 0) {
        removeFromCart(productId);
        return;
    }
    fetch('/api/cart/update', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId, quantity })
    })
    .then(res => {
        if (res.ok) {
            updateNavCartCount();
            if (typeof loadCartItems === 'function') loadCartItems();
            if (typeof renderCheckoutSummary === 'function') renderCheckoutSummary();
        } else {
            res.text().then(err => showToast(err, 'error'));
            if (typeof loadCartItems === 'function') loadCartItems(); 
        }
    });
}

function clearCart() {
    fetch('/api/cart/clear', { method: 'DELETE' })
        .then(() => updateNavCartCount());
}

async function getCartTotal() {
    const items = await getCart();
    return items.reduce((total, item) => {
        const price = item.product.salePrice || item.product.price;
        return total + (price * item.quantity);
    }, 0);
}

function updateNavCartCount() {
    const badge = document.getElementById('nav-cart-badge');
    if (badge) {
        fetch('/api/cart/count')
            .then(res => res.ok ? res.json() : 0)
            .then(count => {
                badge.innerText = count;
                badge.style.display = count > 0 ? 'flex' : 'none';
            })
            .catch(() => {
                badge.style.display = 'none';
            });
    }
}

// Fallback hình ảnh SVG cà phê tuyệt đẹp nếu file đĩa cứng vật lý chưa tồn tại
function handleImgError(img) {
    img.src = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="100" height="100"><rect width="100" height="100" rx="16" fill="%23faf7f4"/><path d="M32 30 L68 30 L62 82 L38 82 Z" fill="%234e2e1e" rx="4"/><path d="M32 30 L68 30 L66 42 L34 42 Z" fill="%23c28c5f"/><path d="M50 50 Q42 56 50 62 Q58 56 50 50 Z" fill="%23e5b880"/><path d="M47 18 Q45 23 49 26" stroke="%23c28c5f" stroke-width="2" fill="none"/><path d="M53 15 Q51 21 55 24" stroke="%23c28c5f" stroke-width="2" fill="none"/></svg>';
}

