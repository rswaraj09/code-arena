import { Box, Typography, Paper, Table, TableHead, TableRow, TableCell, TableBody, Chip, Stack, Avatar } from '@mui/material';
import CircleRoundedIcon from '@mui/icons-material/CircleRounded';

const ROWS = [
  { rank: 1, name: 'Ananya Verma', college: 'VIMEET', solved: 6, score: 980, penalty: 12, lang: 'C++' },
  { rank: 2, name: 'Sameer Iyer', college: 'VJTI', solved: 6, score: 940, penalty: 24, lang: 'Python' },
  { rank: 3, name: 'Rehan Khan', college: 'SPIT', solved: 5, score: 915, penalty: 8, lang: 'Java' },
  { rank: 4, name: 'Meera Chen', college: 'DJ Sanghvi', solved: 5, score: 890, penalty: 15, lang: 'C++' },
  { rank: 5, name: 'Priya Das', college: 'VIMEET', solved: 5, score: 865, penalty: 20, lang: 'JavaScript' },
  { rank: 6, name: 'Karan Rao', college: 'KJ Somaiya', solved: 4, score: 840, penalty: 5, lang: 'Python' },
];

const RANK_COLOR = { 1: '#FFB020', 2: '#C7CEDA', 3: '#B87333' };

const LeaderboardPage = () => (
  <Box>
    <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 0.5 }}>
      <Typography variant="h4" sx={{ fontSize: '1.6rem' }}>Leaderboard</Typography>
      <Stack direction="row" spacing={0.75} alignItems="center">
        <CircleRoundedIcon sx={{ fontSize: 9, color: 'success.main' }} />
        <Typography variant="caption" color="text.secondary">Live — updates via WebSocket</Typography>
      </Stack>
    </Stack>
    <Typography sx={{ color: 'text.secondary', mb: 3 }}>Weekly Contest #43 · ranked by score, then penalty.</Typography>

    <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell width={64}>Rank</TableCell>
            <TableCell>Participant</TableCell>
            <TableCell>College</TableCell>
            <TableCell align="center">Solved</TableCell>
            <TableCell align="center">Language</TableCell>
            <TableCell align="right">Penalty</TableCell>
            <TableCell align="right">Score</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {ROWS.map((r) => (
            <TableRow key={r.rank} sx={{ '& td': { borderColor: 'divider' } }}>
              <TableCell>
                <Typography sx={{ fontFamily: "'JetBrains Mono', monospace", fontWeight: 700, color: RANK_COLOR[r.rank] || 'text.secondary' }}>
                  {r.rank}
                </Typography>
              </TableCell>
              <TableCell>
                <Stack direction="row" alignItems="center" spacing={1.5}>
                  <Avatar sx={{ width: 28, height: 28, fontSize: '0.75rem', bgcolor: 'background.default', border: '1px solid', borderColor: 'divider' }}>
                    {r.name[0]}
                  </Avatar>
                  <Typography variant="body2">{r.name}</Typography>
                </Stack>
              </TableCell>
              <TableCell><Typography variant="body2" color="text.secondary">{r.college}</Typography></TableCell>
              <TableCell align="center">{r.solved}</TableCell>
              <TableCell align="center"><Chip size="small" label={r.lang} /></TableCell>
              <TableCell align="right" sx={{ color: 'text.secondary' }}>+{r.penalty}m</TableCell>
              <TableCell align="right">
                <Typography sx={{ fontFamily: "'JetBrains Mono', monospace", fontWeight: 700 }}>{r.score}</Typography>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  </Box>
);

export default LeaderboardPage;
