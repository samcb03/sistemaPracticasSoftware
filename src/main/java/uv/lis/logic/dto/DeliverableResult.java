package uv.lis.logic.dto;

import java.util.Objects;

public class DeliverableResult {

    private String result;
    private String advancePercentage;
    private String observations;

    public DeliverableResult() {

    }

    public DeliverableResult(String result, String advancePercentage, String observations) {
        this.result = result;
        this.advancePercentage = advancePercentage;
        this.observations = observations;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
            DeliverableResult other = (DeliverableResult) object;
            isEqual = Objects.equals(result, other.result)
                && Objects.equals(advancePercentage, other.advancePercentage)
                && Objects.equals(observations, other.observations);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, advancePercentage, observations);
    }
}