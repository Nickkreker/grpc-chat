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

# Обмен сообщениями
Для того, чтобы отправить с сервера сообщение клиенту с именем username, используйте следующий синтакс:
```bash
some message text -> username
```
