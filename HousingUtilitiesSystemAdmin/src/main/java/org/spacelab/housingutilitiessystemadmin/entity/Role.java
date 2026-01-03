package org.spacelab.housingutilitiessystemadmin.entity;

/**
 * Перечисление ролей пользователей в системе
 */
public enum Role {
    ADMIN, // Администратор с полными правами
    MANAGER, // Менеджер с расширенными правами
    USER, // Обычный пользователь
    SUPPORT, // Служба поддержки
    RESTRICTED, // Пользователь с ограниченными правами (только просмотр)
    CHAIRMAN // Глава ОСМД
}
