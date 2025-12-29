$(document).ready(function () {
    const navbarHtml = `
    <nav class="navbar navbar-expand-lg navbar-dark navbar-custom fixed-top">
        <div class="container">
            <a class="navbar-brand" href="#">KYC Portal</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto align-items-center">
                    <li class="nav-item me-3">
                        <select id="languageSelect" class="form-select form-select-sm">
                            <option value="en">English</option>
                            <option value="hi">Hindi (हिंदी)</option>
                            <option value="te">Telugu (తెలుగు)</option>
                        </select>
                    </li>
                    <li class="nav-item">
                        <span class="text-secondary me-3" id="user-display"></span>
                    </li>
                    <li class="nav-item">
                        <button class="btn btn-sm btn-info rounded-pill px-3 me-2 fw-bold" data-bs-toggle="modal" data-bs-target="#helpModal" title="Help - How to Add Users">
                            Help
                        </button>
                    </li>
                    <li class="nav-item">
                        <button class="btn btn-sm btn-outline-light rounded-pill px-3" onclick="logout()" data-i18n="logout">Logout</button>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    `;
    $('#navbar-placeholder').html(navbarHtml);

    const currentLang = localStorage.getItem('app_language') || 'en';
    $('#languageSelect').val(currentLang);

    // Inject User Help Modal if it doesn't exist (i.e. not on Admin page)
    if ($('#helpModal').length === 0) {
        const userHelpModalHtml = `
        <div class="modal fade" id="helpModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content bg-dark text-white border-secondary">
                    <div class="modal-header border-secondary">
                        <h5 class="modal-title">KYC Help & Instructions</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <h6>1. PAN Verification</h6>
                        <p class="small text-secondary">Ensure you enter a valid PAN number (e.g., ABCDE1234F). Your name must match the one on the PAN card.</p>
                        
                        <h6>2. Video Verification</h6>
                        <p class="small text-secondary">Upload a short MP4/WebM video. Ensure your face is clearly visible and verification text is readable.</p>
                        
                        <h6>3. Common Issues</h6>
                        <ul class="small text-secondary">
                            <li>"Name Mismatch" - Ensure you use your full legal name.</li>
                            <li>"Locked Out" - Wait 30 minutes after 5 failed attempts.</li>
                        </ul>
                    </div>
                    <div class="modal-footer border-secondary">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
        `;
        $('body').append(userHelpModalHtml);
    }

    // Inject Global Message/Confirm Modals if not present
    if ($('#messageModal').length === 0) {
        const globalModalsHtml = `
        <div class="modal fade" id="messageModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content bg-dark text-white border-secondary">
                    <div class="modal-header border-secondary">
                        <h5 class="modal-title" id="msgModalTitle">Notification</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body" id="msgModalBody"></div>
                    <div class="modal-footer border-secondary">
                        <button type="button" class="btn btn-primary" data-bs-dismiss="modal">OK</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="modal fade" id="confirmModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content bg-dark text-white border-secondary">
                    <div class="modal-header border-secondary">
                        <h5 class="modal-title">Confirm Action</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body" id="confirmModalBody">Are you sure?</div>
                    <div class="modal-footer border-secondary">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-danger" id="globalConfirmBtn">Confirm</button>
                    </div>
                </div>
            </div>
        </div>
        `;
        $('body').append(globalModalsHtml);
    }

    // Global Bind for Confirm
    $(document).on('click', '#globalConfirmBtn', function () {
        if (window.pendingConfirmAction) {
            window.pendingConfirmAction();
            window.pendingConfirmAction = null;
            bootstrap.Modal.getInstance(document.getElementById('confirmModal')).hide();
        }
    });
});

// Global Helpers
window.showMessage = function (title, message) {
    $('#msgModalTitle').text(title);
    $('#msgModalBody').text(message);
    new bootstrap.Modal(document.getElementById('messageModal')).show();
};

window.showConfirm = function (message, actionCallback) {
    $('#confirmModalBody').text(message);
    window.pendingConfirmAction = actionCallback;
    new bootstrap.Modal(document.getElementById('confirmModal')).show();
};
