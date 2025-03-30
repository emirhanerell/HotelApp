// Admin Panel JavaScript

// API endpoint'leri
const API_BASE_URL = 'http://localhost:8080/api';

// Form elementlerini seç
const addRoomForm = document.getElementById('addRoomForm');
const roomTableBody = document.getElementById('roomTableBody');

// Sayfa yüklendiğinde çalışacak fonksiyonlar
document.addEventListener('DOMContentLoaded', function() {
    // Oturum kontrolü
    checkAdminSession();
    
    // Event listener'ları ekle
    setupEventListeners();
    
    // İlk verileri yükle
    loadDashboardData();

    // Oda ekleme formu varsa
    if (addRoomForm) {
        addRoomForm.addEventListener('submit', handleAddRoom);
    }

    // Oda listesini yükle
    loadRooms();
});

// Oturum kontrolü
function checkAdminSession() {
    const adminToken = localStorage.getItem('adminToken');
    if (!adminToken) {
        window.location.href = 'login.html';
    }
}

// Event listener'ları ayarla
function setupEventListeners() {
    // Çıkış butonu
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
    
    // Sidebar linkleri
    const sidebarLinks = document.querySelectorAll('.admin-sidebar .nav-link');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', handleSidebarNavigation);
    });
}

// Dashboard verilerini yükle
async function loadDashboardData() {
    try {
        const token = localStorage.getItem('adminToken');
        if (!token) {
            window.location.href = 'login.html';
            return;
        }

        const response = await fetch(`${API_BASE_URL}/rooms`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('adminToken');
                window.location.href = 'login.html';
                return;
            }
            const errorData = await response.json();
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        const rooms = await response.json();
        const stats = {
            totalRooms: rooms.length,
            availableRooms: rooms.filter(room => room.isAvailable).length,
            occupiedRooms: rooms.filter(room => !room.isAvailable).length,
            totalIncome: rooms.reduce((sum, room) => sum + (room.isAvailable ? 0 : room.price), 0)
        };
        updateDashboardStats(stats);
    } catch (error) {
        console.error('Dashboard verileri yüklenirken hata:', error);
        showError(error.message || 'Dashboard verileri yüklenirken bir hata oluştu.');
    }
}

// Dashboard istatistiklerini güncelle
function updateDashboardStats(stats) {
    const statElements = {
        totalRooms: document.getElementById('totalRooms'),
        availableRooms: document.getElementById('availableRooms'),
        occupiedRooms: document.getElementById('occupiedRooms'),
        totalIncome: document.getElementById('totalIncome')
    };

    Object.entries(statElements).forEach(([key, element]) => {
        if (element && stats[key] !== undefined) {
            if (key === 'totalIncome') {
                element.textContent = `₺${stats[key].toLocaleString()}`;
            } else {
                element.textContent = stats[key];
            }
        }
    });
}

// Oda listesini yükle
async function loadRooms() {
    try {
        const token = localStorage.getItem('adminToken');
        if (!token) {
            window.location.href = 'login.html';
            return;
        }

        const response = await fetch(`${API_BASE_URL}/rooms`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('adminToken');
                window.location.href = 'login.html';
                return;
            }
            const errorData = await response.json();
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        const rooms = await response.json();
        displayRooms(rooms);
    } catch (error) {
        console.error('Oda listesi yükleme hatası:', error);
        showError(error.message || 'Oda listesi yüklenirken bir hata oluştu.');
    }
}

// Odaları görüntüleme
function displayRooms(rooms) {
    if (!roomTableBody) return;

    roomTableBody.innerHTML = rooms.map(room => `
        <tr>
            <td>${room.roomNumber}</td>
            <td>${room.roomType}</td>
            <td><span class="badge ${room.isAvailable ? 'bg-success' : 'bg-danger'}">${room.isAvailable ? 'Müsait' : 'Dolu'}</span></td>
            <td>₺${room.price.toLocaleString()}</td>
            <td>
                <button class="btn btn-sm btn-info me-2" onclick="editRoom(${room.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteRoom(${room.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Oda ekleme işlemi
async function handleAddRoom(event) {
    event.preventDefault();
    
    const roomNumber = document.getElementById('roomNumber').value;
    const roomType = document.getElementById('roomType').value;
    const price = document.getElementById('price').value;
    const capacity = document.getElementById('capacity').value;

    try {
        const response = await fetch(`${API_BASE_URL}/rooms`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
            },
            body: JSON.stringify({
                roomNumber,
                roomType,
                price: parseFloat(price),
                capacity: parseInt(capacity),
                isAvailable: true,
                admin: null // Backend'de admin bilgisi token'dan alınacak
            })
        });

        if (!response.ok) {
            if (response.status === 401) {
                window.location.href = 'login.html';
                return;
            }
            const data = await response.json();
            throw new Error(data.message || 'Oda eklenirken bir hata oluştu.');
        }

        const result = await response.json();
        showSuccess('Oda başarıyla eklendi!');
        addRoomForm.reset();
        loadRooms(); // Oda listesini yenile
        loadDashboardData(); // Dashboard verilerini yenile
    } catch (error) {
        console.error('Oda ekleme hatası:', error);
        showError(error.message || 'Oda eklenirken bir hata oluştu. Lütfen tekrar deneyin.');
    }
}

// Oda düzenleme işlemi
async function editRoom(roomId) {
    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
            }
        });

        if (response.ok) {
            const room = await response.json();
            showEditRoomModal(room);
        } else {
            alert('Oda bilgileri alınamadı.');
        }
    } catch (error) {
        console.error('Oda bilgileri alma hatası:', error);
        alert('Oda bilgileri alınırken bir hata oluştu.');
    }
}

// Oda düzenleme modalını göster
function showEditRoomModal(room) {
    const modal = new bootstrap.Modal(document.getElementById('addRoomModal'));
    document.getElementById('addRoomModalLabel').textContent = 'Oda Düzenle';
    document.getElementById('roomNumber').value = room.roomNumber;
    document.getElementById('roomType').value = room.roomType;
    document.getElementById('price').value = room.price;
    document.getElementById('capacity').value = room.capacity;
    
    // Form submit olayını güncelle
    const form = document.getElementById('addRoomForm');
    form.onsubmit = async (e) => {
        e.preventDefault();
        await updateRoom(room.id);
    };
    
    modal.show();
}

// Oda güncelleme işlemi
async function updateRoom(roomId) {
    const roomNumber = document.getElementById('roomNumber').value;
    const roomType = document.getElementById('roomType').value;
    const price = document.getElementById('price').value;
    const capacity = document.getElementById('capacity').value;

    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${roomId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
            },
            body: JSON.stringify({
                roomNumber,
                roomType,
                price: parseFloat(price),
                capacity: parseInt(capacity)
            })
        });

        if (response.ok) {
            alert('Oda başarıyla güncellendi!');
            bootstrap.Modal.getInstance(document.getElementById('addRoomModal')).hide();
            loadRooms(); // Oda listesini yenile
            loadDashboardData(); // Dashboard verilerini yenile
        } else {
            const data = await response.json();
            alert(data.message || 'Oda güncellenirken bir hata oluştu.');
        }
    } catch (error) {
        console.error('Oda güncelleme hatası:', error);
        alert('Oda güncellenirken bir hata oluştu. Lütfen tekrar deneyin.');
    }
}

// Oda silme işlemi
async function deleteRoom(roomId) {
    if (!confirm('Bu odayı silmek istediğinizden emin misiniz?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${roomId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('adminToken')}`
            }
        });

        if (response.ok) {
            alert('Oda başarıyla silindi!');
            loadRooms(); // Oda listesini yenile
            loadDashboardData(); // Dashboard verilerini yenile
        } else {
            const data = await response.json();
            alert(data.message || 'Oda silinirken bir hata oluştu.');
        }
    } catch (error) {
        console.error('Oda silme hatası:', error);
        alert('Oda silinirken bir hata oluştu. Lütfen tekrar deneyin.');
    }
}

// Çıkış işlemi
function handleLogout() {
    localStorage.removeItem('adminToken');
    window.location.href = 'login.html';
}

// Sidebar navigasyonu
function handleSidebarNavigation(event) {
    event.preventDefault();
    const section = event.currentTarget.getAttribute('data-section');
    
    // Aktif link'i güncelle
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    event.currentTarget.classList.add('active');
    
    // İlgili bölümü göster
    showSection(section);
}

// Bölüm gösterme
function showSection(section) {
    // Tüm bölümleri gizle
    document.querySelectorAll('main > div').forEach(div => {
        div.style.display = 'none';
    });
    
    // İlgili bölümü göster
    const targetSection = document.getElementById(section);
    if (targetSection) {
        targetSection.style.display = 'block';
    }
}

// Hata mesajı gösterme fonksiyonu
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger alert-dismissible fade show';
    errorDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    const mainContent = document.querySelector('main');
    mainContent.insertBefore(errorDiv, mainContent.firstChild);
    
    // 5 saniye sonra hata mesajını kaldır
    setTimeout(() => {
        errorDiv.remove();
    }, 5000);
}

// Başarı mesajı gösterme fonksiyonu
function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'alert alert-success alert-dismissible fade show';
    successDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    const mainContent = document.querySelector('main');
    mainContent.insertBefore(successDiv, mainContent.firstChild);
    
    // 5 saniye sonra başarı mesajını kaldır
    setTimeout(() => {
        successDiv.remove();
    }, 5000);
} 