import { useEffect } from 'react';
import styles from './MedicalRecordModal.module.css';

/**
 * Full-detail modal for a medical record (read-only).
 */
export default function MedicalRecordModal({ record, onClose }) {
  // Close on Escape
  useEffect(() => {
    function handleKey(e) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [onClose]);

  // Prevent body scroll
  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => { document.body.style.overflow = ''; };
  }, []);

  if (!record) return null;

  const fields = [
    { label: 'Mã bệnh án', value: `#${record.recordCode}` },
    { label: 'Ngày khám', value: record.visitDate },
    { label: 'Ngày đặt lịch', value: record.appointmentDate || '—' },
    { label: 'Check-in', value: record.checkinTime ? new Date(record.checkinTime).toLocaleString('vi-VN') : '—' },
    { label: 'Tên bệnh', value: record.diseaseSummary, highlight: true },
    { label: 'Khoa khám', value: record.department },
    { label: 'Bác sĩ', value: record.doctorName },
    { label: 'Toa thuốc', value: record.prescription || '—', long: true },
    { label: 'Xét nghiệm', value: record.tests?.length > 0 ? record.tests.join(', ') : '—' },
    { label: 'Ghi chú', value: record.notes || '—', long: true },
  ];

  return (
    <div className={styles.overlay} onClick={onClose} role="dialog" aria-modal="true">
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className={styles.header}>
          <div>
            <h3 className={styles.title}>Chi tiết bệnh án</h3>
            <p className={styles.subtitle}>#{record.recordCode} — {record.diseaseSummary}</p>
          </div>
          <button className={styles.closeBtn} onClick={onClose} type="button" aria-label="Đóng">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        {/* Readonly warning */}
        <div className={styles.warning}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
          Medical records are read-only
        </div>

        {/* Fields */}
        <div className={styles.fields}>
          {fields.map((field, i) => (
            <div
              key={i}
              className={`${styles.field} ${field.long ? styles.fieldLong : ''} ${field.highlight ? styles.fieldHighlight : ''}`}
            >
              <span className={styles.fieldLabel}>{field.label}</span>
              <span className={styles.fieldValue}>{field.value}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
