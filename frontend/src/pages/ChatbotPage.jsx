import { useEffect, useRef, useState } from 'react';
import { ApiError } from '../services/httpClient.js';
import { chatService } from '../services/chatService.js';
import styles from './ChatbotPage.module.css';

const COOLDOWN_MS = 2000;
const DURATION_OPTIONS = [
  { value: '', label: 'Chon thoi gian' },
  { value: 'LESS_THAN_ONE_DAY', label: 'Duoi 1 ngay' },
  { value: 'ONE_TO_THREE_DAYS', label: '1-3 ngay' },
  { value: 'MORE_THAN_THREE_DAYS', label: 'Hon 3 ngay' },
];

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
  const [mode, setMode] = useState('text');
  const [symptoms, setSymptoms] = useState('');
  const [duration, setDuration] = useState('');
  const [description, setDescription] = useState('');
  const [promptPreview, setPromptPreview] = useState('');
  const [history, setHistory] = useState([]);
  const [doctorRecommendations, setDoctorRecommendations] = useState([]);
  const [imageResult, setImageResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [timeoutWarning, setTimeoutWarning] = useState(false);
  const [lastRequestAt, setLastRequestAt] = useState(0);
  const [retryText, setRetryText] = useState(null);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages, loading]);

  useEffect(() => {
    chatService.fetchHistory()
      .then(setHistory)
      .catch(() => setHistory([]));
  }, []);

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
      chatService.fetchHistory().then(setHistory).catch(() => {});
    } catch (requestError) {
      setRetryText(normalized);
      setError(errorMessage(requestError));
    } finally {
      window.clearTimeout(warningTimer);
      setTimeoutWarning(false);
      setLoading(false);
    }
  };

  const sendStructuredMessage = async (payload) => {
    const userSummary = [
      `Trieu chung: ${payload.symptoms}`,
      `Thoi gian: ${DURATION_OPTIONS.find((option) => option.value === payload.duration)?.label}`,
      payload.description ? `Mo ta them: ${payload.description}` : null,
    ].filter(Boolean).join('. ');

    const now = Date.now();
    if (now - lastRequestAt < COOLDOWN_MS) {
      setError('Vui long doi 2 giay truoc khi gui tiep.');
      return;
    }

    setMessages((current) => [...current, createMessage('user', userSummary)]);
    setError(null);
    setTimeoutWarning(false);
    setRetryText(null);
    setLoading(true);
    setLastRequestAt(now);

    const warningTimer = window.setTimeout(() => {
      setTimeoutWarning(true);
    }, 3000);

    try {
      const result = await chatService.checkStructuredSymptoms(payload);
      setMessages((current) => [...current, createMessage('assistant', result)]);
      chatService.fetchHistory().then(setHistory).catch(() => {});
      setSymptoms('');
      setDuration('');
      setDescription('');
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      window.clearTimeout(warningTimer);
      setTimeoutWarning(false);
      setLoading(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (mode === 'structured') {
      if (!symptoms.trim() || !duration) {
        setError('Vui long nhap trieu chung va thoi gian xuat hien.');
        return;
      }
      await sendStructuredMessage({
        symptoms: symptoms.trim(),
        duration,
        description: description.trim(),
      });
      return;
    }

    await sendMessage(inputText);
  };

  const handleRetry = async () => {
    if (retryText) {
      await sendMessage(retryText, { retry: true });
    }
  };

  const handlePreviewPrompt = async () => {
    if (!symptoms.trim() || !duration) {
      setError('Vui long nhap trieu chung va thoi gian truoc khi xem preview.');
      return;
    }

    try {
      setError(null);
      const preview = await chatService.previewStructuredPrompt({
        symptoms: symptoms.trim(),
        duration,
        description: description.trim(),
      });
      setPromptPreview(preview);
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  const handleFeedback = async (rating) => {
    const latestAssistant = history
      .flatMap((conversation) => conversation.messages ?? [])
      .filter((message) => message.role === 'ASSISTANT')
      .at(-1);
    if (!latestAssistant?.id) {
      setError('Chua co cau tra loi AI nao trong lich su de danh gia.');
      return;
    }
    try {
      await chatService.sendFeedback({ messageId: latestAssistant.id, rating });
      setError(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  const handleDoctorRecommendation = async () => {
    const source = mode === 'text' ? inputText : symptoms;
    if (!source.trim()) {
      setError('Nhap trieu chung truoc khi goi y bac si.');
      return;
    }
    try {
      const result = await chatService.recommendDoctors(source.trim());
      setDoctorRecommendations(result);
      setError(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  const handleImageAnalysis = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    try {
      const result = await chatService.analyzeImage(file);
      setImageResult(result);
      setError(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      event.target.value = '';
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

      <section className={styles.toolPanel}>
        <article className={styles.toolCard}>
          <h2>Lich su hoi thoai</h2>
          <ul className={styles.historyList}>
            {history.length === 0 ? <li>Chua co lich su.</li> : history.map((conversation) => (
              <li key={conversation.id}>{conversation.title}</li>
            ))}
          </ul>
          <div className={styles.feedbackRow}>
            <button type="button" onClick={() => handleFeedback(5)}>Huu ich</button>
            <button type="button" onClick={() => handleFeedback(1)}>Chua dung</button>
          </div>
        </article>

        <article className={styles.toolCard}>
          <h2>Goi y bac si</h2>
          <button type="button" onClick={handleDoctorRecommendation}>Tim bac si phu hop</button>
          {doctorRecommendations.map((doctor) => (
            <p key={`${doctor.doctorName}-${doctor.specialty}`}>{doctor.doctorName} - {doctor.specialty} ({Math.round(doctor.matchScore * 100)}%)</p>
          ))}
        </article>

        <article className={styles.toolCard}>
          <h2>Phan tich hinh anh</h2>
          <label>
            Upload image
            <input type="file" accept="image/*" onChange={handleImageAnalysis} />
          </label>
          {imageResult && <p>{imageResult.finding} Confidence {Math.round(imageResult.confidence * 100)}%</p>}
        </article>
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
        {promptPreview && mode === 'structured' && (
          <pre className={styles.previewBox}>{promptPreview}</pre>
        )}

        <form className={styles.inputBar} onSubmit={handleSubmit}>
          <div className={styles.composer}>
            <div className={styles.modeSwitch} aria-label="Chat input mode">
              <button
                type="button"
                className={mode === 'text' ? styles.activeMode : ''}
                onClick={() => setMode('text')}
                disabled={loading}
              >
                Text
              </button>
              <button
                type="button"
                className={mode === 'structured' ? styles.activeMode : ''}
                onClick={() => setMode('structured')}
                disabled={loading}
              >
                Form
              </button>
            </div>

            {mode === 'text' ? (
              <>
                <label className={styles.inputLabel} htmlFor="symptomsText">
                  Trieu chung
                </label>
                <textarea
                  id="symptomsText"
                  value={inputText}
                  onChange={(event) => setInputText(event.target.value)}
                  placeholder="Mo ta trieu chung cua ban..."
                  maxLength={500}
                  disabled={loading}
                />
              </>
            ) : (
              <div className={styles.structuredGrid}>
                <label>
                  <span>Trieu chung</span>
                  <input
                    value={symptoms}
                    onChange={(event) => setSymptoms(event.target.value)}
                    placeholder="Vi du: ho, sot, dau dau"
                    maxLength={180}
                    disabled={loading}
                  />
                </label>
                <label>
                  <span>Thoi gian</span>
                  <select value={duration} onChange={(event) => setDuration(event.target.value)} disabled={loading}>
                    {DURATION_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>
                <label className={styles.fullField}>
                  <span>Mo ta them</span>
                  <textarea
                    value={description}
                    onChange={(event) => setDescription(event.target.value)}
                    placeholder="Tinh trang, muc do dau, thuoc da dung..."
                    maxLength={300}
                    disabled={loading}
                  />
                </label>
                <button
                  className={styles.previewButton}
                  type="button"
                  onClick={handlePreviewPrompt}
                  disabled={loading || !symptoms.trim() || !duration}
                >
                  Preview prompt
                </button>
              </div>
            )}
          </div>
          <button
            type="submit"
            disabled={loading || (mode === 'text' ? !inputText.trim() : !symptoms.trim() || !duration)}
          >
            {loading ? 'Dang xu ly' : 'Gui'}
          </button>
        </form>
      </section>
    </main>
  );
}
