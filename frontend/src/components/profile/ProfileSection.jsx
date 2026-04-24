import styles from './ProfileSection.module.css';

/**
 * Reusable section wrapper for profile page.
 * @param {string} title - Section title
 * @param {React.ReactNode} icon - Section icon
 * @param {'editable'|'readonly'} variant - Visual variant
 * @param {React.ReactNode} action - Optional action button
 * @param {React.ReactNode} children - Section content
 */
export default function ProfileSection({ title, icon, variant = 'editable', action, children }) {
  return (
    <section className={`${styles.section} ${styles[variant]}`}>
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          {icon && <div className={styles.icon}>{icon}</div>}
          <h2 className={styles.title}>{title}</h2>
          <span className={`${styles.badge} ${styles[`badge_${variant}`]}`}>
            {variant === 'editable' ? 'Chỉnh sửa được' : 'Chỉ đọc'}
          </span>
        </div>
        {action && <div className={styles.headerAction}>{action}</div>}
      </div>
      <div className={styles.body}>{children}</div>
    </section>
  );
}
