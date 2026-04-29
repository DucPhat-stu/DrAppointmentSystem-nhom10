import { useEffect, useRef, useState } from 'react';
import { ApiError } from '../services/httpClient.js';
import { chatService } from '../services/chatService.js';
import styles from './ChatbotPage.module.css';

const COOLDOWN_MS = 2000;

function createMessage(role, text) {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    text,
  };
}

export default function ChatbotPage() {
  const [messages, setMessages] = useState([
    createMessage('assistant', 'Mo ta trieu chung cua ban, toi se goi y thong tin tham khao va chuyen khoa phu hop.'),
  ]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [timeoutWarning, setTimeoutWarning] = useState(false);
  const [lastRequestAt, setLastRequestAt] = useState(0);
  const [retryText, setRetryText] = useState(null);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages, loading]);

  const errorMessage = (requestError) => {
    if (requestError instanceof ApiError) {
      if (requestError.status === 400) {
        return 'Vui long nhap trieu chung tu 5 den 500 ky tu.';
      }
      if (requestError.status === 500) {
        return 'Loi server, vui long lien he ho tro.';
      }
      if (requestError.status === 503) {
        return 'AI dang tam thoi qua tai, vui long thu lai sau.';
      }
    }

    if (requestError?.name === 'TypeError') {
      return 'Kiem tra ket noi internet hoac trang thai ai-service.';
    }

    return requestError?.message || 'Co loi xay ra, vui long thu lai.';
  };

  const sendMessage = async (text, options = {}) => {
    const normalized = text.trim();
    if (normalized.length < 5) {
      setError('Vui long nhap trieu chung toi thieu 5 ky tu.');
      return;
    }

    const now = Date.now();
    if (!options.retry && now - lastRequestAt < COOLDOWN_MS) {
      setError('Vui long doi 2 giay truoc khi gui tiep.');
      return;
    }

    if (!options.retry) {
      setMessages((current) => [...current, createMessage('user', normalized)]);
      setInputText('');
    }
    setError(null);
    setTimeoutWarning(false);
    setRetryText(null);
    setLoading(true);
    setLastRequestAt(now);

    const warningTimer = window.setTimeout(() => {
      setTimeoutWarning(true);
    }, 3000);

    try {
      const result = await chatService.checkSymptoms(normalized);
      setMessages((current) => [...current, createMessage('assistant', result)]);
    } catch (requestError) {
      setRetryText(normalized);
      setError(errorMessage(requestError));
    } finally {
      window.clearTimeout(warningTimer);
      setTimeoutWarning(false);
      setLoading(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await sendMessage(inputText);
  };

  const handleRetry = async () => {
    if (retryText) {
      await sendMessage(retryText, { retry: true });
    }
  };

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.eyebrow}>AI symptom assistant</p>
          <h1 className={styles.title}>Chat suc khoe</h1>
          <p className={styles.subtitle}>
            Ket qua chi mang tinh tham khao va khong thay the chan doan y khoa.
          </p>
        </div>
      </section>

      <section className={styles.chatPanel} aria-label="AI chatbot">
        <div className={styles.messages}>
          {messages.map((message) => (
            <article
              key={message.id}
              className={`${styles.message} ${message.role === 'user' ? styles.userMessage : styles.aiMessage}`}
            >
              <span className={styles.messageLabel}>
                {message.role === 'user' ? 'Ban' : 'AI'}
              </span>
              <p>{message.text}</p>
            </article>
          ))}
          {loading && (
            <article className={`${styles.message} ${styles.aiMessage}`}>
              <span className={styles.messageLabel}>AI</span>
              <p>
                <span className={styles.spinner} aria-hidden="true" />
                Dang xu ly...
              </p>
            </article>
          )}
          <div ref={messagesEndRef} />
        </div>

        {timeoutWarning && <div className={styles.warningBox}>Yeu cau dang lau hon du kien, vui long doi them trong giay lat.</div>}
        {error && (
          <div className={styles.errorBox}>
            <span>{error}</span>
            {retryText && !loading && (
              <button type="button" onClick={handleRetry}>
                Thu lai
              </button>
            )}
          </div>
        )}

        <form className={styles.inputBar} onSubmit={handleSubmit}>
          <label className={styles.inputLabel} htmlFor="symptoms">
            Trieu chung
          </label>
          <textarea
            id="symptoms"
            value={inputText}
            onChange={(event) => setInputText(event.target.value)}
            placeholder="Mo ta trieu chung cua ban..."
            maxLength={500}
            disabled={loading}
          />
          <button type="submit" disabled={loading || !inputText.trim()}>
            {loading ? 'Dang xu ly' : 'Gui'}
          </button>
        </form>
      </section>
    </main>
  );
}
