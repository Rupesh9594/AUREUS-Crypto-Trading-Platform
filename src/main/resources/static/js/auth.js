document.addEventListener('DOMContentLoaded', () => {
    // If already authenticated, redirect to dashboard
    if (ApiService.isAuthenticated()) {
        window.location.href = '/dashboard.html';
        return;
    }

    const authForm = document.getElementById('auth-form');
    const switchLink = document.getElementById('switch-link');
    const authTitle = document.getElementById('auth-title');
    const authBtn = document.getElementById('auth-btn');
    const nameGroup = document.getElementById('name-group');
    const nameInput = document.getElementById('name');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const switchText = document.getElementById('switch-text');

    let isLoginMode = true;

    switchLink.addEventListener('click', (e) => {
        e.preventDefault();
        isLoginMode = !isLoginMode;
        
        if (isLoginMode) {
            authTitle.textContent = 'Welcome Back';
            authBtn.textContent = 'Login';
            switchText.textContent = "Don't have an account? ";
            switchLink.textContent = 'Sign Up';
            nameGroup.style.display = 'none';
            nameInput.removeAttribute('required');
        } else {
            authTitle.textContent = 'Create Account';
            authBtn.textContent = 'Sign Up';
            switchText.textContent = "Already have an account? ";
            switchLink.textContent = 'Login';
            nameGroup.style.display = 'block';
            nameInput.setAttribute('required', 'true');
        }
    });

    authForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = emailInput.value;
        const password = passwordInput.value;
        const name = nameInput.value;

        try {
            authBtn.disabled = true;
            authBtn.textContent = 'Please wait...';

            if (isLoginMode) {
                const response = await ApiService.login(email, password);
                if (response.success) {
                    ApiService.setToken(response.data);
                    showToast('Login successful!', 'success');
                    setTimeout(() => window.location.href = '/dashboard.html', 1000);
                } else {
                    showToast(response.message || 'Login failed', 'error');
                }
            } else {
                const response = await ApiService.register(name, email, password);
                if (response.success) {
                    showToast('Registration successful! Please login.', 'success');
                    // switch back to login mode automatically
                    switchLink.click();
                } else {
                    showToast(response.message || 'Registration failed', 'error');
                }
            }
        } catch (error) {
            showToast('An error occurred. Please check server.', 'error');
        } finally {
            authBtn.disabled = false;
            authBtn.textContent = isLoginMode ? 'Login' : 'Sign Up';
        }
    });
});
