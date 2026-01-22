$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Write-Host "Starting Spring Boot Application..."
.\mvnw.cmd spring-boot:run
