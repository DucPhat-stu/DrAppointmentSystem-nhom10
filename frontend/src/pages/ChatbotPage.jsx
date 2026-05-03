import { useEffect, useRef, useState } from 'react';
import { ApiError } from '../services/httpClient.js';
import { chatService } from '../services/chatService.js';
import styles from './ChatbotPage.module.css';

const COOLDOWN_MS = 2000;
const DURATION_OPTIONS = [
  { value: '', label: 'Select a timeframe' },
  { value: 'LESS_THAN_ONE_DAY', label: 'Less than 1 day' },
  { value: 'ONE_TO_THREE_DAYS', label: '1-3 days' },
  { value: 'MORE_THAN_THREE_DAYS', label: 'More than 3 days' },
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
    createMessage('assistant', 'Describe your symptoms, I\'ll suggest relevant information and suitable specialties.'),
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
  const [followUp, setFollowUp] = useState(null);
  const [waitTime, setWaitTime] = useState(null);
  const [trends, setTrends] = useState([]);
  const [riskAlert, setRiskAlert] = useState(null);
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
        return 'Please enter symptoms between 5 and 500 characters.';
      }
      if (requestError.status === 500) {
        return 'Server error, please contact support.';
      }
      if (requestError.status === 503) {
        return 'AI service is temporarily busy, please try again later.';
      }
    }

    if (requestError?.name === 'TypeError') {
      return 'Check your internet connection or AI service status.';
    }

    return requestError?.message || 'An error occurred, please try again.';
  };

  const sendMessage = async (text, options = {}) => {
    const normalized = text.trim();
    if (normalized.length < 5) {
      setError('Please enter at least 5 characters for symptoms.');
      return;
    }

    const now = Date.now();
    if (!options.retry && now - lastRequestAt < COOLDOWN_MS) {
      setError('Please wait 2 seconds before sending again.');
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
      `Symptoms: ${payload.symptoms}`,
      `Duration: ${DURATION_OPTIONS.find((option) => option.value === payload.duration)?.label}`,
      payload.description ? `Additional description: ${payload.description}` : null,
    ].filter(Boolean).join('. ');

    const now = Date.now();
    if (now - lastRequestAt < COOLDOWN_MS) {
      setError('Please wait 2 seconds before sending again.');
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
        setError('Please enter symptoms and duration.');
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
      setError('Please enter symptoms and duration before previewing.');
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
      setError('No AI responses in history to rate.');
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
      setError('Enter symptoms before recommending doctors.');
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

  const currentSymptomText = () => (mode === 'text' ? inputText : symptoms).trim();

  const handleFollowUp = async () => {
    const source = currentSymptomText() || 'general symptoms';
    try {
      setFollowUp(await chatService.suggestFollowUp(source));
      setError(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  const handleWaitTime = async () => {
    try {
      setWaitTime(await chatService.predictWaitTime({ department: 'General Medicine' }));
      setError(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  const handleTrends = async () => {
    try {
      setTrends(await chatService.fetchDiseaseTrends());
      setError(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  const handleRisk = async () => {
    const source = currentSymptomText();
    if (!source) {
      setError('Enter symptoms before creating risk alert.');
      return;
    }
    try {
      setRiskAlert(await chatService.healthRiskAlerts({ symptoms: source }));
      setError(null);
    } catch (requestError) {
      setError(errorMessage(requestError));
    }
  };

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.eyebrow}>AI symptom assistant</p>
          <h1 className={styles.title}>Health Chat</h1>
          <p className={styles.subtitle}>
            Results are for reference only and cannot replace medical diagnosis.
          </p>
        </div>
      </section>

      <section className={styles.toolPanel}>
        <article className={styles.toolCard}>
          <h2>Chat History</h2>
          <ul className={styles.historyList}>
            {history.length === 0 ? <li>No history.</li> : history.map((conversation) => (
              <li key={conversation.id}>{conversation.title}</li>
            ))}
          </ul>
          <div className={styles.feedbackRow}>
            <button type="button" onClick={() => handleFeedback(5)}>Helpful</button>
            <button type="button" onClick={() => handleFeedback(1)}>Not accurate</button>
          </div>
        </article>

        <article className={styles.toolCard}>
          <h2>Doctor Recommendations</h2>
          <button type="button" onClick={handleDoctorRecommendation}>Find suitable doctors</button>
          {doctorRecommendations.map((doctor) => (
            <p key={`${doctor.doctorName}-${doctor.specialty}`}>{doctor.doctorName} - {doctor.specialty} ({Math.round(doctor.matchScore * 100)}%)</p>
          ))}
        </article>

        <article className={styles.toolCard}>
          <h2>Image Analysis</h2>
          <label>
            Upload Image
            <input type="file" accept="image/*" onChange={handleImageAnalysis} />
          </label>
          {imageResult && <p>{imageResult.finding} Confidence {Math.round(imageResult.confidence * 100)}%</p>}
        </article>

        <article className={styles.toolCard}>
          <h2>Follow-up</h2>
          <button type="button" onClick={handleFollowUp}>Suggest follow-up schedule</button>
          {followUp && <p>{followUp.recommendedWindow}: {followUp.reason}</p>}
        </article>

        <article className={styles.toolCard}>
          <h2>Wait Time</h2>
          <button type="button" onClick={handleWaitTime}>Predict</button>
          {waitTime && <p>{waitTime.estimatedMinutes} minutes - {waitTime.confidence}</p>}
        </article>

        <article className={styles.toolCard}>
          <h2>Risk Alert</h2>
          <button type="button" onClick={handleRisk}>Check risk</button>
          {riskAlert && <p>{riskAlert.level}: {riskAlert.nextStep}</p>}
        </article>

        <article className={`${styles.toolCard} ${styles.wideToolCard}`}>
          <h2>Disease Trends</h2>
          <button type="button" onClick={handleTrends}>Load trends dashboard</button>
          <div className={styles.trendGrid}>
            {trends.map((trend) => (
              <p key={trend.disease}>{trend.disease}: {trend.trend} ({trend.cases})</p>
            ))}
          </div>
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
                {message.role === 'user' ? 'You' : 'AI'}
              </span>
              <p>{message.text}</p>
            </article>
          ))}
          {loading && (
            <article className={`${styles.message} ${styles.aiMessage}`}>
              <span className={styles.messageLabel}>AI</span>
              <p>
                <span className={styles.spinner} aria-hidden="true" />
                Processing...
              </p>
            </article>
          )}
          <div ref={messagesEndRef} />
        </div>

        {timeoutWarning && <div className={styles.warningBox}>Request is taking longer than expected, please wait a moment.</div>}
        {error && (
          <div className={styles.errorBox}>
            <span>{error}</span>
            {retryText && !loading && (
              <button type="button" onClick={handleRetry}>
                Retry
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
                  Symptoms
                </label>
                <textarea
                  id="symptomsText"
                  value={inputText}
                  onChange={(event) => setInputText(event.target.value)}
                  placeholder="Describe your symptoms..."
                  maxLength={500}
                  disabled={loading}
                />
              </>
            ) : (
              <div className={styles.structuredGrid}>
                <label>
                  <span>Symptoms</span>
                  <input
                    value={symptoms}
                    onChange={(event) => setSymptoms(event.target.value)}
                    placeholder="E.g., cough, fever, headache"
                    maxLength={180}
                    disabled={loading}
                  />
                </label>
                <label>
                  <span>Duration</span>
                  <select value={duration} onChange={(event) => setDuration(event.target.value)} disabled={loading}>
                    {DURATION_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>
                <label className={styles.fullField}>
                  <span>Additional description</span>
                  <textarea
                    value={description}
                    onChange={(event) => setDescription(event.target.value)}
                    placeholder="Condition, pain level, medications used..."
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
            {loading ? 'Processing' : 'Send'}
          </button>
        </form>
      </section>
    </main>
  );
}
