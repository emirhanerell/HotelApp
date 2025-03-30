// API endpoint'leri
const API_BASE_URL = 'http://localhost:8080/api';

// Form elementleri
const reservationForm = document.getElementById('reservationForm');
const roomSelect = document.getElementById('roomSelect');
const checkInDate = document.getElementById('checkInDate');
const checkOutDate = document.getElementById('checkOutDate');
const numberOfGuests = document.getElementById('numberOfGuests');
const specialRequests = document.getElementById('specialRequests');
const totalPrice = document.getElementById('totalPrice');
const reservationsList = document.getElementById('reservationsList');

// Sayfa yüklendiğinde
document.addEventListener('DOMContentLoaded', () => {
    checkSession();
    loadAvailableRooms();
    setupEventListeners();
});

// Event listener'ları ayarla
function setupEventListeners() {
    // Tarih değişikliklerinde toplam fiyatı güncelle
    checkInDate.addEventListener('change', calculateTotalPrice);
    checkOutDate.addEventListener('change', calculateTotalPrice);
    roomSelect.addEventListener('change', calculateTotalPrice);
}

// Oturum kontrolü
function checkSession() {
    const customerId = localStorage.getItem('customerId');
    const customerToken = localStorage.getItem('customerToken');
    const userRole = localStorage.getItem('userRole');
    
    if (!customerId || !customerToken || userRole !== 'CUSTOMER') {
        console.error('Oturum bilgileri eksik:', { customerId, customerToken, userRole });
        // Oturum bilgilerini temizle
        localStorage.clear();
        window.location.href = 'customer-login.html';
        return;
    }
}

// Müsait odaları yükle
async function loadAvailableRooms() {
    try {
        const response = await fetch(`${API_BASE_URL}/rooms/filtered?available=true`);
        if (!response.ok) {
            throw new Error('Odalar yüklenirken bir hata oluştu.');
        }
        const rooms = await response.json();
        displayAvailableRooms(rooms);
    } catch (error) {
        console.error('Oda listesi yükleme hatası:', error);
        showError('Odalar yüklenirken bir hata oluştu.');
    }
}

// Müsait odaları göster
function displayAvailableRooms(rooms) {
    roomSelect.innerHTML = '<option value="">Oda seçin...</option>';
    rooms.forEach(room => {
        const option = document.createElement('option');
        option.value = room.id;
        option.textContent = `${room.roomNumber} - ${room.roomType} (${room.price} TL/gece)`;
        roomSelect.appendChild(option);
    });
}

// Müşteri rezervasyonlarını yükle
async function loadCustomerReservations() {
    try {
        const customerId = localStorage.getItem('customerId');
        const customerToken = localStorage.getItem('customerToken');
        
        if (!customerId || !customerToken) {
            console.error('Oturum bilgileri eksik');
            return;
        }

        const response = await fetch(`${API_BASE_URL}/reservations/customer/${customerId}`, {
            headers: {
                'Authorization': `Bearer ${customerToken}`
            }
        });

        if (!response.ok) {
            const error = await response.json();
            console.error('Sunucu hatası:', error);
            throw new Error(error.message || 'Rezervasyonlar yüklenirken bir hata oluştu.');
        }

        const reservations = await response.json();
        displayReservations(reservations);
    } catch (error) {
        console.error('Rezervasyon listesi yükleme hatası:', error);
        showError('Rezervasyonlar yüklenirken bir hata oluştu.');
    }
}

// Rezervasyonları göster
function displayReservations(reservations) {
    const reservationsList = document.getElementById('reservationsList');
    reservationsList.innerHTML = '';
    
    if (!reservations || reservations.length === 0) {
        reservationsList.innerHTML = '<div class="col-12"><p class="text-center">Henüz rezervasyonunuz bulunmuyor.</p></div>';
        return;
    }

    reservations.forEach(reservation => {
        const reservationCard = document.createElement('div');
        reservationCard.className = 'col-md-6 mb-3';
        reservationCard.innerHTML = `
            <div class="card h-100">
                <div class="card-body">
                    <h6 class="card-title">Oda ${reservation.room?.roomNumber || 'Bilinmiyor'}</h6>
                    <p class="card-text">
                        <small class="text-muted">Giriş: ${formatDate(reservation.checkInDate)}</small><br>
                        <small class="text-muted">Çıkış: ${formatDate(reservation.checkOutDate)}</small><br>
                        <small class="text-muted">Durum: ${getStatusText(reservation.status)}</small><br>
                        <small class="text-muted">Misafir Sayısı: ${reservation.numberOfGuests}</small><br>
                        <small class="text-muted">Toplam: ${reservation.totalPrice} TL</small>
                    </p>
                    ${reservation.specialRequests ? `
                        <p class="card-text">
                            <small class="text-muted">Özel İstekler: ${reservation.specialRequests}</small>
                        </p>
                    ` : ''}
                    ${reservation.status === 'PENDING' ? `
                        <button class="btn btn-sm btn-danger" onclick="cancelReservation(${reservation.id})">
                            <i class="fas fa-times"></i> İptal Et
                        </button>
                    ` : ''}
                </div>
            </div>
        `;
        reservationsList.appendChild(reservationCard);
    });
}

// Rezervasyon durumunu Türkçe olarak göster
function getStatusText(status) {
    const statusMap = {
        'PENDING': 'Beklemede',
        'CONFIRMED': 'Onaylandı',
        'CANCELLED': 'İptal Edildi',
        'COMPLETED': 'Tamamlandı'
    };
    return statusMap[status] || status;
}

// Toplam fiyatı hesapla
function calculateTotalPrice() {
    const selectedRoom = roomSelect.options[roomSelect.selectedIndex];
    if (!selectedRoom.value || !checkInDate.value || !checkOutDate.value) {
        totalPrice.textContent = '0.00 TL';
        return;
    }

    const price = parseFloat(selectedRoom.text.match(/\((\d+) TL/)[1]);
    const checkIn = new Date(checkInDate.value);
    const checkOut = new Date(checkOutDate.value);
    const days = Math.ceil((checkOut - checkIn) / (1000 * 60 * 60 * 24));
    
    if (days > 0) {
        totalPrice.textContent = `${(price * days).toFixed(2)} TL`;
    } else {
        totalPrice.textContent = '0.00 TL';
    }
}

// Rezervasyon formunu gönder
async function submitReservation() {
    const customerId = localStorage.getItem('customerId');
    const customerToken = localStorage.getItem('customerToken');
    
    if (!customerId || !customerToken) {
        showError('Müşteri bilgisi bulunamadı. Lütfen tekrar giriş yapın.');
        window.location.href = 'customer-login.html';
        return;
    }

    // Form verilerini kontrol et
    if (!roomSelect.value) {
        showError('Lütfen bir oda seçin.');
        return;
    }

    if (!checkInDate.value || !checkOutDate.value) {
        showError('Lütfen giriş ve çıkış tarihlerini seçin.');
        return;
    }

    if (!numberOfGuests.value) {
        showError('Lütfen misafir sayısını girin.');
        return;
    }

    const formData = {
        roomId: parseInt(roomSelect.value),
        customerId: parseInt(customerId),
        checkInDate: checkInDate.value,
        checkOutDate: checkOutDate.value,
        numberOfGuests: parseInt(numberOfGuests.value),
        specialRequests: specialRequests.value
    };

    // Form verilerini kontrol et
    console.log('Gönderilen veri:', formData);

    try {
        const response = await fetch(`${API_BASE_URL}/reservations`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${customerToken}`
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const data = await response.json();
            console.error('Sunucu hatası:', data);
            throw new Error(data.message || 'Rezervasyon oluşturulurken bir hata oluştu.');
        }

        const result = await response.json();
        console.log('Rezervasyon başarılı:', result);
        showSuccess('Rezervasyonunuz başarıyla oluşturuldu!');
        reservationForm.reset();
        totalPrice.textContent = '0.00 TL';
        reservationModal.hide();
        loadCustomerReservations();
    } catch (error) {
        console.error('Rezervasyon oluşturma hatası:', error);
        showError(error.message || 'Rezervasyon oluşturulurken bir hata oluştu.');
    }
}

// Rezervasyon iptal etme
async function cancelReservation(reservationId) {
    if (!confirm('Rezervasyonunuzu iptal etmek istediğinizden emin misiniz?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/reservations/${reservationId}/status?status=CANCELLED`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('customerToken')}`
            }
        });

        if (!response.ok) {
            throw new Error('Rezervasyon iptal edilirken bir hata oluştu.');
        }

        showSuccess('Rezervasyonunuz başarıyla iptal edildi!');
        loadCustomerReservations();
    } catch (error) {
        console.error('Rezervasyon iptal hatası:', error);
        showError('Rezervasyon iptal edilirken bir hata oluştu.');
    }
}

// Tarih formatı
function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('tr-TR');
}

// Hata mesajı gösterme
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger alert-dismissible fade show';
    errorDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    const mainContent = document.querySelector('main');
    mainContent.insertBefore(errorDiv, mainContent.firstChild);
    
    setTimeout(() => {
        errorDiv.remove();
    }, 5000);
}

// Başarı mesajı gösterme
function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'alert alert-success alert-dismissible fade show';
    successDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    const mainContent = document.querySelector('main');
    mainContent.insertBefore(successDiv, mainContent.firstChild);
    
    setTimeout(() => {
        successDiv.remove();
    }, 5000);
}

// Ana sayfayı göster
function showDashboard() {
    document.querySelector('.welcome-section').style.display = 'block';
    document.querySelector('.reservation-section').style.display = 'none';
    document.querySelector('.profile-section').style.display = 'none';
    
    // Aktif menü öğesini güncelle
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelector('.nav-link[onclick="showDashboard()"]').classList.add('active');
}

// Rezervasyonları göster
function showReservations() {
    document.querySelector('.welcome-section').style.display = 'none';
    document.querySelector('.reservation-section').style.display = 'block';
    document.querySelector('.profile-section').style.display = 'none';
    
    // Aktif menü öğesini güncelle
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelector('.nav-link[onclick="showReservations()"]').classList.add('active');
    
    // Rezervasyonları yükle
    loadCustomerReservations();
}

// Profili göster
function showProfile() {
    document.querySelector('.welcome-section').style.display = 'none';
    document.querySelector('.reservation-section').style.display = 'none';
    document.querySelector('.profile-section').style.display = 'block';
    
    // Aktif menü öğesini güncelle
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    document.querySelector('.nav-link[onclick="showProfile()"]').classList.add('active');
} 