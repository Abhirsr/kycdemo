const API_URL = '/api/auth';

function login(username, password) {
    $.ajax({
        url: API_URL + '/login',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ username: username, password: password }),
        success: function (response) {
            if (response.token) {
                document.cookie = "JWT_TOKEN=" + response.token + "; path=/; max-age=86400";

                if (response.role === 'ROLE_ADMIN') {
                    window.location.href = '/admin.html';
                } else {
                    if (response.isPanVerified && response.isVideoUploaded) {
                        window.location.href = '/success.html';
                    } else if (response.isPanVerified) {
                        window.location.href = '/video.html';
                    } else {
                        window.location.href = '/verify-pan.html';
                    }
                }
            }
        },
        error: function (xhr) {
            $('#alertMessage').text(xhr.responseJSON?.error || 'Login failed').removeClass('d-none');
        }
    });
}

function logout() {
    document.cookie = "JWT_TOKEN=; path=/; max-age=0";
    window.location.href = '/index.html';
}

function checkAuth() {
    if (document.cookie.indexOf('JWT_TOKEN') === -1) {
        window.location.href = '/index.html';
    }
}

function redirectIfCompleted() {
    $.ajax({
        url: '/api/auth/me',
        type: 'GET',
        success: function (user) {
            if (user.isPanVerified && user.videoPath) {
                window.location.href = '/success.html';
            } else if (user.isPanVerified && location.pathname.includes('verify-pan.html')) {
                window.location.href = '/video.html';
            }
        }
    });
}
