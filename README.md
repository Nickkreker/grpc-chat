# gRPC Java Chat
Сетевой чат на Java на основе фреймворка gRPC

# Запуск
Для запуска в режиме сервера используйте:
```bash
./gradlew run --args="username 8081" --console=plain
```

Для запуска в режиме клиента используйте:
```bash
./gradlew run --args="username 127.0.0.1 8081" --console=plain
```
