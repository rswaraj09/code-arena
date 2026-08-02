import { Box, Typography, Grid, Paper, Chip, Stack, Button, LinearProgress } from '@mui/material';
import EmojiEventsRoundedIcon from '@mui/icons-material/EmojiEventsRounded';
import ScheduleRoundedIcon from '@mui/icons-material/ScheduleRounded';

const CONTESTS = [
  { id: 1, title: 'Weekly Contest #43', status: 'LIVE', starts: 'Started 42 min ago', problems: 4, participants: 312, progress: 62 },
  { id: 2, title: 'Placement Prep — Mock 7', status: 'UPCOMING', starts: 'Starts tomorrow, 7:00 PM', problems: 5, participants: 0 },
  { id: 3, title: 'Campus Coding League — Round 2', status: 'UPCOMING', starts: 'Starts in 3 days', problems: 6, participants: 0 },
  { id: 4, title: 'Weekly Contest #42', status: 'ENDED', starts: 'Ended 2 days ago', problems: 4, participants: 480 },
];

const STATUS_STYLE = {
  LIVE: { label: 'Live now', color: '#34D399' },
  UPCOMING: { label: 'Upcoming', color: '#38BDF8' },
  ENDED: { label: 'Ended', color: '#8C9AAE' },
};

const ContestsListPage = () => (
  <Box>
    <Typography variant="h4" sx={{ fontSize: '1.6rem', mb: 0.5 }}>Contests</Typography>
    <Typography sx={{ color: 'text.secondary', mb: 3 }}>Compete live, or catch up on past rounds.</Typography>

    <Grid container spacing={2.5}>
      {CONTESTS.map((c) => {
        const style = STATUS_STYLE[c.status];
        return (
          <Grid item xs={12} md={6} key={c.id}>
            <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                <Box>
                  <Typography variant="h6" sx={{ fontSize: '1.05rem', mb: 0.5 }}>{c.title}</Typography>
                  <Stack direction="row" spacing={0.5} alignItems="center" sx={{ color: 'text.secondary' }}>
                    <ScheduleRoundedIcon sx={{ fontSize: 15 }} />
                    <Typography variant="caption">{c.starts}</Typography>
                  </Stack>
                </Box>
                <Chip
                  size="small"
                  label={style.label}
                  sx={{ color: style.color, bgcolor: `${style.color}1F`, fontWeight: 600 }}
                />
              </Stack>

              {c.status === 'LIVE' && (
                <Box sx={{ my: 2 }}>
                  <LinearProgress variant="determinate" value={c.progress} sx={{ height: 6, borderRadius: 3, bgcolor: 'rgba(255,255,255,0.06)' }} />
                </Box>
              )}

              <Stack direction="row" spacing={2} sx={{ my: 2, color: 'text.secondary' }}>
                <Typography variant="caption">{c.problems} problems</Typography>
                {c.participants > 0 && <Typography variant="caption">{c.participants} participants</Typography>}
              </Stack>

              <Button
                fullWidth
                variant={c.status === 'LIVE' ? 'contained' : 'outlined'}
                color={c.status === 'LIVE' ? 'primary' : 'inherit'}
                startIcon={<EmojiEventsRoundedIcon />}
                sx={c.status !== 'LIVE' ? { borderColor: 'divider' } : {}}
                disabled={c.status === 'ENDED'}
              >
                {c.status === 'LIVE' ? 'Enter contest' : c.status === 'UPCOMING' ? 'Register' : 'View results'}
              </Button>
            </Paper>
          </Grid>
        );
      })}
    </Grid>
  </Box>
);

export default ContestsListPage;
