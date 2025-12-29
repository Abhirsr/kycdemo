checkAuth();

// Initial Load
$(document).ready(function () {
    loadUsers();
    loadStats();
});

let statsChart = null;

function loadStats() {
    $.ajax({
        url: '/api/admin/stats',
        type: 'GET',
        success: function (stats) {
            $('#statTotal').text(stats.total);
            $('#statVerified').text(stats.verified);
            $('#statPending').text(stats.pending);
            renderChart(stats);
        }
    });
}

function renderChart(stats) {
    const ctx = document.getElementById('userChart').getContext('2d');
    if (statsChart) statsChart.destroy();

    statsChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Verified', 'Pending'],
            datasets: [{
                data: [stats.verified, stats.pending],
                backgroundColor: ['#198754', '#ffc107'], // Bootstrap Success, Warning
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right', labels: { color: 'white' } }
            }
        }
    });
}

function downloadCsv() {
    window.location.href = '/api/admin/export';
}

function loadUsers() {
    // Refresh stats when users are reloaded
    if (statsChart) loadStats();

    $.ajax({
        url: '/api/admin/users',
        type: 'GET',
        success: function (users) {
            console.log("Loaded users:", users);
            const tbody = $('#userTableBody');
            tbody.empty();
            users.forEach(user => {
                const isVerified = user.isPanVerified || user.panVerified; // Handle both naming conventions
                const row = `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.fullName || '-'}</td>
                    <td>
                        <span class="badge ${isVerified ? 'bg-success' : 'bg-warning'}">
                            ${isVerified ? 'Verified' : 'Pending'}
                        </span>
                    </td>
                    <td>
                        ${user.videoUrl
                        ? `<button class="btn btn-sm btn-outline-success" onclick="viewVideo('${user.videoUrl}')">View Video</button>`
                        : '<span class="badge bg-secondary">Missing</span>'}
                    </td>
                    <td>
                        <button class="btn btn-sm btn-warning me-2" onclick="confirmReset(${user.id})">Reset KYC</button>
                        <button class="btn btn-sm btn-danger" onclick="confirmDelete(${user.id})">Delete</button>
                    </td>
                </tr>
                `;
                tbody.append(row);
            });
        },
        error: function () {
            $('#alertMessage').text('Failed to load users').removeClass('d-none').addClass('alert-danger');
        }
    });
}

function confirmReset(id) {
    showConfirm('Reset KYC for this user?', function () {
        $.ajax({
            url: `/api/admin/users/${id}/reset`,
            type: 'POST',
            success: loadUsers
        });
    });
}

function confirmDelete(id) {
    showConfirm('Delete this user?', function () {
        $.ajax({
            url: `/api/admin/users/${id}`,
            type: 'DELETE',
            success: loadUsers
        });
    });
}

function submitBulkUsers() {
    const jsonStr = $('#bulkJson').val();
    let users;
    try {
        users = JSON.parse(jsonStr);
        if (!Array.isArray(users)) throw new Error("Not an array");
    } catch (e) {
        showMessage("Error", "Invalid JSON: " + e.message);
        return;
    }

    $.ajax({
        url: '/api/admin/users/bulk',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(users),
        success: function () {
            $('#bulkAddModal').modal('hide');
            loadUsers();
            $('#bulkJson').val('');
            showMessage("Success", "Users Added Successfully");
        },
        error: function () {
            showMessage("Error", "Error adding users");
        }
    });
}
