

[README](../README.md)

# SonarCloud Integration Guide

This project is integrated with [SonarCloud](https://sonarcloud.io/) to check code quality and security analysis.

## Setup Instructions
- start docker desktop
- run this script:
`.\scripts\check-sonar-health.ps1`

### SonarCloud Project Configuration
- Log in to [SonarCloud](https://sonarcloud.io/).
- Organization and project key:
- Organization: `keaarimaaproject` (lowercase, as per SonarCloud API).
- Project Key: `KEAArimaaProject_Booking-System-Software-Quality`.

### 2. GitHub Secrets
To enable automatic analysis via GitHub Actions, ensure you have added the following secret to your GitHub repository:
- Go to your repository on GitHub.
- Go to `Settings` > `Secrets and variables` > `Actions`.
- Click `New repository secret`.
- Name: `SONAR_TOKEN`.
- Value: *The SonarCloud analysis token found in your .env or SonarCloud account settings*

Once this secret is added, the `.github/workflows/sonarcloud.yml` will automatically run when pushing or merging to the `main` branch, as well as on pull requests.

### 3. Local Analysis
You can run the analysis locally using Maven. To avoid manually typing your token, you can load it from your `.env` file using the following commands.

**Note:** I have added `<sonar.scanner.skipJreProvisioning>true</sonar.scanner.skipJreProvisioning>` to your `pom.xml`, so you no longer need to worry about the "JRE provisioning" hang.

- **Database Connectivity:** Since `verify` runs integration tests, if your PostgreSQL database (on port 5434 as per `.env`) is not running, the process will fail with connection errors. Ensure your database is active before running the scan.
- **Automatic Analysis Conflict:** If you see an error about "Automatic Analysis" being enabled, you must go to **SonarCloud UI > Administration > Analysis Method** and disable "Automatic Analysis" to allow local Maven scans.
  - **Error Message:** `[ERROR] You are running manual analysis while Automatic Analysis is enabled. Please consider disabling one or the other.`
  - **Direct Link:** [SonarCloud Analysis Method](https://sonarcloud.io/project/configuration?id=KEAArimaaProject_Booking-System-Software-Quality&analysisMode) (Replace with your project key if different)
- **Batch Mode:** Always use the `-B` flag in CI/CD or scripts to ensure Maven doesn't stop to ask for input.
- **Debug Logs:** If it still hangs, add `-X` to the Maven command to see the full debug output.

The analysis will automatically pick up the JaCoCo coverage reports generated during the `verify` phase.

## Change new-code coverage in sonar cloud
When we push code to main in gituhb, we run code coverage of the whole 
project through jacoco. In addition, sonar uses jacoco to 
analyse the new code changes that we try to merge. The coverage
requirement for the new code is 80% by default, can not be set here in the code.
The default is called "Sonar way" and can not be changed 
as long as we are on the free plan. It can be seen here:
- https://sonarcloud.io/project/overview?id=KEAArimaaProject_Booking-System-Software-Quality
- In the left menu, click on "Quality gate" (under the policy section)
- Here, you can see that our current default is "Sonar way".


## Configuration Details

### Maven Configuration (`pom.xml`)
The following properties are configured:
- `sonar.organization`: `KEAArimaaProject`
- `sonar.host.url`: `https://sonarcloud.io`
- `sonar.projectKey`: `KEAArimaaProject_Booking-System-Software-Quality`

The `sonar-maven-plugin` is added to the `<build><plugins>` section.

### GitHub Actions (`.github/workflows/sonarcloud.yml`)
The workflow is configured to run on every push to the `main` branch and on pull requests. It performs a full build, runs tests with JaCoCo coverage, and uploads the results to SonarCloud.

The secrets are set in github here:
- Open the github repository in the browser.
- Go to Settings Click on Settings (top right menu).
- Go to Secrets and variables In the left sidebar, click on Secrets and variables → Actions
- Click the New repository secret button (green button on the right).
- the SONAR_TOKEN secret and its value was added here.


