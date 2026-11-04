# Ручная публикация в новый репозиторий

Проект подготовлен как отдельный Git-репозиторий на ветке `fabric-26.2`, без истории upstream. Публикация выполняется вручную. Remote `origin` указывает на `https://github.com/cat4blep/Creaty-fly-Electro-Energetics.git`.

## Перед публикацией

1. Проверьте `README.md`, `FABRIC_PORT.md`, `CHANGELOG.md`, лицензию и подготовленные файлы:

   ```sh
   git status --short
   git diff --cached --stat
   git diff --cached
   ```

2. Проверьте адрес назначения командой `git remote -v`. Новый репозиторий на хостинге должен быть пустым; README, лицензия и `.gitignore` уже входят в проект.
3. Поле `contact.sources` файла `src/main/resources/fabric.mod.json` указывает на репозиторий порта. Авторство и ссылку на upstream в README следует оставить.
4. Если вы изменили файлы после подготовки, добавьте нужные изменения в индекс и создайте локальный коммит перед отправкой:

   ```sh
   git add <CHANGED_FILES>
   git commit -m "Describe the changes"
   ```

## Проверка коммита и отправка

Перед первым push убедитесь, что в ветке есть хотя бы один коммит:

```sh
git log -1 --oneline
```

Если Git сообщает `does not have any commits yet`, сначала создайте первый локальный коммит из подготовленного индекса:

```sh
git commit -m "Initial Fabric 26.2 port"
```

После ревью отправьте ветку и настройте upstream:

```sh
git push --set-upstream origin fabric-26.2
```

После успешной первой отправки для следующих коммитов достаточно `git push`.

Если хостинг требует выбора основной ветки, выберите `fabric-26.2`.

## Сборка и релиз

```sh
./gradlew build
./gradlew runClientGameTest
```

В PowerShell используйте `.\gradlew.bat`. Для Linux без графического сеанса: `xvfb-run -a ./gradlew build runClientGameTest`.

Для ручного релиза используйте игровой JAR из `build/libs/`. В описании укажите Minecraft 26.2, Java 25, Fabric API и Create Fly, а также ограничения совместимости из `FABRIC_PORT.md`. Публикация релизов и тегов остаётся ручной; CI выполняет только сборку и тесты.
