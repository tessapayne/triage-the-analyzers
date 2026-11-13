package team.projectpulse;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import team.projectpulse.evaluation.PeerEvaluationAverage;
import team.projectpulse.evaluation.WeeklyPeerEvaluationReport;
import team.projectpulse.notification.ReminderService.ReminderResult;
import team.projectpulse.notification.dto.*;
import team.projectpulse.ram.collaboration.dto.CommentThreadDto;
import team.projectpulse.ram.document.dto.*;
import team.projectpulse.ram.document.template.DocumentTemplate;
import team.projectpulse.ram.requirement.dto.ArtifactTraceability;
import team.projectpulse.ram.usecase.dto.*;
import team.projectpulse.rubric.dto.RubricDto;
import team.projectpulse.section.dto.SectionDto;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefensiveCopyTest {
    @Test
    void snapshotsPreserveListOrderDuplicatesAndSetIterationOrder() throws Exception {
        OwingStudentDto first = new OwingStudentDto(1, "First", "Student");
        OwingStudentDto second = new OwingStudentDto(2, "Second", "Student");
        List<OwingStudentDto> students = new ArrayList<>(Arrays.asList(second, null, first, second));
        OwingTeamDto team = new OwingTeamDto(1, "Team", students);
        students.clear();
        assertThat(team.students()).containsExactly(second, null, first, second);

        Set<Long> actorIds = new LinkedHashSet<>(Arrays.asList(3L, null, 1L));
        RecordComponent component = Arrays.stream(UseCaseDto.class.getRecordComponents())
                .filter(candidate -> candidate.getName().equals("secondaryActorIds"))
                .findFirst().orElseThrow();
        UseCaseDto useCase = (UseCaseDto) constructRecord(UseCaseDto.class, component, actorIds);
        actorIds.clear();
        assertThat(useCase.secondaryActorIds()).containsExactly(3L, null, 1L);
    }

    @TestFactory
    Stream<DynamicTest> recordCollectionsProtectInputAndOutputAndPreserveNulls() {
        return Stream.of(OwingTeamDto.class, ReminderResultDto.class, SubmissionStatusDto.class,
                        CommentThreadDto.class, DocumentSectionDto.class, RequirementDocumentDto.class,
                        ArtifactTraceability.class, UseCaseDto.class, UseCaseExtensionDto.class,
                        UseCaseMainStepDto.class, RubricDto.class, SectionDto.class, ReminderResult.class)
                .flatMap(type -> Arrays.stream(type.getRecordComponents())
                        .filter(component -> Collection.class.isAssignableFrom(component.getType()))
                        .map(component -> DynamicTest.dynamicTest(type.getSimpleName() + "." + component.getName(), () -> {
                            Collection<?> source = component.getType() == Set.class
                                    ? new LinkedHashSet<>(Arrays.asList((Object) null))
                                    : new ArrayList<>(Arrays.asList(null, null));
                            Object record = constructRecord(type, component, source);
                            Collection<?> result = (Collection<?>) component.getAccessor().invoke(record);
                            int size = source.size();
                            source.clear();
                            assertThat(result).hasSize(size);
                            assertThat(new ArrayList<>(result)).containsOnlyNulls();
                            assertThrows(UnsupportedOperationException.class, result::clear);
                            assertThat((Collection<?>) component.getAccessor().invoke(record)).hasSize(size);

                            Object nullRecord = constructRecord(type, component, null);
                            assertThat(component.getAccessor().invoke(nullRecord)).isNull();
                        })));
    }

    private static Object constructRecord(Class<?> type, RecordComponent selected, Object value) throws Exception {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
        Object[] arguments = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            if (types[i] == int.class) arguments[i] = 0;
            if (types[i] == boolean.class) arguments[i] = false;
            if (components[i].getName().equals(selected.getName())) arguments[i] = value;
        }
        return type.getDeclaredConstructor(types).newInstance(arguments);
    }

    @TestFactory
    Stream<DynamicTest> beanCollectionsProtectInputAndOutputAndPreserveNulls() {
        return Stream.of(
                new Object[]{PeerEvaluationAverage.class, "PublicComments"},
                new Object[]{PeerEvaluationAverage.class, "PrivateComments"},
                new Object[]{PeerEvaluationAverage.class, "RatingAverages"},
                new Object[]{WeeklyPeerEvaluationReport.class, "PeerEvaluationAverages"},
                new Object[]{DocumentTemplate.class, "Sections"})
                .map(entry -> DynamicTest.dynamicTest(((Class<?>) entry[0]).getSimpleName() + "." + entry[1], () -> {
                    Class<?> type = (Class<?>) entry[0];
                    String property = (String) entry[1];
                    Object bean = type.getDeclaredConstructor().newInstance();
                    var setter = type.getMethod("set" + property, List.class);
                    var getter = type.getMethod("get" + property);
                    List<?> source = new ArrayList<>(Arrays.asList(null, null));
                    setter.invoke(bean, source);
                    source.clear();
                    List<?> result = (List<?>) getter.invoke(bean);
                    assertThat(result).hasSize(2).containsOnlyNulls();
                    result.clear();
                    assertThat((List<?>) getter.invoke(bean)).hasSize(2).containsOnlyNulls();
                    setter.invoke(bean, new Object[]{null});
                    assertThat(getter.invoke(bean)).isNull();
                }));
    }
}
