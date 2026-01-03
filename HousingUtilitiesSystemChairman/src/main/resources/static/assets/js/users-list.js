/**
 * Users List Page
 */
'use strict';

document.addEventListener('DOMContentLoaded', function (e) {
  // Variables
  let currentPage = 0;
  let pageSize = 10;
  let currentFilters = {};

  // Elements
  const usersTable = document.getElementById('usersTable');
  const tbody = usersTable.querySelector('tbody');
  const pagination = document.getElementById('pagination');
  const infoElement = document.getElementById('usersTable_info');

  // Filter elements
  const filterElements = {
    lastName: document.getElementById('filter_lastName'),
    firstName: document.getElementById('filter_firstName'),
    phone: document.getElementById('filter_phone'),
    status: document.getElementById('filter_status')
  };

  // Buttons
  const applyFiltersBtn = document.getElementById('applyFilters');
  const clearFiltersBtn = document.getElementById('clearFilters');

  // Initialize
  init();

  function init() {
    setupEventListeners();
    loadUsers();
  }

  function setupEventListeners() {
    // Apply filters button
    applyFiltersBtn.addEventListener('click', function() {
      currentPage = 0; // Reset to first page when applying filters
      applyFilters();
    });

    // Clear filters button
    clearFiltersBtn.addEventListener('click', function() {
      clearFilters();
    });

    // Enter key on filter inputs
    Object.values(filterElements).forEach(element => {
      if (element && element.tagName === 'INPUT') {
        element.addEventListener('keypress', function(e) {
          if (e.key === 'Enter') {
            currentPage = 0;
            applyFilters();
          }
        });
      }
    });
  }

  function applyFilters() {
    // Collect filter values
    currentFilters = {};
    
    if (filterElements.lastName.value.trim()) {
      currentFilters.filter_lastName = filterElements.lastName.value.trim();
    }
    if (filterElements.firstName.value.trim()) {
      currentFilters.filter_firstName = filterElements.firstName.value.trim();
    }
    if (filterElements.phone.value.trim()) {
      currentFilters.filter_phone = filterElements.phone.value.trim();
    }
    if (filterElements.status.value) {
      currentFilters.filter_status = filterElements.status.value;
    }

    loadUsers();
  }

  function clearFilters() {
    // Clear filter inputs
    Object.values(filterElements).forEach(element => {
      if (element) {
        element.value = '';
      }
    });

    // Clear current filters and reload
    currentFilters = {};
    currentPage = 0;
    loadUsers();
  }

  async function loadUsers() {
    try {
      showLoading();

      // Build query parameters
      const params = new URLSearchParams({
        page: currentPage,
        size: pageSize,
        ...currentFilters
      });

      const response = await fetch((window.contextPath || '') + `users/getAllUsers?${params}`);
      
      if (response.ok) {
        const data = await response.json();
        renderTable(data.content);
        renderPagination(data);
        updateInfo(data);
      } else {
        throw new Error('Ошибка загрузки данных пользователей');
      }
    } catch (error) {
      console.error('Error loading users:', error);
      showError('Ошибка при загрузке списка пользователей');
    } finally {
      hideLoading();
    }
  }

  function renderTable(users) {
    tbody.innerHTML = '';

    if (users.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="11" class="text-center text-muted py-4">
            <i class="ti ti-users ti-lg mb-2"></i>
            <div>Пользователи не найдены</div>
          </td>
        </tr>
      `;
      return;
    }

    users.forEach(user => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td><small class="text-muted">${user.id || ''}</small></td>
        <td>
          <div class="d-flex align-items-center">
            <div class="avatar avatar-sm me-2">
              <img src="${user.photo ? ((window.contextPath || '') + '/' + user.photo) : ((window.contextPath || '') + '/assets/img/avatars/1.png')}" alt="Avatar" class="rounded-circle">
            </div>
            <div>
              <strong>${user.fullName || ''}</strong>
            </div>
          </div>
        </td>
        <td>${user.email || ''}</td>
        <td>${user.phone || ''}</td>
        <td>${user.city ? user.city.name : ''}</td>
        <td>${user.street ? user.street.name : ''}</td>
        <td>${user.houseNumber || ''}</td>
        <td>${user.apartmentNumber || ''}</td>
        <td>${user.accountNumber || ''}</td>
        <td>
          <span class="badge ${getStatusBadgeClass(user.status)}">
            ${getStatusText(user.status)}
          </span>
        </td>
        <td>
          <div class="dropdown">
            <button type="button" class="btn p-0 dropdown-toggle hide-arrow" data-bs-toggle="dropdown">
              <i class="ti ti-dots-vertical"></i>
            </button>
            <div class="dropdown-menu">
              <a class="dropdown-item" href="${(window.contextPath || '') + 'users/' + user.id}">
                <i class="ti ti-eye me-1"></i> Просмотр
              </a>
              <a class="dropdown-item" href="${(window.contextPath || '') + 'users/edit/' + user.id}">
                <i class="ti ti-edit me-1"></i> Редактировать
              </a>
              <button class="dropdown-item text-danger delete-user-btn" data-user-id="${user.id}" data-user-name="${user.fullName}">
                <i class="ti ti-trash me-1"></i> Удалить
              </button>
            </div>
          </div>
        </td>
      `;
      tbody.appendChild(row);
    });

    // Add delete event listeners
    document.querySelectorAll('.delete-user-btn').forEach(btn => {
      btn.addEventListener('click', function() {
        const userId = this.getAttribute('data-user-id');
        const userName = this.getAttribute('data-user-name');
        deleteUser(userId, userName);
      });
    });
  }

  function renderPagination(pageData) {
    pagination.innerHTML = '';

    const totalPages = pageData.totalPages;
    const currentPageNum = pageData.number;

    if (totalPages <= 1) return;

    // Previous button
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPageNum === 0 ? 'disabled' : ''}`;
    prevLi.innerHTML = `
      <a class="page-link" href="javascript:void(0);" data-page="${currentPageNum - 1}">
        <i class="ti ti-chevron-left ti-xs"></i>
      </a>
    `;
    pagination.appendChild(prevLi);

    // Page numbers
    const startPage = Math.max(0, currentPageNum - 2);
    const endPage = Math.min(totalPages - 1, currentPageNum + 2);

    if (startPage > 0) {
      const firstLi = document.createElement('li');
      firstLi.className = 'page-item';
      firstLi.innerHTML = `<a class="page-link" href="javascript:void(0);" data-page="0">1</a>`;
      pagination.appendChild(firstLi);

      if (startPage > 1) {
        const dotsLi = document.createElement('li');
        dotsLi.className = 'page-item disabled';
        dotsLi.innerHTML = `<span class="page-link">...</span>`;
        pagination.appendChild(dotsLi);
      }
    }

    for (let i = startPage; i <= endPage; i++) {
      const li = document.createElement('li');
      li.className = `page-item ${i === currentPageNum ? 'active' : ''}`;
      li.innerHTML = `<a class="page-link" href="javascript:void(0);" data-page="${i}">${i + 1}</a>`;
      pagination.appendChild(li);
    }

    if (endPage < totalPages - 1) {
      if (endPage < totalPages - 2) {
        const dotsLi = document.createElement('li');
        dotsLi.className = 'page-item disabled';
        dotsLi.innerHTML = `<span class="page-link">...</span>`;
        pagination.appendChild(dotsLi);
      }

      const lastLi = document.createElement('li');
      lastLi.className = 'page-item';
      lastLi.innerHTML = `<a class="page-link" href="javascript:void(0);" data-page="${totalPages - 1}">${totalPages}</a>`;
      pagination.appendChild(lastLi);
    }

    // Next button
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPageNum === totalPages - 1 ? 'disabled' : ''}`;
    nextLi.innerHTML = `
      <a class="page-link" href="javascript:void(0);" data-page="${currentPageNum + 1}">
        <i class="ti ti-chevron-right ti-xs"></i>
      </a>
    `;
    pagination.appendChild(nextLi);

    // Add click events to pagination links
    pagination.querySelectorAll('a.page-link').forEach(link => {
      link.addEventListener('click', function(e) {
        e.preventDefault();
        const page = parseInt(this.getAttribute('data-page'));
        if (page >= 0 && page < totalPages && page !== currentPageNum) {
          currentPage = page;
          loadUsers();
        }
      });
    });
  }

  function updateInfo(pageData) {
    const start = pageData.number * pageData.size + 1;
    const end = Math.min(start + pageData.size - 1, pageData.totalElements);
    const total = pageData.totalElements;

    infoElement.textContent = `Показано ${start} - ${end} из ${total} записей`;
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

  async function deleteUser(userId, userName) {
    if (typeof Swal !== 'undefined') {
      const result = await Swal.fire({
        title: 'Удалить пользователя?',
        text: `Вы действительно хотите удалить пользователя "${userName}"? Это действие нельзя отменить.`,
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
            });
            loadUsers(); // Reload the table
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
      if (confirm(`Удалить пользователя "${userName}"?`)) {
        try {
          const response = await fetch((window.contextPath || '') + `users/${userId}`, {
            method: 'DELETE'
          });

          if (response.ok) {
            alert('Пользователь удален успешно');
            loadUsers();
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
    tbody.innerHTML = `
      <tr>
        <td colspan="11" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Загрузка...</span>
          </div>
        </td>
      </tr>
    `;
  }

  function hideLoading() {
    // Loading will be hidden when table is rendered
  }

  function showError(message) {
    tbody.innerHTML = `
      <tr>
        <td colspan="11" class="text-center text-danger py-4">
          <i class="ti ti-alert-circle ti-lg mb-2"></i>
          <div>${message}</div>
        </td>
      </tr>
    `;
  }
});
