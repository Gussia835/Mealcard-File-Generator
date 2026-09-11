# MealCard File Generator

Сервис генерации тестовых файлов банковских ENROLL-форматов с возможностью отправки черезразличные транспортные протоколы (multipart, chunked encoding, gRPC).

## Project Structure

<details>
  <summary> Нажмите, чтобы посмотреть структуру папок</summary>

```text
  ru.mealcard/
  ├── config/
  │   ├── App
  │   ├── Config
  │   ├── PropertyLoader
  |
  ├── controller/       
  │   ├── ErrorHandler
  │   ├── GenerateHandler
  │   ├── MockHandler
  │   └── SendHandler
  |
  ├── exception/           
  |    ├── BlankFileException
  |    ├── ConfigurationException
  |    ├── ContentFileException
  |    ├── FileGenerationException
  |    ├── InvalidRequestException
  |    ├── SendException
  |    ├── UnknownFormatException
  |    ├── UnsupportedDataTypeException
  |    └── WrongEncodingException 
  |
  ├── service/
  │   └── dto/
  |   |     ├── ErroDTO
  |   |     ├── GenerateRequestDTO
  |   |     ├── MockRequestDTO
  |   |     ├── ReponseDTO
  |   |
  |  └── error/
  |  |    └── dto/
  |  |    |      └─ RequestErrorDTO
  |  |    ├── ErrorService
  |  |    ├── FileCorruptor
  |  |
  |   └── format/
  |   |    └── dto/ 
  |   |    |   ├── DataForEnrollDTO
  |   |    |   ├── EnrollDTO
  |   |    └── impl/
  |   |       ├── EnrollVisitor
  |   |
  |   └── generate/
  |   |    ├── GenerateService
  │   |
  |   └── mock/
  |   |     ├── MockDataService
  |   |     ├── MockService
  |   |
  │   └── send/
  |   |     └── dto/
  |   |      |   ├── SendRequestDTO
  |   |      |
  |   |     └── Impl/
  |   |     |    ├── ChunckSender
  |   |     |    ├── GrpcSender
  |   |     |    ├── MultipartSender
  |         |
  |        ├── SendService
  |
  │   └── validator/
  │    |   ├── RequestValidator  
  │    |   ├── FileValidator 
  |    |
  |    ├── FileGeneratorService
  |    ├── ResponseService
  |    ├── ShedulerService
  |
  ├── utils/
  |  └── config/
  |   |   ├── PropertyKeys
  |   |
  │   └── encoding/  
  │   │   ├── FileEncoding
  │   │   ├── EncodingAdapter
  |   |
  │   └── error/ErrorType
  |   | 
  |   └── filename/FilenameGeneratorUtil
  |   | 
  |   └── format/Visitor
  |   |
  |   └── generate_models/
  |   |    ├── TypeOperation
  |   |    ├── TypeProcedure
  |   |
  |   └── request/RequestConverterUtil
  |   |
  │   ├── send_models/      
  │   │   ├── Sender
  │   │   ├── SenderFabric
  |   |   ├── TypeSend
  │   
```
</details>

## technology

- **Java 21**
- **Gradle** — сборка проекта
- **Apache HttpClient 5** — HTTP клиент для multipart/chunk
- **gRPC 1.64.0** + **Protobuf 3.25.3** — gRPC клиент
- **Apache Tika 2.9.2** — кодировки
- **Jackson** — JSON сериализация
- **Datafaker** — генерация случайных данных
- **Logback** — логирование
- **Lombok** - аннотации
- **StringUtils** - строки

## API Endpoints
- **POST /mock** — Генерация файлов с фейковыми данными
```json
{
  "bankCode": "001",
  "branchCode": "032",
  "nameSystem": "GLAER",
  "rowCount": 10,
  "fileCount": 2
}
```
- **POST /generate** — Генерация файла с реальными данными
```json
{
  "bankCode": "001",
  "branchCode": "032",
  "nameSystem": "GLAER",
  "procType": "IN-TIME",
  "processAt": "2026-08-21T10:00:00",
  "cards": [
    {
      "account": "1234567890123456",
      "type": "DR",
      "summ": 1500.50,
      "fio": "Иванов Иван Иванович"
    }
  ]
}
```
- **POST /error**  — Генерация бракованных файлов (для тестов)
```json
{
  "bankCode": "001",
  "branchCode": "032",
  "nameSystem": "GLAER",
  "lineCount": 10,
  "errorType": "WINDOWS_1251"
}
```
Доступные errorType: EMPTY, SPACES, NEWLINES, UTF_16, WINDOWS_1251

- **POST /send** — Отправка файла
```json
{
  "filename": "Z001032.GLAER_ENROLL0010321.223",
  "typeSend": "multipart",
  "meta": {
    "bankCode": "001",
    "branchCode": "032"
  }
}
```
Доступные typeSend: multipart, chunk, grpc.

## Response

- **error**
```json
{
  "status": "ERROR",
  "error": "file is empty"
}
```

- **success**
```json
{
  "status": "SUCCESS",
  "filenames": [
    "Z001032.GLAER_ENROLL0010321.223",
    "Z001032.GLAER_ENROLL0010322.223"
  ]
}
```

## Run app

```bash
# Сборка
./gradlew clean build

# Запуск с внешними конфигами
java -Dgenerator.file.config=/path/to/app.properties \
     -Dlogback.configurationFile=/path/to/logback.xml \
     -jar build/libs/mealcard-generator.jar

# Запуск с внутренними конфигами
java -jar build/libs/mealcard-generator.jar
```

**Автор**
Федотов Дмитрий
git: https://github.com/Gussia835
email: fdtvdmitriy@gmail.com
