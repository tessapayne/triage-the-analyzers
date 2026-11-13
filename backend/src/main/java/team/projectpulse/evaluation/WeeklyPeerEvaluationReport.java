package team.projectpulse.evaluation;

import java.util.ArrayList;
import java.util.List;

public class WeeklyPeerEvaluationReport {

    private String sectionName;
    private String week;
    private List<PeerEvaluationAverage> peerEvaluationAverages = new ArrayList<>();


    public WeeklyPeerEvaluationReport() {
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    // Preserve the existing null contract; an absent collection is not an empty collection.
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public List<PeerEvaluationAverage> getPeerEvaluationAverages() {
        return peerEvaluationAverages == null ? null : new ArrayList<>(peerEvaluationAverages);
    }

    public void setPeerEvaluationAverages(List<PeerEvaluationAverage> peerEvaluationAverages) {
        this.peerEvaluationAverages = peerEvaluationAverages == null ? null : new ArrayList<>(peerEvaluationAverages);
    }

}
