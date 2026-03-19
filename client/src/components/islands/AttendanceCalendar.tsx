import React, { useState, useEffect } from 'react';

interface AttendanceRecord {
    attendance_date: string;
    clock_in: string | null;
    clock_out: string | null;
    status: string;
    work_minutes: number | null;
}

const STATUS_COLORS: Record<string, string> = {
    PRESENT: '#10B981',
    LATE: '#F59E0B',
    ABSENCE: '#EF4444',
    HALF_DAY: '#06B6D4',
    ON_LEAVE: '#8B5CF6',
    HOLIDAY: '#64748B',
};

const STATUS_LABELS: Record<string, string> = {
    PRESENT: 'Hadir',
    LATE: 'Terlambat',
    ABSENCE: 'Tidak Hadir',
    HALF_DAY: 'Setengah Hari',
    ON_LEAVE: 'Cuti',
    HOLIDAY: 'Libur',
};

export default function AttendanceCalendar() {
    const [records, setRecords] = useState<AttendanceRecord[]>([]);
    const [loading, setLoading] = useState(true);
    const [month, setMonth] = useState(new Date());

    const API = (import.meta as any).env?.PUBLIC_API_URL || 'http://localhost:8080/api/v1';

    useEffect(() => {
        fetchAttendance();
    }, [month]);

    async function fetchAttendance() {
        setLoading(true);
        try {
            const token = localStorage.getItem('flowhr_token');
            const from = new Date(month.getFullYear(), month.getMonth(), 1).toISOString().split('T')[0];
            const to = new Date(month.getFullYear(), month.getMonth() + 1, 0).toISOString().split('T')[0];
            const res = await fetch(`${API}/attendance/my?from=${from}&to=${to}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            const data = await res.json();
            if (data.success) setRecords(data.data);
        } finally {
            setLoading(false);
        }
    }

    function prevMonth() {
        setMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1));
    }

    function nextMonth() {
        setMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1));
    }

    const monthName = month.toLocaleDateString('id-ID', { month: 'long', year: 'numeric' });
    const daysInMonth = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
    const firstDay = new Date(month.getFullYear(), month.getMonth(), 1).getDay();

    const recordMap: Record<string, AttendanceRecord> = {};
    records.forEach((r) => {
        const date = r.attendance_date.split('T')[0];
        recordMap[date] = r;
    });

    const cells: (number | null)[] = Array(firstDay === 0 ? 6 : firstDay - 1).fill(null);
    for (let i = 1; i <= daysInMonth; i++) cells.push(i);

    return (
        <div>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                <button onClick={prevMonth} style={navBtn}>&lt;</button>
                <span style={{ fontSize: '16px', fontWeight: '600', color: '#F1F5F9', minWidth: '180px', textAlign: 'center' }}>{monthName}</span>
                <button onClick={nextMonth} style={navBtn}>&gt;</button>
            </div>

            {/* Day labels */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px', marginBottom: '4px' }}>
                {['Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab', 'Min'].map((d) => (
                    <div key={d} style={{ fontSize: '11px', color: '#64748B', textAlign: 'center', padding: '4px', fontWeight: '600' }}>{d}</div>
                ))}
            </div>

            {/* Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px' }}>
                {loading
                    ? Array(35).fill(null).map((_, i) => <div key={i} style={emptyCell} />)
                    : cells.map((day, idx) => {
                        if (!day) return <div key={idx} style={emptyCell} />;
                        const dateStr = `${month.getFullYear()}-${String(month.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                        const rec = recordMap[dateStr];
                        const status = rec?.status;
                        const isToday = dateStr === new Date().toISOString().split('T')[0];

                        return (
                            <div
                                key={idx}
                                title={status ? `${STATUS_LABELS[status]}${rec?.work_minutes ? ` | ${Math.floor(rec.work_minutes / 60)}j ${rec.work_minutes % 60}m` : ''}` : undefined}
                                style={{
                                    ...dayCell,
                                    background: status ? STATUS_COLORS[status] + '22' : 'var(--surface)',
                                    border: isToday ? '1px solid var(--primary)' : '1px solid ' + (status ? STATUS_COLORS[status] + '55' : 'var(--border)'),
                                }}
                            >
                                <span style={{ fontSize: '12px', fontWeight: isToday ? '700' : '500', color: isToday ? '#818CF8' : '#CBD5E1' }}>{day}</span>
                                {status && (
                                    <div style={{ width: '6px', height: '6px', borderRadius: '50%', background: STATUS_COLORS[status], marginTop: '3px' }} />
                                )}
                            </div>
                        );
                    })
                }
            </div>

            {/* Legend */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', marginTop: '16px', paddingTop: '16px', borderTop: '1px solid var(--border)' }}>
                {Object.entries(STATUS_LABELS).map(([key, label]) => (
                    <div key={key} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: STATUS_COLORS[key] }} />
                        <span style={{ fontSize: '11px', color: '#94A3B8' }}>{label}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}

const navBtn: React.CSSProperties = {
    background: 'var(--border)', border: '1px solid #475569', borderRadius: '6px',
    color: '#94A3B8', cursor: 'pointer', padding: '6px 10px', fontSize: '13px',
};

const emptyCell: React.CSSProperties = {
    borderRadius: '0px', height: '48px', background: 'transparent',
};

const dayCell: React.CSSProperties = {
    borderRadius: '0px', height: '48px', padding: '6px',
    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
    cursor: 'default', transition: 'background 0.15s',
};
