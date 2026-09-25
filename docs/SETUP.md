# First-Time Repository Setup

This guide assumes one person is the repository owner.

## 1. Install prerequisites

Each developer should install:

- GitHub Desktop
- Git
- a GitHub account
- VS Code or IntelliJ IDEA
- GitHub Copilot extension/plugin and access
- Node.js LTS
- Java 21 or the Java version the team agrees to use

Each developer also needs access to the shared MongoDB Atlas project or cluster used by the team.

Use the same Java and Node versions across the team.

## 2. Create the repository with GitHub Desktop

Repository owner:

1. Open GitHub Desktop.
2. Sign in to GitHub.
3. Choose **File -> New repository**.
4. Name: `frosted-corner`.
5. Choose a local folder.
6. Do not add another README if this starter structure is already in the folder.
7. Create the repository.
8. Copy the contents of this starter into the repository if needed.
9. In GitHub Desktop, review the changed files.
10. Commit with message: `chore: initialize hackathon repository`.
11. Click **Publish repository**.
12. Prefer a private repository unless the hackathon requires public visibility.

Do not initialize separate repositories for frontend/backend. This project should use one repository.

## 3. Invite the other two developers

On GitHub.com:

1. Open the repository.
2. Open repository **Settings**.
3. Find **Collaborators**, **Collaborators and teams**, or the equivalent access-management page.
4. Invite both developers by GitHub username.
5. They must accept the invitation.

Exact GitHub menu labels can vary by account type and current UI.

## 4. Protect `main` when available

On GitHub.com, locate branch protection or repository rules/rulesets and target `main`.

Recommended MVP settings:

- require a pull request before merging
- require at least 1 approval if your plan supports it
- block force pushes
- block branch deletion for `main`
- optionally require branches to be up to date before merge

Do not add a heavy enterprise approval process.

If branch protection is unavailable on your GitHub plan, use the same policy manually: nobody commits directly to `main`.

## 5. Other developers clone the repo

Each invited developer:

1. Accept the GitHub invitation.
2. Open GitHub Desktop.
3. Choose **File -> Clone repository**.
4. Select `frosted-corner`.
5. Pick a local path.
6. Clone.

## 6. Verify collaboration before coding

Each developer should create a tiny test branch:

`chore/<name>-collaboration-check`

Add their name to a temporary text file or make a harmless README edit, commit it, push it, open a PR, and have another developer review it.

After all three have successfully completed this once, delete the temporary test changes if desired.

This verifies permissions, branching, pushing, PRs, and review before implementation work begins.

## 7. Scaffold the applications

After collaboration is verified:

Developer 1:
- scaffold React + Vite under `frontend/`

Developer 2:
- scaffold one Spring Boot app under `backend/`
- include Spring Web, Spring Data MongoDB, Validation, and test dependencies

Developer 3:
- do not create a separate AI service
- prepare the provider-neutral `AiClient` and deterministic `DemoAiClient` implementation inside the Spring Boot project after the backend baseline exists

Merge the baseline scaffolding before large parallel changes.

## 8. Local environment

Frontend `.env`:

`VITE_API_BASE_URL=http://localhost:8080`

Backend environment variables should provide MongoDB Atlas configuration. Never commit real credentials.

Required backend variables:

- `MONGODB_URI`: MongoDB Atlas connection string
- `MONGODB_DATABASE`: Atlas database name for the application

No external AI variables are required for the MVP.

The backend loads optional local overrides from `backend/.env` through Spring config import. A typical local developer file can look like:

```properties
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>/<options>
MONGODB_DATABASE=frosted-corner
```

Do not commit `backend/.env` or paste real Atlas credentials into repository files, issues, or pull requests.
