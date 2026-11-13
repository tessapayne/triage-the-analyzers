package team.projectpulse.ram.requirement.dto;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public record ArtifactTraceability(Long artifactId,
                                   List<ArtifactLinkViewDto> incoming,
                                   List<ArtifactLinkViewDto> outgoing) {
    public ArtifactTraceability {
        incoming = incoming == null ? null : Collections.unmodifiableList(new ArrayList<>(incoming));
        outgoing = outgoing == null ? null : Collections.unmodifiableList(new ArrayList<>(outgoing));
    }
}
