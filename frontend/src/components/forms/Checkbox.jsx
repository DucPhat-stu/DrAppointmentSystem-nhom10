import styles from './Checkbox.module.css';

export default function Checkbox({ label, checked, onChange, id, name, disabled = false }) {
  return (
    <label className={`${styles.checkbox} ${disabled ? styles.disabled : ''}`} htmlFor={id}>
      <input
        id={id}
        name={name}
        type="checkbox"
        className={styles.input}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
      />
      <span className={styles.box}>
        <svg
          className={styles.check}
          width="12"
          height="12"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="3"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <polyline points="20 6 9 17 4 12" />
        </svg>
      </span>
      {label && <span className={styles.label}>{label}</span>}
    </label>
  );
}
