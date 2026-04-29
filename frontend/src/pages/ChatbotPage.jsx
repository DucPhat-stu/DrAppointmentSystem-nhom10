import { useEffect, useRef, useState } from 'react';
import { chatService } from '../services/chatService.js';
import styles from './ChatbotPage.module.css';

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
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages, loading]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    const text = inputText.trim();
    if (!text) {
      setError('Vui long nhap trieu chung cua ban.');
      return;
    }

    setMessages((current) => [...current, createMessage('user', text)]);
    setInputText('');
    setError(null);
    setLoading(true);

    try {
      const result = await chatService.checkSymptoms(text);
      setMessages((current) => [...current, createMessage('assistant', result)]);
    } catch (requestError) {
      setError(requestError.message || 'Co loi xay ra, vui long thu lai.');
    } finally {
      setLoading(false);
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
              <p>Dang xu ly...</p>
            </article>
          )}
          <div ref={messagesEndRef} />
        </div>

        {error && <div className={styles.errorBox}>{error}</div>}

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
