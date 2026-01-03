/**
 * ============================================
 * МОДУЛЬ: USER EDIT PAGE
 * ============================================
 * Структура:
 * 1. Инициализация страницы (Page Initialization)
 * 2. Загрузка данных (Data Loading)
 * 3. Обработка событий (Event Handlers)
 * 4. Отправка формы (Form Submission)
 * 5. Валидация (Validation)
 * ============================================
 */

const UserEditPage = (function() {
    'use strict';

    // ============================================
    // СОСТОЯНИЕ И КОНФИГУРАЦИЯ
    // ============================================
    const state = {
        isEditMode: false,
        userId: null,
        currentUser: null,
        isPopulatingForm: false
    };

    const elements = {
        form: null,
        userIdField: null,
        avatarImg: null,
        fileInput: null,
        resetBtn: null,
        pageTitle: null,
        fields: {}
    };

    const config = {
        maxFileSize: 800 * 1024, // 800KB
        allowedFileTypes: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'],
        defaultAvatar: (window.contextPath || '') + '/assets/img/avatars/1.png'
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
            await determinePageMode();
            await loadInitialData();
            setupEventListeners();
            await waitForSelect2Ready();

            // Слушатель смены языка для обновления placeholder'ов
            if (typeof i18next !== 'undefined') {
                i18next.on('languageChanged', function() {
                    updateSelect2Placeholders();
                    updateStatusLabels();
                });
            }

            if (state.isEditMode && state.currentUser) {
                await populateForm(state.currentUser);
                // Обновляем заголовок с именем пользователя
                const userName = state.currentUser.firstName || state.currentUser.lastName || '';
                if (userName) {
                    updatePageTitle(userName);
                }
            }
        } catch (error) {
            console.error('Initialization error:', error);
            NotificationModule.show('users.errors.initialization', 'error', true);
        }
    }

    /**
     * Инициализация DOM элементов
     */
    function initElements() {
        elements.form = document.getElementById('formAccountSettings');
        elements.userIdField = document.getElementById('userId');
        elements.avatarImg = document.getElementById('uploadedAvatar');
        elements.fileInput = document.getElementById('photo');
        elements.resetBtn = document.querySelector('.account-image-reset');
        elements.pageTitle = document.getElementById('pageTitle');

        elements.fields = {
            firstName: document.getElementById('firstName'),
            lastName: document.getElementById('lastName'),
            middleName: document.getElementById('middleName'),
            phone: document.getElementById('phone'),
            email: document.getElementById('email'),
            login: document.getElementById('login'),
            cityId: document.getElementById('cityId'),
            addressId: document.getElementById('streetId'),
            houseNumber: document.getElementById('houseId'),
            apartmentNumber: document.getElementById('apartmentNumber'),
            apartmentArea: document.getElementById('apartmentArea'),
            accountNumber: document.getElementById('accountNumber'),
            status: document.getElementById('status'),
            password: document.getElementById('password'),
            confirmPassword: document.getElementById('repeatPassword')
        };
    }

    /**
     * Инициализация Select2 компонентов
     */
    function initSelect2() {
        return new Promise((resolve) => {
            $(document).ready(function() {
                // Функция для получения переведенного placeholder
                const getPlaceholder = (key) => {
                    return typeof i18next !== 'undefined' ? i18next.t(key) : key;
                };

                // Города
                $('#cityId').select2({
                    placeholder: getPlaceholder('users.placeholders.selectCity'),
                    allowClear: true,
                    ajax: {
                        url: (window.contextPath || '') + '/cities/search',
                        dataType: 'json',
                        delay: 250,
                        data: (params) => ({
                            q: params.term,
                            page: params.page || 1
                        }),
                        processResults: (data) => ({
                            results: data.map(city => ({
                                id: city.id,
                                text: city.name
                            })),
                            pagination: { more: false }
                        }),
                        cache: true
                    },
                    minimumInputLength: 0
                });

                // Улицы
                $('#streetId').select2({
                    placeholder: getPlaceholder('users.placeholders.selectCityFirst'),
                    allowClear: true,
                    disabled: true,
                    ajax: {
                        url: (window.contextPath || '') + '/streets/search',
                        dataType: 'json',
                        delay: 250,
                        data: (params) => ({
                            cityId: $('#cityId').val(),
                            q: params.term,
                            page: params.page || 1
                        }),
                        processResults: (data) => ({
                            results: data.map(street => ({
                                id: street.id,
                                text: street.name
                            })),
                            pagination: { more: false }
                        }),
                        cache: true
                    },
                    minimumInputLength: 0
                });

                // Дома
                $('#houseId').select2({
                    placeholder: getPlaceholder('users.placeholders.selectStreetFirst'),
                    allowClear: true,
                    disabled: true,
                    ajax: {
                        url: (window.contextPath || '') + '/houses/search',
                        dataType: 'json',
                        delay: 250,
                        data: (params) => ({
                            streetId: $('#streetId').val(),
                            q: params.term,
                            page: params.page || 1
                        }),
                        processResults: (data) => ({
                            results: data.map(house => ({
                                id: house.id,
                                text: house.number || house.houseNumber || 'Номер не указан'
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
     * Обновление placeholder'ов Select2 при смене языка
     */
    function updateSelect2Placeholders() {
        if (typeof i18next === 'undefined') return;

        const cityPlaceholder = i18next.t('users.placeholders.selectCity');
        const streetPlaceholder = i18next.t('users.placeholders.selectCityFirst');
        const housePlaceholder = i18next.t('users.placeholders.selectStreetFirst');

        // Обновляем placeholder города
        $('#cityId').data('select2').options.options.placeholder = cityPlaceholder;
        $('#cityId').data('select2').$container.find('.select2-selection__placeholder').text(cityPlaceholder);

        // Обновляем placeholder улицы
        $('#streetId').data('select2').options.options.placeholder = streetPlaceholder;
        $('#streetId').data('select2').$container.find('.select2-selection__placeholder').text(streetPlaceholder);

        // Обновляем placeholder дома
        $('#houseId').data('select2').options.options.placeholder = housePlaceholder;
        $('#houseId').data('select2').$container.find('.select2-selection__placeholder').text(housePlaceholder);
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
     * Определение режима страницы (создание/редактирование)
     */
    function determinePageMode() {
        const path = window.location.pathname;
        if (path.includes('/edit/')) {
            state.isEditMode = true;
            state.userId = path.split('/edit/')[1];
        }
        updatePageTitle();
    }

    /**
     * Обновление заголовка страницы
     */
    function updatePageTitle(userName) {
        if (!elements.pageTitle) return;

        if (state.isEditMode) {
            if (userName) {
                // Если имя пользователя передано, используем его
                const title = typeof i18next !== 'undefined'
                    ? i18next.t('users.editingUser', { userName: userName })
                    : `Редактирование пользователя ${userName}`;
                elements.pageTitle.textContent = title;
                elements.pageTitle.removeAttribute('data-i18n');
            } else {
                // Пока имя не загружено, показываем общий заголовок
                elements.pageTitle.textContent = typeof i18next !== 'undefined'
                    ? i18next.t('users.editUser')
                    : 'Редактирование пользователя';
                elements.pageTitle.setAttribute('data-i18n', 'users.editUser');
            }
        } else {
            // Режим создания
            const title = typeof i18next !== 'undefined'
                ? i18next.t('users.newUser')
                : 'Создание нового пользователя';
            elements.pageTitle.textContent = title;
            elements.pageTitle.setAttribute('data-i18n', 'users.newUser');
        }
    }

    /**
     * Ожидание готовности Select2
     */
    function waitForSelect2Ready() {
        return new Promise((resolve) => {
            const checkReady = () => {
                const cityReady = $(elements.fields.cityId).hasClass('select2-hidden-accessible');
                const streetReady = $(elements.fields.addressId).hasClass('select2-hidden-accessible');
                const houseReady = $(elements.fields.houseNumber).hasClass('select2-hidden-accessible');

                if (cityReady && streetReady && houseReady) {
                    resolve();
                } else {
                    requestAnimationFrame(checkReady);
                }
            };
            checkReady();
        });
    }

    // ============================================
    // 2. ЗАГРУЗКА ДАННЫХ
    // ============================================

    /**
     * Загрузка начальных данных
     */
    async function loadInitialData() {
        await Promise.all([
            state.isEditMode ? loadUserData() : Promise.resolve(),
            loadStatuses()
        ]);
    }

    /**
     * Загрузка данных пользователя
     */
    async function loadUserData() {
        try {
            const response = await fetch((window.contextPath || '') + `/users/getUser/${state.userId}`);
            if (response.ok) {
                state.currentUser = await response.json();
            } else {
                throw new Error('User not found');
            }
        } catch (error) {
            console.error('Error loading user:', error);
            NotificationModule.show('users.errors.loadFailed', 'error', true);
        }
    }

    /**
     * Загрузка улиц по городу
     */
    async function loadStreets(cityId) {
        if (!cityId) {
            $(elements.fields.addressId).prop('disabled', true);
            return;
        }

        try {
            const response = await fetch((window.contextPath || '') + `/streets/getByCity/${cityId}`);
            if (response.ok) {
                const streets = await response.json();
                
                $(elements.fields.addressId).empty();
                $(elements.fields.addressId).append('<option value="">Выберите улицу</option>');
                
                streets.forEach(street => {
                    const option = new Option(street.name, street.id, false, false);
                    $(elements.fields.addressId).append(option);
                });
                
                $(elements.fields.addressId).trigger('change');
                $(elements.fields.addressId).prop('disabled', false);
                
                return streets;
            } else {
                throw new Error('Failed to load streets');
            }
        } catch (error) {
            console.error('Error loading streets:', error);
            throw error;
        }
    }

    /**
     * Загрузка домов по улице
     */
    async function loadHouses(streetId) {
        if (!streetId) {
            $(elements.fields.houseNumber).prop('disabled', true);
            return;
        }

        try {
            const response = await fetch((window.contextPath || '') + `/houses/getByStreet/${streetId}`);
            if (response.ok) {
                const houses = await response.json();
                
                $(elements.fields.houseNumber).empty();
                $(elements.fields.houseNumber).append('<option value="">Выберите дом</option>');
                
                houses.forEach(house => {
                    const houseText = house.number || house.houseNumber || 'Номер не указан';
                    const option = new Option(houseText, house.id, false, false);
                    $(elements.fields.houseNumber).append(option);
                });
                
                $(elements.fields.houseNumber).trigger('change');
                $(elements.fields.houseNumber).prop('disabled', false);
                
                return houses;
            } else {
                throw new Error('Failed to load houses');
            }
        } catch (error) {
            console.error('Error loading houses:', error);
            throw error;
        }
    }

    /**
     * Загрузка статусов
     */
    async function loadStatuses() {
        try {
            const response = await fetch((window.contextPath || '') + '/users/getStatuses');
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
            ? i18next.t('users.placeholders.selectStatus')
            : 'Выберите статус';
        statusSelect.innerHTML = `<option value="">${placeholderText}</option>`;

        // Маппинг статусов на i18n ключи
        const statusI18nKeys = {
            'ACTIVE': 'users.statusOptions.active',
            'INACTIVE': 'users.statusOptions.inactive',
            'BLOCKED': 'users.statusOptions.blocked'
        };

        // Fallback значения на случай отсутствия i18n
        const statusFallbacks = {
            'ACTIVE': 'Активный',
            'INACTIVE': 'Неактивный',
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
     * Заполнение формы данными пользователя
     */
    async function populateForm(user) {
        try {
            state.isPopulatingForm = true;

            // Простые поля
            if (user.firstName) elements.fields.firstName.value = user.firstName;
            if (user.lastName) elements.fields.lastName.value = user.lastName;
            if (user.middleName) elements.fields.middleName.value = user.middleName;
            if (user.phone) elements.fields.phone.value = user.phone;
            if (user.email) elements.fields.email.value = user.email;
            if (user.login) elements.fields.login.value = user.login;
            if (user.apartmentNumber) elements.fields.apartmentNumber.value = user.apartmentNumber;
            if (user.apartmentArea) elements.fields.apartmentArea.value = user.apartmentArea;
            if (user.accountNumber) elements.fields.accountNumber.value = user.accountNumber;
            if (user.status) elements.fields.status.value = user.status;

            if (elements.userIdField) elements.userIdField.value = user.id;

            // Select2 поля
            if (user.city?.id) {
                await setSelect2Value($(elements.fields.cityId), {
                    id: user.city.id,
                    text: user.city.name
                });
            }

            if (user.street?.id) {
                $(elements.fields.addressId).prop('disabled', false).select2('enable');
                await setSelect2Value($(elements.fields.addressId), {
                    id: user.street.id,
                    text: user.street.name
                });
            }

            if (user.house?.id) {
                $(elements.fields.houseNumber).prop('disabled', false).select2('enable');
                const houseText = user.house.number || user.house.houseNumber || 'Номер не указан';
                await setSelect2Value($(elements.fields.houseNumber), {
                    id: user.house.id,
                    text: houseText
                });
                $('#houseNumberHidden').val(houseText);
            }

            // Аватар
            if (user.photo && user.photo.trim() !== '' && user.photo !== 'default_photo.jpg') {
                elements.avatarImg.src = (window.contextPath || '') + '/' + user.photo;
            } else {
                elements.avatarImg.src = config.defaultAvatar;
            }

        } catch (error) {
            console.error('Error populating form:', error);
            NotificationModule.show('users.errors.populateForm', 'error', true);
        } finally {
            state.isPopulatingForm = false;
        }
    }

    // ============================================
    // 3. ОБРАБОТКА СОБЫТИЙ
    // ============================================

    /**
     * Настройка обработчиков событий
     */
    function setupEventListeners() {
        // Изменение города
        $(elements.fields.cityId).on('change', handleCityChange);

        // Изменение улицы
        $(elements.fields.addressId).on('change', handleStreetChange);

        // Изменение дома
        $(elements.fields.houseNumber).on('change', handleHouseChange);

        // Отправка формы
        elements.form.addEventListener('submit', handleFormSubmit);

        // Загрузка файла
        if (elements.fileInput) {
            elements.fileInput.addEventListener('change', handleFileUpload);
        }

        // Сброс аватара
        if (elements.resetBtn) {
            elements.resetBtn.addEventListener('click', resetAvatar);
        }

        // Кнопка отмены
        const cancelBtn = document.querySelector('button[type="reset"]');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', function(e) {
                e.preventDefault();
                window.history.back();
            });
        }
    }

    /**
     * Обработчик изменения города
     */
    function handleCityChange() {
        if (state.isPopulatingForm) return;

        const cityId = $(this).val();

        $(elements.fields.addressId).val(null).trigger('change');
        $(elements.fields.addressId).prop('disabled', true);

        $(elements.fields.houseNumber).val(null).trigger('change');
        $(elements.fields.houseNumber).prop('disabled', true);

        if (cityId) {
            loadStreets(cityId).catch(err => {
                console.error('Error loading streets:', err);
                NotificationModule.show('users.errors.loadStreets', 'error', true);
            });
        }
    }

    /**
     * Обработчик изменения улицы
     */
    function handleStreetChange() {
        if (state.isPopulatingForm) return;

        const streetId = $(this).val();

        $(elements.fields.houseNumber).val(null).trigger('change');
        $(elements.fields.houseNumber).prop('disabled', true);
        $('#houseNumberHidden').val('');

        if (streetId) {
            loadHouses(streetId).catch(err => {
                console.error('Error loading houses:', err);
                NotificationModule.show('users.errors.loadHouses', 'error', true);
            });
        }
    }

    /**
     * Обработчик изменения дома
     */
    function handleHouseChange() {
        const selectedOption = $(this).find('option:selected');
        const houseNumber = selectedOption.text();
        if (houseNumber && houseNumber !== 'Выберите дом' && houseNumber !== 'Сначала выберите улицу') {
            $('#houseNumberHidden').val(houseNumber);
        } else {
            $('#houseNumberHidden').val('');
        }
    }

    /**
     * Обработчик загрузки файла
     */
    function handleFileUpload(e) {
        const file = e.target.files[0];
        if (!file) return;

        // Валидация типа файла
        if (!ValidationModule.validateFileType(file, config.allowedFileTypes)) {
            NotificationModule.show('users.validation.invalidFileType', 'error', true);
            e.target.value = '';
            return;
        }

        // Валидация размера файла
        if (!ValidationModule.validateFileSize(file, config.maxFileSize)) {
            NotificationModule.show('users.validation.fileTooLarge', 'error', true, { maxSize: '800KB' });
            e.target.value = '';
            return;
        }

        // Предпросмотр изображения
        const reader = new FileReader();
        reader.onload = function(e) {
            elements.avatarImg.src = e.target.result;
        };
        reader.readAsDataURL(file);
    }

    /**
     * Сброс аватара
     */
    function resetAvatar() {
        elements.avatarImg.src = config.defaultAvatar;
        if (elements.fileInput) {
            elements.fileInput.value = '';
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
            submitBtn.textContent = typeof i18next !== 'undefined'
                ? i18next.t('common.saving')
                : 'Сохранение...';

            // Подготовка данных
            const formData = new FormData(elements.form);
            const repeatPassword = formData.get('repeatPassword');
            if (repeatPassword) {
                formData.delete('repeatPassword');
                formData.append('confirmPassword', repeatPassword);
            }

            // Удаляем поле id, если оно пустое (для создания нового пользователя)
            const userId = formData.get('id');
            if (!userId || userId.trim() === '') {
                formData.delete('id');
            }

            // Отправка запроса
            const url = state.isEditMode ? (window.contextPath || '') + `/users/${state.userId}` : (window.contextPath || '') + '/users/create';
            const method = state.isEditMode ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                body: formData
            });

            if (response.ok) {
                const result = await response.json();
                const userName = result.firstName || elements.fields.firstName.value || '';
                
                NotificationModule.show(
                    state.isEditMode ? 'users.success.updated' : 'users.success.created',
                    'success',
                    true,
                    { userName: userName }
                );

                setTimeout(() => {
                    window.location.href = (window.contextPath || '') + '/users';
                }, 1500);
            } else {
                const errorData = await response.json();
                ValidationModule.handleServerErrors(errorData, elements.fields);
            }

        } catch (error) {
            console.error('Form submission error:', error);
            NotificationModule.show('users.errors.saveFailed', 'error', true);
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
     * Валидация типа файла
     */
    function validateFileType(file, allowedTypes) {
        return allowedTypes.includes(file.type);
    }

    /**
     * Валидация размера файла
     */
    function validateFileSize(file, maxSize) {
        return file.size <= maxSize;
    }

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
                        
                        // Проверяем, является ли ошибка ключом i18n
                        const errorMessage = errors[fieldName];
                        if (errorMessage.includes('.')) {
                            errorContainer.setAttribute('data-i18n', errorMessage);
                            errorContainer.textContent = typeof i18next !== 'undefined'
                                ? i18next.t(errorMessage)
                                : errorMessage;
                        } else {
                            errorContainer.textContent = errorMessage;
                        }
                    }
                }
            });

            NotificationModule.show(
                errorData.message || 'users.errors.validation',
                'error',
                typeof i18next !== 'undefined'
            );
        } else {
            NotificationModule.show(
                errorData.message || 'users.errors.validation',
                'error',
                typeof i18next !== 'undefined'
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
                    errorContainer.removeAttribute('data-i18n');
                }
            }
        });
    }

    return {
        validateFileType,
        validateFileSize,
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
    function show(message, type = 'info', useI18n = false, interpolationData = {}) {
        const titleKey = getTitleKey(type);

        if (useI18n && typeof i18next !== 'undefined' && typeof showI18nToast === 'function') {
            showI18nToast(type, titleKey, message, interpolationData);
        } else {
            if (typeof showToast === 'function') {
                const fallbackTitles = {
                    'error': 'Ошибка',
                    'success': 'Успех',
                    'warning': 'Предупреждение',
                    'info': 'Информация'
                };
                showToast(type, fallbackTitles[type] || 'Информация', message);
            } else {
                console.warn('Toast function not found:', message);
            }
        }
    }

    /**
     * Получить ключ заголовка по типу
     */
    function getTitleKey(type) {
        const titleKeys = {
            'error': 'notifications.titles.error',
            'success': 'notifications.titles.success',
            'warning': 'notifications.titles.warning',
            'info': 'notifications.titles.info'
        };
        return titleKeys[type] || titleKeys.info;
    }

    return {
        show
    };
})();

// ============================================
// ИНИЦИАЛИЗАЦИЯ ПРИ ЗАГРУЗКЕ СТРАНИЦЫ
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    UserEditPage.init();
});
