package br.com.pj2.back.dataprovider.database.repository;

import br.com.pj2.back.dataprovider.database.entity.MonitoringSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MonitoringSessionRepository extends JpaRepository<MonitoringSessionEntity, Long> {
    Optional<MonitoringSessionEntity> findByMonitorRegistrationAndIsStartedTrue(String monitorRegistration);

    @Query(value = """
    SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (ms.end_time - ms.start_time))), 0)
    FROM monitoring_sessions ms
    WHERE ms.monitor_registration = :registration
      AND EXTRACT(MONTH FROM ms.start_time) = :month
      AND EXTRACT(YEAR FROM ms.start_time) = :year
    """, nativeQuery = true)
    Long sumMonitoringSecondsByMonth(
            @Param("registration") String registration,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}
