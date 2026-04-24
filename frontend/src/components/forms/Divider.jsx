import styles from './Divider.module.css';

export default function Divider({ text = 'or' }) {
  return (
    <div className={styles.divider}>
      <span className={styles.line} />
      <span className={styles.text}>{text}</span>
      <span className={styles.line} />
    </div>
  );
}
