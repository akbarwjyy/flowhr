import React, { useState } from 'react';

interface LeaveFormProps {
    onSuccess?: () => void;
}

const leaveTypes = [
    { value: 'ANNUAL', label: 'Cuti Tahunan' },
    { value: 'SICK', label: 'Sakit' },
    { value: 'MATERNITY', label: 'Melahirkan' },
    { value: 'PATERNITY', label: 'Cuti Ayah' },
    { value: 'MARRIAGE', label: 'Pernikahan' },
    { value: 'BEREAVEMENT', label: 'Duka Cita' },
    { value: 'UNPAID', label: 'Tanpa Bayar' },
];

export default function LeaveForm({ onSuccess }: LeaveFormProps) {
    const [form, setForm] = useState({
        leaveType: 'ANNUAL',
        startDate: '',
        endDate: '',
        reason: '',
        attachmentUrl: '',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const API = (import.meta as any).env?.PUBLIC_API_URL || 'http://localhost:8080/api/v1';

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess('');

        try {
            const token = localStorage.getItem('flowhr_token');
            const res = await fetch(`${API}/leaves/apply`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(form),
            });
            const data = await res.json();
            if (!data.success) throw new Error(data.message);
            setSuccess('Pengajuan cuti berhasil dikirim!');
            setForm({ leaveType: 'ANNUAL', startDate: '', endDate: '', reason: '', attachmentUrl: '' });
            onSuccess?.();
        } catch (err: any) {
            setError(err.message || 'Gagal mengajukan cuti');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div className="form-group">
                <label className="form-label">Jenis Cuti</label>
                <select
                    className="form-select"
                    value={form.leaveType}
                    onChange={(e) => setForm({ ...form, leaveType: e.target.value })}
                >
                    {leaveTypes.map((t) => (
                        <option key={t.value} value={t.value}>{t.label}</option>
                    ))}
                </select>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="form-group">
                    <label className="form-label">Tanggal Mulai</label>
                    <input
                        type="date"
                        className="form-input"
                        value={form.startDate}
                        onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <label className="form-label">Tanggal Selesai</label>
                    <input
                        type="date"
                        className="form-input"
                        value={form.endDate}
                        onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                        required
                    />
                </div>
            </div>

            <div className="form-group">
                <label className="form-label">Alasan Cuti</label>
                <textarea
                    className="form-input"
                    rows={3}
                    value={form.reason}
                    onChange={(e) => setForm({ ...form, reason: e.target.value })}
                    placeholder="Jelaskan alasan pengajuan cuti..."
                    required
                    style={{ resize: 'vertical', minHeight: '80px' }}
                />
            </div>

            <div className="form-group">
                <label className="form-label">Lampiran URL (opsional)</label>
                <input
                    type="url"
                    className="form-input"
                    value={form.attachmentUrl}
                    onChange={(e) => setForm({ ...form, attachmentUrl: e.target.value })}
                    placeholder="https://..."
                />
            </div>

            {error && (
                <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: '0px', padding: '12px', fontSize: '13px', color: '#FCA5A5' }}>
                    {error}
                </div>
            )}
            {success && (
                <div style={{ background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.3)', borderRadius: '0px', padding: '12px', fontSize: '13px', color: '#6EE7B7' }}>
                    {success}
                </div>
            )}

            <button
                type="submit"
                disabled={loading}
                style={{
                    padding: '12px', background: loading ? 'var(--border)' : 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
                    border: 'none', borderRadius: '10px', color: 'white',
                    fontSize: '14px', fontWeight: '600', cursor: loading ? 'not-allowed' : 'pointer',
                    transition: 'opacity 0.2s',
                }}
            >
                {loading ? 'Mengirim...' : 'Ajukan Cuti'}
            </button>

            <style>{`
        .form-label { display: block; font-size: 13px; font-weight: 500; color: #CBD5E1; margin-bottom: 6px; }
        .form-input, .form-select {
          width: 100%; padding: 10px 14px;
          background: var(--bg); border: 1px solid var(--border); border-radius: 0px;
          color: #F1F5F9; font-size: 14px; font-family: inherit;
          transition: border-color 0.2s; outline: none;
        }
        .form-input:focus, .form-select:focus { border-color: var(--primary); }
        .form-select option { background: var(--surface); }
      `}</style>
        </form>
    );
}
