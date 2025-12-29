class I18n {
    constructor() {
        this.currentLang = localStorage.getItem('app_language') || 'en';
        this.init();
    }

    init() {
        this.updatePage();
        this.setupEventListeners();
    }

    setLanguage(lang) {
        if (translations[lang]) {
            this.currentLang = lang;
            localStorage.setItem('app_language', lang);
            this.updatePage();
        }
    }

    updatePage() {
        $('[data-i18n]').each((index, element) => {
            const key = $(element).data('i18n');
            const translation = translations[this.currentLang][key];
            if (translation) {
                if ($(element).is('input') || $(element).is('textarea')) {
                    $(element).attr('placeholder', translation);
                } else {
                    $(element).text(translation);
                }
            }
        });

        $('#languageSelect').val(this.currentLang);
        $('html').attr('lang', this.currentLang);
    }

    setupEventListeners() {
        $(document).on('change', '#languageSelect', (e) => {
            this.setLanguage($(e.target).val());
        });
    }
}

$(document).ready(() => {
    window.i18nManager = new I18n();

    if ($('#language-switcher-placeholder').length) {
        const switcherHtml = `
            <select id="languageSelect" class="form-select form-select-sm w-auto">
                <option value="en">English</option>
                <option value="hi">Hindi (हिंदी)</option>
                <option value="te">Telugu (తెలుగు)</option>
            </select>
        `;
        $('#language-switcher-placeholder').html(switcherHtml);
        $('#languageSelect').val(localStorage.getItem('app_language') || 'en');
    }
});
