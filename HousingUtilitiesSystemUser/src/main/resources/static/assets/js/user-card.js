/**
 * User Card/Profile Page
 */
'use strict';

document.addEventListener('DOMContentLoaded', function (e) {
  // Variables
  let userId = null;
  let currentUser = null;

  // Elements
  const userAvatar = document.getElementById('userAvatar');
  const userFullName = document.getElementById('userFullName');
  const userStatus = document.getElementById('userStatus');
  const userAddress = document.getElementById('userAddress');
  const userAccount = document.getElementById('userAccount');
  const userEmail = document.getElementById('userEmail');
  const userPhone = document.getElementById('userPhone');
  const userFirstName = document.getElementById('userFirstName');
  const userLastName = document.getElementById('userLastName');
  const userMiddleName = document.getElementById('userMiddleName');
  const userApartmentNumber = document.getElementById('userApartmentNumber');
  const userApartmentArea = document.getElementById('userApartmentArea');
  const userCity = document.getElementById('userCity');
  const userStreet = document.getElementById('userStreet');
  const userHouseNumber = document.getElementById('userHouseNumber');
  
  // Buttons
  const editUserBtn = document.getElementById('editUserBtn');
  const deleteUserBtn = document.getElementById('deleteUserBtn');
  const billsTableBody = document.getElementById('billsTableBody');

  // Initialize
  init();

  async function init() {
    try {
      // Extract user ID from URL
      const path = window.location.pathname;
      const pathParts = path.split('/');
      userId = pathParts[pathParts.length - 1];

      if (!userId) {
        throw new Error('User ID not found in URL');
      }

      // Setup event listeners
      setupEventListeners();

      // Load user data
      await loadUserData();
      
    } catch (error) {
      console.error('Initialization error:', error);
      showError('Ошибка при инициализации страницы');
    }
  }

  function setupEventListeners() {
    // Edit button
    if (editUserBtn) {
      editUserBtn.addEventListener('click', function() {
        window.location.href = (window.contextPath || '') + `users/edit/${userId}`;
      });
    }

    // Delete button
    if (deleteUserBtn) {
      deleteUserBtn.addEventListener('click', function() {
        deleteUser();
      });
    }
  }

  async function loadUserData() {
    try {
      showLoading();

      const response = await fetch((window.contextPath || '') + `users/getUser/${userId}`);
      
      if (response.ok) {
        currentUser = await response.json();
        populateUserData(currentUser);
      } else if (response.status === 404) {
        throw new Error('Пользователь не найден');
      } else {
        throw new Error('Ошибка при загрузке данных пользователя');
      }
    } catch (error) {
      console.error('Error loading user data:', error);
      showError(error.message);
    } finally {
      hideLoading();
    }
  }

  function populateUserData(user) {
    try {
      // Avatar
      if (user.photo && user.photo.trim() !== '' && user.photo !== 'default_photo.jpg') {
        if (user.photo.startsWith('http')) {
          userAvatar.src = user.photo;
        } else {
          userAvatar.src = (window.contextPath || '') + '/' + user.photo;
        }
      } else {
        userAvatar.src = (window.contextPath || '') + '/assets/img/avatars/1.png';
      }

      // Full name and status
      userFullName.textContent = user.fullName || `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Не указано';
      
      // Status
      userStatus.textContent = getStatusText(user.status);
      userStatus.className = `badge ${getStatusBadgeClass(user.status)}`;

      // Address
      const addressParts = [
        user.city ? user.city.name : '',
        user.street ? user.street.name : '',
        user.houseNumber ? `д. ${user.houseNumber}` : '',
        user.apartmentNumber ? `кв. ${user.apartmentNumber}` : ''
      ].filter(part => part);
      
      userAddress.textContent = addressParts.length > 0 ? addressParts.join(', ') : 'Не указан';

      // Account number
      userAccount.textContent = user.accountNumber || 'Не указан';

      // Contact info
      userEmail.textContent = user.email || 'Не указан';
      userPhone.textContent = user.phone || 'Не указан';

      // Detailed info
      userFirstName.textContent = user.firstName || 'Не указано';
      userLastName.textContent = user.lastName || 'Не указано';
      userMiddleName.textContent = user.middleName || 'Не указано';
      userApartmentNumber.textContent = user.apartmentNumber || 'Не указан';
      userApartmentArea.textContent = user.apartmentArea ? `${user.apartmentArea} м²` : 'Не указана';
      userCity.textContent = user.city ? user.city.name : 'Не указан';
      userStreet.textContent = user.street ? user.street.name : 'Не указана';
      userHouseNumber.textContent = user.houseNumber || 'Не указан';

      // Bills
      loadBillsData(user.bills);

    } catch (error) {
      console.error('Error populating user data:', error);
      showError('Ошибка при отображении данных пользователя');
    }
  }

  function loadBillsData(bills) {
    if (!bills || bills.length === 0) {
      billsTableBody.innerHTML = `
        <tr>
          <td colspan="4" class="text-center text-muted">
            <i class="ti ti-receipt ti-sm me-1"></i>
            Нет данных о счетах
          </td>
        </tr>
      `;
      return;
    }

    billsTableBody.innerHTML = '';
    
    bills.forEach(bill => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${bill.period || 'Не указан'}</td>
        <td>${bill.amount ? `${bill.amount} ₴` : 'Не указана'}</td>
        <td>
          <span class="badge ${getBillStatusBadgeClass(bill.status)}">
            ${getBillStatusText(bill.status)}
          </span>
        </td>
        <td>${bill.date ? new Date(bill.date).toLocaleDateString('ru-RU') : 'Не указана'}</td>
      `;
      billsTableBody.appendChild(row);
    });
  }

  function getStatusBadgeClass(status) {
    switch (status) {
      case 'ACTIVE': return 'bg-label-success';
      case 'INACTIVE': return 'bg-label-secondary';
      case 'BLOCKED': return 'bg-label-danger';
      default: return 'bg-label-secondary';
    }
  }

  function getStatusText(status) {
    switch (status) {
      case 'ACTIVE': return 'Активный';
      case 'INACTIVE': return 'Неактивный';
      case 'BLOCKED': return 'Заблокирован';
      default: return status || 'Неизвестно';
    }
  }

  function getBillStatusBadgeClass(status) {
    switch (status) {
      case 'PAID': return 'bg-label-success';
      case 'PENDING': return 'bg-label-warning';
      case 'OVERDUE': return 'bg-label-danger';
      default: return 'bg-label-secondary';
    }
  }

  function getBillStatusText(status) {
    switch (status) {
      case 'PAID': return 'Оплачен';
      case 'PENDING': return 'Ожидает оплаты';
      case 'OVERDUE': return 'Просрочен';
      default: return status || 'Неизвестно';
    }
  }

  async function deleteUser() {
    if (!currentUser || !userId) return;

    if (typeof Swal !== 'undefined') {
      const result = await Swal.fire({
        title: 'Удалить пользователя?',
        text: `Вы действительно хотите удалить пользователя "${currentUser.fullName || currentUser.email}"? Это действие нельзя отменить.`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Да, удалить',
        cancelButtonText: 'Отмена',
        customClass: {
          confirmButton: 'btn btn-danger waves-effect waves-light',
          cancelButton: 'btn btn-label-secondary waves-effect waves-light'
        },
        buttonsStyling: false
      });

      if (result.isConfirmed) {
        try {
          const response = await fetch((window.contextPath || '') + `users/${userId}`, {
            method: 'DELETE'
          });

          if (response.ok) {
            Swal.fire({
              title: 'Удалено!',
              text: 'Пользователь был успешно удален.',
              icon: 'success',
              customClass: {
                confirmButton: 'btn btn-success waves-effect waves-light'
              },
              buttonsStyling: false
            }).then(() => {
              window.location.href = (window.contextPath || '') + '/users';
            });
          } else {
            throw new Error('Ошибка при удалении пользователя');
          }
        } catch (error) {
          console.error('Error deleting user:', error);
          Swal.fire({
            title: 'Ошибка!',
            text: 'Произошла ошибка при удалении пользователя.',
            icon: 'error',
            customClass: {
              confirmButton: 'btn btn-success waves-effect waves-light'
            },
            buttonsStyling: false
          });
        }
      }
    } else {
      if (confirm(`Удалить пользователя "${currentUser.fullName || currentUser.email}"?`)) {
        try {
          const response = await fetch((window.contextPath || '') + `users/${userId}`, {
            method: 'DELETE'
          });

          if (response.ok) {
            alert('Пользователь удален успешно');
            window.location.href = (window.contextPath || '') + '/users';
          } else {
            throw new Error('Ошибка при удалении');
          }
        } catch (error) {
          console.error('Error deleting user:', error);
          alert('Ошибка при удалении пользователя');
        }
      }
    }
  }

  function showLoading() {
    userFullName.textContent = 'Загрузка...';
    
    // Show loading state for other elements
    const elements = [
      userStatus, userAddress, userAccount, userEmail, userPhone,
      userFirstName, userLastName, userMiddleName, userApartmentNumber,
      userApartmentArea, userCity, userStreet, userHouseNumber
    ];
    
    elements.forEach(element => {
      if (element) {
        element.textContent = 'Загрузка...';
      }
    });
  }

  function hideLoading() {
    // Loading state will be hidden when data is populated
  }

  function showError(message) {
    userFullName.textContent = 'Ошибка загрузки';
    
    if (typeof Swal !== 'undefined') {
      Swal.fire({
        title: 'Ошибка',
        text: message,
        icon: 'error',
        customClass: {
          confirmButton: 'btn btn-primary waves-effect waves-light'
        },
        buttonsStyling: false
      });
    } else {
      alert(message);
    }
  }
});
