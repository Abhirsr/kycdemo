checkAuth();
redirectIfCompleted();

$(document).ready(function () {
    $('#panForm').submit(function (e) {
        e.preventDefault();
        const btn = $(this).find('button[type="submit"]');
        const originalText = btn.text();
        btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Verifying...');

        $.ajax({
            url: '/kyc/verify-pan',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                panNumber: $('#panNumber').val(),
                panName: $('#panName').val()
            }),
            success: function () {
                window.location.href = '/video.html';
            },
            error: function (xhr) {
                btn.prop('disabled', false).text(originalText);
                let msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : 'Verification Failed';

                $('#alertMessage')
                    .text(msg)
                    .addClass('alert-danger')
                    .removeClass('d-none');

                if (xhr.status === 423) {
                    btn.prop('disabled', true).text("Locked Out");
                }
            }
        });
    });
});
