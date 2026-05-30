1. setup .env 
2. cd .\database\
3. docker compose -f docker-compose.psql.yml up -d
4. mvn spring-boot:run or .\mvnw.cmd spring-boot:run

- Run all tests in the terminal root:
`scripts/run-all-tests.ps1`

[Jacoco Documentation](READMEfiles/Jacoco.md)
[SonarCloud Integration Guide](READMEfiles/SonarCloud.md)



