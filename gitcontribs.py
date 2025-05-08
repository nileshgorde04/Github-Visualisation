#!/usr/bin/env python3
"""
Git Contributions Visualization Tool

This tool visualizes Git contributions across multiple repositories.
"""

import os
import argparse
import datetime
import subprocess
import json
from typing import List, Dict, Optional, Tuple

def find_git_repositories(root_dir: str) -> List[str]:
    """
    Find all Git repositories under the given root directory.

    Args:
        root_dir: The root directory to start the search from.

    Returns:
        A list of paths to Git repositories.
    """
    git_repos = []

    for dirpath, dirnames, filenames in os.walk(root_dir):
        if '.git' in dirnames:
            git_repos.append(dirpath)
            # Don't descend into .git directories
            dirnames.remove('.git')

    return git_repos

def get_git_user() -> Tuple[str, str]:
    """
    Get the current Git user's name and email.

    Returns:
        A tuple containing the user's name and email.
    """
    try:
        name = subprocess.check_output(
            ["git", "config", "user.name"], 
            universal_newlines=True
        ).strip()

        email = subprocess.check_output(
            ["git", "config", "user.email"], 
            universal_newlines=True
        ).strip()

        return name, email
    except subprocess.CalledProcessError:
        return "Unknown", "Unknown"

def get_commits(repo_path: str, days: int, author_email: str) -> List[Dict]:
    """
    Get commits for a specific repository within a date range by a specific author.

    Args:
        repo_path: Path to the Git repository.
        days: Number of days to look back.
        author_email: Email of the author to filter commits by.

    Returns:
        A list of commit dictionaries.
    """
    since_date = (datetime.datetime.now() - datetime.timedelta(days=days)).strftime("%Y-%m-%d")

    try:
        # Change to the repository directory
        original_dir = os.getcwd()
        os.chdir(repo_path)

        # Get commits in JSON format
        git_log_cmd = [
            "git", "log", 
            f"--since={since_date}", 
            f"--author={author_email}", 
            "--pretty=format:{\"hash\":\"%H\",\"author_name\":\"%an\",\"author_email\":\"%ae\",\"date\":\"%ad\",\"message\":\"%s\"}",
            "--date=iso"
        ]

        output = subprocess.check_output(git_log_cmd, universal_newlines=True)

        # Change back to the original directory
        os.chdir(original_dir)

        # Parse the JSON output
        commits = []
        if output:
            for line in output.strip().split("\n"):
                if line:
                    commits.append(json.loads(line))

        return commits
    except (subprocess.CalledProcessError, json.JSONDecodeError) as e:
        print(f"Error getting commits for {repo_path}: {e}")
        # Change back to the original directory in case of error
        if 'original_dir' in locals():
            os.chdir(original_dir)
        return []

def calculate_stats(commits: List[Dict]) -> Dict:
    """
    Calculate statistics from a list of commits.

    Args:
        commits: A list of commit dictionaries.

    Returns:
        A dictionary with statistics.
    """
    if not commits:
        return {
            "total_commits": 0,
            "commits_by_date": {},
            "first_commit": None,
            "last_commit": None
        }

    # Initialize stats
    stats = {
        "total_commits": len(commits),
        "commits_by_date": {},
        "first_commit": None,
        "last_commit": None
    }

    # Process each commit
    for commit in commits:
        # Parse the date
        date_str = commit["date"].split()[0]  # Get just the date part

        # Count commits by date
        if date_str in stats["commits_by_date"]:
            stats["commits_by_date"][date_str] += 1
        else:
            stats["commits_by_date"][date_str] = 1

    # Sort commits by date to find first and last
    sorted_commits = sorted(commits, key=lambda x: x["date"])
    if sorted_commits:
        stats["first_commit"] = sorted_commits[0]
        stats["last_commit"] = sorted_commits[-1]

    return stats

def generate_ascii_graph(commits_by_date: Dict[str, int], days: int) -> str:
    """
    Generate an ASCII representation of the contribution graph.

    Args:
        commits_by_date: Dictionary mapping dates to commit counts.
        days: Number of days to include in the graph.

    Returns:
        ASCII representation of the contribution graph.
    """
    # Generate a list of dates for the last 'days' days
    end_date = datetime.datetime.now().date()
    start_date = end_date - datetime.timedelta(days=days-1)

    date_range = []
    current_date = start_date
    while current_date <= end_date:
        date_range.append(current_date.strftime("%Y-%m-%d"))
        current_date += datetime.timedelta(days=1)

    # Create the graph
    graph = []

    # Add header with month names
    months = []
    current_month = ""
    for date_str in date_range:
        month = date_str.split("-")[1]  # Extract month from YYYY-MM-DD
        if month != current_month:
            current_month = month
            month_name = datetime.datetime.strptime(month, "%m").strftime("%b")
            months.append((len(months), month_name))

    month_header = " " * 4
    for pos, name in months:
        padding = " " * (pos * 2)
        month_header += padding + name

    graph.append(month_header)

    # Add the contribution cells
    row = "    "
    for date_str in date_range:
        count = commits_by_date.get(date_str, 0)
        if count == 0:
            row += "·"
        elif count < 5:
            row += "▪"
        elif count < 10:
            row += "▫"
        else:
            row += "█"
        row += " "

    graph.append(row)

    # Add a legend
    graph.append("\nLegend:")
    graph.append("· - No commits")
    graph.append("▪ - 1-4 commits")
    graph.append("▫ - 5-9 commits")
    graph.append("█ - 10+ commits")

    return "\n".join(graph)

def main():
    parser = argparse.ArgumentParser(description='Visualize Git contributions.')
    parser.add_argument('--root', default='.', help='Root directory to search for Git repositories')
    parser.add_argument('--days', type=int, default=30, help='Number of days to analyze')
    parser.add_argument('--email', help='Git user email (defaults to current user)')

    args = parser.parse_args()

    # Get user information
    user_name, user_email = get_git_user()
    if args.email:
        user_email = args.email

    print(f"Analyzing contributions for: {user_name} <{user_email}>")

    # Find Git repositories
    repos = find_git_repositories(args.root)

    if not repos:
        print(f"No Git repositories found under {args.root}")
        return

    print(f"Found {len(repos)} Git repositories:")

    # Collect all commits across repositories
    all_commits = []
    for repo in repos:
        print(f"  - {repo}")
        repo_commits = get_commits(repo, args.days, user_email)
        all_commits.extend(repo_commits)
        print(f"    Found {len(repo_commits)} commits in the last {args.days} days")

    # Calculate overall stats
    stats = calculate_stats(all_commits)

    print(f"\nTotal commits in the last {args.days} days: {stats['total_commits']}")

    if stats['total_commits'] > 0:
        print("\nContribution Graph:")
        graph = generate_ascii_graph(stats['commits_by_date'], args.days)
        print(graph)

if __name__ == "__main__":
    main()
