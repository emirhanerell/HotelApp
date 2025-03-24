// API endpoint'leri
const API_BASE_URL = 'http://localhost:8080/api';

// Sayfa yüklendiğinde çalışacak fonksiyonlar
document.addEventListener('DOMContentLoaded', () => {
    loadRooms();
    loadCustomers();
    loadReports();
    setupEventListeners();
});

// Event listener'ları ayarla
function setupEventListeners() {
    // Rezervasyon formu submit olayı
    const reservationForm = document.getElementById('reservationForm');
    if (reservationForm) {
        reservationForm.addEventListener('submit', handleReservationSubmit);
    }

    // Oda oluşturma formu submit olayı
    const roomCreateForm = document.getElementById('roomCreateForm');
    if (roomCreateForm) {
        roomCreateForm.addEventListener('submit', handleRoomCreateSubmit);
    }
}

// Oda listesini yükle
async function loadRooms() {
    try {
        const response = await fetch(`${API_BASE_URL}/rooms`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            mode: 'cors'
        });
        const rooms = await response.json();
        displayRooms(rooms);
    } catch (error) {
        console.error('Odalar yüklenirken hata oluştu:', error);
    }
}

// Odaları görüntüle
function displayRooms(rooms) {
    const roomsContainer = document.getElementById('roomsContainer');
    if (!roomsContainer) return;

    roomsContainer.innerHTML = rooms.map(room => `
        <div class="room-card">
            <h3>Oda ${room.roomNumber}</h3>
            <p>Tip: ${room.type}</p>
            <p>Durum: ${room.status}</p>
            <p>Fiyat: ${room.price} TL</p>
            <p>Kapasite: ${room.capacity} Kişilik</p>
        </div>
    `).join('');
}

// Müşteri listesini yükle
async function loadCustomers() {
    try {
        const response = await fetch(`${API_BASE_URL}/customers`);
        const customers = await response.json();
        displayCustomers(customers);
    } catch (error) {
        console.error('Müşteriler yüklenirken hata oluştu:', error);
    }
}

// Müşterileri görüntüle
function displayCustomers(customers) {
    const customersContainer = document.getElementById('customersContainer');
    if (!customersContainer) return;

    customersContainer.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Müşteri Adı</th>
                    <th>Oda No</th>
                    <th>Giriş Tarihi</th>
                    <th>Çıkış Tarihi</th>
                    <th>Durum</th>
                </tr>
            </thead>
            <tbody>
                ${customers.map(customer => `
                    <tr>
                        <td>${customer.name}</td>
                        <td>${customer.roomNumber}</td>
                        <td>${formatDate(customer.checkIn)}</td>
                        <td>${formatDate(customer.checkOut)}</td>
                        <td>${customer.status}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

// Raporları yükle
async function loadReports() {
    try {
        const [occupancyResponse, revenueResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/reports/occupancy`),
            fetch(`${API_BASE_URL}/reports/revenue`)
        ]);

        const occupancy = await occupancyResponse.json();
        const revenue = await revenueResponse.json();

        displayReports(occupancy, revenue);
    } catch (error) {
        console.error('Raporlar yüklenirken hata oluştu:', error);
    }
}

// Raporları görüntüle
function displayReports(occupancy, revenue) {
    const dailyOccupancy = document.getElementById('dailyOccupancy');
    const monthlyRevenue = document.getElementById('monthlyRevenue');

    if (dailyOccupancy) {
        dailyOccupancy.innerHTML = `
            <p>Günlük Doluluk Oranı: ${occupancy.dailyRate}%</p>
            <p>Dolu Oda Sayısı: ${occupancy.occupiedRooms}</p>
            <p>Boş Oda Sayısı: ${occupancy.availableRooms}</p>
        `;
    }

    if (monthlyRevenue) {
        monthlyRevenue.innerHTML = `
            <p>Toplam Gelir: ${revenue.total} TL</p>
            <p>Ortalama Günlük Gelir: ${revenue.averageDaily} TL</p>
            <p>En Yüksek Gelirli Ay: ${revenue.highestMonth}</p>
        `;
    }
}

// Rezervasyon formu gönderimi
async function handleReservationSubmit(event) {
    event.preventDefault();

    const formData = {
        roomNumber: document.getElementById('roomNumber').value,
        customerName: document.getElementById('customerName').value,
        checkIn: document.getElementById('checkIn').value,
        checkOut: document.getElementById('checkOut').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/reservations`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            mode: 'cors',
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert('Rezervasyon başarıyla oluşturuldu!');
            event.target.reset();
            loadRooms();
            loadCustomers();
            loadReports();
        } else {
            throw new Error('Rezervasyon oluşturulamadı');
        }
    } catch (error) {
        console.error('Rezervasyon hatası:', error);
        alert('Rezervasyon oluşturulurken bir hata oluştu.');
    }
}

// Oda oluşturma formu gönderimi
async function handleRoomCreateSubmit(event) {
    event.preventDefault();

    const formData = {
        roomNumber: parseInt(document.getElementById('roomNumber').value),
        type: document.getElementById('roomType').value,
        price: parseFloat(document.getElementById('roomPrice').value),
        capacity: parseInt(document.getElementById('roomCapacity').value),
        status: document.getElementById('roomStatus').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/rooms`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            mode: 'cors',
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            alert('Oda başarıyla eklendi!');
            event.target.reset();
            loadRooms(); // Oda listesini yenile
        } else {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Oda eklenirken bir hata oluştu');
        }
    } catch (error) {
        console.error('Oda ekleme hatası:', error);
        alert(error.message || 'Oda eklenirken bir hata oluştu.');
    }
}

// Tarih formatla
function formatDate(dateString) {
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('tr-TR', options);
} 