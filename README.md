NOTES:
- Tests are in EnrichServiceTest and EnrichControllerTest
- Application uses super-csv (which is very handy for processing large files and validations)
- EnrichService.enrichCsv validates cvs by CallProcessors (which enforce format)
- ProductRepository is loading all products into memory, I suggest to use cached JPA repository to load products from database (or other datasource).

TODO:
- use DeferredResult (in controller) for non-blocking calls
- Spring Boot 3 requires upgrade of Java (to Java 17)
- add Swagger