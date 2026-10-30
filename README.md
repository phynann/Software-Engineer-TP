Startup examples:

\$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
... The following 1 profile is active: "dev"
... H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:shop'

\$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
... The following 1 profile is active: "prod"
... H2 console is disabled
