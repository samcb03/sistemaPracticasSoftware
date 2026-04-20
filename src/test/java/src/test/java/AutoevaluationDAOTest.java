package src.test.java;

import org.junit.jupiter.api.Test;
import uv.lis.logic.dto.Autoevaluation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AutoevaluationDAOTest {

    @Test
    void constructor_nullAnswer_throwsIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () ->
             new Autoevaluation("S013322",null)
        );
    }
     @Test
    void constructor_wrongNumberOfAnswers_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Autoevaluation("S200123", new int[]{1, 2, 3})
        );
    }

    @Test
    void constructor_validAnswers_setsIdStudent() {
        Autoevaluation autoevaluation = new Autoevaluation("S200123", 
        new int[]{5, 5, 5, 5, 5, 5, 5, 5, 5, 5});

        assertEquals("S200123", autoevaluation.getIdStudent());
    }

    @Test
    void calculateFinalScore_allFives_returns100Percent() {
        Autoevaluation autoevaluation = new Autoevaluation("S200123", 
        new int[]{5, 5, 5, 5, 5, 5, 5, 5, 5, 5});

        assertEquals(100.0, autoevaluation.getFinalScore());
    }

    @Test
    void calculateFinalScore_allZeros_returns0Percent() {
        Autoevaluation autoevaluation = new Autoevaluation("S200123", 
        new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0});

        assertEquals(0.0, autoevaluation.getFinalScore());
    }

    @Test
    void calculateFinalScore_mixed_returnsCorrectPercent() {
        Autoevaluation autoevaluation = new Autoevaluation("S200123", 
        new int[]{5, 5, 5, 5, 5, 5, 5, 5, 5, 0});

        assertEquals(90.0, autoevaluation.getFinalScore());
    }
}