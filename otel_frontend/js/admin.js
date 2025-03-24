// Admin Panel JavaScript

// Sayfa yüklendiğinde çalışacak fonksiyonlar
document.addEventListener('DOMContentLoaded', function() {
    // Oturum kontrolü
    checkAdminSession();
    
    // Event listener'ları ekle
    setupEventListeners();
    
    // İlk verileri yükle
    loadDashboardData();
});

// Oturum kontrolü
function checkAdminSession() {
    // Burada admin oturum kontrolü yapılacak
    // Örnek: localStorage'dan token kontrolü
    const adminToken = localStorage.getItem('adminToken');
    if (!adminToken) {
        window.location.href = 'login.html';
    }
}

// Event listener'ları ayarla
function setupEventListeners() {
    // Çıkış butonu
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
    
    // Oda ekleme formu
    const addRoomForm = document.getElementById('addRoomForm');
    if (addRoomForm) {
        addRoomForm.addEventListener('submit', handleAddRoom);
    }
    
    // Sidebar linkleri
    const sidebarLinks = document.querySelectorAll('.admin-sidebar .nav-link');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', handleSidebarNavigation);
    });
}

// Dashboard verilerini yükle
function loadDashboardData() {
    // Burada API'den veriler çekilecek
    // Örnek veriler:
    updateDashboardStats({
        totalRooms: 50,
        activeCustomers: 120,
        todayReservations: 15,
        dailyIncome: 5000
    });
    
    loadRoomsTable();
    loadReservationsTable();
}

// Dashboard istatistiklerini güncelle
function updateDashboardStats(stats) {
    // İstatistikleri güncelle
    document.querySelector('.stat-card:nth-child(1) p').textContent = stats.totalRooms;
    document.querySelector('.stat-card:nth-child(2) p').textContent = stats.activeCustomers;
    document.querySelector('.stat-card:nth-child(3) p').textContent = stats.todayReservations;
    document.querySelector('.stat-card:nth-child(4) p').textContent = `₺${stats.dailyIncome}`;
}

// Oda tablosunu yükle
function loadRoomsTable() {
    // Burada API'den oda verileri çekilecek
    // Örnek veri:
    const rooms = [
        {
            roomNumber: 101,
            type: 'Standart',
            status: 'Müsait',
            price: 500
        }
        // Diğer odalar...
    ];
    
    const tbody = document.querySelector('.table tbody');
    tbody.innerHTML = rooms.map(room => `
        <tr>
            <td>${room.roomNumber}</td>
            <td>${room.type}</td>
            <td><span class="badge bg-success">${room.status}</span></td>
            <td>₺${room.price}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="editRoom(${room.roomNumber})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteRoom(${room.roomNumber})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Rezervasyon tablosunu yükle
function loadReservationsTable() {
    // Burada API'den rezervasyon verileri çekilecek
    // Örnek veri:
    const reservations = [
        {
            id: 12345,
            customer: 'Ahmet Yılmaz',
            room: 101,
            checkIn: '2024-03-24',
            checkOut: '2024-03-26',
            status: 'Aktif'
        }
        // Diğer rezervasyonlar...
    ];
    
    const tbody = document.querySelector('.table tbody');
    tbody.innerHTML = reservations.map(reservation => `
        <tr>
            <td>#${reservation.id}</td>
            <td>${reservation.customer}</td>
            <td>${reservation.room}</td>
            <td>${reservation.checkIn}</td>
            <td>${reservation.checkOut}</td>
            <td><span class="badge bg-success">${reservation.status}</span></td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewReservation(${reservation.id})">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn btn-sm btn-warning" onclick="editReservation(${reservation.id})">
                    <i class="fas fa-edit"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Oda ekleme işlemi
function handleAddRoom(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const roomData = {
        roomNumber: formData.get('roomNumber'),
        type: formData.get('type'),
        price: formData.get('price'),
        capacity: formData.get('capacity'),
        description: formData.get('description')
    };
    
    // Burada API'ye oda ekleme isteği yapılacak
    console.log('Yeni oda ekleniyor:', roomData);
    
    // Modal'ı kapat
    const modal = bootstrap.Modal.getInstance(document.getElementById('addRoomModal'));
    modal.hide();
    
    // Tabloyu güncelle
    loadRoomsTable();
}

// Çıkış işlemi
function handleLogout() {
    // Oturum bilgilerini temizle
    localStorage.removeItem('adminToken');
    
    // Login sayfasına yönlendir
    window.location.href = 'login.html';
}

// Sidebar navigasyonu
function handleSidebarNavigation(event) {
    event.preventDefault();
    
    // Aktif link'i güncelle
    document.querySelectorAll('.admin-sidebar .nav-link').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // İlgili içeriği yükle
    const section = event.target.getAttribute('href').substring(1);
    loadSection(section);
}

// Bölüm yükleme
function loadSection(section) {
    // Burada ilgili bölümün içeriği yüklenecek
    console.log(`${section} bölümü yükleniyor...`);
}

// Oda düzenleme
function editRoom(roomNumber) {
    console.log(`${roomNumber} numaralı oda düzenleniyor...`);
}

// Oda silme
function deleteRoom(roomNumber) {
    if (confirm(`${roomNumber} numaralı odayı silmek istediğinizden emin misiniz?`)) {
        console.log(`${roomNumber} numaralı oda siliniyor...`);
    }
}

// Rezervasyon görüntüleme
function viewReservation(reservationId) {
    console.log(`${reservationId} numaralı rezervasyon görüntüleniyor...`);
}

// Rezervasyon düzenleme
function editReservation(reservationId) {
    console.log(`${reservationId} numaralı rezervasyon düzenleniyor...`);
} 