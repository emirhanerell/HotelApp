// API endpoint'leri
const API_BASE_URL = 'http://localhost:8080/api';

// Form elementlerini seç
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

// Login form işlemi
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch(`${API_BASE_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                // Token'ı localStorage'a kaydet
                localStorage.setItem('token', data.token);
                
                // Kullanıcıyı ana sayfaya yönlendir
                window.location.href = 'index.html';
            } else {
                const error = await response.json();
                showError(loginForm, error.message || 'Giriş başarısız.');
            }
        } catch (error) {
            showError(loginForm, 'Bir hata oluştu. Lütfen tekrar deneyin.');
        }
    });
}

// Register form işlemi
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const name = document.getElementById('name').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Şifre kontrolü
        if (password !== confirmPassword) {
            showError(registerForm, 'Şifreler eşleşmiyor.');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name, email, password })
            });

            if (response.ok) {
                const data = await response.json();
                // Token'ı localStorage'a kaydet
                localStorage.setItem('token', data.token);
                
                // Kullanıcıyı ana sayfaya yönlendir
                window.location.href = 'index.html';
            } else {
                const error = await response.json();
                showError(registerForm, error.message || 'Kayıt başarısız.');
            }
        } catch (error) {
            showError(registerForm, 'Bir hata oluştu. Lütfen tekrar deneyin.');
        }
    });
}

// Hata mesajı gösterme fonksiyonu
function showError(form, message) {
    // Varolan hata mesajını temizle
    const existingError = form.querySelector('.error-message');
    if (existingError) {
        existingError.remove();
    }

    // Yeni hata mesajı oluştur
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;

    // Hata mesajını forma ekle
    form.appendChild(errorDiv);
}

// Token kontrolü
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token && !window.location.href.includes('login.html') && !window.location.href.includes('register.html')) {
        // Token yoksa ve login/register sayfasında değilse, login sayfasına yönlendir
        window.location.href = 'login.html';
    }
}

// Sayfa yüklendiğinde token kontrolü yap
document.addEventListener('DOMContentLoaded', checkAuth); 