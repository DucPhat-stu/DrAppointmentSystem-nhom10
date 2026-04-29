import { useEffect, useMemo, useState } from 'react';
import { aiTemplateService } from '../services/aiTemplateService.js';
import styles from './AdminPromptPage.module.css';

const emptyForm = {
  id: null,
  name: '',
  template: '',
  variablesText: 'symptoms, duration, description',
};

function toForm(template) {
  return {
    id: template.id,
    name: template.name ?? '',
    template: template.template ?? '',
    variablesText: (template.variables ?? []).join(', '),
  };
}

function toPayload(form) {
  return {
    name: form.name.trim(),
    template: form.template.trim(),
    variables: form.variablesText
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean),
  };
}

export default function AdminPromptPage() {
  const [templates, setTemplates] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);
  const [preview, setPreview] = useState('');

  const activeTemplate = useMemo(() => templates.find((template) => template.active), [templates]);

  const loadTemplates = async () => {
    setStatus('loading');
    setError(null);
    try {
      setTemplates(await aiTemplateService.list());
      setStatus('success');
    } catch (requestError) {
      setError(requestError.message || 'Khong the tai prompt templates.');
      setStatus('error');
    }
  };

  useEffect(() => {
    loadTemplates();
  }, []);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      if (form.id) {
        await aiTemplateService.update(form.id, toPayload(form));
      } else {
        await aiTemplateService.create(toPayload(form));
      }
      setForm(emptyForm);
      await loadTemplates();
    } catch (requestError) {
      setError(requestError.message || 'Khong the luu prompt template.');
    }
  };

  const handleActivate = async (id) => {
    setError(null);
    try {
      await aiTemplateService.activate(id);
      await loadTemplates();
    } catch (requestError) {
      setError(requestError.message || 'Khong the activate template.');
    }
  };

  const handlePreview = async () => {
    setError(null);
    try {
      setPreview(await aiTemplateService.preview({
        template: form.template,
        symptoms: 'Ho khan, sot cao',
        duration: 'ONE_TO_THREE_DAYS',
        description: 'Met moi va mat ngu',
      }));
    } catch (requestError) {
      setError(requestError.message || 'Khong the preview prompt template.');
    }
  };

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Admin</p>
          <h1 className={styles.title}>Prompt templates</h1>
          <p className={styles.subtitle}>
            Active template: {activeTemplate?.name || 'Chua co'}
          </p>
        </div>
        <button type="button" className={styles.secondaryButton} onClick={() => setForm(emptyForm)}>
          New template
        </button>
      </section>

      {error && <div className={styles.alert}>{error}</div>}

      <section className={styles.layout}>
        <div className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Templates</h2>
            <span>{status === 'loading' ? 'Loading...' : `${templates.length} items`}</span>
          </div>
          <div className={styles.list}>
            {templates.map((template) => (
              <article key={template.id} className={styles.item}>
                <div>
                  <strong>{template.name}</strong>
                  <p>v{template.version} · {template.active ? 'Active' : 'Inactive'}</p>
                </div>
                <div className={styles.rowActions}>
                  <button type="button" onClick={() => setForm(toForm(template))}>
                    Edit
                  </button>
                  <button type="button" disabled={template.active} onClick={() => handleActivate(template.id)}>
                    Activate
                  </button>
                </div>
              </article>
            ))}
          </div>
        </div>

        <form className={styles.panel} onSubmit={handleSubmit}>
          <div className={styles.panelHeader}>
            <h2>{form.id ? 'Edit template' : 'Create template'}</h2>
          </div>
          <label className={styles.field}>
            <span>Name</span>
            <input value={form.name} onChange={(event) => updateField('name', event.target.value)} required />
          </label>
          <label className={styles.field}>
            <span>Variables</span>
            <input
              value={form.variablesText}
              onChange={(event) => updateField('variablesText', event.target.value)}
              placeholder="symptoms, duration, description"
              required
            />
          </label>
          <label className={styles.field}>
            <span>Template</span>
            <textarea
              value={form.template}
              onChange={(event) => updateField('template', event.target.value)}
              placeholder="Symptoms: {{symptoms}}"
              required
            />
          </label>
          {preview && <pre className={styles.preview}>{preview}</pre>}
          <div className={styles.actions}>
            <button className={styles.secondaryButton} type="button" onClick={handlePreview}>
              Preview
            </button>
            <button className={styles.primaryButton} type="submit">
              Save template
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}
