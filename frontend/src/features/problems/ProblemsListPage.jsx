import { useMemo, useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  InputAdornment,
  Chip,
  Stack,
  Paper,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  ToggleButtonGroup,
  ToggleButton,
} from '@mui/material';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import { Link } from 'react-router-dom';

const DIFFICULTY_COLOR = { Easy: 'success.main', Medium: '#FFB020', Hard: 'error.main' };

const PROBLEMS = [
  { slug: 'two-sum', title: 'Two Sum', difficulty: 'Easy', tags: ['Array', 'Hash Map'], solved: true, acceptance: 62 },
  { slug: 'longest-substring', title: 'Longest Substring Without Repeating Characters', difficulty: 'Medium', tags: ['String', 'Sliding Window'], solved: false, acceptance: 41 },
  { slug: 'merge-intervals', title: 'Merge Intervals', difficulty: 'Medium', tags: ['Array', 'Sorting'], solved: false, acceptance: 46 },
  { slug: 'valid-parentheses', title: 'Valid Parentheses', difficulty: 'Easy', tags: ['Stack', 'String'], solved: true, acceptance: 71 },
  { slug: 'course-schedule', title: 'Course Schedule', difficulty: 'Hard', tags: ['Graph', 'Topological Sort'], solved: false, acceptance: 28 },
  { slug: 'lru-cache', title: 'LRU Cache', difficulty: 'Medium', tags: ['Design', 'Hash Map'], solved: false, acceptance: 38 },
];

const ProblemsListPage = () => {
  const [query, setQuery] = useState('');
  const [difficulty, setDifficulty] = useState('ALL');

  const filtered = useMemo(
    () =>
      PROBLEMS.filter((p) => {
        const matchesQuery = p.title.toLowerCase().includes(query.toLowerCase());
        const matchesDifficulty = difficulty === 'ALL' || p.difficulty === difficulty;
        return matchesQuery && matchesDifficulty;
      }),
    [query, difficulty]
  );

  return (
    <Box>
      <Typography variant="h4" sx={{ fontSize: '1.6rem', mb: 0.5 }}>Problems</Typography>
      <Typography sx={{ color: 'text.secondary', mb: 3 }}>{PROBLEMS.length} problems available across all difficulties.</Typography>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 3 }}>
        <TextField
          placeholder="Search problems…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          fullWidth
          size="small"
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchRoundedIcon fontSize="small" /></InputAdornment> }}
        />
        <ToggleButtonGroup
          exclusive
          size="small"
          value={difficulty}
          onChange={(_, v) => v && setDifficulty(v)}
          sx={{ '& .MuiToggleButton-root': { textTransform: 'none', px: 2 } }}
        >
          <ToggleButton value="ALL">All</ToggleButton>
          <ToggleButton value="Easy">Easy</ToggleButton>
          <ToggleButton value="Medium">Medium</ToggleButton>
          <ToggleButton value="Hard">Hard</ToggleButton>
        </ToggleButtonGroup>
      </Stack>

      <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell width={40} />
              <TableCell>Title</TableCell>
              <TableCell>Difficulty</TableCell>
              <TableCell>Tags</TableCell>
              <TableCell align="right">Acceptance</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filtered.map((p) => (
              <TableRow
                key={p.slug}
                component={Link}
                to={`/problems/${p.slug}`}
                hover
                sx={{ textDecoration: 'none', cursor: 'pointer', '& td': { border: 'none', borderTop: '1px solid', borderColor: 'divider' } }}
              >
                <TableCell>
                  {p.solved && <CheckCircleRoundedIcon sx={{ fontSize: 18, color: 'success.main' }} />}
                </TableCell>
                <TableCell sx={{ color: 'text.primary', fontWeight: 500 }}>{p.title}</TableCell>
                <TableCell>
                  <Typography variant="body2" sx={{ color: DIFFICULTY_COLOR[p.difficulty], fontWeight: 600 }}>
                    {p.difficulty}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Stack direction="row" spacing={0.5} flexWrap="wrap">
                    {p.tags.map((t) => <Chip key={t} label={t} size="small" />)}
                  </Stack>
                </TableCell>
                <TableCell align="right" sx={{ fontFamily: "'JetBrains Mono', monospace", color: 'text.secondary' }}>
                  {p.acceptance}%
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
};

export default ProblemsListPage;
