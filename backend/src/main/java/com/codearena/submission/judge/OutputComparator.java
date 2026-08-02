package com.codearena.submission.judge;

import com.codearena.submission.Verdict;

/**
 * Exact match → ACCEPTED. Same content but differing only in surrounding/
 * trailing whitespace → PRESENTATION_ERROR (a deliberately distinct verdict
 * so students learn to match output formatting exactly, rather than being
 * silently marked wrong). Anything else → WRONG_ANSWER.
 */
public final class OutputComparator {

    private OutputComparator() {
    }

    public static Verdict compare(String actual, String expected) {
        String actualExact = actual == null ? "" : actual;
        String expectedExact = expected == null ? "" : expected;

        if (actualExact.equals(expectedExact)) {
            return Verdict.ACCEPTED;
        }

        if (normalize(actualExact).equals(normalize(expectedExact))) {
            return Verdict.PRESENTATION_ERROR;
        }

        return Verdict.WRONG_ANSWER;
    }

    private static String normalize(String s) {
        return s.strip().replaceAll("[ \\t]+\\n", "\n").replaceAll("\\n+$", "");
    }
}
