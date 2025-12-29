checkAuth();
redirectIfCompleted();
let mediaRecorder;
let chunks = [];
let stream;
const RECORDING_TIME_MS = 5000;

$('#startBtn').click(async function () {
    try {
        stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        $('#preview')[0].srcObject = stream;

        $('#startBtn').addClass('d-none');
        $('#statusText').text("Recording... Please speak clearly.");
        $('#timer').removeClass('d-none').text("00:05");

        mediaRecorder = new MediaRecorder(stream);
        chunks = [];

        mediaRecorder.ondataavailable = e => chunks.push(e.data);

        mediaRecorder.onstop = () => {
            const blob = new Blob(chunks, { type: 'video/webm' });
            const videoURL = URL.createObjectURL(blob);

            stream.getTracks().forEach(track => track.stop());

            const $preview = $('#preview');
            $preview[0].srcObject = null;
            $preview[0].src = videoURL;
            $preview[0].controls = true;
            $preview[0].muted = false;

            $('#timer').addClass('d-none');
            $('#statusText').text("Recording complete. Please review and upload.");
            $('#uploadBtn').removeClass('d-none').data('blob', blob);
            $('#reRecordBtn').removeClass('d-none');
        };

        mediaRecorder.start();

        let checkTime = 5;
        const timerInterval = setInterval(() => {
            checkTime--;
            $('#timer').text("00:0" + checkTime);
            if (checkTime <= 0) clearInterval(timerInterval);
        }, 1000);

        setTimeout(() => {
            if (mediaRecorder.state === 'recording') {
                mediaRecorder.stop();
            }
        }, RECORDING_TIME_MS);

    } catch (err) {
        $('#alertMessage')
            .text("Camera access denied or error: " + err.message)
            .addClass('alert-danger')
            .removeClass('d-none');
    }
});

$('#reRecordBtn').click(function () {
    $('#preview')[0].src = "";
    $('#preview')[0].controls = false;
    $('#preview')[0].muted = true;

    $(this).addClass('d-none');
    $('#uploadBtn').addClass('d-none');
    $('#startBtn').removeClass('d-none');
    $('#statusText').text("Click Start. Recording stops automatically after 5 seconds.");
});

$('#uploadBtn').click(function () {
    const blob = $(this).data('blob');
    const formData = new FormData();
    formData.append('video', blob, 'kyc-video.webm');

    $(this).prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Uploading...');

    $.ajax({
        url: '/kyc/upload-video',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
            if (response.verifiedName) {
                localStorage.setItem('verifiedName', response.verifiedName);
            }
            window.location.href = '/success.html?video=' + encodeURIComponent(response.videoUrl);
        },
        error: function (xhr) {
            $('#uploadBtn').prop('disabled', false).text("Upload Video");
            $('#alertMessage')
                .text(xhr.responseJSON?.error || 'Upload Failed')
                .addClass('alert-danger')
                .removeClass('d-none');
        }
    });
});
