/**
 * ==========================================================================================
 * Notyf - Конфигурация и инициализация
 * ==========================================================================================
 */

// Расширяем класс Notyf, чтобы разрешить использование HTML в сообщениях
class CustomNotyf extends Notyf {
    _renderNotification(options) {
        const notification = super._renderNotification(options);

        // ВАЖНО: Используем innerHTML только для САНИТИЗИРОВАННОГО контента!
        if (options.message) {
            const messageContainer = notification.querySelector('.notyf__message');
            if (messageContainer) {
                // Сообщение уже санитизировано в функции showToast
                messageContainer.innerHTML = options.message;
            }
        }
        return notification;
    }
}


// Создаем и настраиваем экземпляр Notyf
const notyf = new CustomNotyf({
    duration: 5000,       // Длительность показа в миллисекундах
    ripple: true,         // Эффект "волны" при клике
    dismissible: true,    // Можно ли закрыть уведомление
    position: { x: 'right', y: 'top' }, // Позиция по умолчанию
    types: [
        {
            type: 'info',
            backgroundColor: '#2b9af3',
            icon: {
                className: 'ti tabler-info-circle',
                tagName: 'i',
                color: 'white'
            }
        },
        {
            type: 'warning',
            backgroundColor: '#ff9f43',
            icon: {
                className: 'ti tabler-alert-triangle',
                tagName: 'i',
                color: 'white'
            }
        },
        {
            type: 'success',
            backgroundColor: '#7367f0',
            icon: {
                className: 'ti tabler-circle-check',
                tagName: 'i',
                color: 'white'
            }
        },
        {
            type: 'error',
            backgroundColor: '#ea5455',
            icon: {
                className: 'ti tabler-alert-circle',
                tagName: 'i',
                color: 'white'
            }
        }
    ]
});

/**
 * ==========================================================================================
 * Основные функции для отображения уведомлений
 * ==========================================================================================
 */

/**
 * Отображает уведомление Notyf с возможностью перевода и интерактивными элементами.
 *
 * @param {string} type - Тип уведомления ('success', 'error', 'warning', 'info').
 * @param {string | null} title - Заголовок уведомления. Может быть ключом для i18next.
 * @param {string} msg - Сообщение уведомления. Может быть ключом для i18next или содержать HTML.
 * @param {boolean} [isI18n=false] - Флаг, указывающий на необходимость перевода title и msg через i18next.
 * @param {object} [options={}] - Дополнительные опции для Notyf (например, duration, dismissible).
 * @param {object} [interpolationData={}] - Данные для интерполяции в переводах i18next (например, {name: 'John', count: 5}).
 *
 * @example
 * // С интерполяцией
 * showToast('success', 'notifications.welcome.title', 'notifications.welcome.message',
 *           true, {}, {username: 'Иван', count: 3});
 * // В файле переводов: "Привет, {{username}}! У вас {{count}} новых сообщений"
 */
function showToast(type, title, msg, isI18n = false, options = {}, interpolationData = {}) {
    let translatedTitle = title;
    let translatedMsg = msg;

    // Если передан флаг isI18n, используем i18next для перевода с интерполяцией
    if (isI18n && typeof i18next !== 'undefined') {
        if (translatedTitle && translatedTitle.trim() !== '') {
            // Передаем данные интерполяции в i18next
            translatedTitle = i18next.t(translatedTitle, interpolationData);
        }
        if (translatedMsg && translatedMsg.trim() !== '') {
            // Передаем данные интерполяции в i18next
            translatedMsg = i18next.t(translatedMsg, interpolationData);
        }
    }

    // Экранируем заголовок для безопасности (заголовок не должен содержать HTML)
    if (translatedTitle && translatedTitle.trim() !== '') {
        // Создаем временный элемент для экранирования
        const titleElement = document.createElement('div');
        titleElement.textContent = translatedTitle;
        translatedTitle = titleElement.innerHTML;
    }

    // КРИТИЧНО: Санитизируем HTML-контент сообщения для защиты от XSS
    // ВАЖНО: Подключите библиотеку DOMPurify: <script src="https://cdn.jsdelivr.net/npm/dompurify@3.0.6/dist/purify.min.js"></script>
    if (typeof DOMPurify !== 'undefined') {
        // Санитизация с настройкой разрешенных тегов и атрибутов
        translatedMsg = DOMPurify.sanitize(translatedMsg, {
            ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a', 'p', 'br', 'span', 'div', 'button', 'ul', 'ol', 'li'],
            ALLOWED_ATTR: ['href', 'title', 'id', 'class', 'style', 'data-i18n'],
            ALLOW_DATA_ATTR: true, // Разрешаем data-* атрибуты для i18n
            KEEP_CONTENT: true     // Сохраняем содержимое запрещенных тегов
        });
    } else {
        console.warn('DOMPurify не найден! HTML не санитизирован. Установите: npm install dompurify');
    }

    // Объединяем заголовок и сообщение в один HTML-блок
    let finalMessage = '';
    if (translatedTitle && translatedTitle.trim() !== '') {
        finalMessage += `<strong>${translatedTitle}</strong><br>`;
    }
    finalMessage += translatedMsg;

    // Собираем опции для уведомления
    const notificationOptions = {
        type: type,
        message: finalMessage,
        ...options // Позволяет переопределить любые стандартные опции
    };

    // Отображаем уведомление и получаем его экземпляр для дальнейшей работы
    const notification = notyf.open(notificationOptions);

    // Если в сообщении есть интерактивные элементы, находим их и назначаем обработчики
    // Этот подход гораздо надежнее, чем setTimeout и getElementById
    if (notification && notification.dom) {
        // Обрабатываем кнопки закрытия с переводом через data-i18n
        const closeButtons = notification.dom.querySelectorAll('[data-action="close"]');
        closeButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                notyf.dismiss(notification);
            });
        });

        // Обрабатываем кастомные кнопки действий
        const actionButtons = notification.dom.querySelectorAll('[data-action]');
        actionButtons.forEach(btn => {
            const action = btn.getAttribute('data-action');
            if (action && action !== 'close') {
                btn.addEventListener('click', () => {
                    // Можно передать callback через options
                    if (options.onAction && typeof options.onAction === 'function') {
                        options.onAction(action, notification);
                    }
                });
            }
        });

        // Устаревшие обработчики (для обратной совместимости)
        const okBtn = notification.dom.querySelector('#okBtn');
        if (okBtn) {
            okBtn.addEventListener('click', () => {
                notyf.dismiss(notification);
            });
        }

        const surpriseBtn = notification.dom.querySelector('#surpriseBtn');
        if (surpriseBtn) {
            surpriseBtn.addEventListener('click', () => {
                if (options.onSurprise && typeof options.onSurprise === 'function') {
                    options.onSurprise();
                } else {
                    alert('Surprise! You could perform an action here.');
                }
            });
        }
    }

    return notification; // Возвращаем notification для возможности программного управления
}

/**
 * Удобная обертка для показа уведомлений с автоматическим переводом ключей i18next.
 *
 * @param {string} type - Тип уведомления ('success', 'error', 'warning', 'info').
 * @param {string} titleKey - Ключ i18next для заголовка.
 * @param {string} msgKey - Ключ i18next для сообщения.
 * @param {object} [interpolationData={}] - Данные для подстановки в переводы.
 * @param {object} [options={}] - Дополнительные опции для Notyf.
 *
 * @example
 * showI18nToast('success', 'notifications.saved.title', 'notifications.saved.message',
 *               {filename: 'document.pdf'});
 */
function showI18nToast(type, titleKey, msgKey, interpolationData = {}, options = {}) {
    return showToast(type, titleKey, msgKey, true, options, interpolationData);
}

/**
 * Вспомогательная функция для создания HTML сообщения с переведенными кнопками.
 * Используйте её для создания безопасных интерактивных уведомлений.
 *
 * @param {string} messageKey - Ключ i18next для основного сообщения.
 * @param {Array} buttons - Массив объектов кнопок: [{textKey: 'key', action: 'actionName', classes: 'btn-primary'}]
 * @returns {string} - HTML-строка с сообщением и кнопками (будет санитизирована автоматически)
 *
 * @example
 * const html = createNotificationWithButtons('notifications.confirm.message', [
 *     {textKey: 'buttons.ok', action: 'close', classes: 'btn-primary btn-sm'},
 *     {textKey: 'buttons.cancel', action: 'cancel', classes: 'btn-secondary btn-sm'}
 * ]);
 */
function createNotificationWithButtons(messageKey, buttons = []) {
    // Переводим основное сообщение
    let message = typeof i18next !== 'undefined' ? i18next.t(messageKey) : messageKey;

    // Начинаем формировать HTML
    let html = `<div>${message}</div>`;

    // Если есть кнопки, добавляем их с переводом
    if (buttons.length > 0) {
        html += '<div style="margin-top: 10px;">';
        buttons.forEach((btn, index) => {
            const buttonText = typeof i18next !== 'undefined' ? i18next.t(btn.textKey) : btn.textKey;
            const classes = btn.classes || 'btn btn-sm';
            const marginLeft = index > 0 ? ' style="margin-left: 5px;"' : '';
            html += `<button data-action="${btn.action}" class="${classes}"${marginLeft}>${buttonText}</button>`;
        });
        html += '</div>';
    }

    return html;
}

/**
 * Закрывает все активные уведомления.
 * Полезно при смене языка, чтобы старые непереведенные уведомления исчезли.
 */
function dismissAllNotifications() {
    if (notyf && typeof notyf.dismissAll === 'function') {
        notyf.dismissAll();
    }
}

/**
 * ==========================================================================================
 * Примеры использования
 * ==========================================================================================
 */

// Пример 1: Простое сообщение (без HTML)
// showToast('success', 'Успех', 'Операция выполнена успешно!');

// Пример 2: Сообщение с кастомными опциями
// showToast('error', 'Ошибка', 'Не удалось загрузить данные.', false, { duration: 9000 });

// Пример 3: Перевод через i18next с интерполяцией
// Файл переводов: "Пользователь {{username}} успешно сохранен"
// showI18nToast('success', 'notifications.user.saved.title', 'notifications.user.saved.message', 
//               {username: 'Иван Петров'});

// Пример 4: Интерактивное уведомление с переведенными кнопками
/*
const htmlMessage = createNotificationWithButtons('notifications.confirm.delete', [
    {textKey: 'buttons.confirm', action: 'confirm', classes: 'btn-danger btn-sm'},
    {textKey: 'buttons.cancel', action: 'close', classes: 'btn-secondary btn-sm'}
]);

showToast('warning', null, htmlMessage, false, {
    duration: 10000,
    dismissible: true,
    onAction: (action, notification) => {
        if (action === 'confirm') {
            // Выполняем удаление
            console.log('Удаление подтверждено');
            notyf.dismiss(notification);
            showI18nToast('success', null, 'notifications.deleted.success');
        }
    }
});
*/

// Пример 5: Закрытие всех уведомлений при смене языка
/*
i18next.changeLanguage(newLang, (err, t) => {
    if (err) return;
    dismissAllNotifications(); // Закрываем старые уведомления
    localize();
});
*/

// Пример 6: Уведомление с HTML и безопасной санитизацией
/*
// Этот HTML будет автоматически санитизирован от опасных элементов
const safeHtml = `
    <div>Загрузка файла <strong>document.pdf</strong> завершена.</div>
    <div style="margin-top: 10px;">
        <button data-action="open" class="btn btn-primary btn-sm">Открыть</button>
        <button data-action="close" class="btn btn-secondary btn-sm" style="margin-left: 5px;">Закрыть</button>
    </div>
`;

showToast('info', 'Загрузка завершена', safeHtml, false, {
    onAction: (action) => {
        if (action === 'open') {
            window.open('/files/document.pdf', '_blank');
        }
    }
});
*/
