import { useEffect, useState, useCallback } from 'react';
import PersonalInfoSection from '../components/profile/PersonalInfoSection.jsx';
import MedicalRecordList from '../components/profile/MedicalRecordList.jsx';
import {
  createCertification,
  deleteCertification,
  fetchCertifications,
  fetchMedicalRecords,
  fetchProfile,
  updateProfile,
  uploadAvatar,
} from '../services/userService.js';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [records, setRecords] = useState([]);
  const [certifications, setCertifications] = useState([]);
  const [certForm, setCertForm] = useState({ name: '', issuingAuthority: '', issueDate: '', expiryDate: '', documentUrl: '' });
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
      setError(err.message || 'Unable to load profile information');
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
    } catch {
      // Don't block the profile page if medical records fail to load
      setRecords([]);
    } finally {
      setLoadingRecords(false);
    }
  }, []);

  const loadCertifications = useCallback(async () => {
    try {
      const res = await fetchCertifications();
      setCertifications(res.data ?? []);
    } catch {
      setCertifications([]);
    }
  }, []);

  useEffect(() => {
    loadProfile();
    loadRecords();
    loadCertifications();
  }, [loadProfile, loadRecords, loadCertifications]);

  // Save profile
  async function handleSave(formData) {
    try {
      setSaving(true);
      const res = await updateProfile(formData);
      setProfile(res.data);
      showToast('success', 'Profile updated successfully!');
    } catch (err) {
      showToast('error', err.message || 'Failed to save, please try again.');
      throw err; // re-throw so PersonalInfoSection stays in edit mode
    } finally {
      setSaving(false);
    }
  }

  function showToast(type, message) {
    setToast({ type, message });
    setTimeout(() => setToast(null), 4000);
  }

  async function handleAvatarChange(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    try {
      setSaving(true);
      const res = await uploadAvatar(file);
      setProfile(res.data);
      showToast('success', 'Avatar updated successfully.');
    } catch (err) {
      showToast('error', err.message || 'Failed to upload avatar.');
    } finally {
      setSaving(false);
      event.target.value = '';
    }
  }

  async function handleCertSubmit(event) {
    event.preventDefault();
    try {
      setSaving(true);
      await createCertification({
        ...certForm,
        issueDate: certForm.issueDate || null,
        expiryDate: certForm.expiryDate || null,
      });
      setCertForm({ name: '', issuingAuthority: '', issueDate: '', expiryDate: '', documentUrl: '' });
      await loadCertifications();
      showToast('success', 'Certification added.');
    } catch (err) {
      showToast('error', err.message || 'Failed to save certification.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteCertification(id) {
    try {
      setSaving(true);
      await deleteCertification(id);
      await loadCertifications();
      showToast('success', 'Certification deleted.');
    } catch (err) {
      showToast('error', err.message || 'Failed to delete certification.');
    } finally {
      setSaving(false);
    }
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
          <h3>Unable to load profile</h3>
          <p>{error}</p>
          <button className={styles.retryBtn} onClick={loadProfile} type="button">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Page Header */}
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>My Profile</h1>
        <p className={styles.pageSubtitle}>
          Manage personal information and view medical history
        </p>
      </div>

      {/* Sections */}
      <div className={styles.sections}>
        <section className={styles.profileTools}>
          <div className={styles.avatarPanel}>
            <div className={styles.avatarPreview}>
              {profile?.avatarUrl ? <img src={profile.avatarUrl} alt="" /> : <span>{profile?.fullName?.charAt(0) ?? 'U'}</span>}
            </div>
            <label className={styles.uploadButton}>
              Upload avatar
              <input type="file" accept="image/*" onChange={handleAvatarChange} disabled={saving} />
            </label>
          </div>
        </section>

        <PersonalInfoSection
          profile={profile}
          onSave={handleSave}
          saving={saving}
        />

        <section className={styles.certSection}>
          <div className={styles.certHeader}>
            <div>
              <h2>Professional Profile</h2>
              <p>Manage certifications and professional licenses for doctors.</p>
            </div>
          </div>
          <form className={styles.certForm} onSubmit={handleCertSubmit}>
            <input placeholder="Certification Name" value={certForm.name} onChange={(e) => setCertForm((f) => ({ ...f, name: e.target.value }))} required />
            <input placeholder="Issuing Authority" value={certForm.issuingAuthority} onChange={(e) => setCertForm((f) => ({ ...f, issuingAuthority: e.target.value }))} />
            <input type="date" value={certForm.issueDate} onChange={(e) => setCertForm((f) => ({ ...f, issueDate: e.target.value }))} />
            <input type="date" value={certForm.expiryDate} onChange={(e) => setCertForm((f) => ({ ...f, expiryDate: e.target.value }))} />
            <input placeholder="Document URL" value={certForm.documentUrl} onChange={(e) => setCertForm((f) => ({ ...f, documentUrl: e.target.value }))} />
            <button type="submit" disabled={saving}>Add Certification</button>
          </form>
          <div className={styles.certList}>
            {certifications.length === 0 ? (
              <p>No certifications yet.</p>
            ) : certifications.map((cert) => (
              <article key={cert.id} className={styles.certItem}>
                <div>
                  <strong>{cert.name}</strong>
                  <span>{cert.issuingAuthority || 'Unknown issuing authority'}</span>
                  <small>{cert.issueDate || '-'} to {cert.expiryDate || 'no expiry'}</small>
                </div>
                <button type="button" onClick={() => handleDeleteCertification(cert.id)} disabled={saving}>Delete</button>
              </article>
            ))}
          </div>
        </section>

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
