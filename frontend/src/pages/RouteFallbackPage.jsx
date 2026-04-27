import { Link, useRouteError } from 'react-router-dom';
import styles from './RouteFallbackPage.module.css';

export default function RouteFallbackPage() {
  const error = useRouteError();
  const message = error?.message ?? 'This view is not available right now.';

  return (
    <main className={styles.page}>
      <section className={styles.panel}>
        <p className={styles.eyebrow}>System</p>
        <h1>Unable to load this page</h1>
        <p>{message}</p>
        <Link className={styles.button} to="/doctors">
          Go to doctors
        </Link>
      </section>
    </main>
  );
}
