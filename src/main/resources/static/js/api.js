const API_BASE_URL = '/api';

class ApiService {
    static getToken() {
        return localStorage.getItem('token');
    }

    static setToken(token) {
        localStorage.setItem('token', token);
    }

    static clearToken() {
        localStorage.removeItem('token');
    }

    static isAuthenticated() {
        return !!this.getToken();
    }

    static async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        if (options.body && typeof options.body === 'object') {
            config.body = JSON.stringify(options.body);
        }

        try {
            const response = await fetch(url, config);
            
            // Handle 401 Unauthorized
            if (response.status === 401 || response.status === 403) {
                this.clearToken();
                window.location.href = '/index.html';
                return null;
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    // Auth
    static login(email, password) {
        return this.request('/auth/login', {
            method: 'POST',
            body: { email, password }
        });
    }

    static register(name, email, password, extraInfo = {}) {
        return this.request('/auth/register', {
            method: 'POST',
            body: { name, email, password, ...extraInfo }
        });
    }

    // Wallet & Dashboard
    static getDashboard() {
        return this.request('/wallet/dashboard');
    }

    static getWallet() {
        return this.request('/wallet');
    }

    static getPortfolio() {
        return this.request('/wallet/portfolio');
    }

    static deposit(amount) {
        return this.request('/wallet/deposit', {
            method: 'POST',
            body: { amount: parseFloat(amount) }
        });
    }

    static withdraw(amount) {
        return this.request('/wallet/withdraw', {
            method: 'POST',
            body: { amount: parseFloat(amount) }
        });
    }

    // Trading
    static buyCrypto(symbol, quantity) {
        return this.request('/wallet/buy', {
            method: 'POST',
            body: { symbol, quantity: parseFloat(quantity) }
        });
    }

    static sellCrypto(symbol, quantity) {
        return this.request('/wallet/sell', {
            method: 'POST',
            body: { symbol, quantity: parseFloat(quantity) }
        });
    }

    // Market
    static getTop50() {
        return this.request('/market/top50');
    }

    static getTransactions() {
        return this.request('/transactions');
    }
}

// Toast Utility
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
