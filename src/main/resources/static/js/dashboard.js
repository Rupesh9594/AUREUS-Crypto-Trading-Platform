document.addEventListener('DOMContentLoaded', () => {
    
    if (!ApiService.isAuthenticated()) {
        window.location.href = '/index.html';
        return;
    }

    // Elements
    const logoutBtn = document.getElementById('logout-btn');
    const navItems = document.querySelectorAll('.nav-item');
    const viewSections = document.querySelectorAll('.view-section');
    const themeToggle = document.getElementById('theme-toggle');
    const profileLogoutBtn = document.getElementById('profile-logout-btn');
    
    // Formatting util
    const formatCurrency = (val) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val || 0);
    const formatNumber = (val) => parseFloat(val || 0).toFixed(4);

    // Global State
    let coinPrices = {}; 
    let myPortfolio = [];
    let stompClient = null;
    let portfolioPieChart = null;
    let mainCryptoChart = null;
    let currentTradeType = 'MARKET'; // or LIMIT

    // Initial Load
    initDashboard();
    connectWebSocket();

    // ---------------- LOGOUT ----------------
    logoutBtn.addEventListener('click', handleLogout);
    if(profileLogoutBtn) profileLogoutBtn.addEventListener('click', handleLogout);

    function handleLogout() {
        ApiService.clearToken();
        if(stompClient) stompClient.disconnect();
        window.location.href = '/index.html';
    }

    // ---------------- NAVIGATION ----------------
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
            
            const targetId = item.getAttribute('data-target');
            viewSections.forEach(section => section.classList.remove('active'));
            document.getElementById(targetId).classList.add('active');

            if(targetId === 'view-social') loadLeaderboard();
            if(targetId === 'view-dashboard') drawPieChart();
            if(targetId === 'view-trade') refreshTradeChart();
            if(targetId === 'view-profile') loadUserProfile();
        });
    });

    async function initDashboard() {
        await loadWallet();
        await loadDashboard();
        await loadMarket(true);
        await loadWatchlist();
        await loadOpenOrders();
        await loadHistory();
        await loadUserProfile(true); // Initial silent load for theme
    }

    // ---------------- WEBSOCKETS ----------------
    function connectWebSocket() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // hide debug logs
        
        stompClient.connect({}, (frame) => {
            // Subscribe to live market broadcast
            stompClient.subscribe('/topic/market', (message) => {
                const coins = JSON.parse(message.body);
                handleMarketUpdate(coins);
            });

            // Subscribe to private user alerts using email from JWT token
            const token = ApiService.getToken(); 
            const email = JSON.parse(atob(token.split('.')[1])).sub;
            
            stompClient.subscribe('/topic/alerts/' + email, (message) => {
                if (message.body.startsWith("{")) {
                    // It's a dashboard update
                    const dash = JSON.parse(message.body);
                    
                    // Fire off toast if profit/loss status changes
                    let newProfit = dash.profit !== undefined ? dash.profit : 0;
                    if (window.lastKnownProfit === undefined) window.lastKnownProfit = newProfit;
                    if (Math.abs(newProfit - window.lastKnownProfit) > 0.01 || window.lastKnownProfit === 0 && newProfit !== 0) {
                        if (newProfit > 0) {
                            showToast(`Portfolio is in PROFIT: +${formatCurrency(newProfit)} 📈`, 'success');
                        } else if (newProfit < 0) {
                            showToast(`Portfolio is in LOSS: ${formatCurrency(newProfit)} 📉`, 'error');
                        }
                        window.lastKnownProfit = newProfit;
                    }

                    updateDashboardUI(dash, true);
                } else {
                    // It's a text alert
                    showToast(message.body, 'success');
                    initDashboard(); // refresh
                }
            });
        });
    }

    function handleMarketUpdate(coins) {
        let tbody = document.getElementById('market-table-body');
        if (!tbody.hasChildNodes()) loadMarket(false, coins);

        let tickerHtml = '';

        coins.forEach(coin => {
            const sym = coin.symbol.toUpperCase();
            const oldPrice = coinPrices[sym];
            const newPrice = coin.currentPrice || coin.current_price;
            
            coinPrices[sym] = newPrice;

            // Flash market table row
            const tr = document.getElementById(`market-row-${sym}`);
            if (tr) {
                const priceTd = tr.querySelector('.price-col');
                priceTd.textContent = formatCurrency(newPrice);
                
                if (oldPrice && newPrice > oldPrice) {
                    tr.classList.remove('flash-down');
                    void tr.offsetWidth;
                    tr.classList.add('flash-up');
                } else if (oldPrice && newPrice < oldPrice) {
                    tr.classList.remove('flash-up');
                    void tr.offsetWidth;
                    tr.classList.add('flash-down');
                }
            }

            // Append to ticker
            const change24h = coin.price_change_percentage_24h || 0;
            const isUp = change24h >= 0;
            tickerHtml += `<span class="${isUp ? 'text-green' : 'text-red'}" style="margin-right:2rem">${sym}: ${formatCurrency(newPrice)} (${isUp ? '+' : ''}${change24h.toFixed(2)}%) ${isUp ? '▲' : '▼'}</span>`;
            
            
            // Update Trade Screen Live Price
            if (document.getElementById('trade-symbol').value === sym) {
                const lp = document.getElementById('trade-price-display');
                lp.textContent = "Live: " + formatCurrency(newPrice);
                if (oldPrice && newPrice > oldPrice) { lp.classList.remove('flash-down'); void lp.offsetWidth; lp.classList.add('flash-up'); }
                if (oldPrice && newPrice < oldPrice) { lp.classList.remove('flash-up'); void lp.offsetWidth; lp.classList.add('flash-down'); }
            }
        });

        const tradeSelect = document.getElementById('trade-symbol');
        if (tradeSelect && tradeSelect.options.length <= 1) {
            tradeSelect.innerHTML = '';
            coins.forEach(coin => {
                const opt = document.createElement('option');
                opt.value = coin.symbol.toUpperCase();
                opt.textContent = `${coin.symbol.toUpperCase()} - ${coin.name}`;
                tradeSelect.appendChild(opt);
            });
        }

        // Set live ticker
        document.getElementById('live-ticker-banner').innerHTML = tickerHtml;

        
        // Soft refresh portfolio prices without strict API call since we know them
        updatePortfolioLivePrices();
        updateWatchlistLivePrices();
    }

    // ---------------- API CORE FETCHING ----------------
    async function loadWallet() {
        const res = await ApiService.getWallet();
        if (res && res.success) {
            const bal = res.data.balance || 0;
            document.getElementById('nav-wallet-balance').textContent = formatCurrency(bal);
            document.getElementById('wallet-page-balance').textContent = formatCurrency(bal);
        }
    }

    async function loadDashboard() {
        const res = await ApiService.getDashboard();
        if (res && res.success) updateDashboardUI(res.data, false);
    }

    async function loadCoinsProfit() {
        const res = await ApiService.request('/wallet/profit');
        if (res && res.success) {
            myPortfolio = res.data;
            const tbody = document.getElementById('portfolio-table-body');
            const tradeTbody = document.getElementById('trade-holdings-body');
            tbody.innerHTML = '';
            tradeTbody.innerHTML = '';

            res.data.forEach(item => {
                const coinPrice = coinPrices[item.symbol] || item.currentPrice;
                const profitVal = (coinPrice - item.avgBuyPrice) * item.quantity;
                const isP = profitVal >= 0;
                
                const rowHtml = `
                    <tr id="port-row-${item.symbol}">
                        <td><strong>${item.symbol}</strong></td>
                        <td>${formatNumber(item.quantity)}</td>
                        <td>${formatCurrency(item.avgBuyPrice)}</td>
                        <td class="port-price-col">${formatCurrency(coinPrice)}</td>
                        <td class="${isP?'text-green':'text-red'} port-profit-col">${isP?'+':''}${formatCurrency(profitVal)}</td>
                    </tr>
                `;
                tbody.innerHTML += rowHtml;

                tradeTbody.innerHTML += `
                    <tr>
                        <td><strong>${item.symbol}</strong></td>
                        <td>${formatNumber(item.quantity)}</td>
                        <td class="${isP?'text-green':'text-red'}">${isP?'+':''}${formatCurrency(profitVal)}</td>
                        <td><button class="btn btn-sm" onclick="quickSell('${item.symbol}', ${item.quantity})">Sell</button></td>
                    </tr>
                `;
            });
            drawPieChart();
            updateTradeHoldingInfo();
        }
    }

    window.quickSell = function(symbol, qty) {
        document.getElementById('trade-symbol').value = symbol;
        document.getElementById('trade-qty').value = qty;
        document.getElementById('trade-symbol-display').textContent = symbol;
        document.getElementById('trade-price-display').textContent = "Live: " + formatCurrency(coinPrices[symbol]);
        refreshTradeChart(symbol);
        updateTradeHoldingInfo();
    };

    function updateTradeHoldingInfo() {
        const sym = document.getElementById('trade-symbol').value;
        const infoBox = document.getElementById('trade-holding-info');
        const holding = myPortfolio.find(p => p.symbol === sym);

        if (holding && holding.quantity > 0) {
            infoBox.style.display = 'block';
            document.getElementById('trade-holding-qty').textContent = formatNumber(holding.quantity);
            document.getElementById('trade-holding-symbol').textContent = sym;
            
            const liveP = coinPrices[sym] || holding.currentPrice;
            const liveProfit = (liveP - holding.avgBuyPrice) * holding.quantity;
            const pEl = document.getElementById('trade-holding-profit');
            pEl.textContent = (liveProfit >= 0 ? '+' : '') + formatCurrency(liveProfit);
            pEl.className = liveProfit >= 0 ? 'text-green' : 'text-red';
        } else {
            infoBox.style.display = 'none';
        }
    }

    async function loadMarket(fetchApi = true, injectedCoins = null) {
        let coins = injectedCoins;
        if (fetchApi) {
            const res = await ApiService.getTop50();
            if (res && res.success) {
                handleMarketUpdate(res.data);
                return;
            }
        }

        if(!coins) return;
        const tbody = document.getElementById('market-table-body');
        tbody.innerHTML = '';
        coins.forEach(coin => {
            const sym = coin.symbol.toUpperCase();
            const p = coin.currentPrice || coin.current_price;
            const change24h = coin.price_change_percentage_24h || 0;
            const colorClass = change24h >= 0 ? 'text-green' : 'text-red';
            coinPrices[sym] = p;
            
            tbody.innerHTML += `
                <tr id="market-row-${sym}">
                    <td><strong>${sym}</strong></td>
                    <td>${coin.name}</td>
                    <td class="price-col ${colorClass}">${formatCurrency(p)}</td>
                    <td>
                        <button class="btn btn-sm btn-buy-direct" onclick="jumpToTrade('${sym}', ${p})" style="background:var(--accent);">Trade</button>
                        <button class="btn btn-sm" onclick="addWatchlist('${sym}')">★</button>
                    </td>
                </tr>
            `;
        });
    }

    // ---------------- DYNAMIC UI UPDATERS ----------------
    function updateDashboardUI(data, flash) {
        const valEl = document.getElementById('dashboard-total-value');
        const invEl = document.getElementById('dashboard-total-invested');
        const proEl = document.getElementById('dashboard-total-profit');
        
        valEl.textContent = formatCurrency(data.currentValue || data.totalPortfolioValue || 0);
        invEl.textContent = formatCurrency(data.totalInvestment || data.totalInvested || 0);
        
        let profit = data.profit !== undefined ? data.profit : (data.totalProfit || 0);
        const isP = profit >= 0;
        proEl.textContent = `${isP?'+':''}${formatCurrency(profit)}`;
        proEl.className = `stat-value ${isP?'text-green':'text-red'}`;

        if(flash) {
            valEl.classList.remove('flash-up', 'flash-down');
            void valEl.offsetWidth;
            valEl.classList.add(isP ? 'flash-up' : 'flash-down');
        }

        loadCoinsProfit();
    }

    function updatePortfolioLivePrices() {
        myPortfolio.forEach(item => {
            const sym = item.symbol;
            if(coinPrices[sym]) {
                const tr = document.getElementById(`port-row-${sym}`);
                if(tr) {
                    const priceTd = tr.querySelector('.port-price-col');
                    const profitTd = tr.querySelector('.port-profit-col');
                    
                    priceTd.textContent = formatCurrency(coinPrices[sym]);
                    const profitVal = (coinPrices[sym] - item.avgBuyPrice) * item.quantity;
                    const isP = profitVal >= 0;
                    profitTd.textContent = `${isP?'+':''}${formatCurrency(profitVal)}`;
                    profitTd.className = `port-profit-col ${isP ? 'text-green' : 'text-red'}`;
                }
            }
        });
    }
    
    function updateWatchlistLivePrices() {
        const rows = document.querySelectorAll('.wl-row');
        rows.forEach(tr => {
            const sym = tr.getAttribute('data-sym');
            if(coinPrices[sym]) {
                tr.querySelector('.wl-price-col').textContent = formatCurrency(coinPrices[sym]);
            }
        });
    }

    window.jumpToTrade = function(symbol, price) {
        document.getElementById('trade-symbol').value = symbol;
        document.getElementById('trade-symbol-display').textContent = symbol;
        document.getElementById('trade-price-display').textContent = "Live: " + formatCurrency(price);
        document.querySelector('[data-target="view-trade"]').click();
        refreshTradeChart(symbol);
    };

    // ---------------- CHARTS ----------------
    function drawPieChart() {
        if(!myPortfolio.length) return;
        const ctx = document.getElementById('portfolioPieChart').getContext('2d');
        const labels = myPortfolio.map(p => p.symbol);
        const data = myPortfolio.map(p => p.quantity * (coinPrices[p.symbol] || p.averageBuyPrice));
        const bgColors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6'];

        if(portfolioPieChart) {
            portfolioPieChart.data.labels = labels;
            portfolioPieChart.data.datasets[0].data = data;
            portfolioPieChart.update();
        } else {
            portfolioPieChart = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels,
                    datasets: [{ data, backgroundColor: bgColors, borderWidth: 0 }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { position: 'bottom', labels: { color: '#f8fafc' } } }
                }
            });
        }
    }

    async function refreshTradeChart(symbol = null) {
        const sym = symbol || document.getElementById('trade-symbol').value || 'BTC';
        const ctx = document.getElementById('cryptoPriceChart').getContext('2d');
        
        try {
            // CoinGecko Historical Sparkline hack via simple search
            const idMap = {
                'BTC':'bitcoin', 'ETH':'ethereum', 'SOL':'solana', 'XRP':'ripple', 
                'DOGE':'dogecoin', 'USDT':'tether', 'BNB':'binancecoin', 'USDC':'usd-coin',
                'ADA':'cardano', 'AVAX':'avalanche-2', 'SHIB':'shiba-inu', 'TON':'the-open-network',
                'DOT':'polkadot', 'TRX':'tron', 'LINK':'chainlink', 'MATIC':'matic-network'
            };
            const geckoId = idMap[sym.toUpperCase()] || 'bitcoin';
            
            const req = await fetch(`https://api.coingecko.com/api/v3/coins/${geckoId}/market_chart?vs_currency=usd&days=7`);
            const data = await req.json();
            
            const prices = data.prices.map(p => p[1]);
            const labels = data.prices.map((p, i) => i); // Dummies
            
            const color = prices[prices.length-1] >= prices[0] ? '#10b981' : '#ef4444';

            if(mainCryptoChart) mainCryptoChart.destroy();
            mainCryptoChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels,
                    datasets: [{
                        label: `${sym} 7d Trend`,
                        data: prices,
                        borderColor: color,
                        tension: 0.1,
                        pointRadius: 0
                    }]
                },
                options: {
                    responsive: true,
                    scales: { x: { display: false }, y: { display: true, ticks:{color:'#94a3b8'} } },
                    plugins: { legend: { display: false } }
                }
            });
        } catch (e) { console.log('Chart load err'); }
    }

    document.getElementById('trade-symbol').addEventListener('change', (e) => {
        const val = e.target.value.toUpperCase();
        document.getElementById('trade-symbol-display').textContent = val || '???';
        document.getElementById('trade-price-display').textContent = coinPrices[val] ? "Live: " + formatCurrency(coinPrices[val]) : 'Price Unknown';
        refreshTradeChart(val);
        updateTradeHoldingInfo();
    });

    // ---------------- TRADE & LIMIT ORDERS ----------------
    document.getElementById('tab-market').addEventListener('click', (e) => {
        currentTradeType = 'MARKET';
        e.target.classList.add('btn-active');
        document.getElementById('tab-limit').classList.remove('btn-active');
        document.getElementById('limit-price-group').style.display = 'none';
        document.getElementById('btn-buy').textContent = 'Buy Market';
        document.getElementById('btn-sell').textContent = 'Sell Market';
    });

    document.getElementById('tab-limit').addEventListener('click', (e) => {
        currentTradeType = 'LIMIT';
        e.target.classList.add('btn-active');
        document.getElementById('tab-market').classList.remove('btn-active');
        document.getElementById('limit-price-group').style.display = 'block';
        document.getElementById('btn-buy').textContent = 'Place Buy Limit';
        document.getElementById('btn-sell').textContent = 'Place Sell Limit';
    });

    async function handleTradeAction(actionType) {
        const sym = document.getElementById('trade-symbol').value.toUpperCase();
        const qty = document.getElementById('trade-qty').value;
        const target = document.getElementById('trade-target').value;

        if(!sym || !qty) return showToast('Fill all fields', 'error');

        if(currentTradeType === 'MARKET') {
            const res = actionType === 'BUY' ? await ApiService.buyCrypto(sym, qty) : await ApiService.sellCrypto(sym, qty);
            if (res && res.success) {
                showToast(`Market ${actionType} filled!`, 'success');
                initDashboard();
            } else showToast(res.message, 'error');
        } else {
            if(!target) return showToast('Enter limit price', 'error');
            const res = await ApiService.request('/orders/limit', {
                method: 'POST', body: {type: actionType, symbol: sym, quantity: qty, targetPrice: target}
            });
            if (res && res.success) {
                showToast('Limit order placed', 'success');
                loadOpenOrders();
            } else showToast(res.message, 'error');
        }
    }

    document.getElementById('btn-buy').addEventListener('click', () => handleTradeAction('BUY'));
    document.getElementById('btn-sell').addEventListener('click', () => handleTradeAction('SELL'));

    async function loadOpenOrders() {
        const res = await ApiService.request('/orders');
        if(res && res.success) {
            const tbody = document.getElementById('open-orders-body');
            tbody.innerHTML = '';
            res.data.forEach(o => {
                const c = o.type === 'BUY' ? 'badge-green' : 'badge-red';
                tbody.innerHTML += `
                    <tr>
                        <td><span class="badge ${c}">LIMIT ${o.type}</span></td>
                        <td><strong>${o.symbol}</strong></td>
                        <td>${o.quantity}</td>
                        <td>${formatCurrency(o.targetPrice)}</td>
                        <td><button class="btn btn-sm" onclick="cancelOrder(${o.id})">Cancel</button></td>
                    </tr>
                `;
            });
        }
    }

    window.cancelOrder = async function(id) {
        const res = await ApiService.request('/orders/cancel', { method: 'POST', body: {id} });
        if(res && res.success) { showToast('Order cancelled', 'success'); loadOpenOrders(); }
    }

    // ---------------- WATCHLIST ----------------
    async function loadWatchlist() {
        const res = await ApiService.request('/watchlist');
        if(res && res.success) {
            const tbody = document.getElementById('watchlist-table-body');
            tbody.innerHTML = '';
            res.data.forEach(w => {
                tbody.innerHTML += `
                    <tr class="wl-row" data-sym="${w.symbol.toUpperCase()}">
                        <td><strong>${w.symbol.toUpperCase()}</strong></td>
                        <td>${w.name}</td>
                        <td class="wl-price-col">${formatCurrency(coinPrices[w.symbol.toUpperCase()] || w.currentPrice)}</td>
                        <td><button class="btn btn-sm btn-sell" onclick="removeWatchlist('${w.symbol}')">🗑</button></td>
                    </tr>
                `;
            });
        }
    }

    window.addWatchlist = async function(symbol) {
        const res = await ApiService.request('/watchlist/add', {method: 'POST', body: {symbol}});
        if(res && res.success) { showToast('Added to watchlist', 'success'); loadWatchlist(); }
    }
    window.removeWatchlist = async function(symbol) {
        const res = await ApiService.request('/watchlist/remove', {method: 'POST', body: {symbol}});
        if(res && res.success) { showToast('Removed from watchlist', 'success'); loadWatchlist(); }
    }

    // ---------------- LEADERBOARD ----------------
    async function loadLeaderboard() {
        const res = await ApiService.request('/users/leaderboard');
        if(res && res.success) {
            const tbody = document.getElementById('leaderboard-table-body');
            tbody.innerHTML = '';
            res.data.forEach((u, i) => {
                const isP = u.totalProfit >= 0;
                tbody.innerHTML += `
                    <tr>
                        <td><strong>#${i+1}</strong></td>
                        <td>${u.name}</td>
                        <td class="${isP?'text-green':'text-red'} font-bold">
                            ${isP?'+':''}${formatCurrency(u.totalProfit)}
                        </td>
                    </tr>
                `;
            });
        }
    }

    // Wallet & History simplified bindings
    // Re-implemented to avoid losing them
    document.getElementById('btn-deposit').addEventListener('click', async () => {
        const amt = document.getElementById('wallet-amount').value;
        if (!amt) return;
        const res = await ApiService.deposit(amt);
        if(res && res.success) { showToast('Deposited', 'success'); loadWallet(); }
    });

    document.getElementById('btn-withdraw').addEventListener('click', async () => {
        const amt = document.getElementById('wallet-amount').value;
        if (!amt) return;
        const res = await ApiService.withdraw(amt);
        if(res && res.success) { showToast('Withdrawn', 'success'); loadWallet(); }
    });

    async function loadHistory() {
        const res = await ApiService.getTransactions();
        if(res && res.success) {
            const tbody = document.getElementById('history-table-body');
            tbody.innerHTML = '';
            [...res.data].reverse().forEach(item => {
                const typeClass = item.type.includes('BUY') ? 'badge-green' : 'badge-red';
                tbody.innerHTML += `
                    <tr>
                        <td><span class="badge ${typeClass}">${item.type.replace('_LIMIT', ' (LMT)')}</span></td>
                        <td><strong>${item.crypto}</strong></td>
                        <td>${formatNumber(item.quantity)}</td>
                        <td>${formatCurrency(item.price)}</td>
                        <td>${formatCurrency(item.totalAmount)}</td>
                    </tr>
                `;
            });
        }
    }

    // ---------------- PROFILE & THEME ----------------
    async function loadUserProfile(isSilent = false) {
        const res = await ApiService.request('/users/profile');
        if(res && res.success) {
            const user = res.data;
            
            // Apply Theme
            if(user.themePreference === 'light') {
                document.body.classList.add('light-theme');
                updateThemeUI(true);
            } else {
                document.body.classList.remove('light-theme');
                updateThemeUI(false);
            }

            if(isSilent) return;

            // Fill UI
            document.getElementById('profile-name-display').textContent = user.name;
            document.getElementById('profile-email-display').textContent = user.email;
            document.getElementById('prof-name').value = user.name;
            document.getElementById('prof-bio').value = user.bio || '';
            document.getElementById('prof-phone').value = user.phoneNumber || '';
            document.getElementById('prof-photo-url').value = user.profilePhotoUrl || '';
            document.getElementById('prof-gender').value = user.gender || 'male';
            document.getElementById('prof-country').value = user.country || '';
            
            document.getElementById('prof-joined-date').textContent = new Date(user.joinedAt).toLocaleDateString();
            document.getElementById('prof-dob-text').textContent = user.dateOfBirth ? new Date(user.dateOfBirth).toLocaleDateString() : 'Not Set';
            
            const tierEl = document.getElementById('profile-tier-badge');
            tierEl.textContent = user.userTier;
            tierEl.className = 'tier-badge tier-' + user.userTier.toLowerCase().replace(' ', '-');
            document.getElementById('prof-tier-text').textContent = user.userTier;

            const kycBadge = document.getElementById('profile-kyc-badge');
            if(user.isKycVerified) kycBadge.style.display = 'inline-block';
            else kycBadge.style.display = 'none';

            if(user.profilePhotoUrl) {
                document.getElementById('profile-img-large').src = user.profilePhotoUrl;
                document.getElementById('nav-profile-img').src = user.profilePhotoUrl;
            } else {
                const defaultAvatar = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=3b82f6&color=fff`;
                document.getElementById('profile-img-large').src = defaultAvatar;
                document.getElementById('nav-profile-img').src = defaultAvatar;
            }
        }
    }

    function updateThemeUI(isLight) {
        const icon = document.getElementById('theme-icon');
        const text = document.getElementById('theme-text');
        if(isLight) {
            icon.textContent = '☀️';
            text.textContent = 'Switch to Dark Mode';
        } else {
            icon.textContent = '🌙';
            text.textContent = 'Switch to Light Mode';
        }
    }

    themeToggle.addEventListener('click', async () => {
        const isLight = document.body.classList.toggle('light-theme');
        updateThemeUI(isLight);
        
        await ApiService.request('/users/profile', {
            method: 'PUT',
            body: { themePreference: isLight ? 'light' : 'dark' }
        });
    });

    document.getElementById('btn-save-profile').addEventListener('click', async () => {
        const payload = {
            name: document.getElementById('prof-name').value,
            bio: document.getElementById('prof-bio').value,
            phoneNumber: document.getElementById('prof-phone').value,
            profilePhotoUrl: document.getElementById('prof-photo-url').value,
            gender: document.getElementById('prof-gender').value,
            country: document.getElementById('prof-country').value
        };

        const res = await ApiService.request('/users/profile', {
            method: 'PUT',
            body: payload
        });

        if(res && res.success) {
            showToast('Profile updated successfully!');
            loadUserProfile();
        }
    });

});
