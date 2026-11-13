package team.projectpulse.section;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SectionSpecsTest {

    @Test
    @ResourceLock(Resources.LOCALE)
    @SuppressWarnings("unchecked")
    void containsSectionNameUsesLocaleIndependentSearchPattern() {
        Root<Section> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<String> sectionName = mock(Path.class);
        Expression<String> lowerName = mock(Expression.class);
        when(root.<String>get("sectionName")).thenReturn(sectionName);
        when(builder.lower(sectionName)).thenReturn(lowerName);

        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            SectionSpecs.containsSectionName("INTRO").toPredicate(root, query, builder);

            // An ASCII name such as "intro" must remain searchable on a Turkish JVM.
            verify(builder).like(lowerName, "%intro%");
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
}
