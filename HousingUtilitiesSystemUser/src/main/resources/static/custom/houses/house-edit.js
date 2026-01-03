/**
 * ============================================
 * МОДУЛЬ: HOUSE EDIT PAGE
 * ============================================
 * Структура:
 * 1. Инициализация страницы (Page Initialization)
 * 2. Загрузка данных (Data Loading)
 * 3. Обработка событий (Event Handlers)
 * 4. Отправка формы (Form Submission)
 * 5. Валидация (Validation)
 * ============================================
 */

const HouseEditPage = (function() {
    'use strict';

    // ============================================
    // СОСТОЯНИЕ И КОНФИГУРАЦИЯ
    // ============================================
    const state = {
        isEditMode: false,
        houseId: null,
        currentHouse: null
    };

    const elements = {
        form: null,
        houseIdField: null,
        fields: {}
    };

    // ============================================
    // 1. ИНИЦИАЛИЗАЦИЯ СТРАНИЦЫ
    // ============================================

    /**
     * Главная функция инициализации
     */
    async function init() {
        try {
            initElements();
            await initSelect2();
            determinePageMode();
            
            if (state.isEditMode) {
                await loadHouseData();
            }
            
            await loadStatuses();
            setupEventListeners();
            
            // Слушатель смены языка для обновления меток статусов
            if (typeof i18next !== 'undefined') {
                i18next.on('languageChanged', function() {
                    updateStatusLabels();
                });
            }
            
            if (state.isEditMode && state.currentHouse) {
                await populateForm(state.currentHouse);
            }
        } catch (error) {
            console.error('Initialization error:', error);
            NotificationModule.show('Ошибка при инициализации страницы', 'error');
        }
    }

    /**
     * Инициализация DOM элементов
     */
    function initElements() {
        elements.form = document.getElementById('formHouseSettings');
        elements.houseIdField = document.getElementById('houseId');

        elements.fields = {
            houseNumber: document.getElementById('houseNumber'),
            streetId: document.getElementById('streetId'),
            chairmanId: document.getElementById('chairmanId'),
            status: document.getElementById('status')
        };
    }

    /**
     * Инициализация Select2 компонентов
     */
    function initSelect2() {
        return new Promise((resolve) => {
            $(document).ready(function() {
                // Улицы
                $('#streetId').select2({
                    placeholder: 'Выберите улицу',
                    allowClear: true,
                    ajax: {
                        url: (window.contextPath || '') + '/streets/search',
                        dataType: 'json',
                        delay: 250,
                        data: (params) => ({
                            q: params.term,
                            page: params.page || 1
                        }),
                        processResults: (data) => ({
                            results: data.map(street => ({
                                id: street.id,
                                text: street.name + ' (' + (street.city ? street.city.name : '') + ')'
                            })),
                            pagination: { more: false }
                        }),
                        cache: true
                    },
                    minimumInputLength: 0
                });

                // Председатели
                $('#chairmanId').select2({
                    placeholder: 'Выберите председателя',
                    allowClear: true,
                    ajax: {
                        url: (window.contextPath || '') + '/chairmen/search',
                        dataType: 'json',
                        delay: 250,
                        data: (params) => ({
                            q: params.term,
                            page: params.page || 1
                        }),
                        processResults: (data) => ({
                            results: data.map(chairman => ({
                                id: chairman.id,
                                text: chairman.fullName || (chairman.lastName + ' ' + chairman.firstName)
                            })),
                            pagination: { more: false }
                        }),
                        cache: true
                    },
                    minimumInputLength: 0
                });

                resolve();
            });
        });
    }

    /**
     * Определение режима страницы (создание/редактирование)
     */
    function determinePageMode() {
        const path = window.location.pathname;
        if (path.includes('/edit/')) {
            state.isEditMode = true;
            state.houseId = path.split('/edit/')[1];
        }
    }

    // ============================================
    // 2. ЗАГРУЗКА ДАННЫХ
    // ============================================

    /**
     * Загрузка данных дома
     */
    async function loadHouseData() {
        try {
            const response = await fetch((window.contextPath || '') + `/houses/getHouse/${state.houseId}`);
            if (response.ok) {
                state.currentHouse = await response.json();
            } else {
                throw new Error('Дом не найден');
            }
        } catch (error) {
            console.error('Error loading house:', error);
            NotificationModule.show('Ошибка при загрузке данных дома', 'error');
        }
    }

    /**
     * Загрузка статусов
     */
    async function loadStatuses() {
        try {
            const response = await fetch((window.contextPath || '') + '/houses/getStatuses');
            if (response.ok) {
                const statuses = await response.json();
                populateStatusSelect(statuses);
            }
        } catch (error) {
            console.error('Error loading statuses:', error);
        }
    }

    /**
     * Заполнение селекта статусов
     */
    function populateStatusSelect(statuses) {
        const statusSelect = elements.fields.status;
        
        // Получаем placeholder с учетом i18n
        const placeholderText = typeof i18next !== 'undefined' 
            ? i18next.t('houses.placeholders.selectStatus')
            : 'Выберите статус';
        statusSelect.innerHTML = `<option value="">${placeholderText}</option>`;

        // Маппинг статусов на i18n ключи
        const statusI18nKeys = {
            'NEW': 'houses.statusOptions.new',
            'ACTIVE': 'houses.statusOptions.active',
            'DEACTIVATED': 'houses.statusOptions.deactivated',
            'BLOCKED': 'houses.statusOptions.blocked'
        };

        // Fallback значения на случай отсутствия i18n
        const statusFallbacks = {
            'NEW': 'Новый',
            'ACTIVE': 'Активный',
            'DEACTIVATED': 'Деактивирован',
            'BLOCKED': 'Заблокирован'
        };

        statuses.forEach(status => {
            const option = document.createElement('option');
            option.value = status;
            
            // Используем i18n если доступен, иначе fallback
            if (typeof i18next !== 'undefined' && statusI18nKeys[status]) {
                option.textContent = i18next.t(statusI18nKeys[status]);
                option.setAttribute('data-i18n', statusI18nKeys[status]);
            } else {
                option.textContent = statusFallbacks[status] || status;
            }
            
            statusSelect.appendChild(option);
        });
    }

    /**
     * Обновление меток статусов при смене языка
     */
    function updateStatusLabels() {
        if (typeof i18next === 'undefined') return;

        const statusSelect = elements.fields.status;
        const currentValue = statusSelect.value;

        // Обновляем все опции с data-i18n атрибутом
        Array.from(statusSelect.options).forEach(option => {
            const i18nKey = option.getAttribute('data-i18n');
            if (i18nKey) {
                option.textContent = i18next.t(i18nKey);
            }
        });

        // Восстанавливаем выбранное значение
        if (currentValue) {
            statusSelect.value = currentValue;
        }
    }

    /**
     * Установка значения Select2
     */
    function setSelect2Value(selectElement, data) {
        if (!data || !data.id) return Promise.resolve();

        return new Promise((resolve) => {
            if (selectElement.find(`option[value="${data.id}"]`).length) {
                selectElement.val(data.id).trigger('change');
                resolve();
            } else {
                const option = new Option(data.text, data.id, true, true);
                selectElement.append(option);
                selectElement.one('select2:select', () => resolve());
                selectElement.trigger({
                    type: 'select2:select',
                    params: { data: data }
                });
                setTimeout(resolve, 50);
            }
        });
    }

    /**
     * Заполнение формы данными дома
     */
    async function populateForm(house) {
        try {
            console.log('Populating form with house:', house);

            // Заполнение простых полей
            if (house.houseNumber) elements.fields.houseNumber.value = house.houseNumber;
            if (house.status) elements.fields.status.value = house.status;

            // Set house ID
            if (elements.houseIdField) elements.houseIdField.value = house.id;

            // Устанавливаем улицу
            if (house.street?.id) {
                await setSelect2Value($(elements.fields.streetId), {
                    id: house.street.id,
                    text: house.street.name + ' (' + (house.street.city ? house.street.city.name : '') + ')'
                });
            }

            // Устанавливаем председателя
            if (house.chairman?.id) {
                await setSelect2Value($(elements.fields.chairmanId), {
                    id: house.chairman.id,
                    text: house.chairman.fullName
                });
            }

            console.log('Form populated successfully');

        } catch (error) {
            console.error('Error populating form:', error);
            NotificationModule.show('Ошибка при заполнении формы', 'error');
        }
    }

    // ============================================
    // 3. ОБРАБОТКА СОБЫТИЙ
    // ============================================

    /**
     * Настройка обработчиков событий
     */
    function setupEventListeners() {
        // Отправка формы
        elements.form.addEventListener('submit', handleFormSubmit);

        // Кнопка отмены
        const cancelBtn = document.querySelector('button[type="reset"]');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', function(e) {
                e.preventDefault();
                window.history.back();
            });
        }
    }

    // ============================================
    // 4. ОТПРАВКА ФОРМЫ
    // ============================================

    /**
     * Обработчик отправки формы
     */
    async function handleFormSubmit(e) {
        e.preventDefault();

        ValidationModule.clearErrors(elements.fields);

        const submitBtn = elements.form.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;

        try {
            // Блокируем кнопку
            submitBtn.disabled = true;
            submitBtn.textContent = 'Сохранение...';

            // Подготовка данных
            const formData = new FormData(elements.form);
            
            // Ensure Select2 values are included
            const streetId = $(elements.fields.streetId).val();
            if (streetId) {
                formData.set('streetId', streetId);
            }
            
            const chairmanId = $(elements.fields.chairmanId).val();
            if (chairmanId) {
                formData.set('chairmanId', chairmanId);
            } else {
                formData.delete('chairmanId');
            }

            console.log('Form data being sent:');
            for (let [key, value] of formData.entries()) {
                console.log(`${key}: ${value}`);
            }

            // API endpoint and method
            const url = state.isEditMode ? (window.contextPath || '') + `/houses/${state.houseId}` : (window.contextPath || '') + '/houses/create';
            const method = state.isEditMode ? 'PUT' : 'POST';

            console.log(`Sending ${method} request to ${url}`);

            // For create, we need JSON; for update, we need FormData
            let requestOptions;
            if (state.isEditMode) {
                requestOptions = {
                    method: method,
                    body: formData
                };
            } else {
                // For create, convert FormData to JSON
                const jsonData = {};
                for (let [key, value] of formData.entries()) {
                    if (value && value !== '') {
                        jsonData[key] = value;
                    }
                }
                console.log('JSON data for create:', jsonData);
                requestOptions = {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(jsonData)
                };
            }

            const response = await fetch(url, requestOptions);

            if (response.ok) {
                const result = await response.json();
                console.log('Success response:', result);
                
                const houseName = result.houseNumber || elements.fields.houseNumber.value || '';
                
                NotificationModule.show(
                    state.isEditMode 
                        ? `Дом ${houseName} успешно обновлен` 
                        : `Дом ${houseName} успешно создан`,
                    'success'
                );

                setTimeout(() => {
                    window.location.href = (window.contextPath || '') + '/houses';
                }, 1500);
            } else {
                const errorData = await response.json();
                console.error('Error response:', errorData);
                ValidationModule.handleServerErrors(errorData, elements.fields);
            }

        } catch (error) {
            console.error('Form submission error:', error);
            NotificationModule.show('Ошибка при сохранении данных', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    }

    // ============================================
    // ПУБЛИЧНЫЙ API
    // ============================================
    return {
        init: init
    };
})();

// ============================================
// 5. МОДУЛЬ ВАЛИДАЦИИ
// ============================================
const ValidationModule = (function() {
    'use strict';

    /**
     * Обработка ошибок с сервера
     */
    function handleServerErrors(errorData, fields) {
        clearErrors(fields);

        const errors = errorData.fieldErrors || errorData.validationErrors;

        if (errors) {
            Object.keys(errors).forEach(fieldName => {
                const field = fields[fieldName];
                if (field) {
                    const errorContainer = field.parentElement.querySelector('.invalid-feedback');
                    if (errorContainer) {
                        field.classList.add('is-invalid');
                        errorContainer.textContent = errors[fieldName];
                    }
                }
            });

            NotificationModule.show(
                errorData.message || 'Ошибка валидации данных',
                'error'
            );
        } else {
            NotificationModule.show(
                errorData.message || 'Ошибка валидации данных',
                'error'
            );
        }
    }

    /**
     * Очистка ошибок валидации
     */
    function clearErrors(fields) {
        Object.values(fields).forEach(field => {
            if (field) {
                field.classList.remove('is-invalid');
                const errorContainer = field.parentElement.querySelector('.invalid-feedback');
                if (errorContainer) {
                    errorContainer.textContent = '';
                }
            }
        });
    }

    return {
        handleServerErrors,
        clearErrors
    };
})();

// ============================================
// МОДУЛЬ УВЕДОМЛЕНИЙ
// ============================================
const NotificationModule = (function() {
    'use strict';

    /**
     * Показать уведомление
     */
    function show(message, type = 'info') {
        const titles = {
            'error': 'Ошибка',
            'success': 'Успех',
            'warning': 'Предупреждение',
            'info': 'Информация'
        };

        if (typeof showToast === 'function') {
            showToast(type, titles[type] || 'Информация', message);
        } else {
            console.warn('Toast function not found:', message);
        }
    }

    return {
        show
    };
})();

// ============================================
// ИНИЦИАЛИЗАЦИЯ ПРИ ЗАГРУЗКЕ СТРАНИЦЫ
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    HouseEditPage.init();
});
