import React, { useState } from 'react';
import { Container, Row, Col, Form, Button, Alert, Spinner } from 'react-bootstrap';
import ContributionGraph from './components/ContributionGraph';
import axios from 'axios';

function App() {
  const [rootDirectory, setRootDirectory] = useState('');
  const [remoteRepoUrl, setRemoteRepoUrl] = useState('');
  const [days, setDays] = useState(30);
  const [userEmail, setUserEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [contributionData, setContributionData] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await axios.post('/api/contributions', {
        rootDirectory,
        days,
        userEmail: userEmail || null,
        remoteRepoUrl: remoteRepoUrl || null
      });
      setContributionData(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred while fetching contribution data');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="py-4">
      <Row className="header">
        <Col>
          <h1>Git Contributions Visualization Tool</h1>
          <p className="lead">Visualize your Git contributions across multiple repositories</p>
        </Col>
      </Row>

      <Row>
        <Col md={6} className="mx-auto">
          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Root Directory</Form.Label>
              <Form.Control
                type="text"
                placeholder="Enter the root directory to search for Git repositories"
                value={rootDirectory}
                onChange={(e) => setRootDirectory(e.target.value)}
                required={!remoteRepoUrl}
                disabled={!!remoteRepoUrl}
              />
              <Form.Text className="text-muted">
                The tool will search for Git repositories in this directory and its subdirectories.
              </Form.Text>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Remote Repository URL (Optional)</Form.Label>
              <Form.Control
                type="text"
                placeholder="Enter a remote Git repository URL (e.g., https://github.com/username/repo.git)"
                value={remoteRepoUrl}
                onChange={(e) => setRemoteRepoUrl(e.target.value)}
                disabled={!!rootDirectory}
              />
              <Form.Text className="text-muted">
                Alternatively, you can analyze a single remote repository by providing its URL.
              </Form.Text>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Number of Days</Form.Label>
              <Form.Control
                type="number"
                min="1"
                max="365"
                value={days}
                onChange={(e) => setDays(parseInt(e.target.value))}
                required
              />
              <Form.Text className="text-muted">
                The number of days to analyze contributions for.
              </Form.Text>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Git User Email (Optional)</Form.Label>
              <Form.Control
                type="email"
                placeholder="Enter your Git user email"
                value={userEmail}
                onChange={(e) => setUserEmail(e.target.value)}
              />
              <Form.Text className="text-muted">
                If not provided, the tool will use the email from your Git configuration.
              </Form.Text>
            </Form.Group>

            <Button variant="primary" type="submit" disabled={loading}>
              {loading ? (
                <>
                  <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" />
                  <span className="ms-2">Analyzing...</span>
                </>
              ) : (
                'Analyze Contributions'
              )}
            </Button>
          </Form>
        </Col>
      </Row>

      {error && (
        <Row className="mt-4">
          <Col>
            <Alert variant="danger">{error}</Alert>
          </Col>
        </Row>
      )}

      {contributionData && (
        <Row className="mt-4">
          <Col>
            <Alert variant="success">
              Found {contributionData.totalRepositories} repositories with a total of {contributionData.totalCommits} commits
              by {contributionData.userName} ({contributionData.userEmail})
            </Alert>
            <ContributionGraph data={contributionData} />
          </Col>
        </Row>
      )}
    </Container>
  );
}

export default App;
