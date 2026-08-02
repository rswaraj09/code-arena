import { Box, Grid, Paper, Typography, Stack, Avatar, LinearProgress } from '@mui/material';
import { Bar } from 'react-chartjs-2';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import EmojiEventsRoundedIcon from '@mui/icons-material/EmojiEventsRounded';
import AssignmentRoundedIcon from '@mui/icons-material/AssignmentRounded';
import GradeRoundedIcon from '@mui/icons-material/GradeRounded';
import StatCard from '@/components/common/StatCard';
import { baseChartOptions } from '@/theme/chartSetup';

const submissionData = {
  labels: ['Contest 41', 'Assignment 3', 'Quiz 5', 'Contest 42', 'Assignment 4'],
  datasets: [
    {
      label: 'Avg. score',
      data: [72, 65, 80, 76, 68],
      backgroundColor: '#7C5CFF',
      borderRadius: 6,
      maxBarThickness: 36,
    },
  ],
};

const topPerformers = [
  { name: 'Ananya Verma', score: 980, solved: 42 },
  { name: 'Sameer Iyer', score: 940, solved: 39 },
  { name: 'Rehan Khan', score: 915, solved: 38 },
];

const TrainerDashboard = () => (
  <Box>
    <Typography variant="h4" sx={{ fontSize: '1.6rem', mb: 0.5 }}>Trainer dashboard</Typography>
    <Typography sx={{ color: 'text.secondary', mb: 4 }}>Your cohorts, contests and assignments at a glance.</Typography>

    <Grid container spacing={2.5} sx={{ mb: 3 }}>
      <Grid item xs={6} md={3}>
        <StatCard label="Students" value="214" sublabel="Across 3 batches" icon={GroupsRoundedIcon} />
      </Grid>
      <Grid item xs={6} md={3}>
        <StatCard label="Active contests" value="2" sublabel="1 starting today" icon={EmojiEventsRoundedIcon} accent="secondary.main" />
      </Grid>
      <Grid item xs={6} md={3}>
        <StatCard label="Assignments" value="7" sublabel="2 pending grading" icon={AssignmentRoundedIcon} accent="success.main" />
      </Grid>
      <Grid item xs={6} md={3}>
        <StatCard label="Avg. score" value="74%" sublabel="Last 30 days" icon={GradeRoundedIcon} accent="#FFB020" />
      </Grid>
    </Grid>

    <Grid container spacing={2.5}>
      <Grid item xs={12} md={7}>
        <Paper elevation={0} sx={{ p: 3, borderRadius: 3, height: 340 }}>
          <Typography variant="h6" sx={{ fontSize: '1rem', mb: 2 }}>Average score by event</Typography>
          <Box sx={{ height: 260 }}>
            <Bar data={submissionData} options={baseChartOptions} />
          </Box>
        </Paper>
      </Grid>

      <Grid item xs={12} md={5}>
        <Paper elevation={0} sx={{ p: 3, borderRadius: 3, height: 340 }}>
          <Typography variant="h6" sx={{ fontSize: '1rem', mb: 2 }}>Top performers</Typography>
          <Stack spacing={2}>
            {topPerformers.map((p, i) => (
              <Stack key={p.name} direction="row" alignItems="center" spacing={1.5}>
                <Avatar sx={{ width: 32, height: 32, bgcolor: i === 0 ? 'primary.main' : 'background.paper', border: '1px solid', borderColor: 'divider', color: i === 0 ? '#12100A' : 'text.primary', fontSize: '0.8rem' }}>
                  {p.name[0]}
                </Avatar>
                <Box sx={{ flex: 1 }}>
                  <Typography variant="body2" fontWeight={600}>{p.name}</Typography>
                  <Typography variant="caption" color="text.secondary">{p.solved} solved</Typography>
                </Box>
                <Typography sx={{ fontFamily: "'JetBrains Mono', monospace", fontWeight: 700 }}>{p.score}</Typography>
              </Stack>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid item xs={12}>
        <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
          <Typography variant="h6" sx={{ fontSize: '1rem', mb: 2 }}>Workshop attendance</Typography>
          <Stack spacing={2}>
            {[
              { label: 'React Fundamentals — Batch A', value: 92 },
              { label: 'DSA Bootcamp — Batch B', value: 78 },
              { label: 'System Design 101 — Batch C', value: 65 },
            ].map((w) => (
              <Box key={w.label}>
                <Stack direction="row" justifyContent="space-between" sx={{ mb: 0.5 }}>
                  <Typography variant="body2">{w.label}</Typography>
                  <Typography variant="caption" color="text.secondary">{w.value}%</Typography>
                </Stack>
                <LinearProgress variant="determinate" value={w.value} sx={{ height: 6, borderRadius: 3, bgcolor: 'rgba(255,255,255,0.06)' }} color="secondary" />
              </Box>
            ))}
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  </Box>
);

export default TrainerDashboard;
