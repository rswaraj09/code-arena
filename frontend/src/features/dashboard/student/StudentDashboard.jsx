import { Box, Grid, Paper, Typography, Stack, Chip, LinearProgress } from '@mui/material';
import { Line } from 'react-chartjs-2';
import LocalFireDepartmentRoundedIcon from '@mui/icons-material/LocalFireDepartmentRounded';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import EmojiEventsRoundedIcon from '@mui/icons-material/EmojiEventsRounded';
import AssignmentLateRoundedIcon from '@mui/icons-material/AssignmentLateRounded';
import StatCard from '@/components/common/StatCard';
import VerdictChip from '@/components/common/VerdictChip';
import { baseChartOptions } from '@/theme/chartSetup';
import { useAuth } from '@/app/hooks';

const activityData = {
  labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
  datasets: [
    {
      label: 'Problems solved',
      data: [2, 3, 1, 4, 2, 5, 3],
      borderColor: '#FFB020',
      backgroundColor: 'rgba(255,176,32,0.12)',
      tension: 0.35,
      fill: true,
      pointRadius: 3,
    },
  ],
};

const recentSubmissions = [
  { problem: 'Two Sum', verdict: 'ACCEPTED', lang: 'Java', time: '2 min ago' },
  { problem: 'Longest Substring', verdict: 'WRONG_ANSWER', lang: 'Python', time: '1 hr ago' },
  { problem: 'Merge Intervals', verdict: 'TIME_LIMIT_EXCEEDED', lang: 'C++', time: 'Yesterday' },
  { problem: 'Valid Parentheses', verdict: 'ACCEPTED', lang: 'JavaScript', time: 'Yesterday' },
];

const upcoming = [
  { title: 'Weekly Contest #43', when: 'Tomorrow, 7:00 PM', type: 'Contest' },
  { title: 'DSA Assignment 4', when: 'Due in 2 days', type: 'Assignment' },
  { title: 'React Fundamentals Quiz', when: 'Due in 4 days', type: 'Quiz' },
];

const StudentDashboard = () => {
  const { user } = useAuth();

  return (
    <Box>
      <Typography variant="h4" sx={{ fontSize: '1.6rem', mb: 0.5 }}>
        Welcome back{user?.name ? `, ${user.name.split(' ')[0]}` : ''}
      </Typography>
      <Typography sx={{ color: 'text.secondary', mb: 4 }}>Here's where you stand today.</Typography>

      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        <Grid item xs={6} md={3}>
          <StatCard label="Rank" value="#128" sublabel="Top 12% overall" icon={EmojiEventsRoundedIcon} />
        </Grid>
        <Grid item xs={6} md={3}>
          <StatCard label="Solved" value="86" sublabel="of 340 problems" icon={CheckCircleRoundedIcon} accent="success.main" />
        </Grid>
        <Grid item xs={6} md={3}>
          <StatCard label="Streak" value="12 days" sublabel="Personal best: 21" icon={LocalFireDepartmentRoundedIcon} accent="#FB6467" />
        </Grid>
        <Grid item xs={6} md={3}>
          <StatCard label="Pending" value="3" sublabel="Assignments due soon" icon={AssignmentLateRoundedIcon} accent="secondary.main" />
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        <Grid item xs={12} md={7}>
          <Paper elevation={0} sx={{ p: 3, borderRadius: 3, height: 340 }}>
            <Typography variant="h6" sx={{ fontSize: '1rem', mb: 2 }}>This week's activity</Typography>
            <Box sx={{ height: 260 }}>
              <Line data={activityData} options={baseChartOptions} />
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} md={5}>
          <Paper elevation={0} sx={{ p: 3, borderRadius: 3, height: 340, overflow: 'auto' }}>
            <Typography variant="h6" sx={{ fontSize: '1rem', mb: 2 }}>Upcoming</Typography>
            <Stack spacing={2}>
              {upcoming.map((item) => (
                <Box key={item.title} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Box>
                    <Typography variant="body2" fontWeight={600}>{item.title}</Typography>
                    <Typography variant="caption" color="text.secondary">{item.when}</Typography>
                  </Box>
                  <Chip size="small" label={item.type} />
                </Box>
              ))}
            </Stack>
          </Paper>
        </Grid>

        <Grid item xs={12} md={7}>
          <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ fontSize: '1rem', mb: 2 }}>Recent submissions</Typography>
            <Stack spacing={1.5}>
              {recentSubmissions.map((s, i) => (
                <Box key={i} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', py: 1, borderBottom: i < recentSubmissions.length - 1 ? '1px solid' : 'none', borderColor: 'divider' }}>
                  <Box>
                    <Typography variant="body2" fontWeight={600}>{s.problem}</Typography>
                    <Typography variant="caption" color="text.secondary">{s.lang} · {s.time}</Typography>
                  </Box>
                  <VerdictChip verdict={s.verdict} />
                </Box>
              ))}
            </Stack>
          </Paper>
        </Grid>

        <Grid item xs={12} md={5}>
          <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
            <Typography variant="h6" sx={{ fontSize: '1rem', mb: 2 }}>Skill progress</Typography>
            <Stack spacing={2}>
              {[
                { label: 'Arrays & Strings', value: 78 },
                { label: 'Dynamic Programming', value: 42 },
                { label: 'Graphs', value: 55 },
                { label: 'SQL', value: 90 },
              ].map((skill) => (
                <Box key={skill.label}>
                  <Stack direction="row" justifyContent="space-between" sx={{ mb: 0.5 }}>
                    <Typography variant="body2">{skill.label}</Typography>
                    <Typography variant="caption" color="text.secondary">{skill.value}%</Typography>
                  </Stack>
                  <LinearProgress variant="determinate" value={skill.value} sx={{ height: 6, borderRadius: 3, bgcolor: 'rgba(255,255,255,0.06)' }} />
                </Box>
              ))}
            </Stack>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default StudentDashboard;
