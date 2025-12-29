$(document).ready(function () {
    if (document.cookie.indexOf('JWT_TOKEN') !== -1) {
        $.ajax({
            url: '/api/auth/me',
            type: 'GET',
            success: function (response) {
                if (response.role === 'ROLE_ADMIN') {
                    window.location.href = '/admin.html';
                } else {
                    if (response.isPanVerified && response.videoPath) {
                        window.location.href = '/success.html';
                    } else if (response.isPanVerified) {
                        window.location.href = '/video.html';
                    } else {
                        window.location.href = '/verify-pan.html';
                    }
                }
            },
            error: function () {
                document.cookie = "JWT_TOKEN=; path=/; max-age=0";
            }
        });
    }

    $('#loginForm').submit(function (e) {
        e.preventDefault();
        login($('#username').val(), $('#password').val());
    });
});
