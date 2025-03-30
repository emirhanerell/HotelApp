// API endpoint'leri
const API_BASE_URL = 'http://localhost:8080/api';

// Form elementlerini seç
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

// Auth JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Kayıt formu varsa
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // Giriş formu varsa
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

// Kayıt işlemi
async function handleRegister(event) {
    event.preventDefault();
    
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Şifre kontrolü
    if (password !== confirmPassword) {
        alert('Şifreler eşleşmiyor!');
        return;
    }

    try {
        console.log('Kayıt isteği gönderiliyor:', {
            name,
            email,
            password,
            role: 'ROLE_ADMIN'
        });

        // API'ye kayıt isteği gönder
        const response = await fetch(`${API_BASE_URL}/admin/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                name,
                email,
                password,
                role: 'ROLE_ADMIN'
            })
        });

        const data = await response.json();
        console.log('Kayıt cevabı:', data);

        if (response.ok) {
            // Başarılı kayıt
            localStorage.setItem('adminToken', data.token);
            window.location.href = 'admin-dashboard.html';
        } else {
            // Hata durumu
            alert(data.message || 'Kayıt sırasında bir hata oluştu.');
        }
    } catch (error) {
        console.error('Kayıt hatası:', error);
        alert('Kayıt sırasında bir hata oluştu. Lütfen tekrar deneyin.');
    }
}

// Giriş işlemi
async function handleLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        console.log('Giriş isteği gönderiliyor:', {
            email,
            password
        });

        // API'ye giriş isteği gönder
        const response = await fetch(`${API_BASE_URL}/admin/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                email,
                password
            })
        });

        const data = await response.json();
        console.log('Giriş cevabı:', data);

        if (response.ok) {
            if (data.token) {
                // Başarılı giriş
                localStorage.setItem('adminToken', data.token);
                window.location.href = 'admin-dashboard.html';
            } else {
                throw new Error('Token alınamadı');
            }
        } else {
            throw new Error(data.message || 'Giriş başarısız');
        }
    } catch (error) {
        console.error('Giriş hatası:', error);
        alert(error.message || 'Giriş sırasında bir hata oluştu. Lütfen tekrar deneyin.');
    }
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
    const token = localStorage.getItem('adminToken');
    const currentPage = window.location.pathname.split('/').pop();
    
    // Eğer login veya register sayfasındaysa token kontrolü yapma
    if (currentPage === 'login.html' || currentPage === 'register.html') {
        if (token) {
            // Token varsa admin paneline yönlendir
            window.location.href = 'admin-dashboard.html';
        }
        return;
    }
    
    // Diğer sayfalarda token yoksa login sayfasına yönlendir
    if (!token) {
        window.location.href = 'login.html';
    }
}

// Sayfa yüklendiğinde token kontrolü yap
document.addEventListener('DOMContentLoaded', checkAuth); 