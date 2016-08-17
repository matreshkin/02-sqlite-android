Клиентская часть (говнокод, исключительно для ознакомления требований клиентского кода):
app/src/main/java/com/yandex/minavegador/TabsActivity.java - при навигации отправляет сайты для записи в БД.
Подписывается на NotificationManager и обращется только к DpProvider за данными.
app/src/main/java/com/yandex/minavegador/NewTabActivity.java - показывает список подсказок при вводе адреса.

БД-часть.
app/src/main/java/com/yandex/minavegador/db/*
Асинхронный запрос через DbProvider (из NewTabActivity) организован через каллбек
 
Пример тестов с роболектриком.
app/src/test/java/com/yandex/minavegador/db/DbBackendTest.java
Роболектрик для каждого теста создает БД в памяти, поэтому можно работать с ней как с обычной БД, и она очень быстрая.
