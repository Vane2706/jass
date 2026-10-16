package jass.com.agua.service.dashboard;

import jass.com.agua.dto.dashboard.DashboardDTO;

public interface DashboardService {

    DashboardDTO obtenerDashboard(
            Integer anio,
            Integer mes
    );
}