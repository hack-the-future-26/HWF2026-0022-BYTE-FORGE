# TitleVerify AI

## 🔗 Project Links

- **Live Prototype:** [Try TitleVerify AI](https://titleverify-ai.onrender.com/)

### Pre-Submission Title Verification & AI-Assisted Headline Selection

TitleVerify AI is an AI-assisted title verification system designed to help newspapers and other publications check proposed publication titles against existing registered titles before submission.

The system combines traditional text-matching algorithms, phonetic analysis, BM25 relevance ranking, AI-based semantic similarity, rule-based verification, risk scoring, and explainable decision-making.

It also provides an AI Headline Generator that can generate multiple headline options from article content, verify each generated headline, and recommend the most suitable option.


## 1. Problem Statement

When a new publication title is submitted, it is important to check whether the proposed title is already registered or is too similar to an existing title.

The challenge is that title similarity is not always an exact match.

Two titles may:

- Have different spelling
- Use slightly different words
- Sound similar
- Share important words
- Have similar meanings
- Be similar only in certain publication contexts

Therefore, checking only for exact matches is not sufficient.

TitleVerify AI addresses this problem by combining multiple verification techniques instead of relying on a single similarity algorithm.


# 2. Solution

TitleVerify AI provides an automated pre-submission screening workflow.

The user provides:

- Publication type
- Language
- State / Union Territory
- District
- Periodicity
- One or more proposed titles

The system then processes each proposed title through multiple verification stages.

### Verification Pipeline

Proposed Title
      |
      v
Title Normalization
      |
      v
Exact Match Check
      |
      v
Candidate Retrieval
      |
      +-----------------------------+
      |                             |
      v                             v
Fuzzy Similarity              Phonetic Similarity
      |                             |
      +-------------+---------------+
                    |
                    v
             Gemini Embeddings
          Semantic Similarity
                    |
                    v
              BM25 Matching
                    |
                    v
              Rule Engine
                    |
                    v
              Risk Scoring
                    |
                    v
            Decision Engine
                    |
                    v
        Decision + Explanation

The final result can be:

ACCEPT
REVIEW
HIGH RISK

3. Key Features
3.1 Multi-Signal Title Verification

TitleVerify AI does not depend on a single similarity method.

It combines:

Exact matching
Fuzzy similarity
Phonetic similarity
BM25 relevance matching
Gemini semantic similarity

This provides a broader view of title similarity.

3.2 Exact Match Detection

The system first checks whether the proposed title already exists in the registered title database.

This helps identify direct title conflicts quickly.

3.3 Fuzzy Similarity

Fuzzy similarity is used to identify textual differences and spelling variations.

The current implementation uses Levenshtein distance.

For example:

Karnataka News
Karnataka Newz

Even though the spelling is different, fuzzy similarity can identify the relationship between the titles.

3.4 Phonetic Similarity

Phonetic matching helps identify titles that may sound similar even when their spelling is different.

The project uses a custom MetaphoneEncoder and then compares the encoded values using fuzzy similarity.

This helps detect cases where spelling alone may not reveal similarity.

3.5 BM25 Title Matching

BM25 is used for word-level relevance and candidate ranking.

It helps identify titles that share important words with the proposed title.

BM25 is different from semantic embeddings:

BM25
→ Word-level relevance

Gemini Embeddings
→ Meaning-level similarity

The system uses both approaches together.

After registered titles are retrieved, BM25 can rank the candidates based on their word-level relevance.

3.6 Gemini Semantic Similarity

TitleVerify AI uses Gemini embeddings for semantic similarity.

The system converts titles into numerical embedding vectors and compares them using cosine similarity.

This allows the system to identify titles that may have similar meanings even when their exact words are different.

The project currently uses:

gemini-embedding-001

for semantic embeddings.

3.7 Rule-Based Verification

Similarity alone is not enough for title verification.

The system also applies verification rules.

Current rules include:

Prohibited Word Rule
Periodicity Modifier Rule
Prefix / Suffix Rule
Combination Rule
Context Rule

These rules provide additional verification signals alongside the similarity algorithms.

3.8 Risk Scoring

The system combines the verification signals into a deterministic prototype risk score.

The current development scoring configuration includes:

Exact Match       → 100
Fuzzy Similarity  → 30
Phonetic          → 25
Semantic          → 25

HIGH rule         → +40
WARNING rule      → +15
INFO rule         → +0

Decision thresholds:

Risk < 40
→ ACCEPT

Risk 40–69.99
→ REVIEW

Risk >= 70
→ HIGH RISK

An exact match or a HIGH severity rule can force a HIGH RISK decision.

Note: These values are prototype scoring configurations and are not official PRGI approval probabilities.

4. Explainable Verification

TitleVerify AI does not only provide a final decision.

It also explains why the title received that result.

The verification report can show:

Risk score
Approval confidence
Similarity values
Closest registered title
Rule findings
Recommendation
Explanation

Example:

Decision: REVIEW

Risk Score: 52.5 / 100

Main Reason:
High semantic similarity with an existing registered title.

This makes the result easier for a reviewer to understand.

5. Five-Title Comparison

The system allows users to submit up to five proposed titles.

Each title is independently verified.

The system then provides a comparison of:

Decision
Risk score
Risk level
Confidence
BM25 similarity
Fuzzy similarity
Phonetic similarity
Semantic similarity
Closest registered title

The system also recommends the best option using a deterministic ranking strategy.

Recommendation Priority
1. Better decision
2. Lower risk score
3. Fewer triggered rules
4. Lower maximum similarity
5. Original applicant preference order

This allows users to compare multiple title options before choosing one.

6. AI Headline Generator

TitleVerify AI also includes an AI-assisted headline generation feature.

The user provides:

News topic or article content
Publication type
Language
State
District
Periodicity

The system generates five possible headlines.

Each generated headline is then passed through the title verification pipeline.

Article Content
      |
      v
Gemini Headline Generation
      |
      v
5 Candidate Headlines
      |
      v
Title Verification Pipeline
      |
      v
Content Relevance
+
Title Similarity
+
Rules
+
Risk Score
      |
      v
Recommended Headline

The system therefore does not simply generate headlines.

It:

GENERATE
   ↓
VERIFY
   ↓
COMPARE
   ↓
CHOOSE
7. Verification History

TitleVerify AI stores submitted publication applications and their proposed titles.

Users can access previous applications through the Verification History page.

The history view provides:

Application ID
Publication metadata
Proposed titles
Verification status
Date/time
View Analysis
Compare Titles
Download PDF Report

This makes previous verification results easier to revisit.

8. Downloadable PDF Reports

The system can generate a complete verification report as a PDF.

The report includes:

Document Information
TitleVerify AI
Report reference
Verification engine version
Application Profile
Application ID
Publication type
Language
State
District
Periodicity
Submission date
Number of titles
Executive Summary
Recommended option
Recommended title
Decision
Risk score
Risk level
Approval confidence
Recommendation explanation
Title Comparison

For every proposed title:

Decision
Risk score
Risk level
Confidence
BM25
Fuzzy
Phonetic
Semantic
Closest match
Detailed Findings
Similarity results
Rule findings
Recommendations
Disclaimer

The PDF is generated directly using OpenPDF.

9. Registered Title CSV Import

The system supports importing registered publication titles through CSV.

Expected CSV format:

Title,Language,State,Periodicity,Publication Type
Karnataka Morning,English,Karnataka,Daily,Newspaper

The import process:

CSV Upload
     |
     v
Validate Rows
     |
     v
Normalize Titles
     |
     v
Check Duplicates
     |
     v
Save New Titles
     |
     v
Refresh BM25 Corpus

Imported records are tagged as:

IMPORTED

Existing development records remain:

DEMO_DATA

Existing records are never overwritten.

10. Duplicate Detection

During CSV import, the system checks for duplicate titles.

Duplicates are detected:

Within the uploaded CSV
Against existing database records
After title normalization

For example:

Karnataka Herald
karnataka herald
  Karnataka Herald

can be identified as the same normalized title.

Duplicate records are skipped instead of being inserted again.

The import result clearly separates:

Skipped Duplicates

from:

Import Errors

This prevents duplicate records from being incorrectly treated as actual import failures.

11. BM25 Corpus Refresh

BM25 maintains an in-memory corpus based on registered titles.

After new titles are imported:

New Titles Imported
        |
        v
Database Updated
        |
        v
BM25 Corpus Refreshed

The corpus is refreshed once after the complete import operation rather than after every individual row.

This keeps the import process efficient.

12. Technology Stack
Backend
Java 17
Spring Boot
Spring Data JPA
Hibernate
Maven
Database
PostgreSQL
Frontend
HTML
CSS
JavaScript
Thymeleaf
AI
Google Gemini
Gemini Embeddings
gemini-embedding-001
Algorithms
Exact Matching
Levenshtein Distance
Metaphone-based Phonetic Matching
BM25
Cosine Similarity
Rule-based Verification
PDF
OpenPDF
Development Tools
Git
GitHub
VS Code / Antigravity IDE
13. System Architecture

The project follows a layered Spring Boot architecture.

Frontend
   |
   v
Controllers
   |
   v
Application / Service Layer
   |
   +-------------------------+
   |                         |
   v                         v
Verification Services      Import Services
   |                         |
   v                         v
Similarity + Rules        Registry Database
   |
   v
Risk & Decision Engine
   |
   v
PostgreSQL
14. Backend Verification Architecture

The main verification process is coordinated by:

VerificationService

The service coordinates:

TitleNormalizationService
ExactMatchService
CandidateRetrievalService
FuzzySimilarityService
PhoneticSimilarityService
SemanticSimilarityService
Bm25SimilarityService
RuleEngineService
RiskScoringService
DecisionEngineService
DecisionExplanationService

This keeps the verification process modular.

15. Database

The project uses PostgreSQL.

Important entities include:

PublicationApplication

Stores application-level information such as:

Publication type
Language
State
District
Periodicity
Submission information
ProposedTitle

Stores proposed titles associated with an application.

RegisteredPublicationTitle

Stores registered title information used during verification.

Important registry fields include:

id
title
normalizedTitle
language
state
periodicity
publicationType
status
16. Registered Title Data

The prototype initially uses synthetic development data.

Seeded records are marked:

DEMO_DATA

Imported records are marked:

IMPORTED

The system is designed so that a future production deployment can connect the registry to an authorized official data source.

17. Project Structure

A simplified project structure:

titleverify-ai/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/titleverify/titleverify_ai/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   └── js/
│   │       │
│   │       └── templates/
│   │
│   └── test/
│       └── java/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
18. Main User Workflow
Workflow 1 — Verify Proposed Titles
Open Application Analyzer
        |
        v
Enter Publication Details
        |
        v
Enter 1–5 Proposed Titles
        |
        v
Analyze Titles
        |
        v
Verification Pipeline
        |
        v
Risk + Decision + Explanation
        |
        v
Detailed Verification Report
Workflow 2 — Compare Titles
Submit up to 5 titles
        |
        v
Verify each title
        |
        v
Compare all results
        |
        v
Rank options
        |
        v
Recommend best title
Workflow 3 — Generate Headlines
Enter article content
        |
        v
Generate 5 headlines
        |
        v
Verify all headlines
        |
        v
Compare relevance + verification
        |
        v
Recommend headline
Workflow 4 — Import Registered Titles
Prepare CSV
      |
      v
Upload CSV
      |
      v
Validate + Normalize
      |
      v
Duplicate Detection
      |
      v
Import New Titles
      |
      v
Refresh BM25 Corpus
19. API Endpoints

Important application routes include:

/registry/import

Used for the registered title import page.

POST /registry/import

Used to upload a CSV registry file.

/report/{applicationId}/download

Used to download the PDF verification report.

Other application routes provide:

Application analysis
Verification history
Detailed verification results
Five-title comparison
Headline generation
20. Testing

The project contains unit, controller, and integration tests.

Testing covers:

Similarity calculations
Verification services
Gemini failure handling
Database failure handling
Invalid title input
BM25 matching
Verification history
Five-title comparison
PDF generation
PDF download
Registry CSV import
Duplicate handling
Registry import UI
Controller endpoints
Template rendering

The latest complete test suite has:

212 / 212 tests passing
0 failures
0 errors
0 skipped
BUILD SUCCESS
21. Error Handling & Reliability

The system is designed to avoid complete pipeline failure when individual components encounter problems.

Examples include:

Database query failures
Gemini/API failures
Invalid title input
Empty titles
Malformed CSV rows
Duplicate registry records
Invalid uploads

For example, if semantic analysis becomes unavailable, the verification pipeline can continue using the other available signals rather than completely failing.

22. Security Considerations

The prototype follows basic security practices including:

Parameterized database access through JPA
No hardcoded API secrets in source code
Environment-based configuration for external services
Validation of uploaded files
Validation of title input
Duplicate protection
Separation of development/demo data from imported data

Secrets and API keys should never be committed to GitHub.

23. Limitations

This project is currently a prototype.

Important limitations include:

The registry data used for demonstration is synthetic development data.
Imported registry data depends on the quality and structure of the uploaded CSV.
Risk scores and confidence values are prototype estimates.
They do not represent official PRGI approval probabilities.
Final title approval remains with the appropriate official authority.
Production deployment would require an authorized and reliable registered-title data source.
Similarity thresholds and rule weights require further validation using real-world datasets.

24. Future Enhancements

Potential future improvements include:

Integration with an authorized official registered-title database
Automated registry synchronization
More advanced multilingual semantic matching
Improved language-specific phonetic algorithms
Better domain-specific similarity models
Advanced audit logs
User authentication and role-based access
Administrative registry management
Improved analytics and dashboards
Production-grade monitoring
Scalable vector search
More extensive real-world validation
Improved risk calibration using verified historical outcomes

25. Demo Data vs Production Data

The prototype clearly distinguishes between:

DEMO_DATA

and:

IMPORTED

This distinction is intentional.

Demo records are used to demonstrate and test different verification scenarios.

Imported records represent registry data supplied through the CSV import functionality.

In a production environment, the registry should be populated using an authorized official data source.

26. Why a Hybrid Approach?

TitleVerify AI does not depend entirely on AI.

Instead, it combines:

Traditional Algorithms
        +
Rule-Based Verification
        +
BM25
        +
AI Semantic Embeddings
        +
Risk & Decision Logic

This hybrid approach provides multiple independent signals.

For example:

BM25
"Are important words relevant?"

Fuzzy
"Are the texts similar?"

Phonetic
"Do the titles sound similar?"

Gemini
"Do the titles have similar meaning?"

Rules
"Are there contextual or publication-specific concerns?"

These signals are then combined before generating the final decision.

27. Installation
Prerequisites

Install:

Java 17
PostgreSQL
Git

Maven Wrapper is included in the project, so a separate Maven installation is not required for normal project execution.

Clone the Repository
git clone <https://github.com/hack-the-future-26/HWF2026-0022-BYTE-FORGE>
cd titleverify-ai
Configure PostgreSQL

Create a PostgreSQL database and configure the database connection in the application's environment/configuration.

Example configuration:

spring.datasource.url=jdbc:postgresql://localhost:5432/titleverify
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

Do not commit real passwords or API keys.

Configure Gemini

Add the required Gemini API configuration through environment variables or the application's local configuration.

Never commit the API key to GitHub.

28. Run the Application

On Windows:

.\mvnw.cmd spring-boot:run

The application will start as a Spring Boot web application.

Open the application in a browser using the configured local server address.

29. Run Tests

Run the complete test suite:

.\mvnw.cmd test

A successful build should report:

BUILD SUCCESS
30. CSV Import Format

The registered title importer expects:

Title,Language,State,Periodicity,Publication Type
Karnataka Morning,English,Karnataka,Daily,Newspaper
Required field
Title
Optional fields
Language
State
Periodicity
Publication Type

Blank titles are rejected.

Duplicate titles are skipped.

31. Example Verification

Example:

Proposed Title:
Darshan News

The system may find:

Closest Match:
Namaskar News

Example signals:

BM25       → 0.13
Fuzzy      → 61.5%
Phonetic   → 45.5%
Semantic   → 90.8%

The system then combines these signals with rule findings and produces a final decision.

32. Project Goal

The goal of TitleVerify AI is to make the initial title verification process:

Faster
More consistent
Explainable
Easier to review
Less dependent on manual comparison

The system combines traditional algorithms, rules, and AI rather than depending on a single technique.

33. Team
Team Byte Forge

Project: TitleVerify AI

SIH Problem Statement: SIH1782

The project was developed as a collaborative hackathon solution using a GitHub-based team workflow with separate development branches, pull requests, reviews, and protected main branch practices.

34. Disclaimer

DISCLAIMER: TitleVerify AI is an automated pre-submission screening prototype. Risk scores, confidence values, and similarity signals are advisory and do not represent official PRGI approval decisions or legal clearance.

Final approval must follow the applicable official process.

35. Summary

TitleVerify AI provides an end-to-end title verification workflow:

CREATE / SUBMIT
       ↓
NORMALIZE
       ↓
EXACT MATCH
       ↓
CANDIDATE RETRIEVAL
       ↓
FUZZY + PHONETIC
       ↓
BM25 + GEMINI SEMANTIC
       ↓
RULE VERIFICATION
       ↓
RISK SCORING
       ↓
DECISION
       ↓
EXPLANATION
       ↓
COMPARE
       ↓
PDF REPORT
       ↓
VERIFICATION HISTORY

For headline generation:

ARTICLE
   ↓
GENERATE 5 HEADLINES
   ↓
VERIFY EACH HEADLINE
   ↓
COMPARE
   ↓
RECOMMEND

For registry management:

CSV
 ↓
VALIDATE
 ↓
NORMALIZE
 ↓
DEDUPLICATE
 ↓
IMPORT
 ↓
REFRESH BM25
TitleVerify AI

Generate. Verify. Compare. Explain. Choose.



# 🚀 Hack the Future 26
## Team Repository Guide

Welcome to the official GitHub repository for **Team `HTF26-021-Byte-Forge`**.

This repository is your team's workspace for developing and submitting your Hack the Future 26 project.

You will use this repository to:

- Store your project code
- Work together with your teammates
- Keep track of changes
- Review each other's work
- Submit your final project

---

# 🔐 Important: How This Repository Works

The `main` branch is **protected**.

This means:

> **You cannot directly push your changes to `main`.**

Don't worry. This is intentional and helps prevent someone from accidentally breaking the team's main code.

Instead, everyone should work on their **own branch** and then create a **Pull Request**.

The basic workflow is:

```text
Create a branch
      ↓
Make your changes
      ↓
Save your changes
      ↓
Push your branch
      ↓
Create a Pull Request
      ↓
Another teammate reviews it
      ↓
1 approval required
      ↓
Merge into main
Think of main as the team's safe and stable version of the project.

🟢 1. Start Working on the Project

When you open the repository, you will see options such as:

Code
Issues
Pull requests
Actions

You normally do not need to change anything in the repository settings.

The organizers have already configured the repository rules.

🌿 2. Create Your Own Branch

A branch is your own working area inside the repository.

For example:

main
│
├── feature-login
├── feature-ai-model
├── fix-camera
└── docs-readme

You work on your branch instead of directly changing main.

🖱️ Method A: Using GitHub Website

This is the easiest method if you are new to Git.

Step 1

Open the repository on GitHub.

Click the branch selector near the top of the file list.

You will see something similar to:

main ▼
Step 2

Type the name of your new branch.

Example:

feature-login

Step 3

GitHub will show an option similar to:

Create branch: feature-login from main

Click it.

🎉 Your branch has now been created.

💻 Method B: Using Git

If you are using Git on your computer:

git checkout -b feature-login

Then check your current branch:

git branch

You should see:

* feature-login
  main

The * means you are currently working on feature-login.

🛠️ 3. Make Your Changes

Now work normally.

You can:

Add files
Edit files
Delete files
Add features
Fix bugs
Improve documentation

Your changes are happening on your branch, not directly on main.

💾 4. Save Your Changes

There are two ways to save your work to GitHub.

🖱️ Method A: Using GitHub Website

If you are creating or editing a file directly on GitHub:

Open the file.
Click the pencil/Edit button.
Make your changes.
Scroll down to the commit section.
Enter a short description.

Example:

Add login page
Choose:

Create a new branch for this commit and start a pull request

Click Propose changes.

Your changes will now be saved to a branch.

💻 Method B: Using Git

After changing files on your computer:

git add .

Create a commit:

git commit -m "Add login page"

A commit is basically a saved checkpoint of your work.

⬆️ 5. Push Your Branch to GitHub

If you are using Git locally:

git push origin feature-login

Your branch will now appear on GitHub.

If you are using the GitHub website, you do not need this step.

🔀 6. Create a Pull Request

A Pull Request, usually called a PR, means:

"I finished my changes. Can someone check them before they become part of main?"

🖱️ Creating a PR from GitHub

After pushing your branch, GitHub may show:

Compare & pull request

Click it.

If you don't see it:

Open Pull requests.
Click New pull request.
Select:
base: main
compare: your-branch

Example:

base: main
compare: feature-login
Add a clear title

Good:

Add user login system

Bad:

changes
Explain what you did

Example:

## What I changed

- Added login page
- Added email validation
- Added logout button

## Testing

- Tested login with valid credentials
- Tested invalid password

Then click:

Create pull request

👀 7. Ask a Teammate to Review

Your Pull Request needs to be checked.

Our repository requires:

At least 1 approval before merging.

A teammate should check:

Does the code work?
Does the feature do what it should?
Is anything broken?
Is the code understandable?
Are there unnecessary changes?
Are passwords or API keys accidentally included?

If everything looks good, the reviewer can click:

Approve

💬 8. What If the Reviewer Finds a Problem?

Don't worry.

You do not need to create another Pull Request.

Make the required changes on the same branch.

For example:

Reviewer:
"Please fix the login validation."

        ↓

You fix it

        ↓

Commit the change

        ↓

Push the branch

        ↓

The existing PR automatically updates

The reviewer can then check the new changes.

✅ 9. Merge the Pull Request

Once the Pull Request has received the required approval:

Check that the required approval is present.
Check that there are no important problems.
Click Merge pull request.
Confirm the merge.

The repository allows:

Merge commit
Squash and merge
Rebase and merge

If your team isn't sure which one to use, Squash and merge is a simple choice for many small hackathon changes.

🚫 10. Don't Push Directly to Main

Do not try to push directly to main.

For example, this is not allowed:

git push origin main

Instead:

Create branch
      ↓
Make changes
      ↓
Pull Request
      ↓
1 teammate approves
      ↓
Merge

This protects everyone's work.

🔥 11. Never Force-Push to Main

Do not try to force-push to main.

Avoid:

git push --force

The protected main branch is designed to prevent this.

🔑 12. NEVER Upload Passwords or API Keys

Very important.

Never put these inside your repository:

API keys
Passwords
Access tokens
Private keys
Database passwords
.env files containing real secrets

For example, do NOT commit:

API_KEY=123456789abcdef

inside a public repository.

If you accidentally upload a secret:

Tell the organizers immediately.

Simply deleting the file may not be enough because the secret could still exist in Git history.

📁 13. Keep the Repository Organized

Try to keep the project clean.

For example:

project/
│
├── src/
├── docs/
├── tests/
├── README.md
├── requirements.txt
└── .gitignore

Don't upload unnecessary files such as:

Huge videos
Temporary files
Build files
Personal files
Passwords
IDE-specific junk

Use .gitignore where appropriate.

🏷️ 14. Use Clear Branch Names

Good examples:

feature-login
feature-chatbot
feature-dashboard
fix-payment-error
fix-camera-bug
docs-installation

Avoid names such as:

test
abc
new
branch1
mybranch
asdf

A clear branch name makes teamwork easier.

📝 15. Use Clear Commit Messages

A commit message should tell your teammates what you changed.

Good
Add login page
Fix camera initialization
Add chatbot API
Update project documentation
Avoid
changes
update
done
final
final2
final-final

Keep commit messages short and meaningful.

🧑‍💻 16. Recommended Team Workflow

For every new feature:

1. Start from main
        ↓
2. Create a new branch
        ↓
3. Work on the feature
        ↓
4. Commit your changes
        ↓
5. Push the branch
        ↓
6. Create Pull Request
        ↓
7. Teammate reviews
        ↓
8. Get 1 approval
        ↓
9. Merge into main
        ↓
10. Start the next feature
🆘 17. If You Get Stuck
GitHub says your push was rejected

Check that you are not trying to push directly to main.

Create a branch instead.

Your Pull Request cannot be merged

Check whether:

You have the required approval.
There are merge conflicts.
GitHub is showing another problem.
You accidentally committed a secret

Tell the organizers immediately.

You don't understand Git

That's okay.

Ask your teammates or the Hack the Future 26 organizers for help.

🏆 Hack the Future 26

Build together. Review together. Ship together.

Keep main stable.

Work in branches.

Use Pull Requests.

Get your teammate's approval.

Then merge.

Happy hacking! 🚀