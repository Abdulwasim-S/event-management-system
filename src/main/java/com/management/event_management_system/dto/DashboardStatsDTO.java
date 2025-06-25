package com.management.event_management_system.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
	private long totalEvents;
	private long totalBookings;
	private long totalUsers;

	private double totalIncome;
	private double bookingCompletionRate;

	private long confirmedCount;
	private long cancelledCount;
	private long pendingCount;

	private List<IncomePoint> incomeOverTime;
}
