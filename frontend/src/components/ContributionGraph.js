import React from 'react';
import { Card } from 'react-bootstrap';

const ContributionGraph = ({ data }) => {
  if (!data || !data.commitsByDate) {
    return null;
  }

  // Generate a list of dates for the last 'days' days
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(endDate.getDate() - (data.overallStats?.totalCommits > 0 ? Object.keys(data.commitsByDate).length : 30) + 1);

  const dateRange = [];
  const currentDate = new Date(startDate);
  while (currentDate <= endDate) {
    dateRange.push(new Date(currentDate));
    currentDate.setDate(currentDate.getDate() + 1);
  }

  // Group dates by month
  const months = [];
  let currentMonth = -1;
  dateRange.forEach((date, index) => {
    const month = date.getMonth();
    if (month !== currentMonth) {
      currentMonth = month;
      months.push({
        index,
        name: date.toLocaleString('default', { month: 'short' })
      });
    }
  });

  // Group dates by week
  const weeks = [];
  for (let i = 0; i < dateRange.length; i += 7) {
    weeks.push(dateRange.slice(i, i + 7));
  }

  // Get commit count for a date
  const getCommitCount = (date) => {
    const dateStr = date.toISOString().split('T')[0];
    return data.commitsByDate[dateStr] || 0;
  };

  // Determine cell color based on commit count
  const getCellColor = (count) => {
    if (count === 0) return 'contribution-cell-0';
    if (count < 5) return 'contribution-cell-1';
    if (count < 10) return 'contribution-cell-2';
    if (count < 15) return 'contribution-cell-3';
    return 'contribution-cell-4';
  };

  return (
    <Card className="contribution-graph">
      <Card.Header>Contribution Graph</Card.Header>
      <Card.Body>
        <div className="month-labels">
          {months.map((month, index) => (
            <div key={index} style={{ marginLeft: index === 0 ? '0' : `${month.index * 19}px` }}>
              {month.name}
            </div>
          ))}
        </div>
        <div style={{ display: 'flex' }}>
          <div className="day-labels">
            <div className="day-label">Mon</div>
            <div className="day-label">Wed</div>
            <div className="day-label">Fri</div>
          </div>
          <div>
            {weeks.map((week, weekIndex) => (
              <div key={weekIndex} className="contribution-row">
                {week.map((date, dateIndex) => {
                  const count = getCommitCount(date);
                  return (
                    <div
                      key={dateIndex}
                      className={`contribution-cell ${getCellColor(count)}`}
                      title={`${date.toDateString()}: ${count} commits`}
                    />
                  );
                })}
              </div>
            ))}
          </div>
        </div>
        <div className="contribution-legend">
          <span>Less</span>
          <div className="legend-item">
            <div className="legend-color contribution-cell-0"></div>
          </div>
          <div className="legend-item">
            <div className="legend-color contribution-cell-1"></div>
          </div>
          <div className="legend-item">
            <div className="legend-color contribution-cell-2"></div>
          </div>
          <div className="legend-item">
            <div className="legend-color contribution-cell-3"></div>
          </div>
          <div className="legend-item">
            <div className="legend-color contribution-cell-4"></div>
          </div>
          <span>More</span>
        </div>
      </Card.Body>
    </Card>
  );
};

export default ContributionGraph;