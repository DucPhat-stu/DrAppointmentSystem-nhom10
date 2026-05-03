import styles from './ProfileSection.module.css';

export default function ProfileSection({ title, icon, variant = 'editable', action, children }) {
  return (
    <section className={`${styles.section} ${styles[variant]}`}>
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          {icon && <div className={styles.icon}>{icon}</div>}
          <h2 className={styles.title}>{title}</h2>
          <span className={`${styles.badge} ${styles[`badge_${variant}`]}`}>
            {variant === 'editable' ? 'Editable' : 'Read-only'}
          </span>
        </div>
        {action && <div className={styles.headerAction}>{action}</div>}
      </div>
      <div className={styles.body}>{children}</div>
    </section>
  );
}
