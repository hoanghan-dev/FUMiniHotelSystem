import { describe, it, expect } from 'vitest';

describe('Booking Total & Price Calculations', () => {
  const calculateDays = (startDateStr, endDateStr) => {
    if (!startDateStr || !endDateStr) return 0;
    const start = new Date(startDateStr);
    const end = new Date(endDateStr);
    const diff = Math.round((end - start) / (1000 * 60 * 60 * 24));
    return diff > 0 ? diff : 0;
  };

  const calculateRoomActualPrice = (pricePerDay, days) => {
    return pricePerDay * days;
  };

  const calculateBookingTotal = (roomList, selectedIds, days) => {
    if (days <= 0) return 0;
    return selectedIds.reduce((sum, id) => {
      const room = roomList.find((r) => r.roomID === id);
      return sum + (room ? room.roomPricePerDay * days : 0);
    }, 0);
  };

  it('correctly calculates stay duration in days', () => {
    expect(calculateDays('2026-11-01', '2026-11-05')).toBe(4);
    expect(calculateDays('2026-11-01', '2026-11-02')).toBe(1);
    expect(calculateDays('2026-11-05', '2026-11-01')).toBe(0); // Invalid end before start
    expect(calculateDays('2026-11-01', '2026-11-01')).toBe(0); // Same day
  });

  it('correctly calculates single room actual price based on days', () => {
    const pricePerDay = 55.0;
    const days = 3;
    expect(calculateRoomActualPrice(pricePerDay, days)).toBe(165.0);
  });

  it('correctly calculates grand total for multiple booked rooms', () => {
    const mockRooms = [
      { roomID: 101, roomNumber: '101', roomPricePerDay: 50.0 },
      { roomID: 201, roomNumber: '201', roomPricePerDay: 90.0 },
      { roomID: 301, roomNumber: '301', roomPricePerDay: 220.0 },
    ];

    const selectedRoomIds = [101, 201]; // Room 101 ($50) + Room 201 ($90) = $140/day
    const days = 4; // 4 nights

    const grandTotal = calculateBookingTotal(mockRooms, selectedRoomIds, days);
    expect(grandTotal).toBe(560.0); // 140 * 4 = 560
  });

  it('returns 0 total when duration is 0 or negative', () => {
    const mockRooms = [{ roomID: 101, roomPricePerDay: 50.0 }];
    expect(calculateBookingTotal(mockRooms, [101], 0)).toBe(0);
    expect(calculateBookingTotal(mockRooms, [101], -1)).toBe(0);
  });
});
