package com.codearena.contest;

import com.codearena.common.exception.BadRequestException;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.contest.dto.ContestDetailResponse;
import com.codearena.contest.dto.ContestSummaryResponse;
import com.codearena.contest.dto.CreateContestRequest;
import com.codearena.problem.Problem;
import com.codearena.problem.ProblemRepository;
import com.codearena.problem.dto.ProblemSummaryResponse;
import com.codearena.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestParticipantRepository participantRepository;
    private final ProblemRepository problemRepository;

    public Contest create(CreateContestRequest request, User creator) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Contest end time must be after the start time.");
        }
        List<Problem> problems = problemRepository.findAllById(request.problemIds());
        if (problems.size() != request.problemIds().size()) {
            throw new BadRequestException("One or more problem IDs are invalid.");
        }

        Contest contest = Contest.builder()
                .title(request.title())
                .description(request.description())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .negativeMarking(request.negativeMarking())
                .createdBy(creator)
                .problems(problems)
                .build();

        return contestRepository.save(contest);
    }

    public List<ContestSummaryResponse> list() {
        return contestRepository.findAll().stream()
                .map(c -> new ContestSummaryResponse(
                        c.getId(), c.getTitle(), c.getStartTime(), c.getEndTime(),
                        c.getStatus(), c.getProblems().size(), participantRepository.findByContestId(c.getId()).size()
                ))
                .toList();
    }

    public ContestDetailResponse getDetail(String contestId, User currentUser) {
        Contest contest = getById(contestId);
        boolean registered = currentUser != null
                && participantRepository.existsByContestIdAndUserId(contestId, currentUser.getId());

        List<ProblemSummaryResponse> problems = contest.getProblems().stream()
                .map(p -> new ProblemSummaryResponse(p.getId(), p.getTitle(), p.getSlug(), p.getDifficulty(), p.getTags(), false, 0.0))
                .toList();

        return new ContestDetailResponse(
                contest.getId(), contest.getTitle(), contest.getDescription(),
                contest.getStartTime(), contest.getEndTime(), contest.isNegativeMarking(),
                contest.getStatus(), problems, registered
        );
    }

    public void register(String contestId, User user) {
        Contest contest = getById(contestId);
        if (contest.getStatus() == ContestStatus.ENDED) {
            throw new BadRequestException("This contest has already ended.");
        }
        if (participantRepository.existsByContestIdAndUserId(contestId, user.getId())) {
            return; // idempotent — already registered
        }
        participantRepository.save(ContestParticipant.builder().contest(contest).user(user).build());
    }

    public Contest getById(String id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Contest", "id", id));
    }
}
