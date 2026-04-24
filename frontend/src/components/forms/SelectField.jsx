import { useState } from 'react';
import styles from './SelectField.module.css';

export default function SelectField({
  label,
  options = [],
  value,
  onChange,
  error = '',
  placeholder = 'Select...',
  id,
  name,
  required = false,
  disabled = false,
}) {
  const [isFocused, setIsFocused] = useState(false);

  const wrapperClass = [
    styles.wrapper,
    isFocused ? styles.focused : '',
    error ? styles.hasError : '',
    disabled ? styles.disabled : '',
  ].filter(Boolean).join(' ');

  return (
    <div className={styles.field}>
      {label && (
        <label className={styles.label} htmlFor={id}>
          {label}
          {required && <span className={styles.required}>*</span>}
        </label>
      )}

      <div className={wrapperClass}>
        <select
          id={id}
          name={name}
          className={`${styles.select} ${!value ? styles.placeholder : ''}`}
          value={value}
          onChange={onChange}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          required={required}
          disabled={disabled}
        >
          <option value="" disabled>{placeholder}</option>
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>

        <span className={styles.chevron}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </span>
      </div>

      {error && <p className={styles.error}>{error}</p>}
    </div>
  );
}
