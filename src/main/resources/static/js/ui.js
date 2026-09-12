const ui = {
    // Basic DOM elements
    authScreen: document.getElementById('auth-screen'),
    appScreen: document.getElementById('app-screen'),
    
    // Switch between Login and Register tabs
    switchAuthTab: (tab) => {
        const loginForm = document.getElementById('login-form');
        const registerForm = document.getElementById('register-form');
        const btns = document.querySelectorAll('.auth-tabs .tab-btn');
        
        btns.forEach(b => b.classList.remove('active'));
        
        if(tab === 'login') {
            loginForm.classList.remove('hidden');
            registerForm.classList.add('hidden');
            btns[0].classList.add('active');
        } else {
            loginForm.classList.add('hidden');
            registerForm.classList.remove('hidden');
            btns[1].classList.add('active');
        }
    },

    // Main App Navigation Tabs
    showTab: (tabId) => {
        document.querySelectorAll('.content-tab').forEach(el => {
            el.classList.add('hidden');
            el.classList.remove('active', 'slide-up');
        });
        document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));

        const target = document.getElementById(`${tabId}-tab`);
        if (target) {
            target.classList.remove('hidden');
            target.classList.add('active');
            // retrigger animation
            void target.offsetWidth; 
            target.classList.add('slide-up');
        }

        const navBtn = document.querySelector(`.nav-item[onclick="ui.showTab('${tabId}')"]`);
        if(navBtn) navBtn.classList.add('active');
    },

    // Modals
    openModal: (modalId) => {
        const modal = document.getElementById(modalId);
        if(modal) {
            modal.classList.remove('hidden');
        }
    },
    closeModal: (modalId) => {
        const modal = document.getElementById(modalId);
        if(modal) {
            modal.classList.add('hidden');
            // reset forms if necessary inside
            const inputs = modal.querySelectorAll('input');
            inputs.forEach(i => i.value = '');
            if(modalId === 'trade-modal') {
                document.getElementById('trade-estimation').innerText = '$0.00';
            }
        }
    },

    // Trade Modal specific
    openTradeModal: (coinId, symbol, currentPrice, defaultTab='buy') => {
        document.getElementById('trade-coin-id').value = coinId;
        document.getElementById('trade-coin-symbol').innerText = symbol;
        document.getElementById('trade-title').innerText = `Trade ${symbol}`;
        document.getElementById('trade-market-price').innerText = `$${currentPrice.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 6})}`;
        // Store price as dataset for estimation calculation
        document.getElementById('trade-market-price').dataset.price = currentPrice;
        
        ui.switchTradeTab(defaultTab);
        ui.openModal('trade-modal');
    },

    switchTradeTab: (type) => {
        document.getElementById('trade-type').value = type;
        const buyBtn = document.getElementById('btn-buy-tab');
        const sellBtn = document.getElementById('btn-sell-tab');
        if (type === 'buy') {
            buyBtn.classList.add('active');
            sellBtn.classList.remove('active');
        } else {
            buyBtn.classList.remove('active');
            sellBtn.classList.add('active');
        }
        ui.updateTradeEstimation();
    },

    updateTradeEstimation: () => {
        const amount = parseFloat(document.getElementById('trade-amount').value) || 0;
        const price = parseFloat(document.getElementById('trade-market-price').dataset.price) || 0;
        const total = amount * price;
        document.getElementById('trade-estimation').innerText = `$${total.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
    },

    // Toasts
    showToast: (message, type = 'success') => {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerText = message;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideInRight 0.3s reverse forwards';
            setTimeout(() => {
                if(container.contains(toast)) {
                    container.removeChild(toast);
                }
            }, 300);
        }, 3000);
    },

    // Loading State for Buttons
    setButtonLoading: (btnId, isLoading) => {
        const btn = document.getElementById(btnId);
        if(btn) {
            if(isLoading) {
                btn.classList.add('loading');
                btn.disabled = true;
            } else {
                btn.classList.remove('loading');
                btn.disabled = false;
            }
        }
    }
};
