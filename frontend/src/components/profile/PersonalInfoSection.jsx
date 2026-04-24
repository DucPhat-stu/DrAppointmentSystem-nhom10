import { useState } from 'react';
import ProfileSection from './ProfileSection.jsx';
import InfoRow from './InfoRow.jsx';
import styles from './PersonalInfoSection.module.css';

const GENDER_LABELS = {
  MALE: 'Nam',
  FEMALE: 'Nữ',
  OTHER: 'Khác',
};

/**
 * Personal information section with view/edit toggle.
 */
export default function PersonalInfoSection({ profile, onSave, saving }) {
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [errors, setErrors] = useState({});

  function startEdit() {
    setForm({
      fullName: profile?.fullName ?? '',
      phone: profile?.phone ?? '',
      address: profile?.address ?? '',
      dateOfBirth: profile?.dateOfBirth ?? '',
      gender: profile?.gender ?? '',
      emergencyContact: profile?.emergencyContact ?? '',
    });
    setErrors({});
    setEditing(true);
  }

  function cancelEdit() {
    setEditing(false);
    setErrors({});
  }

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
    // Clear error when user types
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: null }));
    }
  }

  function validate() {
    const errs = {};
    if (!form.fullName?.trim()) errs.fullName = 'Họ tên không được để trống';
    if (form.phone && !/^\d{10,11}$/.test(form.phone)) errs.phone = 'Số điện thoại phải là 10-11 chữ số';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  async function handleSave() {
    if (!validate()) return;
    await onSave?.(form);
    setEditing(false);
  }

  const icon = (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>
  );

  const actionButton = editing ? (
    <div className={styles.actions}>
      <button className={styles.cancelBtn} onClick={cancelEdit} disabled={saving} type="button">
        Hủy
      </button>
      <button className={styles.saveBtn} onClick={handleSave} disabled={saving} type="button">
        {saving ? (
          <span className={styles.spinner} />
        ) : (
          'Lưu'
        )}
      </button>
    </div>
  ) : (
    <button className={styles.editBtn} onClick={startEdit} type="button">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
      </svg>
      Chỉnh sửa
    </button>
  );

  return (
    <ProfileSection title="Thông tin cá nhân" icon={icon} variant="editable" action={actionButton}>
      <div className={`${styles.grid} ${editing ? styles.editingGrid : ''}`}>
        <InfoRow
          label="Họ và tên"
          value={editing ? form.fullName : profile?.fullName}
          editing={editing}
          onChange={(v) => updateField('fullName', v)}
          error={errors.fullName}
          placeholder="Nhập họ và tên"
        />
        <InfoRow
          label="Email"
          value={profile?.email}
          editing={editing}
          disabled
          type="email"
        />
        <InfoRow
          label="Điện thoại"
          value={editing ? form.phone : profile?.phone}
          editing={editing}
          onChange={(v) => updateField('phone', v)}
          error={errors.phone}
          type="tel"
          placeholder="0901234567"
        />
        <InfoRow
          label="Địa chỉ"
          value={editing ? form.address : profile?.address}
          editing={editing}
          onChange={(v) => updateField('address', v)}
          placeholder="Nhập địa chỉ"
        />
        <InfoRow
          label="Ngày sinh"
          value={editing ? form.dateOfBirth : profile?.dateOfBirth}
          editing={editing}
          onChange={(v) => updateField('dateOfBirth', v)}
          type="date"
        />
        <InfoRow
          label="Giới tính"
          value={editing ? form.gender : GENDER_LABELS[profile?.gender] ?? profile?.gender}
          editing={editing}
          onChange={(v) => updateField('gender', v)}
          placeholder="MALE / FEMALE / OTHER"
        />
        <InfoRow
          label="Liên hệ khẩn cấp"
          value={editing ? form.emergencyContact : profile?.emergencyContact}
          editing={editing}
          onChange={(v) => updateField('emergencyContact', v)}
          placeholder="Số điện thoại người thân"
        />
      </div>
    </ProfileSection>
  );
}
