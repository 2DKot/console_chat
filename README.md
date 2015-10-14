# console_chat
Simple console application: ChatServer, ChatClient, ChatBot
====
#### Сборка:
    mvn install


#### Запуск сервера:
    mvn exec:java -Dexec.mainClass="juz.chat.ChatServer"

#### Запуск клиента:
    mvn exec:java -Dexec.mainClass="juz.chat.ChatServer"

#### Запуск ботов:
    mvn exec:java -Dexec.mainClass="juz.chat.ChatBot"

#### Настройка сервера *ChatServer.xml*:
    port - порт сервера
    userNamePattern - regexp Шаблон имени пользователя
    loginTryCount - количество попыток для входа (если неверный формат имени или введено существующее имя)
    msgQueueMaxLength - количество хранимых последних сообщений (для вывода вновь подключенному клиенту)
    commandPattern - regexp Шаблон команд серверу (если введенный текст соответствует шаблону - ищем класс, исполняющий команду)
    commandMapFileName - xml-файл с маппингом команды и реализующего её класса.

#### Настройка клиента *ChatClient.xml*:
    host, 
    port

#### Настройка ботов *ChatBot.xml*:
    botCount - количество ботов (имена bot1, bot2,...)
    msgCount - количество тестовых сообщений бота ("bot1_phrase_1",..."bot5_phrase_20",...)
    delay - задержка отправки сообщений (в милисекундах).

Бот запускает пул потоков ChatClient, подменяя ему пользовательский поток ввода system.in, на ByteArrayInputStream с количеством тестовых сообщений.
Выводы всех ботов пишут В ОДИН system.out, 
поэтому создается видимость, что сервер отдает медленно, на самом деле это не так - видно по консоли сервера - он справляется с нагрузкой.

#### Класс CommandFactory.

Для обработки и возможности добавления новых команд серверу реализована Фабрика команд.

Все команды - это классы реализующие интерфейс IChatServerCommand 
с методом doIt(String cmd, ChatServer srv),в который передается объект сервера (ChatServer).

Фабрика команд маппит посредством java.reflection текст команды и исполняющий её класс,
из файла *CommandMap.xml*:
    ...
    <entry key="#userlist">juz.commands.UserListCommand</entry>
    <entry key="#help">juz.commands.HelpCommand</entry>
    <entry key="#usercount">juz.commands.UserCountCommand</entry>
    ...
При добавлении новых команд, необходимо написать класс-реализацию новой команды и смаппить в  CommandMap.xml с шаблоном.
Можно добавлять псевдонимы команд, связав несколько вариантов с одним классом.

Выход, командой *exit*.
