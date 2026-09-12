const app = {
    marketInterval: null,
    
    init: async () => {
        // Check if user is logged in
        const token = localStorage.getItem('token');
        if (token) {
            ui.authScreen.classList.add('hidden');
            ui.appScreen.classList.remove('hidden');
            await app.loadDashboard();
            await app.loadMarketData();
            
            // Start polling market data every 10 seconds
            app.marketInterval = setInterval(app.loadMarketData, 10000);
        } else {
            ui.authScreen.classList.remove('hidden');
            ui.appScreen.classList.add('hidden');
        }
    },

    // --- Authentication --- //

    handleLogin: async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        
        ui.setButtonLoading('login-btn', true);
        try {
            const res = await API.login({ email, password });
            if (res && res.data) { // data contains token based on backend structure
                localStorage.setItem('token', res.data);
                ui.showToast('Login successful!');
                setTimeout(() => window.location.reload(), 500); // reload to init
            }
        } catch (error) {
            ui.showToast(error.message || 'Login failed', 'error');
        } finally {
            ui.setButtonLoading('login-btn', false);
        }
    },

    handleRegister: async (e) => {
        e.preventDefault();
        const name = document.getElementById('reg-name').value;
        const email = document.getElementById('reg-email').value;
        const password = document.getElementById('reg-password').value;
        
        ui.setButtonLoading('reg-btn', true);
        try {
            const res = await API.register({ name, email, password });
            ui.showToast('Registration successful! Please log in.');
            ui.switchAuthTab('login');
        } catch (error) {
            ui.showToast(error.message || 'Registration failed', 'error');
        } finally {
            ui.setButtonLoading('reg-btn', false);
        }
    },

    logout: () => {
        localStorage.removeItem('token');
        if(app.marketInterval) clearInterval(app.marketInterval);
        window.location.reload();
    },

    // --- Dashboard logic --- //

    loadDashboard: async () => {
        try {
            const wallet = await API.getWalletDashboard();
            // Wallet response shape: { cashBalance: 10000, portfolioValue: 500,...}
            if (wallet) {
                // Formatting values
                document.getElementById('nav-balance').innerText = wallet.cashBalance.toLocaleString(undefined, {minimumFractionDigits: 2});
                document.getElementById('cash-balance').innerText = wallet.cashBalance.toLocaleString(undefined, {minimumFractionDigits: 2});
                
                // Usually dashboard API returns all this, if not we fall back to generic 0
                document.getElementById('total-portfolio-value').innerText = (wallet.portfolioValue || 0).toLocaleString(undefined, {minimumFractionDigits: 2});
                
                const profitChip = document.getElementById('total-profit');
                const profitVal = wallet.profit || 0;
                document.getElementById('profit-value').innerText = Math.abs(profitVal).toLocaleString(undefined, {minimumFractionDigits: 2});
                if(profitVal >= 0) {
                    profitChip.classList.add('positive');
                    profitChip.classList.remove('negative');
                    profitChip.innerHTML = `+$<span id="profit-value">${Math.abs(profitVal).toLocaleString(undefined, {minimumFractionDigits: 2})}</span> (All Time)`;
                } else {
                    profitChip.classList.add('negative');
                    profitChip.classList.remove('positive');
                    profitChip.innerHTML = `-$<span id="profit-value">${Math.abs(profitVal).toLocaleString(undefined, {minimumFractionDigits: 2})}</span> (All Time)`;
                }
            }

            // Load Holdings
            await app.loadHoldings();

        } catch (error) {
            console.error('Failed to load dashboard', error);
        }
    },

    loadHoldings: async () => {
        try {
            const holdings = await API.getPortfolio();
            const tbody = document.getElementById('holdings-tbody');
            if (!holdings || holdings.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center empty-state">No assets found in portfolio</td></tr>';
                return;
            }

            let html = '';
            holdings.forEach(h => {
                const totalVal = h.amount * (h.currentPrice || h.averageBuyPrice); // Fallback to avg buy if no current
                html += `
                    <tr>
                        <td><strong>${h.cryptoSymbol}</strong></td>
                        <td>${h.amount.toLocaleString(undefined, {maximumFractionDigits: 6})}</td>
                        <td>$${h.averageBuyPrice.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                        <td>$${totalVal.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                        <td>
                            <button class="action-btn" onclick="ui.openTradeModal('${h.cryptoSymbol}', '${h.cryptoSymbol}', ${h.currentPrice || h.averageBuyPrice}, 'sell')">Trade</button>
                        </td>
                    </tr>
                `;
            });
            tbody.innerHTML = html;
        } catch (error) {
            console.error('Failed to load holdings', error);
        }
    },

    executeDeposit: async () => {
        const amount = document.getElementById('deposit-amount').value;
        if (!amount || amount <= 0) {
            ui.showToast('Please enter a valid amount', 'error');
            return;
        }

        try {
            await API.deposit(amount);
            ui.showToast(`Successfully deposited $${amount}`);
            ui.closeModal('deposit-modal');
            await app.loadDashboard(); // refresh
        } catch (error) {
            ui.showToast(error.message || 'Deposit failed', 'error');
        }
    },

    // --- Market logic --- //

    loadMarketData: async () => {
        try {
            const markets = await API.getTop10();
            const tbody = document.getElementById('market-tbody');
            if (!markets || markets.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center empty-state">No market data available</td></tr>';
                return;
            }

            let html = '';
            markets.forEach(c => {
                const changeClass = c.percentChange24h >= 0 ? 'text-positive' : 'text-negative';
                const changeSign = c.percentChange24h >= 0 ? '+' : '';
                html += `
                    <tr>
                        <td>
                            <div style="display:flex; align-items:center; gap:10px;">
                                <strong style="font-size:16px;">${c.symbol}</strong>
                            </div>
                        </td>
                        <td style="font-weight: 500;">$${c.currentPrice.toLocaleString(undefined, {maximumFractionDigits: 6})}</td>
                        <td class="${changeClass}">${changeSign}${(c.percentChange24h || 0).toFixed(2)}%</td>
                        <td>$${(c.marketCap || 0).toLocaleString()}</td>
                        <td>
                            <button class="btn positive-btn" style="padding: 5px 15px; border-radius: 4px; font-size: 13px;" onclick="ui.openTradeModal('${c.symbol}', '${c.symbol}', ${c.currentPrice}, 'buy')">Buy</button>
                        </td>
                    </tr>
                `;
            });
            tbody.innerHTML = html;
        } catch (error) {
            console.error('Failed to load market data', error);
            // Don't override with empty state on interval errors, just keep old data
        }
    },

    executeTrade: async () => {
        const coinId = document.getElementById('trade-coin-id').value;
        const type = document.getElementById('trade-type').value;
        const amount = document.getElementById('trade-amount').value;

        if (!amount || amount <= 0) {
            ui.showToast('Please enter a valid amount', 'error');
            return;
        }

        const btn = document.querySelector('.confirm-trade-btn');
        btn.innerHTML = '<span class="loader small"></span> Processing...';
        btn.disabled = true;

        try {
            if (type === 'buy') {
                await API.buyCrypto(coinId, amount);
                ui.showToast(`Successfully bought ${amount} ${coinId}`);
            } else {
                await API.sellCrypto(coinId, amount);
                ui.showToast(`Successfully sold ${amount} ${coinId}`);
            }
            ui.closeModal('trade-modal');
            
            // Refresh Data
            await app.loadDashboard();
            await app.loadMarketData();
            
        } catch (error) {
            ui.showToast(error.message || `Failed to ${type} crypto`, 'error');
        } finally {
            btn.innerHTML = 'Confirm Trade';
            btn.disabled = false;
        }
    }
};

// Initialize App
document.addEventListener('DOMContentLoaded', app.init);
