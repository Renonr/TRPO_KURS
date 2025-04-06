# Используем официальный образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем Maven
RUN apt-get update && apt-get install -y maven

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем все файлы проекта в контейнер
COPY . .

# Сборка проекта с помощью Maven
RUN mvn clean package -DskipTests

# Указываем, какой порт будет использовать приложение
EXPOSE 8080

# Запуск приложения
CMD ["java", "-jar", "target/airlines-0.0.1-SNAPSHOT.jar"]
