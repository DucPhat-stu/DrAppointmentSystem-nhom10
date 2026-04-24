import styles from './InfoRow.module.css';

/**
 * A single label-value row that toggles between view and edit mode.
 *
 * @param {string} label - Field label
 * @param {string} value - Current value
 * @param {boolean} editing - Whether in edit mode
 * @param {function} onChange - Change handler for edit mode
 * @param {string} type - Input type (text, email, tel, date)
 * @param {boolean} disabled - Whether field is always disabled (e.g. email)
 * @param {string} error - Validation error message
 * @param {string} placeholder - Input placeholder
 */
export default function InfoRow({
  label,
  value,
  editing = false,
  onChange,
  type = 'text',
  disabled = false,
  error,
  placeholder,
}) {
  return (
    <div className={`${styles.row} ${editing ? styles.editing : ''} ${error ? styles.hasError : ''}`}>
      <label className={styles.label}>{label}</label>
      {editing && !disabled ? (
        <div className={styles.inputWrap}>
          <input
            className={styles.input}
            type={type}
            value={value ?? ''}
            onChange={(e) => onChange?.(e.target.value)}
            placeholder={placeholder}
            aria-label={label}
          />
          {error && <span className={styles.error}>{error}</span>}
        </div>
      ) : (
        <span className={`${styles.value} ${disabled && editing ? styles.disabled : ''}`}>
          {value || '—'}
        </span>
      )}
    </div>
  );
}
