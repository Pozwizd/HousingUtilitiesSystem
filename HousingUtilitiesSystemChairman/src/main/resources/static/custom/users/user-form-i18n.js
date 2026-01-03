/**
 * ============================================
 * ИНТЕРНАЦИОНАЛИЗАЦИЯ ФОРМЫ ПОЛЬЗОВАТЕЛЯ
 * ============================================
 */

document.addEventListener('DOMContentLoaded', function() {
    if (typeof i18next === 'undefined') {
        console.warn('i18next not loaded');
        return;
    }

    // Ждем загрузки i18next
    if (i18next.isInitialized) {
        translateForm();
    } else {
        i18next.on('initialized', translateForm);
    }

    // Переводим при смене языка
    i18next.on('languageChanged', function() {
        translateForm();
    });
});

function translateForm() {
    // Заголовок карточки (определяем режим редактирования или создания)
    const cardHeader = document.querySelector('.card-header');
    const userId = document.getElementById('userId');
    if (cardHeader) {
        if (userId && userId.value) {
            // Режим редактирования
            cardHeader.setAttribute('data-i18n', 'users.editUser');
            cardHeader.textContent = i18next.t('users.editUser');
        } else {
            // Режим создания
            cardHeader.setAttribute('data-i18n', 'users.newUser');
            cardHeader.textContent = i18next.t('users.newUser');
        }
    }

    // Переводим все элементы с data-i18n
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        
        // Обработка атрибутов в квадратных скобках
        if (key.startsWith('[')) {
            const match = key.match(/\[(.+?)\](.+)/);
            if (match) {
                const attribute = match[1];
                const translationKey = match[2];
                
                if (attribute === 'placeholder') {
                    element.placeholder = i18next.t(translationKey);
                } else if (attribute === 'aria-label') {
                    element.setAttribute('aria-label', i18next.t(translationKey));
                } else if (attribute === 'title') {
                    element.title = i18next.t(translationKey);
                }
            }
        } else {
            // Обычный перевод текстового содержимого
            const translation = i18next.t(key);
            if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
                element.value = translation;
            } else {
                element.textContent = translation;
            }
        }
    });

    // Переводим опции select
    document.querySelectorAll('select option[data-i18n]').forEach(option => {
        const key = option.getAttribute('data-i18n');
        option.textContent = i18next.t(key);
    });
}
