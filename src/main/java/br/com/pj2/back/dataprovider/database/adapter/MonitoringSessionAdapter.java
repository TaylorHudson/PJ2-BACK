package br.com.pj2.back.dataprovider.database.adapter;

import br.com.pj2.back.core.domain.MonitoringDomain;
import br.com.pj2.back.core.domain.MonitoringSessionDomain;
import br.com.pj2.back.core.domain.enumerated.ErrorCode;
import br.com.pj2.back.core.exception.ResourceNotFoundException;
import br.com.pj2.back.core.gateway.MonitoringSessionGateway;
import br.com.pj2.back.dataprovider.database.entity.MonitoringEntity;
import br.com.pj2.back.dataprovider.database.entity.MonitoringScheduleEntity;
import br.com.pj2.back.dataprovider.database.entity.MonitoringSessionEntity;
import br.com.pj2.back.dataprovider.database.entity.StudentEntity;
import br.com.pj2.back.dataprovider.database.repository.MonitoringRepository;
import br.com.pj2.back.dataprovider.database.repository.MonitoringSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitoringSessionAdapter implements MonitoringSessionGateway {
    private final MonitoringSessionRepository monitoringSessionRepository;
    private final MonitoringRepository monitoringRepository;

    private static String convertToStringTopics(List<String> topics) {
        return String.join(",", topics);
    }

    private static List<String> convertToListTopics(String topics) {
        return List.of(topics.split(","));
    }

    @Override
    public MonitoringSessionDomain save(MonitoringSessionDomain domain) {
        var discipline = monitoringRepository.findByName(domain.getMonitoring())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MONITORING_NOT_FOUND));
        var topics = domain.getTopics() != null && !domain.getTopics().isEmpty() ? convertToStringTopics(domain.getTopics()): "";
        var entity = monitoringSessionRepository.save(
                MonitoringSessionEntity.builder()
                        .id(domain.getId())
                        .monitor(StudentEntity.builder().registration(domain.getMonitor()).build())
                        .monitoring(MonitoringEntity.builder().id(discipline.getId()).build())
                        .monitoringSchedule(MonitoringScheduleEntity.builder().id(domain.getMonitoringSchedule()).build())
                        .startTime(domain.getStartTime())
                        .endTime(domain.getEndTime())
                        .topics(topics)
                        .isStarted(domain.isStarted())
                        .build()
        );
        return toDomain(entity);
    }

    @Override
    public MonitoringSessionDomain findByMonitorAndIsStartedTrue(String monitorRegistration) {
        var entity = monitoringSessionRepository.findByMonitorRegistrationAndIsStartedTrue(monitorRegistration)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NO_STARTED_MONITORING_SESSION_WAS_FOUND));
        return toDomain(entity);
    }

    private MonitoringSessionDomain toDomain(MonitoringSessionEntity entity) {
        return MonitoringSessionDomain.builder()
                .id(entity.getId())
                .monitor(entity.getMonitor().getRegistration())
                .monitoring(entity.getMonitoring().getName())
                .monitoringSchedule(entity.getMonitoringSchedule().getId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .topics(convertToListTopics(entity.getTopics()))
                .isStarted(entity.isStarted())
                .build();
    }

    public HashMap<String, Integer> countTopicsInSessionMonitoring(MonitoringDomain monitoring){
        HashMap<String, Integer> topicsCount = new HashMap<>();

        // Recuperar lista de sessões de monitoria que foram finalizadas com tópicos.
        List<MonitoringSessionEntity> monitoringSessions = monitoringSessionRepository.findAll()
                .stream()
                .filter(session -> session.getTopics() != null && session.getMonitoring().getId().equals(monitoring.getId()))
                .toList();

        List<String> topics = monitoring.getTopics();

        if(monitoringSessions.isEmpty()){
            for(String topic : topics){
                topicsCount.put(topic, 0);
            }
            return topicsCount;
        }

        StringBuilder sessionTopics = new StringBuilder();

        for(MonitoringSessionEntity session: monitoringSessions){
            sessionTopics.append(session.getTopics());
            sessionTopics.append(",");
        }

        for(String topic : topics){
            topicsCount.put(topic,countString(topic, sessionTopics.toString()));
        }
        return topicsCount;

    }

    private int countString(String word, String topics) {
        if (word == null || topics == null || topics.isBlank()) {
            return 0;
        }

        String[] topicArray = topics.split(",");
        int count = 0;

        for (String t : topicArray) {
            if (t.trim().equalsIgnoreCase(word.trim())) {
                count++;
            }
        }

        return count;
    }

    public Duration getWorkedHoursByMonth(String registration, Integer month, Integer year) {
        Long totalSeconds = monitoringSessionRepository.sumMonitoringSecondsByMonth(registration, month, year);
        return Duration.ofSeconds(totalSeconds);
    }

}
