checkAuth();
redirectIfCompleted();

$(document).ready(function () {
    $('#panForm').submit(function (e) {
        e.preventDefault();

        const $btn = $('#verifyBtn');
        const $inputs = $('#panNumber, #panName');
        const $spinner = $btn.find('.spinner-border');

        // Block everything
        $btn.prop('disabled', true);
        $inputs.prop('disabled', true);
        $spinner.removeClass('d-none');

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
                $spinner.addClass('d-none');

                let msg = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : 'Verification Failed';

                $('#alertMessage')
                    .text(msg)
                    .addClass('alert-danger')
                    .removeClass('d-none');

                // If LOCKED (423), keep everything disabled
                if (xhr.status === 423) {
                    $btn.prop('disabled', true).text("Locked Out");
                    $inputs.prop('disabled', true); // Ensure inputs stay disabled
                } else {
                    // If just failed, re-enable inputs for retry
                    $btn.prop('disabled', false);
                    $inputs.prop('disabled', false);
                }
            }
        });
    });
});
