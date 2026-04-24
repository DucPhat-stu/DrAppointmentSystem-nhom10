import { useState } from 'react';
import ProfileSection from './ProfileSection.jsx';
import MedicalRecordModal from './MedicalRecordModal.jsx';
import styles from './MedicalRecordList.module.css';

/**
 * Medical records list (read-only).
 */
export default function MedicalRecordList({ records, loading }) {
  const [selectedRecord, setSelectedRecord] = useState(null);

  const icon = (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
      <polyline points="14 2 14 8 20 8"/>
      <line x1="16" y1="13" x2="8" y2="13"/>
      <line x1="16" y1="17" x2="8" y2="17"/>
    </svg>
  );

  const readonlyLabel = (
    <span className={styles.readonlyTag}>
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
      </svg>
      Hồ sơ bệnh án chỉ đọc
    </span>
  );

  return (
    <>
      <ProfileSection title="Hồ sơ bệnh án" icon={icon} variant="readonly" action={readonlyLabel}>
        {loading ? (
          <div className={styles.skeleton}>
            {[1, 2, 3].map((i) => (
              <div key={i} className={styles.skeletonCard} />
            ))}
          </div>
        ) : !records || records.length === 0 ? (
          <div className={styles.empty}>
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            <p>Chưa có hồ sơ bệnh án nào</p>
          </div>
        ) : (
          <div className={styles.grid}>
            {records.map((record, i) => (
              <button
                key={record.id}
                className={styles.card}
                onClick={() => setSelectedRecord(record)}
                style={{ animationDelay: `${i * 60}ms` }}
                type="button"
              >
                <div className={styles.cardHeader}>
                  <span className={styles.recordCode}>#{record.recordCode}</span>
                  <span className={styles.department}>{record.department}</span>
                </div>
                <h4 className={styles.disease}>{record.diseaseSummary}</h4>
                <div className={styles.cardMeta}>
                  <span className={styles.doctor}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    {record.doctorName}
                  </span>
                  <span className={styles.date}>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                      <line x1="16" y1="2" x2="16" y2="6"/>
                      <line x1="8" y1="2" x2="8" y2="6"/>
                      <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                    {record.visitDate}
                  </span>
                </div>
              </button>
            ))}
          </div>
        )}
      </ProfileSection>

      {selectedRecord && (
        <MedicalRecordModal
          record={selectedRecord}
          onClose={() => setSelectedRecord(null)}
        />
      )}
    </>
  );
}
