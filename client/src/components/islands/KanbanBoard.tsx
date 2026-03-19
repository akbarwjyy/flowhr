import React, { useState, useEffect } from 'react';

interface Applicant {
    id: number;
    first_name: string;
    last_name: string;
    email: string;
    stage: string;
    applied_at: string;
    current_company?: string;
}

interface Column {
    key: string;
    label: string;
    color: string;
}

const COLUMNS: Column[] = [
    { key: 'APPLIED', label: 'Lamaran Masuk', color: 'var(--primary)' },
    { key: 'HR_INTERVIEW', label: 'Interview HR', color: '#06B6D4' },
    { key: 'USER_INTERVIEW', label: 'Interview User', color: '#F59E0B' },
    { key: 'OFFERING', label: 'Offering', color: '#8B5CF6' },
    { key: 'HIRED', label: 'Diterima', color: '#10B981' },
];

interface KanbanBoardProps {
    jobId: number;
}

export default function KanbanBoard({ jobId }: KanbanBoardProps) {
    const [applicants, setApplicants] = useState<Applicant[]>([]);
    const [loading, setLoading] = useState(true);
    const [dragging, setDragging] = useState<number | null>(null);

    const API = (import.meta as any).env?.PUBLIC_API_URL || 'http://localhost:8080/api/v1';
    const token = localStorage.getItem('flowhr_token');

    useEffect(() => {
        fetchApplicants();
    }, [jobId]);

    async function fetchApplicants() {
        try {
            const res = await fetch(`${API}/ats/jobs/${jobId}/applicants`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            const data = await res.json();
            if (data.success) setApplicants(data.data);
        } finally {
            setLoading(false);
        }
    }

    async function moveApplicant(applicantId: number, newStage: string) {
        try {
            await fetch(`${API}/ats/applicants/${applicantId}/stage?stage=${newStage}`, {
                method: 'PUT',
                headers: { Authorization: `Bearer ${token}` },
            });
            setApplicants((prev) =>
                prev.map((a) => (a.id === applicantId ? { ...a, stage: newStage } : a))
            );
        } catch (e) {
            console.error('Move failed:', e);
        }
    }

    function handleDrop(e: React.DragEvent, targetStage: string) {
        e.preventDefault();
        if (dragging !== null) {
            moveApplicant(dragging, targetStage);
            setDragging(null);
        }
    }

    if (loading) {
        return <div style={{ color: '#94A3B8', padding: '40px', textAlign: 'center' }}>Memuat pipeline...</div>;
    }

    return (
        <div style={{ display: 'flex', gap: '16px', overflowX: 'auto', paddingBottom: '16px' }}>
            {COLUMNS.map((col) => {
                const colApplicants = applicants.filter((a) => a.stage === col.key);
                return (
                    <div
                        key={col.key}
                        onDragOver={(e) => e.preventDefault()}
                        onDrop={(e) => handleDrop(e, col.key)}
                        style={{
                            minWidth: '240px', width: '240px',
                            background: 'var(--surface)', border: '1px solid var(--border)',
                            borderRadius: '0px', padding: '16px',
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
                            <div style={{ width: '10px', height: '10px', borderRadius: '50%', background: col.color }} />
                            <span style={{ fontSize: '13px', fontWeight: '600', color: '#CBD5E1' }}>{col.label}</span>
                            <span style={{
                                marginLeft: 'auto', background: col.color + '33', color: col.color,
                                borderRadius: '100px', padding: '2px 8px', fontSize: '12px', fontWeight: '700',
                            }}>{colApplicants.length}</span>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', minHeight: '60px' }}>
                            {colApplicants.map((applicant) => (
                                <div
                                    key={applicant.id}
                                    draggable
                                    onDragStart={() => setDragging(applicant.id)}
                                    onDragEnd={() => setDragging(null)}
                                    style={{
                                        background: 'var(--bg)', border: '1px solid var(--border)',
                                        borderRadius: '0px', padding: '12px',
                                        cursor: 'grab', userSelect: 'none',
                                        opacity: dragging === applicant.id ? 0.5 : 1,
                                    }}
                                >
                                    <div style={{ fontSize: '13px', fontWeight: '600', color: '#F1F5F9' }}>
                                        {applicant.first_name} {applicant.last_name}
                                    </div>
                                    <div style={{ fontSize: '11px', color: '#94A3B8', marginTop: '3px' }}>
                                        {applicant.email}
                                    </div>
                                    {applicant.current_company && (
                                        <div style={{ fontSize: '11px', color: '#64748B', marginTop: '2px' }}>
                                            {applicant.current_company}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                );
            })}
        </div>
    );
}
