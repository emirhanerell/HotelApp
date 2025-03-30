document.addEventListener('DOMContentLoaded', () => {
    const customerRegisterForm = document.getElementById('customerRegisterForm');
    
    if (customerRegisterForm) {
        // Şifre validasyonu
        const passwordInput = document.getElementById('password');
        if (passwordInput) {
            passwordInput.addEventListener('input', (e) => {
                const password = e.target.value;
                if (password.length < 6) {
                    e.target.setCustomValidity('Şifre en az 6 karakter olmalıdır.');
                } else {
                    e.target.setCustomValidity('');
                }
            });
        }

        customerRegisterForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Form verilerini topla
            const formData = new FormData(customerRegisterForm);
            const customerData = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName'),
                email: formData.get('email'),
                password: formData.get('password'),
                phoneNumber: formData.get('phoneNumber'),
                birthDate: formData.get('birthDate'),
                city: formData.get('city'),
                country: formData.get('country'),
                postalCode: formData.get('postalCode'),
                idNumber: formData.get('idNumber'),
                specialRequests: formData.get('specialRequests'),
                communicationPreferences: Array.from(formData.getAll('communicationPreferences')),
                marketingPreferences: Array.from(formData.getAll('marketingPreferences'))
            };

            try {
                const response = await fetch('http://localhost:8080/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(customerData)
                });

                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('customerToken', data.token);
                    window.location.href = 'customer-dashboard.html';
                } else {
                    const error = await response.json();
                    console.error('Sunucu hatası:', error);
                    alert(error.message || 'Kayıt işlemi başarısız oldu.');
                }
            } catch (error) {
                console.error('Kayıt hatası:', error);
                alert('Kayıt işlemi sırasında bir hata oluştu.');
            }
        });
    }

    // Giriş formu işlemleri
    const customerLoginForm = document.getElementById('customerLoginForm');
    if (customerLoginForm) {
        customerLoginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const formData = new FormData(customerLoginForm);
            const loginData = {
                email: formData.get('email'),
                password: formData.get('password')
            };

            try {
                const response = await fetch('http://localhost:8080/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(loginData)
                });

                if (response.ok) {
                    const data = await response.json();
                    console.log('Giriş başarılı:', data);
                    // Önce mevcut oturum bilgilerini temizle
                    localStorage.clear();
                    // Yeni oturum bilgilerini kaydet
                    localStorage.setItem('customerToken', data.token);
                    localStorage.setItem('customerId', data.customerId);
                    localStorage.setItem('userRole', 'CUSTOMER');
                    // Yönlendirme öncesi kısa bir gecikme ekle
                    setTimeout(() => {
                        window.location.href = 'customer-dashboard.html';
                    }, 100);
                } else {
                    const error = await response.json();
                    console.error('Giriş hatası:', error);
                    alert(error.message || 'Giriş işlemi başarısız oldu. Lütfen e-posta ve şifrenizi kontrol edin.');
                }
            } catch (error) {
                console.error('Giriş hatası:', error);
                alert('Giriş işlemi sırasında bir hata oluştu.');
            }
        });
    }
});

// Müşteri giriş kontrolü
function checkCustomerAuth() {
    const customerToken = localStorage.getItem('customerToken');
    const userRole = localStorage.getItem('userRole');
    
    // Eğer müşteri girişi yapılmışsa ve login/register sayfalarında değilsek
    if (customerToken && userRole === 'CUSTOMER' && 
        (window.location.href.includes('customer-login.html') || 
         window.location.href.includes('customer-register.html'))) {
        window.location.href = 'customer-dashboard.html';
        return;
    }
    
    // Eğer müşteri girişi yapılmamışsa ve login/register sayfalarında değilsek
    if (!customerToken && !window.location.href.includes('customer-login.html') && 
        !window.location.href.includes('customer-register.html')) {
        window.location.href = 'customer-login.html';
    }
}

// Sayfa yüklendiğinde auth kontrolü yap
document.addEventListener('DOMContentLoaded', () => {
    checkCustomerAuth();
}); 