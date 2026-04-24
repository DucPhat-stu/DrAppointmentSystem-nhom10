import { useEffect, useState, useCallback } from 'react';
import PersonalInfoSection from '../components/profile/PersonalInfoSection.jsx';
import MedicalRecordList from '../components/profile/MedicalRecordList.jsx';
import { fetchProfile, updateProfile, fetchMedicalRecords } from '../services/userService.js';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [records, setRecords] = useState([]);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [loadingRecords, setLoadingRecords] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);

  // Load profile
  const loadProfile = useCallback(async () => {
    try {
      setLoadingProfile(true);
      const res = await fetchProfile();
      setProfile(res.data);
    } catch (err) {
      setError(err.message || 'Không thể tải thông tin cá nhân');
    } finally {
      setLoadingProfile(false);
    }
  }, []);

  // Load medical records
  const loadRecords = useCallback(async () => {
    try {
      setLoadingRecords(true);
      const res = await fetchMedicalRecords();
      setRecords(res.data ?? []);
    } catch (err) {
      // Don't block page for records error
      console.error('Failed to load records:', err);
      setRecords([]);
    } finally {
      setLoadingRecords(false);
    }
  }, []);

  useEffect(() => {
    loadProfile();
    loadRecords();
  }, [loadProfile, loadRecords]);

  // Save profile
  async function handleSave(formData) {
    try {
      setSaving(true);
      const res = await updateProfile(formData);
      setProfile(res.data);
      showToast('success', 'Cập nhật thông tin thành công!');
    } catch (err) {
      showToast('error', err.message || 'Lưu thất bại, vui lòng thử lại.');
      throw err; // re-throw so PersonalInfoSection stays in edit mode
    } finally {
      setSaving(false);
    }
  }

  function showToast(type, message) {
    setToast({ type, message });
    setTimeout(() => setToast(null), 4000);
  }

  if (loadingProfile) {
    return (
      <div className={styles.page}>
        <div className={styles.pageHeader}>
          <div className={styles.titleSkeleton} />
          <div className={styles.subtitleSkeleton} />
        </div>
        <div className={styles.sectionSkeleton} />
        <div className={styles.sectionSkeleton} />
      </div>
    );
  }

  if (error && !profile) {
    return (
      <div className={styles.page}>
        <div className={styles.errorState}>
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="15" y1="9" x2="9" y2="15"/>
            <line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
          <h3>Không thể tải hồ sơ</h3>
          <p>{error}</p>
          <button className={styles.retryBtn} onClick={loadProfile} type="button">
            Thử lại
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Page Header */}
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>Hồ sơ của tôi</h1>
        <p className={styles.pageSubtitle}>
          Quản lý thông tin cá nhân và xem lịch sử khám bệnh
        </p>
      </div>

      {/* Sections */}
      <div className={styles.sections}>
        <PersonalInfoSection
          profile={profile}
          onSave={handleSave}
          saving={saving}
        />

        <MedicalRecordList
          records={records}
          loading={loadingRecords}
        />
      </div>

      {/* Toast Notification */}
      {toast && (
        <div className={`${styles.toast} ${styles[`toast_${toast.type}`]}`}>
          {toast.type === 'success' ? (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          ) : (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          )}
          {toast.message}
        </div>
      )}
    </div>
  );
}
