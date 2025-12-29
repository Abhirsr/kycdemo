checkAuth();

$(document).ready(function () {
    $.ajax({
        url: '/api/auth/me',
        type: 'GET',
        success: function (user) {
            $('#userName').text(user.fullName || 'N/A');
            $('#userUsername').text(user.username || 'N/A');
            $('#userPan').text(user.panNumber || 'N/A');

            if (user.videoPath) {
                $('#finalVideo').attr('src', user.videoPath);
            } else {
                const params = new URLSearchParams(window.location.search);
                const videoUrl = params.get('video');
                if (videoUrl) {
                    $('#finalVideo').attr('src', videoUrl);
                }
            }
        },
        error: function () {
            $('#userName').text("Error fetching details");
        }
    });
});
