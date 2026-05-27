package uv.lis.logic.dto;

import java.util.Objects;

public class ActivityProgress {

    private String name;
    private String advancePercentage;
    private String observations;

    public ActivityProgress() {

    }

    public ActivityProgress(String name, String advancePercentage, String observations) {
        this.name = name;
        this.advancePercentage = advancePercentage;
        this.observations = observations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdvancePercentage() {
        return advancePercentage;
    }

    public void setAdvancePercentage(String advancePercentage) {
        this.advancePercentage = advancePercentage;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            ActivityProgress other = (ActivityProgress) object;
            isEqual = Objects.equals(name, other.name)
                && Objects.equals(advancePercentage, other.advancePercentage)
                && Objects.equals(observations, other.observations);
        }
        return isEqual;
    }
}