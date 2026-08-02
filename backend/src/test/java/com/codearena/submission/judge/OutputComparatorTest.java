package com.codearena.submission.judge;

import com.codearena.submission.Verdict;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutputComparatorTest {

    @Test
    void exactMatch_isAccepted() {
        assertThat(OutputComparator.compare("42\n", "42\n")).isEqualTo(Verdict.ACCEPTED);
    }

    @Test
    void differingOnlyInTrailingWhitespace_isPresentationError() {
        assertThat(OutputComparator.compare("42   \n\n", "42")).isEqualTo(Verdict.PRESENTATION_ERROR);
    }

    @Test
    void differentContent_isWrongAnswer() {
        assertThat(OutputComparator.compare("41", "42")).isEqualTo(Verdict.WRONG_ANSWER);
    }

    @Test
    void nullActual_isTreatedAsEmptyNotAnException() {
        assertThat(OutputComparator.compare(null, "")).isEqualTo(Verdict.ACCEPTED);
    }
}
