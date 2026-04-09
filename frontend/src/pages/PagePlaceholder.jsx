import styles from './PagePlaceholder.module.css';

export default function PagePlaceholder({ eyebrow, title, description }) {
  return (
    <section className={styles.card}>
      <p className={styles.eyebrow}>{eyebrow}</p>
      <h1 className={styles.title}>{title}</h1>
      <p className={styles.description}>{description}</p>
    </section>
  );
}

