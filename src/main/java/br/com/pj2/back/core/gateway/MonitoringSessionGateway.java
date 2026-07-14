package br.com.pj2.back.core.gateway;

import br.com.pj2.back.core.domain.MonitoringSessionDomain;

import java.time.Duration;

public interface MonitoringSessionGateway {
    MonitoringSessionDomain save(MonitoringSessionDomain domain);
    MonitoringSessionDomain findByMonitorAndIsStartedTrue(String monitorRegistration);
    Duration getWorkedHoursByMonth(String registration, Integer month, Integer year);
}
