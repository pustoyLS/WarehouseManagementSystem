Описание
Java-приложение с графическим интерфейсом (JavaFX) для управления товарными запасами на складе. Позволяет отслеживать движение товаров (приход, расход, перемещение), управлять остатками, генерировать отчёты и получать уведомления о критических уровнях запасов.
Ключевая особенность: приложение умеет работать с двумя СУБД одновременно — PostgreSQL и MS SQL Server. Переключение между ними происходит через файл конфигурации без изменения кода.
🛠 Стек технологий
Язык: Java 17
GUI: JavaFX 21
Сборка: Maven
Доступ к БД: JDBC (без ORM)
СУБД: PostgreSQL 16 + MS SQL Server 2022
IDE: IntelliJ IDEA
Клиент БД: DBeaver / pgAdmin 4 / SSMS
✨ Функционал
📊 Отчёты и мониторинг
Таблица остатков по всем складам с автоматической подсветкой критических позиций
Отдельный отчёт «Критические остатки» с расчётом дефицита
📝 Справочники (полный CRUD)
Склады (название, адрес)
Товары (артикул, название, категория, минимальный уровень)
Контрагенты (поставщики и клиенты)
📥📤 Документы движения
Создание накладных: приход, расход, перемещение
Автоматический пересчёт остатков в рамках одной транзакции
Защита от отрицательных остатков
⚙️ Настройки
Переключение между PostgreSQL и MS SQL Server через меню приложения
Тест подключения к активной БД
📸 Скриншоты
(Здесь вставьте скриншоты вашего приложения — главное окно, вкладка остатков, диалог создания документа, меню переключения СУБД)

Структура проекта
WarehouseManagementSystem/
├── src/
│   ── main/
│       ├── java/com/warehouse/
│       │   ├── config/          # DatabaseConfig.java
│       │   ├── dao/             # WarehouseDao, ProductDao, InventoryDao, MovementDao
│       │   ├── model/           # Warehouse, Product, Inventory, Movement, MovementItem
│       │   ├── ui/              # MainWindow, InventoryView, MovementsView и др.
│       │   ├── MainApp.java     # Точка входа JavaFX
│       │   └── Launcher.java    # Класс-обёртка для запуска JAR
│       └── resources/
│           └── config.properties
├── sql/
│   ├── warehouse_ddl.sql        # Создание таблиц
│   ├── warehouse_data.sql       # Тестовые данные
│   ├── warehouse_mssql_ddl.sql  # Версия для MS SQL Server
│   └── warehouse_mssql_data.sql
├── pom.xml
└── README.md

Требования к системе
ОС: Windows 10/11, Linux (Ubuntu/Kali), macOS
Java: JDK 17 или новее
Maven: 3.6+ (или использовать Maven Wrapper)
СУБД: PostgreSQL 14+ или MS SQL Server 2019+
Свободное место: ~1.5 ГБ
Интернет: нужен при первом запуске (Maven скачает зависимости)
🚀 Пошаговая инструкция по развёртыванию
Шаг 1. Установка JDK 17
Windows:
Скачайте JDK с https://adoptium.net/temurin/releases/?version=17
Установите, поставив галочки «Set JAVA_HOME variable» и «Add to PATH»
Проверьте в командной строке:
cmd

1
Linux (Ubuntu/Kali):
bash

123
Шаг 2. Установка PostgreSQL
Windows:
Скачайте установщик: https://www.postgresql.org/download/windows/
При установке:
Компоненты: PostgreSQL Server, pgAdmin 4, Command Line Tools
Пароль для пользователя postgres: задайте и запомните (например, postgres123)
Порт: 5432
Проверьте, что служба запущена: Win+R → services.msc → найдите postgresql-x64-16 → должна быть «Выполняется»
Linux:
sudo apt install postgresql postgresql-contrib -y
sudo systemctl start postgresql
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres123';"

Шаг 3. Создание базы данных и выполнение скриптов
3.1. Создание БД
Через pgAdmin 4 (Windows):
Откройте pgAdmin 4
Задайте мастер-пароль (любой, например 12345)
Раскройте: Servers → PostgreSQL 16
Введите пароль postgres
Правой кнопкой по Databases → Create → Database
Имя: warehouse → Save
Через консоль (Linux):
sudo -u postgres psql
CREATE DATABASE warehouse;
\c warehouse

3.2. Выполнение SQL-скриптов
Откройте Query Tool в pgAdmin (или подключитесь через psql) и выполните по очереди:
sql/warehouse_ddl.sql — создаст 6 таблиц:
warehouses (склады)
products (товары)
contractors (контрагенты)
inventory (остатки)
movements (документы)
movement_items (позиции документов)
sql/warehouse_data.sql — внесёт тестовые данные (минимум 5 строк в каждой таблице)
Проверка: в каждой таблице должно быть не менее 5 записей.
Шаг 4. Клонирование репозитория
git clone https://gitverse.ru/ВАШ_ЛОГИН/WarehouseManagementSystem.git
cd WarehouseManagementSystem

Шаг 5. Настройка подключения к БД
Откройте файл src/main/resources/config.properties и отредактируйте:
# Активная СУБД: postgres или mssql
db.active=postgres

# PostgreSQL
db.postgres.url=jdbc:postgresql://localhost:5432/warehouse
db.postgres.user=postgres
db.postgres.password=postgres123
db.postgres.driver=org.postgresql.Driver

# MS SQL Server (настройте, если используете)
db.mssql.url=jdbc:sqlserver://localhost:1433;databaseName=warehouse;encrypt=true;trustServerCertificate=true;
db.mssql.user=sa
db.mssql.password=ВашПарольSa
db.mssql.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
⚠️ Важно:
В db.postgres.password= укажите тот пароль, который задали при установке PostgreSQL
Не оставляйте пробелов до/после знака =
Имя базы должно быть точно warehouse
Шаг 6. Запуск приложения
Способ A: Через Maven (рекомендуется)
Из корня проекта выполните:
Windows:
mvn clean javafx:run

Linux/macOS:
mvn clean javafx:run
Первый запуск займёт 3-5 минут (Maven скачает зависимости). Последующие — 10-20 секунд.
Способ B: Через IntelliJ IDEA
Откройте проект в IDEA: File → Open → выберите папку проекта
Дождитесь импорта Maven (индикатор внизу справа)
Найдите класс com.warehouse.Launcher
Правой кнопкой → Run 'Launcher.main()'
Способ C: Через собранный JAR
mvn clean package
java --module-path "ПУТЬ_К_JAVAFX_SDK/lib" --add-modules javafx.controls,jafx.fxml -jar target/WarehouseManagementSystem-1.0-SNAPSHOT.jar

✅ Проверка работы
После запуска убедитесь, что:
Вкладка «Остатки» — отображает таблицу, некоторые строки подсвечены красным (критический уровень)
Вкладка «Склады» — кнопка «➕ Добавить» создаёт новый склад
Вкладка «Документы» — можно создать документ «Приход», добавить позиции, остатки увеличатся
Меню «База данных → Тест подключения» — показывает «Подключение успешно!»
Переключение между СУБД
Вариант 1: Через меню приложения
База данных → MS SQL Server (или PostgreSQL) → подтверждение → данные перезагрузятся.
Вариант 2: Через config.properties
Измените db.active=postgres на db.active=mssql, перезапустите приложение.
⚠️ Для MS SQL Server необходимо:
Установить MS SQL Server Express
Включить TCP/IP в SQL Server Configuration Manager
Открыть порт 1433 в брандмауэре Windows
Включить пользователя sa и задать пароль (см. документацию Microsoft)
Тестирование на другой ОС
Для выполнения требования методички о кроссплатформенности:
Установите VirtualBox + Kali Linux (или Ubuntu)
Настройте сетевой адаптер VM: Сетевой мост (Bridged)
На Kali установите JDK и Maven:
   sudo apt install openjdk-17-jdk maven git -y
   Склонируйте репозиторий и запустите:
   git clone https://gitverse.ru/ВАШ_ЛОГИН/WarehouseManagementSystem.git
   cd WarehouseManagementSystem
   mvn clean javafx:run

   В config.properties замените localhost на IP-адрес хостовой Windows-машины
🛠 Возможные проблемы
❌ «mvn не является внутренней или внешней командой»
Решение: Maven не установлен или не добавлен в PATH. Используйте Maven Wrapper из проекта:
mvnw.cmd clean javafx:run

❌ «Connection refused» при подключении к БД
Причина: PostgreSQL не запущен.
Решение: services.msc → найдите postgresql-x64-16 → Запустить.
❌ «База данных warehouse не существует»
Решение: Создайте БД через pgAdmin 4 (Шаг 3.1) и выполните скрипты.
❌ «Module javafx.controls not found»
Решение: Запускайте через mvn clean javafx:run, а не через java -jar. Maven сам подтянет модули.
❌ «config.properties не найден»
Решение: Убедитесь, что файл лежит в src/main/resources/config.properties, а не в корне проекта.
❌ «FATAL: password authentication failed»
Решение: Проверьте пароль в config.properties — он должен совпадать с паролем, заданным при установке PostgreSQL.
❌ Кракозябры в консоли вместо русского текста
Решение: Это проблема кодировки Windows-консоли, на работу приложения не влияет.
📝 Тестовые учётные данные
Параметр
Значение
СУБД
PostgreSQL 16
Хост
localhost
Порт
5432
База данных
warehouse
Пользователь
postgres
Пароль
postgres123 (или ваш)
